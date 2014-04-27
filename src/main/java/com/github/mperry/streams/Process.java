package com.github.mperry.streams;

import fj.F;
import fj.P1;
import fj.data.Stream;

/**
 * Created by MarkPerry on 27/04/2014.
 */
public abstract class Process<I, O> {

    public static <I, O> Emit<I, O> emit(Iterable<O> head, Process<I, O> f) {
        return new Emit<I, O>(head, f);
    }

    public static <I, O> Emit<I, O> emit(Iterable<O> head) {
        return new Emit<I, O>(head);
    }

    public static <I, O> Await<I, O> await(F<I, Process<I, O>> receive, Process<I, O> fallback) {
        return new Await<I, O>(receive, fallback);
    }

    public static <I, O> Halt<I, O> halt() {
        return new Halt<I, O>();
    }

    public abstract Stream<O> apply(Stream<I> s);

    public abstract <O2> Process<I, O2> map(F<O, O2> f);

    public abstract Process<I, O> append(P1<Process<I, O>> p);

    public Process<I, O> emitAll(Iterable<O> head, Process<I, O> tail) {
        return new Emit<I, O>(head, tail);
    }

    public Process<I, O> emitAll(O head, Process<I, O> tail) {
        return emitAll(Stream.stream(head), tail);
    }

    public abstract <O2> Process<I, O2> flatMap(F<O, Process<I, O2>> f);

//    public static<I, O> Process<I, O> feed(Iterable<I> it) {
//        return emit(it);
//    }

    public abstract Process<I, O> feed(Iterable<I> in, Stream<O> out);
//    public abstract <O2> Process<I, O2> pipe(Process<O, O2> p2);

}


