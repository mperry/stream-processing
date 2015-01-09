package com.github.mperry.stream;

import com.github.mperry.fj.Match;
import com.github.mperry.fj.When;
import fj.*;
import fj.data.*;
import fj.data.Stream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import static com.github.mperry.fj.When.whenClass;
import static com.github.mperry.stream.Await1.awaiti;
import static com.github.mperry.stream.Halt1.halt;
import static fj.F1Functions.andThen;
import static fj.data.Option.none;
import static fj.data.Option.some;

/**
 * Created by MarkPerry on 7/01/2015.
 *
 * This implementation is based on pattern matching
 */
public class Process1<I, O> {


    /**
     * Sample driver, section 15.2
     */
    public Stream<O> apply(Stream<I> s) {
        List<When<Process1<I, O>, Stream<O>>> l = List.<When<Process1<I, O>, Stream<O>>>list(
            whenClass(Halt1.class, h -> Stream.<O>nil()),
            whenClass(Await1.class, (Await1<I, O> a) -> {
                if (s.isNotEmpty()) {
                    Stream<I> t = s.tail()._1();
                    return a.receive.f(some(s.head())).apply(t);
                } else {
                    return a.receive.f(none()).apply(s);
                }
            }),
            whenClass(Emit1.class, (Emit1<I, O> e) -> {
                return Stream.cons(e.head, P.lazy(u -> e.tail.apply(s)));
            })
        );
        // default is halt case, repeated from above
        return Match.createMatch(l, p -> Stream.<O>nil()).match(this);
    }

    /**
     * Emit a single element
     * Section 15.2.1
     */
    public static <I, O> Process1<I, O> liftOne(F<I, O> f) {
        return Await1.await(o -> o.isSome() ? Emit1.emit(f.f(o.some())) : halt());
    }

    /**
     * Section 15.2.1
     * Transform a whole stream
     */
    public Process1<I, O> repeat() {
        return Process1.<I, O>repeat(this, this);
    }

    /**
     * Section 15.2.1
     * Transform a whole stream
     */
    public static <I, O> Process1<I, O> repeat(Process1<I, O> original, Process1<I, O> p) {
        List<When<Process1<I, O>, Process1<I, O>>> list1 = List.<When<Process1<I, O>, Process1<I, O>>>list(
            // restart the original process if it wants to halt, let the stream halt it itself
            whenClass(Halt1.class, (Halt1<I, O> h) -> repeat(original, original)),
            whenClass(Await1.class, (Await1<I, O> a) -> {
                return Await1.await((Option<I> o) ->
                                o.isNone() ? a.receive.f(o) : repeat(original, a.receive.f(o))
                );
            }),
            whenClass(Emit1.class, (Emit1<I, O> e) -> {
                return Emit1.emit(e.head, repeat(original, e.tail));
            })
        );
        // default is halt case, repeated from above
        Match<Process1<I, O>, Process1<I, O>> m = Match.createMatch(list1, p2 -> repeat(original, original));
        return m.match(p);
    }

    /**
     * Section 15.2.1
     */
    public static <I, O> Process1<I, O> lift(F<I, O> f) {
        return liftOne(f).repeat();
    }

    /**
     * Section 15.2.1
     */
    public static <I> Process1<I, I> filter(F<I, Boolean> f) {
        return Await1.<I, I>await(o -> {
            if (o.isNone()) {
                return halt();
            } else {
                I i = o.some();
                return f.f(i) ? Emit1.emit(i) : halt();
            }

        }).repeat();
    }

    /**
     * Section 15.2.1, Exercise 15.1
     */
    public static <I> Process1<I, I> take(int n) {
        return n <= 0 ? halt() : Await1.<I, I>awaiti(i -> Emit1.<I, I>emit(i, take(n - 1)));
    }

    /**
     * Section 15.2.1, Exercise 15.1
     */
    public static <I> Process1<I, I> drop(int n) {
        return n <= 0 ? identity() : Await1.<I, I>awaiti(i -> drop(n - 1));
    }

    /**
     * Section 15.2.1
     */
    public static Process1<Double, Double> sum(double acc) {
        return Await1.await((Option<Double> o) -> {
            if (o.isNone()) {
                return halt();
            } else {
                double d = o.some();
                return Emit1.emit(d + acc, sum(d + acc));
            }
        });
    }

    /**
     * Section 15.2.1
     */
    public static Process1<Double, Double> sum() {
        return sum(0.0);

    }

    public static <I> Process1<I, I> identity() {
        return lift(Function.<I>identity());

    }

    /**
     * Section 15.2.1, Exercise 15.1
     */
    public static <I> Process1<I, I> takeWhile() {
        // TODO
        throw Bottom.error("Process.takeWhile not yet implemented");
    }

    /**
     * Section 15.2.1, Exercise 15.1
     */
    public static <I> Process1<I, I> dropWhile() {
        // TODO
        throw Bottom.error("Process.dropWhile not yet implemented");
    }

    /**
     * Section 15.2.1, Exercise 15.2
     */
    public static <I> Process1<I, Integer> count() {
        // TODO
        throw Bottom.error("Process.count not yet implemented");
    }

    /**
     * Section 15.2.1, Exercise 15.3
     */
    public static Process1<Double, Double> mean() {
        // TODO
        throw Bottom.error("Process.mean not yet implemented");
    }

    /**
     * Section 15.2.1, helps with exercise 15.4
     */
    public static <S, I, O> Process1<I, O> loop(S acc, F2<I, S, P2<O, S>> f) {
        return awaiti((I i) -> {
            P2<O, S> p = f.f(i, acc);
            return Emit1.<I, O>emit(p._1(), loop(p._2(), f));
        });
    }

    /**
     * Section 15.2.1, Exercise 15.4
     */
    public static <I> Process1<I, Integer> count2() {
        return loop(0, (i, s) -> P.p(s + 1, s + 1));
    }

    /**
     * Section 15.2.1, Exercise 15.4
     */
    public static <I> Process1<Double, Double> sum2() {
        return loop(0.0, (i, s) -> P.p(s + i, s + i));
    }

    /**
     * Section 15.2.2, Exercise 15.5
     */
    public <O2> Process1<I, O2> pipe(Process1<O, O2> p2) {
        List<When<Process1<O, O2>, Process1<I, O2>>> l1 = List.<When<Process1<O, O2>, Process1<I, O2>>>list(
                // use halt as the default case in the outer match
                // whenClass(Halt.class, (Halt<O, O2> h) -> Halt.<I, O2>halt()),
                whenClass(Emit1.class, (Emit1<O, O2> e) -> Emit1.emit(e.head, this.pipe(e.tail))),
                whenClass(Await1.class, (Await1<O, O2> a) -> {
                    List<When<Process1<I, O>, Process1<I, O2>>> l2 = List.list(
                            // use Halt case as the default inner match below
                            whenClass(Emit1.class, (Emit1<I, O> e) -> e.tail.pipe(a.receive.f(Option.some(e.head)))),
                            whenClass(Await1.class, (Await1<I, O> a2) -> {
                                return Await1.await((Option<I> o) -> a2.receive.f(o).pipe(p2));
                            })
                    );
                    // halt case
                    return Match.createMatch(l2, h -> Halt1.<I, O>halt().pipe(a.receive.f(Option.none()))).match(this);
                })
        );
        return Match.createMatch(l1, h -> Halt1.halt()).match(p2);
    }

    /**
     * section 15.2.2
     */
    public <O2> Process1<I, O2> map(F<O, O2> f) {
        return this.pipe(lift(f));
    }

    /**
     * section 15.2.2
     */
    public Process1<I, O> append(Process1<I, O> p) {
        return Match.createMatch(List.<When<Process1<I, O>, Process1<I, O>>>list(
                // halt case below
                whenClass(Emit1.class, (Emit1<I, O> e) -> Emit1.emit(e.head, e.tail.append(p))),
                whenClass(Await1.class, (Await1<I, O> a) -> Await1.await(andThen(a.receive, p2 -> p2.append(p))))

        ), h -> halt()).match(this);
    }

    public Process1<I, O> append2(Process1<I, O> p) {
        return Match.match(this, h -> halt(),
            When.<Emit1<I, O>, Process1<I, O>, Process1<I, O>>whenClass(Emit1.class,
                (Emit1<I, O> e) -> Emit1.emit(e.head, e.tail.append(p))
            ).appendClass(Await1.class,
                (Await1<I, O> a) -> Await1.await(andThen(a.receive, p2 -> p2.append(p)))
            )
        );
    }

    /**
     * section 15.2.2
     */
    public <O2> Process1<I, O2> flatMap(F<O, Process1<I, O2>> f) {
        return Match.createMatch(List.<When<Process1<I, O>, Process1<I, O2>>>list(
                whenClass(Emit1.class, (Emit1<I, O> e) -> f.f(e.head).append(e.tail.flatMap(f))),
                whenClass(Await1.class, (Await1<I, O> a) -> Await1.await(andThen(a.receive, x -> x.flatMap(f))))
        ), h -> halt()).match(this);
    }

    /**
     * Section 15.2.2, Exercise 15.6
     * TODO: zipWithIndex
     */

    /**
     * Section 15.2.2, Exercise 15.8
     * We choose to emit all intermediate values, and not halt.
     */
    public static <I> Process1<I, Boolean> exists(F<I, Boolean> f) {
        return lift(f).pipe(any());
    }

    /**
     * Emits whether a `true` input has ever been received.
     * */
    public static Process1<Boolean, Boolean> any() {
        return loop(false, (b, s) -> P.p(s || b, s || b));
    }

    public static <A, B> IO<B> processFile(File f, Process1<String, A> p, B acc, F2<B, A, B> g) {
        return IOFunctions.lazy(u -> {
            try (java.util.stream.Stream<String> s1 = Files.lines(f.toPath())) {
                return process(s1.iterator(), p, acc, g);
            } catch (IOException e) {
                e.printStackTrace();
                return acc;
            }
        });
    }

    /**
     * Helper function for processFile, named go in section 15.2.3
     *
     * TODO: This is tail recursive and will overflow the stack.  This should be fixed, probably using a trampoline.
     */
    public static <A, B> B process(Iterator<String> it, Process1<String, A> p, B acc, F2<B, A, B> f) {
        return Match.createMatch(List.list(
                whenClass(Halt1.class, h -> acc),
                whenClass(Await1.class, (Await1<String, A> a) -> {
                    Process1<String, A> next = it.hasNext() ? a.receive.f(some(it.next())) : a.receive.f(none());
                    return process(it, next, acc, f);
                }),
                whenClass(Emit1.class, (Emit1<String, A> e) -> process(it, e.tail, f.f(acc, e.head), f))
        ), h -> acc).match(p);
    }


}
