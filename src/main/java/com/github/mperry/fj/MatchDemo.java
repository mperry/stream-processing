package com.github.mperry.fj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.mperry.fj.When.when;
import static com.github.mperry.fj.When.whenClass;
import static fj.data.List.list;

/**
 * Created by MarkPerry on 7/01/2015.
 */
public class MatchDemo {

    public static void main(String [] args) {
        option();
    }


    private static void option() {
        fj.data.List<When<? extends java.util.List<Integer>, String>> list1 = list(
                When.<ArrayList, java.util.List<Integer>, String>whenClass(ArrayList.class, (l) -> "ArrayList"),
                When.<java.util.List, java.util.List<Integer>, String>whenClass(java.util.List.class, l -> "list")
        );

        fj.data.List<When<java.util.List<Integer>, String>> list2 = list(
            When.<ArrayList, java.util.List<Integer>, String>whenClass(ArrayList.class, (l) -> "ArrayList"),
            When.whenClass(ArrayList.class, (l) -> "ArrayList"),
//            When.<java.util.List, java.util.List<Integer>, String>whenClass(java.util.List.class, l -> "list"),
            When.whenClass(ArrayList.class, (l) -> "ArrayList")
        );

        Match<java.util.List<Integer>, String> m = Match.<java.util.List<Integer>, String>match(fj.data.List.list(
                whenClass(ArrayList.class, l -> "ArrayList"),
                whenClass(java.util.List.class, l -> "list")
//                when(Map.class, (java.util.List<Integer> l) -> "map")
//                When.<List<Integer>, String, ArrayList>when(ArrayList.class, (List<Integer> l) -> "ArrayList")
        ), l -> "default");
        java.util.List<Integer> list = Arrays.asList(1, 2);
        System.out.println(m.apply(list));
        System.out.println(list.getClass());
    }

}
