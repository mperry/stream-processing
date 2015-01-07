package com.github.mperry.match;

/**
 * Created by MarkPerry on 7/01/2015.
 */
public class Emit<I, O> extends Process<I, O> {

    final O head;
    final Process<I, O> tail;

    private Emit(O h, Process<I, O> t) {
        head = h;
        tail = t;
    }

    public static <I, O> Emit<I, O> emit(O head, Process<I, O> tail) {
        return new Emit<I, O>(head, tail);
    }

    public static <I, O> Emit<I, O> emit(O head) {
        return new Emit<I, O>(head, Halt.halt());
    }


}
