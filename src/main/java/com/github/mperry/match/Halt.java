package com.github.mperry.match;

/**
 * Created by MarkPerry on 7/01/2015.
 */
public class Halt<I, O> extends Process<I, O> {

    private Halt() {}

    public static <I, O> Halt<I, O> halt() {
        return new Halt<I, O>();
    }

}
