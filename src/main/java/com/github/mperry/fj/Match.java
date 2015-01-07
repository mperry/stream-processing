package com.github.mperry.fj;

import fj.F;
import fj.data.List;
import fj.data.Option;

import static fj.F1Functions.andThen;

/**
 * Created by MarkPerry on 6/01/2015.
 */
public class Match<A, B> {

    List<When<A, B>> cases;
    F<A, B> other;

    private Match(List<When<A, B>> list, F<A, B> o) {
        cases = list;
        other = o;
    }

    public static <A, B> Match<A, B> match(List<When<A, B>> cases, F<A, B> other) {
        return new Match<A, B>(cases, other);
    }

    public Option<B> applyWithoutDefault(A a) {
        return cases.foldLeft((acc, w) -> {
            if (acc.isSome()) {
                return acc;
            } else {
                return w.guard.f(a) ? w.f(a) : acc;
            }
        }, Option.<B>none());
    }

    public B apply(A a) {
        return applyWithoutDefault(a).orSome(other.f(a));
    }

    public B apply(A a, F<A, B> defaultValue) {
        return applyWithoutDefault(a).orSome(defaultValue.f(a));
    }

    public Match<A, B> when(When<A, B> w) {
        return match(cases.snoc(w), other);
    }

    public <C> Match<A, C> map(F<B, C> f) {
        return match(cases.map((w) -> w.map(f)), andThen(other, f));
    }

    public <C> Match<A, C> flatMap(F<B, Match<A, C>> f) {

//        F<B, C> g = b -> f.f(b).other;
//        C c2 = g.f(other);
//        C c = f.f(other).other;
        F<A, C> h = a -> andThen(other, f).f(a).other.f(a);
//        andThen(other, f.f())
        return match(cases.map(w -> When.when(w.guard, a -> f.f(w.transform.f(a)).apply(a, h))), h);

    }

    public Match<A, B> append(Match<A, B> m) {
        return match(cases.append(m.cases), other);
    }

}
