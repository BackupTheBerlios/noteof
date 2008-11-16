package de.notEOF.test;

import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        System.out.println("Hello World");

        try {
            ProcessBuilder builder = new ProcessBuilder(args[0]);
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
