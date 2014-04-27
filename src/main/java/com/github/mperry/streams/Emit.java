package com.github.mperry.streams;

import fj.F;
import fj.P;
import fj.P1;
import fj.data.Stream;

/**
 * Created by MarkPerry on 27/04/2014.
 *
 * Emit(head,tail) indicates to the driver that the head values should be emitted to the
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

    @Override
    public Process<I, O> append(P1<Process<I, O>> p) {
        return emitAll(head, tail.append(p));

    }

    public Process<I, O> emitAll(Iterable<O> h, Process<I, O> t) {
        Stream<O> s2 = Stream.<O>iterableStream(head);
        Stream<O> s = Stream.iterableStream(h).append(s2);
        return new Emit<>(s, tail);

    }

    @Override
    public <O2> Process<I, O2> flatMap(F<O, Process<I, O2>> f) {
        Stream<O> s = Stream.iterableStream(head);
        if (s.isEmpty()) {
            return tail.flatMap(f);
        } else {
            Process<I, O2> p = f.f(s.head());
            return p.append(P.p(emitAll(s.tail()._1(), tail).flatMap(f)));
        }
    }

}
