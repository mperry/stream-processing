package com.github.mperry.stream;

/**
 * Created by MarkPerry on 7/01/2015.
 *
 * Halt indicates to the driver that no more elements should be read from
 * the input stream or emitted to the output.
 *
 */
public class Halt1<I, O> extends Process1<I, O> {

    private Halt1() {}

    public static <I, O> Halt1<I, O> halt() {
        return new Halt1<I, O>();
    }

}
