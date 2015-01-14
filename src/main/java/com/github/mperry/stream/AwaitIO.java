package com.github.mperry.stream;

import fj.F;
import fj.data.IO;
import fj.data.Validation;

/**
 * Created by mperry on 9/01/2015.
 * Section 15.3.1
 */
public class AwaitIO<I, I2, O> extends ProcessIO<I2, O> {
//    public class AwaitIO<I, I2, O> extends ProcessIO<I, O> {

    final IO<I> request;
    final F<Validation<Throwable, I>, ProcessIO<I2, O>> receive;

    public AwaitIO(IO<I> req, F<Validation<Throwable, I>, ProcessIO<I2, O>> rec) {
        request = req;
        receive = rec;
    }

    public static <I, I2, O> AwaitIO<I, I2, O> await(IO<I> request, F<Validation<Throwable, I>, ProcessIO<I2, O>> receive) {
        return new AwaitIO<I, I2, O>(request, receive);
    }

}
