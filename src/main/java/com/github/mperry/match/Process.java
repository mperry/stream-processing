package com.github.mperry.match;

import com.github.mperry.fj.Match;
import com.github.mperry.fj.When;
import fj.F;
import fj.P;
import fj.data.List;
import fj.data.Option;
import fj.data.Stream;

import static com.github.mperry.fj.When.when;
import static com.github.mperry.fj.When.whenClass;
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
        return Await.await(o -> o.isSome() ? Emit.emit(f.f(o.some())) : Halt.halt());
    }

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

    public static <I, O> Process<I, O> lift(F<I, O> f) {
        return liftOne(f).repeat();
    }

    public static <I> Process<I, I> filter(F<I, Boolean> f) {
        return Await.<I, I>await(o -> {
            if (o.isNone()) {
                return Halt.halt();
            } else {
                I i = o.some();
                return f.f(i) ? Emit.emit(i) : Halt.halt();
            }

        }).repeat();
    }

    public static Process<Double, Double> sum(double acc) {
        return Await.await((Option<Double> o) -> {
            if (o.isNone()) {
                return Halt.halt();
            } else {
                double d = o.some();
                return Emit.emit(d + acc, sum(d + acc));
            }
//            return o.map(d -> Emit.emit(d + acc, sum(d + acc))).orSome(Halt.<Double, Double>halt());
        });
    }

    public static Process<Double, Double> sum() {
        return sum(0.0);

    }

}
