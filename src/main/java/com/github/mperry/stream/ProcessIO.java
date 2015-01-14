package com.github.mperry.stream;

import com.github.mperry.fj.Match;
import com.github.mperry.fj.When;
import fj.*;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Seq;
import fj.data.Validation;

import static fj.F1Functions.andThen;

/**
 * Created by mperry on 9/01/2015.
 */
public class ProcessIO<I, O> {


    /**
     * Helper for append, section 15.3, listing 15.5
     */
    public ProcessIO<I, O> onHalt(F<Throwable, ProcessIO<I, O>> f) {
        return Match.unsafeMatch(this,
            When.<HaltIO<I, O>, ProcessIO<I, O>, ProcessIO<I, O>>whenClass(
                HaltIO.class, h -> doTry(P.lazy(u -> f.f(h.error)))
            ).appendClass(
                EmitIO.class, (EmitIO<I, O> e) -> EmitIO.emit(e.head, e.tail.onHalt(f))
            ).appendClass(
                AwaitIO.class, (AwaitIO<I, I, O> a) -> AwaitIO.await(a.request, andThen(a.receive, x -> x.onHalt(f)))
            )
        );
    }

    /**
     * section 15.3, listing 15.5
     */
    public ProcessIO<I, O> append(P1<ProcessIO<I, O>> p) {
        return onHalt(t -> {
            return End.class.isInstance(t) ? p._1() : HaltIO.halt(t);
        });
    }

    /**
     * Catches any exceptions throw by the process and converts them to return halt
     */
    public static <I, O> ProcessIO<I, O> doTry(P1<ProcessIO<I, O>> p) {
        try {
            return p._1();
        } catch (Throwable t) {
            return HaltIO.halt(t);
        }
    }

    /**
     * section 15.3
     */
    public <O2> ProcessIO<I, O2> flatMap(F<O, ProcessIO<I, O2>> f) {

        return Match.unsafeMatch(this,
                When.<HaltIO<I, O>, ProcessIO<I, O>, ProcessIO<I, O2>>whenClass(
                        HaltIO.class, (HaltIO<I, O> h) -> HaltIO.halt(h.error)
                ).appendClass(
                        EmitIO.class, (EmitIO<I, O> e) -> doTry(P.lazy(u -> f.f(e.head))).append(P.lazy(u -> e.tail.flatMap(f)))
                ).appendClass(
                        AwaitIO.class, (AwaitIO<I, I, O> a) -> AwaitIO.await(a.request, andThen(a.receive, x -> x.flatMap(f)))
                )
        );
    }

    /**
     * Section 15.3.1, listing 15.6
     */
    public static <I, O> IO<Seq<O>> runLog(ProcessIO<I, O> src) {

        // Note: The example in the book uses a fixed thread pool and shuts down
        // the pool in the finally block below
        return IOFunctions.lazy(u -> {
            try {
                return go(src, Seq.empty());
            } finally {
            }
        });


    }

    /**
     * Section 15.3.1, listing 15.6
     */
    public static <I, O> Seq<O> go(ProcessIO<I, O> p, Seq<O> acc) {
        return Match.unsafeMatch(p,
            When.<HaltIO<I, O>, ProcessIO<I, O>, Seq<O>>whenClass(
                HaltIO.class, (HaltIO<I, O> h) -> {
                    if (End.class.isInstance(h.error)) {
                        return acc;
                    } else {
                        throw new RuntimeException(h.error);
                    }
                }
            ).appendClass(
                    EmitIO.class, (EmitIO<I, O> e) -> go(e.tail, acc.snoc(e.head))
            ).appendClass(
                AwaitIO.class, (AwaitIO<I, I, O> a) -> {
                    F<AwaitIO<I, I, O>, ProcessIO<I, O>> g = (AwaitIO<I, I, O> a2) -> {
                        try {
                            return a2.receive.f(Validation.success(a.request.run()));
                        } catch (Throwable t) {
                            return a2.receive.f(Validation.fail(t));
                        }
                    };
                    return go(g.f(a), acc);
                }
            )
        );
    }



//    public ProcessIO<I, O> error(String msg) {
//        Bottom.error(msg);
//        return this;
//    }


    /**
     * Skip definding the more general runLog, exercise 15.10 as it need typeclasses
     */
}
