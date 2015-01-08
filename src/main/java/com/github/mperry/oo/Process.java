package com.github.mperry.oo;

import fj.F;
import fj.P1;
import fj.data.Stream;

/**
 * Created by MarkPerry on 27/04/2014.
 *
 * This implementation is based on inheritance and overriding methods in subclasses
 */
public abstract class Process<I, O> {

    public static <I, O> Emit<I, O> emit(Iterable<O> head, Process<I, O> f) {
        return new Emit<I, O>(head, f);
    }

    public static <I, O> Emit<I, O> emit(Iterable<O> head) {
        return new Emit<I, O>(head);
    }

    public static <I, O> Await<I, O> await(F<I, Process<I, O>> receive, Process<I, O> fallback) {
        return new Await<I, O>(receive, fallback);
    }

    public static <I, O> Halt<I, O> halt() {
        return new Halt<I, O>();
    }

    public abstract Stream<O> apply(Stream<I> s);

    public abstract <O2> Process<I, O2> map(F<O, O2> f);

    public abstract Process<I, O> append(P1<Process<I, O>> p);

    public Process<I, O> emitAll(Iterable<O> head, Process<I, O> tail) {
        return new Emit<I, O>(head, tail);
    }

    public Process<I, O> emitAll(O head, Process<I, O> tail) {
        return emitAll(Stream.stream(head), tail);
    }

    public abstract <O2> Process<I, O2> flatMap(F<O, Process<I, O2>> f);

//    public static<I, O> Process<I, O> feed(Iterable<I> it) {
//        return emit(it);
//    }

    public abstract Process<I, O> feed(Iterable<I> in, Stream<O> out);

    /**
     * Feed the output of this `Process` as input of `p2`. The implementation
     * will fuse the two processes, so this process will only generate
     * values as they are demanded by `p2`.
    */
    public <O2> Process<I, O2> pipe(Process<O, O2> p2) {
        return p2.pipeFrom(this);
    }

    public abstract <I2> Process<I2, O> pipeFrom(Process<I2, I> p);

    public abstract <O2> Process<I, O2> pipeToAwait(Await<O, O2> p);


    public static <I, O> Process<I, O> lift(F<I, O> f) {
        return Process.<I, O>await((I i) -> Process.emit(Stream.stream(f.f(i))), lift(f));

    }

    public abstract Process<I, O> repeat();


    public static <I, O> F<Process<I, O>, Process<I, O>> repeat_() {
        return (p) -> p.repeat();
    }

}


