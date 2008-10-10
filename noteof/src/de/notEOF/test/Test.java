package de.notEOF.test;

import java.util.Random;

public class Test {

    public static void main(String[] args) {
        System.out.println("Hello World");

        for (int y = 0; y < 10; y++) {
            Random random = new Random();

            System.out.println("-----------------------------------");
            for (int i = 0; i < 10; i++) {
                System.out.println(Math.abs(random.nextInt()));
            }
        }
    }
}
