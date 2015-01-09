package com.github.mperry.stream;

/**
 * Created by MarkPerry on 7/01/2015.
 *
 * Emit(head,tail) indicates to the driver that the head values should be
 * emitted to the output stream, and that tail should be the next state
 * following that.
 */
public class Emit1<I, O> extends Process1<I, O> {

    final O head;
    final Process1<I, O> tail;

    private Emit1(O h, Process1<I, O> t) {
        head = h;
        tail = t;
    }

    public static <I, O> Emit1<I, O> emit(O head, Process1<I, O> tail) {
        return new Emit1<I, O>(head, tail);
    }

    public static <I, O> Emit1<I, O> emit(O head) {
        return new Emit1<I, O>(head, Halt1.halt());
    }


}
