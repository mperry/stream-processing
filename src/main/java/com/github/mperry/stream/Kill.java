package com.github.mperry.stream;

/**
 * Created by mperry on 9/01/2015.
 */
public class Kill extends Exception {

    private Kill() {}

    public static Kill kill() {
        return new Kill();
    }

}
