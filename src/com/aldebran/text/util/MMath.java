package com.aldebran.text.util;

public class MMath {

    public static double log2(double a) {
        return log(2, a);
    }

    public static double log1_5(double a) {
        return log(1.5, a);
    }

    public static double log(double a, double b) {
        return Math.log10(b) / Math.log10(a);
    }
}
