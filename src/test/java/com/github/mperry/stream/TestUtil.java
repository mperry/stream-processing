package com.github.mperry.stream;

import fj.Bottom;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by mperry on 9/01/2015.
 */
public class TestUtil {


    public static File testFile(String filename, Class<?> clazz) throws URISyntaxException {
//        Path p = Paths.get(clazz.getResource("/" + filename).toURI());
        Path p = Paths.get(clazz.getResource("/" + filename).toURI());
        return p.toFile();
    }

    public static File testFile(String filename) {
        try {
            return testFile(filename, TestUtil.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw Bottom.error(e.getMessage());
        }
    }


}
