package com.github.flowersbloom.util;

import java.util.Random;

public class RandomUtil {
    private static final Random random = new Random();

    public static String randomNumber(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            String randomNumber = randomNumber(10);
            System.out.println(randomNumber);
        }
        System.out.println(String.valueOf(System.currentTimeMillis()).substring(5));
    }
}
