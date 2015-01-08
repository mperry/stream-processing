package com.github.mperry.match;

import fj.Unit;
import fj.data.IO;
import fj.data.List;
import fj.data.Stream;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fj.data.List.list;
import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

/**
 * Created by mperry on 8/01/2015.
 */
public class ProcessMatchTest {

    @Test
    public void liftOne() {
        Stream<Integer> s = Process.liftOne((Integer i) -> i * 2).apply(Stream.range(1));
        out.println(s.toList());
        assertTrue(s.toList().equals(list(2)));
    }

    @Test
    public void repeat() {
        Stream<Unit> s1 = Stream.repeat(Unit.unit());
        Process<Unit, Integer> p = Process.lift((Unit u) -> 1);
        Stream<Integer> s2 = p.apply(s1);
        out.println(s2.take(10).toList());
        assertTrue(s2.take(2).toList().equals(list(1, 1)));
    }

    @Test
    public void filter() {
        List<Integer> l = Process.filter((Integer i) -> i % 2 == 0).apply(Stream.range(1, 5)).toList();
        out.println(l);
        assertTrue(l.equals(list(2, 4)));

    }

    @Test
    public void sum() {
        List<Double> l = Process.sum().apply(Stream.range(1, 5).map(i -> (double) i)).toList();
        out.println(l);
        assertTrue(l.equals(list(1.0, 3.0, 6.0, 10.0)));
    }

    @Test
    public void take() {
        List<Integer> l = Process.<Integer>take(2).apply(Stream.range(1)).toList();
        out.println(l);
        assertTrue(l.equals(list(1, 2)));
    }

    @Test
    public void drop() {
        List<Integer> l = Process.<Integer>drop(2).apply(Stream.range(1, 5)).toList();
        out.println(l);
        assertTrue(l.equals(list(3, 4)));
    }

    @Test
    public void pipe() {
        Process<Integer, Integer> p = Process.filter((Integer i) -> i % 2 == 0).pipe(Process.lift((Integer i) -> i + 1));
        List<Integer> l = p.apply(Stream.range(1, 10)).toList();
        out.println(l);
        assertTrue(l.equals(list(3, 5, 7, 9)));
    }

    @Test
    public void processFileWithCount() {
        try {
            Process<String, Boolean> p = Process.<String>count2().pipe(Process.exists((Integer i) -> i == 3));
            IO<Boolean> io = Process.processFile(basicFile(), p, false, (a, b) -> a || b);
            Boolean result = io.run();
            System.out.println(result);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String RESOURCE_DIR = "test/resources";
    String BASE_FILE = "abc.txt";

    File testFile(String filename) throws URISyntaxException {
        Path p = Paths.get(getClass().getResource("/" + filename).toURI());
        return p.toFile();
    }

    File basicFile() throws URISyntaxException {
        return testFile(BASE_FILE);
    }

    @Test
    public void processFileWithAppend() {
        try {
            Process<String, String> p = Process.<String>identity();
            IO<String> io = Process.processFile(basicFile(), p, "", (a, b) -> a + "***" + b);
            String r = io.run();
            System.out.println(r);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
