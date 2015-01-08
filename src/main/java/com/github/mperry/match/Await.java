package com.github.mperry.match;

import fj.F;
import fj.data.Option;

/**
 * Created by MarkPerry on 7/01/2015.
 */
public class Await<I, O> extends Process<I, O> {

    final F<Option<I>, Process<I, O>> receive;

    private Await(F<Option<I>, Process<I, O>> r) {
        receive = r;
    }

    public static <I, O> Await<I, O> await(F<Option<I>, Process<I, O>> receive) {
        return new Await<I, O>(receive);
    }

    public static <I, O> Await<I, O> await(F<I, Process<I, O>> f, Process<I, O> fallback) {
        return await(o -> o.isNone() ? fallback : f.f(o.some()));
    }

    public static <I, O> Await<I, O> awaiti(F<I, Process<I, O>> f) {
        return await(f, Halt.halt());
    }


}
