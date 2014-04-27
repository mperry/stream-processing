package com.github.mperry.streams;

import fj.F;
import fj.data.Stream;

/**
 * Created by MarkPerry on 27/04/2014.
 *
 * Emit(head,tail)indicates to the driver that the  values should be emitted to the head
 * output stream, and that tail should be the next state following that.
 */
public class Emit<I, O> extends Process<I, O> {

    Iterable<O> head;
    Process<I, O> tail;

    public Emit(Iterable<O> h, Process<I, O> t) {
        head = h;
        tail = t;
    }

    public Emit(Iterable<O> h) {
        this(h, Process.<I, O>halt());
    }

    @Override
    public Stream<O> apply(Stream<I> s) {
        return Stream.iterableStream(head).append(tail.apply(s));
    }

    @Override
    public <O2> Process<I, O2> map(F<O, O2> f) {
        return new Emit<>(Stream.iterableStream(head).map(f), tail.map(f));
    }
}
