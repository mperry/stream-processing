{-# Language ExistentialQuantification, GADTs #-}
 
module Control.Process where
 
import Data.Monoid
import Prelude hiding (zip, zipWith)
import Control.Applicative
import System.IO
 
data Process f a = Halt 
                 | Emit a (Process f a) 
                 | forall r . Await (f r) (r -> Process f a) (Process f a)
 
(<>) :: Process k a -> Process k a -> Process k a
(<>) = mappend
 
instance Monoid (Process k a) where
  mempty = Halt
 
  mappend Halt p = p
  mappend (Emit h t) p2 = Emit h (t <> p2)
  mappend (Await req recv fb) p2 = Await req (\res -> recv res <> p2) (fb <> p2) 
 
instance Monad (Process f) where
  return a = Emit a Halt
  Halt >>= f = Halt
  (Emit h t) >>= f = f h <> (t >>= f)  
  (Await req recv fb) >>= f = Await req (\res -> recv res >>= f) (fb >>= f)
 
type Source a = Process IO a
type Process1 a b = Process (Is a) b 
type Tee a b c = Process (T a b) c
type Wye a b c = Process (Y a b) c
type Sink a = Source (a -> IO ())
type Channel a b = Source (a -> IO b) 
type Server s a b = Source (s -> a -> (s, b))
 
-- run :: Monad m => Process m a -> m [a]
 
(|>) :: Process k a -> Process1 a b -> Process k b 
p1                    |> Halt                 = disconnect p1
(Emit h t)            |> (Await Refl recv fb) = t |> recv h 
Halt                  |> (Await Refl recv fb) = Halt |> fb
(Await req recv1 fb1) |> p2                   = Await req (\a -> recv1 a |> p2) (fb1 |> disconnect p2)
 
tee :: Tee a b c -> Process f a -> Process f b -> Process f c
tee Halt p1 p2 = disconnect p1 <> disconnect p2 
tee (Await L recv fb) (Emit h t) r = tee (recv h) t r 
tee (Await L recv fb) Halt r = tee fb Halt r
tee t@(Await L _ fb) l@(Await req recvl fbl) r = Await req (\res -> tee t (recvl res) r) (disconnect l <> disconnect r)  
tee (Await R recv fb) l (Emit h t) = tee (recv h) l t 
tee (Await R recv fb) l Halt = tee fb l Halt 
 
wye :: Nondeterminism f => Wye a b c -> Process f a -> Process f b -> Process f c
wye Halt p1 p2 = disconnect p1 <> disconnect p2 
wye _ _ _ = error "todo"
 
class Nondeterminism f where
  choose :: f a -> f b -> f (Either a b)
 
await req recv = Await req recv Halt
await1 = Await Refl
emit h = Emit h Halt
 
lift :: (a -> b) -> Process1 a b
lift f = repeatedly $ await Refl (emit . f) 
 
zipWith :: (a -> b -> c) -> Tee a b c
zipWith f = repeatedly $
  await L (\a -> 
  await R (\b -> emit $ f a b)) 
 
zip = zipWith (,)
 
flatten :: Process f (f a) -> Process f a
flatten (Emit h t) = Await h (\a -> Emit a (flatten t)) Halt 
flatten Halt = Halt
flatten (Await req recv fb) = Await req (flatten . recv) (flatten fb)
 
connect :: Process f a -> Process f (a -> b) -> Process f b 
connect p1 p2 = tee (zipWith (flip ($))) p1 p2
 
-- through :: Source a -> Channel a b -> Source b
through :: Process f a -> Process f (a -> f b) -> Process f b
through src chan = flatten $ connect src chan
 
observe :: Source a -> Sink a -> Source a
observe src snk = flatten $ connect src (unswallow snk)
 
cap :: Source a -> Sink a -> Source ()
cap src snk = flatten $ connect src snk
 
bcap :: Nondeterminism f => Int -> Process f a -> Process f (a -> f ()) -> Process f ()
bcap maxBuffer src snk = flatten $ wye (buffer maxBuffer) src snk
 
buffer :: Int -> Wye a (a -> b) b
buffer maxBuffer = go [] 0
  where 
    -- there are three cases:
    --  * the buffer is full, in which case we block on the sink
    --  * the buffer is empty, in which case we block on the source
    --  * the buffer is nonempty, in which case we accept either a source or a sink
    --  not quite right - buffer is in reverse order
    --  need to handle flushing buffer?
    go (a:as) n | n > maxBuffer = await Y $ \f -> Emit (f a) (go as (n-1)) 
    go [] n = await X $ \a -> go [a] (n+1)
    go buf n = await Z (step buf n)
 
    step buf n (Left a) = go (a : buf) (n+1) 
    step (a:as) n (Right f) = Emit (f a) (go as (n-1))
 
passX :: Wye a b c
passX = undefined
 
passY :: Wye a b c
passY = undefined
 
resource :: IO r -> (r -> IO (Maybe a)) -> (r -> IO ()) -> Source a
resource acquire step release = Await acquire recv0 Halt
  where
    recv0 res = go (step res) (release res)
    go step release = Await step (maybe Halt (\a -> Emit a (go step release))) (stop release)
    stop release = Await release (const Halt) Halt
 
linesR :: FilePath -> Source String
linesR f = resource (openFile f ReadMode) step hClose
  where
    step h = hIsEOF h >>= \b -> 
      if b then Just <$> hGetLine h 
           else return Nothing
 
fileW :: FilePath -> Sink String
fileW f = resource (openFile f WriteMode) step hClose
  where 
    step h = return . Just $ \s -> hPutStr h s 
 
unswallow :: Sink a -> Channel a a
unswallow snk = snk |> lift go
  where 
    go f a = f a >> return a 
 
repeatedly :: Process k a -> Process k a
repeatedly p = let pr = p <> repeatedly p in pr 
 
ignore :: Process1 k a
ignore = Await Refl (const ignore) Halt
 
disconnect :: Process k a -> Process k b
disconnect (Emit h t) = disconnect t
disconnect Halt = Halt
disconnect (Await req recv fb) = fb |> ignore
 
data Is a b where
  Refl :: Is a a
 
data T a b c where
  L :: T a b a
  R :: T a b b
 
data Y a b c where
  X :: Y a b a
  Y :: Y a b b
  Z :: Y a b (Either a b)