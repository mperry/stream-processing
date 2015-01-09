package com.github.mperry.stream;

import fj.P;
import fj.P1;
import fj.Unit;
import fj.data.*;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

}
