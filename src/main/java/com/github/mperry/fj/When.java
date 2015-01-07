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

    public static <A, B, C extends A> When<A, B> when(final java.lang.Class<C> clazz, F<A, B> f) {
        return when((A a) -> clazz.isInstance(a), f);
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
