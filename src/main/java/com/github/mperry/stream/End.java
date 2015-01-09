package com.github.mperry.stream;

/**
 * Created by mperry on 9/01/2015.
 */
public class End extends Exception {

    private End() {}
    
    public static End end() {
        return new End();
    }

}
