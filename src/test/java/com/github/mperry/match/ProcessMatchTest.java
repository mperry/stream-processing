package com.github.mperry.match;

import fj.Unit;
import fj.data.List;
import fj.data.Stream;
import org.junit.Test;

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
        assertTrue(l.equals(List.list(1.0, 3.0, 6.0, 10.0)));
    }

}
