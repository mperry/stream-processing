package com.github.mperry.match;

/**
 * Created by MarkPerry on 7/01/2015.
 *
 * Halt indicates to the driver that no more elements should be read from
 * the input stream or emitted to the output.
 *
 */
public class Halt<I, O> extends Process<I, O> {

    private Halt() {}

    public static <I, O> Halt<I, O> halt() {
        return new Halt<I, O>();
    }

}
