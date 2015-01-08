package com.github.mperry.fj;

import fj.F;
import fj.data.Stream;

import java.util.ArrayList;
import java.util.Arrays;

import static com.github.mperry.fj.When.when;
import static com.github.mperry.fj.When.whenClass;
import static fj.data.List.list;

/**
 * Created by MarkPerry on 7/01/2015.
 */
public class MatchDemo {

    public static void main(String [] args) {
        fizzbuzz();
        option();
    }

    private static boolean divBy(int n, int divideBy) {
        return n % divideBy == 0;
    }

    private static F<Integer, Boolean> divBy(int divideBy) {
        return i -> divBy(i, divideBy);
    }

    private static void fizzbuzz() {
        int fizz = 3;
        int buzz = 5;
        Match<Integer, String> m = Match.createMatch(fj.data.List.<When<Integer, String>>list(
                when(i -> divBy(i, fizz) && divBy(i, buzz), i -> "fizzbuzz"),
                when(divBy(fizz), i -> "fizz"),
                when(divBy(buzz), i -> "buzz")
        ), i -> i.toString());
        Stream<Integer> s1 = Stream.range(1);
        Stream<String> s2 = s1.map(i -> m.match(i));
        System.out.println(s1.zip(s2).take(20).toList());
    }

    /**
     * This is a prototype to play around with the generic types for When.whenClass, Java warnings and Intellij warnings
     */
    private static void option() {
        fj.data.List<When<? extends java.util.List<Integer>, String>> list1 = list(
//                When.<ArrayList<Integer>, java.util.List<Integer>, String>whenClass(ArrayList.class, (l) -> "ArrayList"),
                When.whenClass(ArrayList.class, (l) -> "ArrayList"),
//                When.<java.util.List, java.util.List<Integer>, String>whenClass(java.util.List.class, l -> "list")
                When.whenClass(java.util.List.class, l -> "list")
        );

        fj.data.List<When<java.util.List<Integer>, String>> list2 = fj.data.List.list(
//            When.<ArrayList, java.util.List<Integer>, String>whenClass(ArrayList.class, (l) -> "ArrayList"),
                When.whenClass(ArrayList.class, (l) -> "ArrayList"),
                When.whenClass(ArrayList.class, (l) -> "ArrayList"),
//            When.<java.util.List, java.util.List<Integer>, String>whenClass(java.util.List.class, l -> "list"),
                When.whenClass(ArrayList.class, (l) -> "ArrayList")
        );

        Match<java.util.List<Integer>, String> m = Match.<java.util.List<Integer>, String>createMatch(fj.data.List.list(
                whenClass(ArrayList.class, l -> "ArrayList"),
                whenClass(java.util.List.class, l -> "list")
//                when(Map.class, (java.util.List<Integer> l) -> "map")
//                When.<List<Integer>, String, ArrayList>when(ArrayList.class, (List<Integer> l) -> "ArrayList")
        ), l -> "default");
        java.util.List<Integer> list = Arrays.asList(1, 2);
        System.out.println(m.match(list));
        System.out.println(list.getClass());
    }

}
