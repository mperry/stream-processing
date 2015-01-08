package com.github.mperry.oo;

import fj.F;
import fj.P1;
import fj.data.Stream;

/**
 * Created by MarkPerry on 27/04/2014.
 *
 * Halt indicates to the driver that no more elements should be read from the input stream
 * or emitted to the output.
 */
public class Halt<I, O> extends Process<I, O> {

    public Halt() {
    }

    public Stream<O> apply(Stream<I> s) {
        return Stream.<O>nil();
    }

    @Override
    public <O2> Process<I, O2> map(F<O, O2> f) {
        return new Halt<>();
    }

    @Override
    public Process<I, O> append(P1<Process<I, O>> p) {
        return p._1();
    }

    @Override
    public <O2> Process<I, O2> flatMap(F<O, Process<I, O2>> f) {
        return new Halt<>();
    }

    @Override
    public Process<I, O> feed(Iterable<I> in, Stream<O> out) {
        return Process.emit(out, this);
    }

    @Override
    public <I2> Process<I2, O> pipeFrom(Process<I2, I> p) {
        return new Halt<>();
    }

    public <O2> Process<I, O2> pipeToAwait(Await<O, O2> a) {
        return Process.<I, O>halt().pipe(a.fallback);
    }

    public <O2> Process<I, O2> pipe2(Process<O, O2> p) {
        return null;
    }

    @Override
    public Process<I, O> repeat() {
        // TODO: infinitely recursive
        return repeat();
    }

}
