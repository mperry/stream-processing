package com.github.mperry.fj;

import fj.F;
import fj.F1Functions;
import fj.data.Option;

import static fj.data.Option.none;
import static fj.data.Option.some;

/**
 * Created by MarkPerry on 6/01/2015.
 */
public class When<A, B> {

    public final F<A, Boolean> guard;
    public final F<A, B> transform;

    private When(F<A, Boolean> g, F<A, B> t) {
        guard = g;
        transform = t;
    }

    public static <A, B> When<A, B> when(F<A, Boolean> g, F<A, B> t) {
        return new When<A, B>(g, t);
    }

    public static <A extends C, B extends A, C, D> When<C, D> whenClass(final java.lang.Class<?> clazz, F<B, D> f) {
        return when((C c) -> clazz.isInstance(c), (C c) -> {
            B b = (B) c;
            return f.f(b);
        });
    }

    public Option<B> f(A a) {
        return guard.f(a) ? some(transform.f(a)) : none();
    }

    public <C> When<A, C> map(F<B, C> f) {
        return when(guard, F1Functions.andThen(transform, f));
    }

    public <C> When<A, C> flatMap(F<B, When<A, C>> f) {
        return when(guard, a -> f.f(transform.f(a)).transform.f(a));
    }

}
