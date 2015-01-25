package com.github.mperry.stream;

import fj.*;
import fj.data.*;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import static com.github.mperry.stream.AwaitIO.await;
import static com.github.mperry.stream.HaltIO.halt;
import static com.github.mperry.stream.ProcessIO.runLog;
import static fj.data.IOFunctions.io;

/**
 * Created by mperry on 9/01/2015.
 */
public class ProcessIOTest {

    @Test
    public void test1() {

    }


    /**
     *
     * do something
     * section 15.3.1, listing 15.6
     */
    @Test
    public void test2() {

//        ProcessIO<String, String> p = null;
        IO<BufferedReader> io = io(() -> {
            return new BufferedReader(new FileReader(TestUtil.testFile("abc.txt")));
        });
        ProcessIO<String, String> p = AwaitIO.<BufferedReader, String, String>await(io, v -> {
            if (v.isFail()) {
                return halt(v.fail());
            } else {
                BufferedReader br = v.success();
                final P1<ProcessIO<String, String>> next = next(br);

//                final P1<ProcessIO<String, String>> next = P.lazy(u -> AwaitIO.await(IOFunctions.<String>io(() -> br.readLine()), (Validation<Throwable, String> v2) -> {
//                    if (v2.isFail()) {
//                        return await(io(() -> {
//                            br.close();
////                            return (String) null;
//                            return null;
////                            return br;
////                            return Unit.unit();
//                        }), x -> halt(v2.fail()));
//                    } else {
//                        String line = v2.success();
//                        if (line == null) {
//                            return halt(End.end());
//                        } else {
//                            return EmitIO.emit(line, next._1());
//                        }
//                    }
//                }));
                return next._1();
            }
        });
        IO<Seq<String>> i = runLog(p);
        try {
            Seq<String> s = i.run();
            List<String> l = s.foldLeft((b, a) -> b.snoc(a), List.<String>list());
            System.out.println(l);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Section 15.3.1, listing 15.6
     * @param br
     * @return
     */
    final P1<ProcessIO<String, String>> next(BufferedReader br) {
        return P.lazy(u -> AwaitIO.await(IOFunctions.<String>io(() -> br.readLine()), (Validation<Throwable, String> v2) -> {
            if (v2.isFail()) {
                return await(io(() -> {
                    br.close();
//                            return (String) null;
                    return null;
//                            return br;
//                            return Unit.unit();
                }), x -> halt(v2.fail()));
            } else {
                String line = v2.success();
                if (line == null) {
                    return halt(End.end());
                } else {
                    return EmitIO.emit(line, next(br)._1());
                }
            }
//                    return null;
        }));
    }


    static <I> ProcessIO<I, String> lines(IO<Option<String>> io) {
        return ProcessIO.<I, Option<String>>eval(io).flatMap(os -> {
            if (os.isNone()) {
                return HaltIO.halt(End.end());
            } else {
                return EmitIO.<I, String>emit(os.some(), lines(io));
            }
        });

    }

    /**
     * Section 15.3.2, method lines
     */
    @Test
    public void resourceSafe() {

        IO<BufferedReader> io = io(() -> {
            return new BufferedReader(new FileReader(TestUtil.testFile("abc.txt")));
        });
        F<BufferedReader, ProcessIO<String, String>> use = src -> {
            Stream<String> s = lines(src);
            Iterator<String> it = s.iterator();
            P1<Option<String>> f = P.lazy(u -> it.hasNext() ? Option.some(it.next()) : Option.none());
            return lines(IOFunctions.io(f));
        };
        ProcessIO<String, String> p = ProcessIO.resource(io, use,
                (BufferedReader src) -> ProcessIO.eval_(IOFunctions.lazy(u -> {
                    TryEffect.f(() -> src.close());
                    return Unit.unit();
                })));
//        ProcessIO.resource(io,
        IO<Seq<String>> i = runLog(p);
        try {
            Seq<String> s = i.run();
            List<String> l = s.foldLeft((b, a) -> b.cons(a), List.<String>list()).reverse();
            System.out.println(l);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Stream<String> lines(BufferedReader r) {

        Validation<IOException, String> v = Try.<String, IOException>f(() -> r.readLine())._1();
        if (v.isFail()) {
            return Stream.nil();
        } else {
            Option<String> o = Option.fromNull(v.success());
            if (o.isNone()) {
                return Stream.nil();
            } else {
                return Stream.stream(o.some()).append(P.lazy(u -> {
                    return lines(r);
                }));
            }
        }

    }



}
