package com.github.mperry.streams;

import fj.Bottom;
import fj.F;
import fj.F1Functions;
import fj.P1;
import fj.data.Stream;

import static fj.F1Functions.*;

/**
 * Created by MarkPerry on 27/04/2014.
 *
 * Await(recv, fallback) requests a value from the input stream, indicating that recv
 * should be used by the driver to produce the next state, and that fallback should be
 * consulted if the input has no more elements available.
 */
public class Await<I, O> extends Process<I, O> {

    F<I, Process<I, O>> receive;
    Process<I, O> fallback;

    public Await(F<I, Process<I, O>> r, Process<I, O> fb) {
        receive = r;
        fallback = fb;
    }

    public Await(F<I, Process<I, O>> r) {
        this(r, Process.<I, O>halt());
    }

    public Stream<O> apply(Stream<I> s) {
        return s.isEmpty() ? fallback.apply(s) : receive.f(s.head()).apply(s.tail()._1());
    }

    @Override
    public <O2> Process<I, O2> map(F<O, O2> f) {
        return new Await<>(andThen(receive, p -> p.map(f)), fallback.map(f));
    }

    @Override
    public Process<I, O> append(P1<Process<I, O>> p) {
        return new Await<>(andThen(receive, p2 -> p2.append(p)), fallback.append(p));
    }

    @Override
    public <O2> Process<I, O2> flatMap(F<O, Process<I, O2>> f) {
        return new Await<>(andThen(receive, p -> p.flatMap(f)), fallback.flatMap(f));
    }

    @Override
    public Process<I, O> feed(Iterable<I> in, Stream<O> out) {
        Stream<I> s = Stream.iterableStream(in);
        if (s.isEmpty()) {
            return Process.emit(out, this);
        } else {
            Process<I, O> next = receive.f(s.head());
            return next.feed(s.tail()._1(), out);
        }
    }

    @Override
    public <I2> Process<I2, O> pipeFrom(Process<I2, I> p) {
        return p.pipeToAwait(this);

    }

    @Override
    public <O2> Process<I, O2> pipeToAwait(Await<O, O2> p) {
        // Await(F<I, Process<I, O2>>, Process<I, O2>
        throw Bottom.error("await pipe to await");
//        return null;
    }


}
