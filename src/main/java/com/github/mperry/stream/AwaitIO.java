package com.github.mperry.stream;

import fj.F;
import fj.data.IO;
import fj.data.Validation;

/**
 * Created by mperry on 9/01/2015.
 */
public class AwaitIO<I, O> extends ProcessIO<I, O> {

    final IO<I> request;
    final F<Validation<Throwable, I>, ProcessIO<I, O>> receive;

    public AwaitIO(IO<I> req, F<Validation<Throwable, I>, ProcessIO<I, O>> rec) {
        request = req;
        receive = rec;
    }

    public static <I, O> AwaitIO<I, O> await(IO<I> request, F<Validation<Throwable, I>, ProcessIO<I, O>> receive) {
        return new AwaitIO<I, O>(request, receive);
    }

}
