package com.github.mperry.stream;

/**
 * Created by MarkPerry on 7/01/2015.
 *
 * Emit(head,tail) indicates to the driver that the head values should be
 * emitted to the output stream, and that tail should be the next state
 * following that.
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
