package com.github.mperry.streams;

import fj.F;
import fj.data.Stream;

/**
 * Created by MarkPerry on 27/04/2014.
 */
public abstract class Process<I, O> {

    public static <I, O> Emit<I, O> emit(Iterable<O> head, Process<I, O> f) {
        return new Emit<>(head, f);
    }

    public static <I, O> Await<I, O> await(F<I, Process<I, O>> receive, Process<I, O> fallback) {
        return new Await<>(receive, fallback);
    }

    public static <I, O> Halt<I, O> halt() {
        return new Halt<>();
    }

    public abstract Stream<O> apply(Stream<I> s);


    public abstract <O2> Process<I, O2> map(F<O, O2> f);

}


