package com.github.mperry.match;

import fj.Unit;
import fj.data.List;
import fj.data.Stream;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by mperry on 8/01/2015.
 */
public class ProcessMatchTest {

    @Test
    public void liftOne() {
        Stream<Integer> s = Process.liftOne((Integer i) -> i * 2).apply(Stream.range(1));
        System.out.println(s.toList());
        assertTrue(s.toList().equals(List.list(2)));
    }

    @Test
    public void repeat() {
        Stream<Unit> s1 = Stream.repeat(Unit.unit());
        Process<Unit, Integer> p = Process.lift((Unit u) -> 1);
        Stream<Integer> s2 = p.apply(s1);
        System.out.println(s2.take(10).toList());
        assertTrue(s2.take(2).toList().equals(List.list(1, 1)));
    }

}
