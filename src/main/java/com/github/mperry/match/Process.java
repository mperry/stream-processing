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
 */
public class Process<I, O> {


    /**
     * Sample driver, ch 15.2, p272
     */
    public Stream<O> apply(Stream<I> s) {


        List<When<Process<I, O>, Stream<O>>> list2 = List.<When<Process<I, O>, Stream<O>>>list(
            whenClass(Halt.class, z -> Stream.<O>nil()),
            whenClass(Await.class, z -> {
                Await<I, O> a = z;
                if (s.isNotEmpty()) {
                    return a.receive.f(some(s.head())).apply(s.tail()._1());
                } else {
                    return a.receive.f(none()).apply(s);
                }
            }),
            whenClass(Emit.class, z -> {
                Emit<I, O> e = z;
                return Stream.cons(e.head, P.lazy(u -> e.tail.apply(s)));
            })
        );

        // default is halt case
        return Match.match(list2, p -> Stream.<O>nil()).apply(this);

    }

    /**
     * Emit a single element
     * Ch 15.2.1
     */
    public static <I, O> Process<I, O> liftOne(F<I, O> f) {
//        return Await.await(o -> o.map(i -> Emit.<I, O>emit(f.f(i))).orSome(Halt.<I, O>halt()));
        return Await.await(o -> o.isSome() ? Emit.emit(f.f(o.some())) : Halt.halt());
    }

    /**
     * Ch 15.2.1
     * Transform a whole stream
     */
    public static <I, O> Process<I, O> repeat(Process<I, O> p) {
        List<When<Process<I, O>, Process<I, O>>> list1 = List.<When<Process<I, O>, Process<I, O>>>list(
            whenClass(Halt.class, h -> repeat(p)),
            whenClass(Await.class, z -> {
                Await<I, O> a = z;
                return Await.await((Option<I> o) -> o.isNone() ? a.receive.f(o) : repeat(a.receive.f(o)));
            }),
            whenClass(Emit.class, z -> {
                Emit<I, O> e = z;
                return Emit.emit(e.head, repeat(e.tail));
            })

        );
        // default is halt case, repeated from above
        return Match.match(list1, h -> repeat(h)).apply(p);
//        return null;


    }

}
