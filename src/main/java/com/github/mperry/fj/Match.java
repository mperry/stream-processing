package com.github.mperry.fj;

import fj.F;
import fj.P;
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

    public static <A, B> Match<A, B> createMatch(List<When<A, B>> cases, F<A, B> other) {
        return new Match<A, B>(cases, other);
    }

    public static <A, B> B match(A a, F<A, B> fallback, List<When<A, B>> cases) {
        return createMatch(cases, fallback).match(a);
    }

    public static <A, B> B match(A a, F<A, B> fallback, When<A, B> w) {
        return match(a, fallback, List.list(w));
    }


    public Option<B> matchWithoutDefault(A a) {
        return cases.foldLeft((acc, w) -> {
            if (acc.isSome()) {
                return acc;
            } else {
                return w.guard.f(a) ? w.f(a) : acc;
            }
        }, Option.<B>none());
    }

    public B match(A a) {
        Option<B> o = matchWithoutDefault(a);
        return o.orSome(P.lazy(u -> other.f(a)));
    }

    public B match(A a, F<A, B> defaultValue) {
        return matchWithoutDefault(a).orSome(defaultValue.f(a));
    }

    public Match<A, B> createMatch(When<A, B> w) {
        return createMatch(cases.snoc(w), other);
    }

    public <C> Match<A, C> map(F<B, C> f) {
        return createMatch(cases.map((w) -> w.map(f)), andThen(other, f));
    }

    public <C> Match<A, C> flatMap(F<B, Match<A, C>> f) {

//        F<B, C> g = b -> f.f(b).other;
//        C c2 = g.f(other);
//        C c = f.f(other).other;
        F<A, C> h = a -> andThen(other, f).f(a).other.f(a);
//        andThen(other, f.f())
        return createMatch(cases.map(w -> When.when(w.guard, a -> f.f(w.transform.f(a)).match(a, h))), h);

    }

    public Match<A, B> append(Match<A, B> m) {
        return createMatch(cases.append(m.cases), other);
    }

}
