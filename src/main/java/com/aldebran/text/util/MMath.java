package com.aldebran.text.util;

public class MMath {

    public static double log2(double a) {
        return log(2, a);
    }

    public static double log(double a, double b) {
        return Math.log10(b) / Math.log10(a);
    }

    public static boolean legalDoubleValue(double d) {
        return !Double.isNaN(d) && Double.isFinite(d);
    }

    public static boolean legalPositiveDoubleValue(double d) {
        return d > 0 && legalDoubleValue(d);
    }
}
