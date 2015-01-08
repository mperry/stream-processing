package com.github.mperry.fj;

import fj.P1;

import java.util.Iterator;

/**
 * Created by mperry on 8/01/2015.
 */
public class Util {



    public static <A> fj.data.Stream<A> toFJStream(java.util.stream.Stream<A> s) {
        return iteratorStream(s.iterator());
    }

    public static <A> fj.data.Stream<A> iteratorStream(final Iterator<A> i) {
        if (i.hasNext()) {
            final A a = i.next();
            return fj.data.Stream.cons(a, new P1<fj.data.Stream<A>>() {
                public fj.data.Stream<A> _1() {
                    return iteratorStream(i);
                }
            });
        } else
            return fj.data.Stream.nil();
    }
}
