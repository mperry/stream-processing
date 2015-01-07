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

}
