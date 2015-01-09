package com.github.mperry.stream;

/**
 * Created by mperry on 9/01/2015.
 */
public class EmitIO<I, O> extends ProcessIO<I, O> {

    final O head;
    final ProcessIO<I, O> tail;

    private EmitIO(O h, ProcessIO<I, O> t) {
        head = h;
        tail = t;
    }

    public static <I, O> EmitIO<I, O> emit(O h, ProcessIO<I, O> t) {
        return new EmitIO<I, O>(h, t);
    }


}
