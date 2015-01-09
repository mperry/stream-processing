package com.github.mperry.stream;

/**
 * Created by mperry on 9/01/2015.
 */
public class HaltIO<I, O> extends ProcessIO<I, O> {

    final Throwable error;

    private HaltIO(Throwable t) {
        error = t;
    }

    public static <I, O> HaltIO<I, O> halt(Throwable t) {
        return new HaltIO<I, O>(t);
    }

}
