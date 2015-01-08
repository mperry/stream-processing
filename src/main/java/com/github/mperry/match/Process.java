package com.github.mperry.match;

import com.github.mperry.fj.Match;
import com.github.mperry.fj.Util;
import com.github.mperry.fj.When;
import fj.*;
import fj.data.*;
import fj.data.Stream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.*;

import static com.github.mperry.fj.When.when;
import static com.github.mperry.fj.When.whenClass;
import static com.github.mperry.match.Await.awaiti;
import static com.github.mperry.match.Halt.halt;
import static fj.F1Functions.andThen;
import static fj.data.Option.none;
import static fj.data.Option.some;

/**
 * Created by MarkPerry on 7/01/2015.
 *
 * This implementation is based on pattern matching
 */
public class Process<I, O> {


    /**
     * Sample driver, section 15.2
     */
    public Stream<O> apply(Stream<I> s) {
        List<When<Process<I, O>, Stream<O>>> l = List.<When<Process<I, O>, Stream<O>>>list(
            whenClass(Halt.class,
                    h -> Stream.<O>nil()
            ),
            whenClass(Await.class, (Await<I, O> a) -> {
                if (s.isNotEmpty()) {
                    Stream<I> t = s.tail()._1();
                    return a.receive.f(some(s.head())).apply(t);
                } else {
                    return a.receive.f(none()).apply(s);
                }
            }),
            whenClass(Emit.class, (Emit<I, O> e) -> {
                return Stream.cons(e.head, P.lazy(u -> e.tail.apply(s)));
            })
        );
        // default is halt case, repeated from above
        return Match.match(l, p -> Stream.<O>nil()).apply(this);
    }

    /**
     * Emit a single element
     * Section 15.2.1
     */
    public static <I, O> Process<I, O> liftOne(F<I, O> f) {
        return Await.await(o -> o.isSome() ? Emit.emit(f.f(o.some())) : halt());
    }

    /**
     * Section 15.2.1
     * Transform a whole stream
     */
    public Process<I, O> repeat() {
        return Process.<I, O>repeat(this, this);
    }

    /**
     * Section 15.2.1
     * Transform a whole stream
     */
    public static <I, O> Process<I, O> repeat(Process<I, O> original, Process<I, O> p) {
        List<When<Process<I, O>, Process<I, O>>> list1 = List.<When<Process<I, O>, Process<I, O>>>list(
            // restart the original process if it wants to halt, let the stream halt it itself
            whenClass(Halt.class, (Halt<I, O> h) -> repeat(original, original)),
            whenClass(Await.class, (Await<I, O> a) -> {
//                Await<I, O> a = z;
                return Await.await((Option<I> o) ->
                        o.isNone() ? a.receive.f(o) : repeat(original, a.receive.f(o))
                );
            }),
            whenClass(Emit.class, (Emit<I, O> e) -> {
//                Emit<I, O> e = z;
                return Emit.emit(e.head, repeat(original, e.tail));
            })
        );
        // default is halt case, repeated from above
        Match<Process<I, O>, Process<I, O>> m = Match.match(list1, p2 -> repeat(original, original));
        return m.apply(p);
    }

    /**
     * Section 15.2.1
     */
    public static <I, O> Process<I, O> lift(F<I, O> f) {
        return liftOne(f).repeat();
    }

    /**
     * Section 15.2.1
     */
    public static <I> Process<I, I> filter(F<I, Boolean> f) {
        return Await.<I, I>await(o -> {
            if (o.isNone()) {
                return halt();
            } else {
                I i = o.some();
                return f.f(i) ? Emit.emit(i) : halt();
            }

        }).repeat();
    }

    /**
     * Section 15.2.1, Exercise 15.1
     */
    public static <I> Process<I, I> take(int n) {
        return n <= 0 ? halt() : Await.<I, I>awaiti(i -> Emit.<I, I>emit(i, take(n - 1)));
    }

    /**
     * Section 15.2.1, Exercise 15.1
     */
    public static <I> Process<I, I> drop(int n) {
        return n <= 0 ? identity() : Await.<I, I>awaiti(i -> drop(n - 1));
    }

    /**
     * Section 15.2.1
     */
    public static Process<Double, Double> sum(double acc) {
        return Await.await((Option<Double> o) -> {
            if (o.isNone()) {
                return halt();
            } else {
                double d = o.some();
                return Emit.emit(d + acc, sum(d + acc));
            }
//            return o.map(d -> Emit.emit(d + acc, sum(d + acc))).orSome(Halt.<Double, Double>halt());
        });
    }

    /**
     * Section 15.2.1
     */
    public static Process<Double, Double> sum() {
        return sum(0.0);

    }

    public static <I> Process<I, I> identity() {
        return lift(Function.<I>identity());

    }

    /**
     * Section 15.2.1, Exercise 15.1
     */
    public static <I> Process<I, I> takeWhile() {
        // TODO
        throw Bottom.error("Process.takeWhile not yet implemented");
    }

    /**
     * Section 15.2.1, Exercise 15.1
     */
    public static <I> Process<I, I> dropWhile() {
        // TODO
        throw Bottom.error("Process.dropWhile not yet implemented");
    }

    /**
     * Section 15.2.1, Exercise 15.2
     */
    public static <I> Process<I, Integer> count() {
        // TODO
        throw Bottom.error("Process.count not yet implemented");
    }

    /**
     * Section 15.2.1, Exercise 15.3
     */
    public static Process<Double, Double> mean() {
        // TODO
        throw Bottom.error("Process.mean not yet implemented");
    }

    /**
     * Section 15.2.1, helps with exercise 15.4
     */
    public static <S, I, O> Process<I, O> loop(S acc, F2<I, S, P2<O, S>> f) {
        return awaiti((I i) -> {
            P2<O, S> p = f.f(i, acc);
            return Emit.<I, O>emit(p._1(), loop(p._2(), f));
//            return null;
        });
    }

    /**
     * Section 15.2.1, Exercise 15.4
     */
    public static <I> Process<I, Integer> count2() {
        return loop(0, (i, s) -> P.p(s + 1, s + 1));
    }

    /**
     * Section 15.2.1, Exercise 15.4
     */
    public static <I> Process<Double, Double> sum2() {
        return loop(0.0, (i, s) -> P.p(s + i, s + i));
    }

    /**
     * Section 15.2.2, Exercise 15.5
     */
    public <O2> Process<I, O2> pipe(Process<O, O2> p2) {
        List<When<Process<O, O2>, Process<I, O2>>> l1 = List.<When<Process<O, O2>, Process<I, O2>>>list(
                // use halt as the default case in the outer match
                // whenClass(Halt.class, (Halt<O, O2> h) -> Halt.<I, O2>halt()),
                whenClass(Emit.class, (Emit<O, O2> e) -> Emit.emit(e.head, this.pipe(e.tail))),
                whenClass(Await.class, (Await<O, O2> a) -> {
                    List<When<Process<I, O>, Process<I, O2>>> l2 = List.list(
                        // use Halt case as the default inner match below
                        whenClass(Emit.class, (Emit<I, O> e) -> e.tail.pipe(a.receive.f(Option.some(e.head)))),
                        whenClass(Await.class, (Await<I, O> a2) -> {
                            return Await.await((Option<I> o) -> a2.receive.f(o).pipe(p2));
                        })
                    );
                    // halt case
                    return Match.match(l2, h -> Halt.<I, O>halt().pipe(a.receive.f(Option.none()))).apply(this);
                })
        );
        return Match.match(l1, h -> Halt.halt()).apply(p2);
    }

    /**
     * section 15.2.2
     */
    public <O2> Process<I, O2> map(F<O, O2> f) {
        return this.pipe(lift(f));
    }

    /**
     * section 15.2.2
     */
    public Process<I, O> append(Process<I, O> p) {
        return Match.match(List.<When<Process<I, O>, Process<I, O>>>list(
            // halt case below
            whenClass(Emit.class, (Emit<I, O> e) -> Emit.emit(e.head, e.tail.append(p))),
            whenClass(Await.class, (Await<I, O> a) -> Await.await(andThen(a.receive, p2 -> p2.append(p))))

        ), h -> halt()).apply(this);
//        return null;
    }

    /**
     * section 15.2.2
     */
    public <O2> Process<I, O2> flatMap(F<O, Process<I, O2>> f) {
        return Match.match(List.<When<Process<I, O>, Process<I, O2>>>list(
            whenClass(Emit.class, (Emit<I, O> e) -> f.f(e.head).append(e.tail.flatMap(f))),
            whenClass(Await.class, (Await<I, O> a) -> Await.await(andThen(a.receive, x -> x.flatMap(f))))
        ), h -> halt()).apply(this);
    }

    /**
     * Section 15.2.2, Exercise 15.6
     * TODO: zipWithIndex
     */

    /**
     * Section 15.2.2, Exercise 15.8
     * We choose to emit all intermediate values, and not halt.
     */
    public static <I> Process<I, Boolean> exists(F<I, Boolean> f) {
        return lift(f).pipe(any());
    }

    /**
     * Emits whether a `true` input has ever been received.
     * */
    public static Process<Boolean, Boolean> any() {
        return loop(false, (b, s) -> P.p(s || b, s || b));
    }

    public static <A, B> IO<B> processFile(File f, Process<String, A> p, B acc, F2<B, A, B> g) {
        return IOFunctions.lazy(u -> {
            try (java.util.stream.Stream<String> s1 = Files.lines(f.toPath())) {
                return helper(s1.iterator(), p, acc, g);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                return acc;
            }
        });
    }

    public static <A, B> B helper(Iterator<String> ss, Process<String, A> curr, B acc, F2<B, A, B> g) {
        Match.match(List.list(
                whenClass(Halt.class, h -> acc),
                whenClass(Await.class, (Await<String, A> a) -> {
                    Process<String, A> next = ss.hasNext() ? a.receive.f(some(ss.next())) : a.receive.f(none());
                    return helper(ss, next, acc, g);
                }),
                whenClass(Emit.class, (Emit<String, A> e) -> helper(ss, e.tail, g.f(acc, e.head), g))
        ), h -> acc).apply(curr);
        return null;
    }


}
