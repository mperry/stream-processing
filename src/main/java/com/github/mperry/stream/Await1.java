package com.github.mperry.stream;

import fj.F;
import fj.data.Option;

/**
 * Created by MarkPerry on 7/01/2015.
 *
 * Await(receive, fallback) requests a value from the input stream, indicating
 * that receive should be used by the driver to produce the next state, and
 * that fallback should be consulted if the input has no more elements available.
 */
public class Await1<I, O> extends Process1<I, O> {

    final F<Option<I>, Process1<I, O>> receive;

    private Await1(F<Option<I>, Process1<I, O>> r) {
        receive = r;
    }

    public static <I, O> Await1<I, O> await(F<Option<I>, Process1<I, O>> receive) {
        return new Await1<I, O>(receive);
    }

    public static <I, O> Await1<I, O> await(F<I, Process1<I, O>> f, Process1<I, O> fallback) {
        return await(o -> o.isNone() ? fallback : f.f(o.some()));
    }

    public static <I, O> Await1<I, O> awaiti(F<I, Process1<I, O>> f) {
        return await(f, Halt1.halt());
    }


}
