package main.java.com.aldebran.text.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * 统计工具，针对数值类型
 *
 * @author aldebran
 */
public class StatisticsUtil {

    public static class BasicStatistics implements Serializable {

        public double arithmeticAverage;

        public double geometricAverage;

        public double maxValue;

        public double minValue;

    }

    public static boolean closeTo(Number n1, Number n2) {
        return Math.abs(n1.doubleValue() - n2.doubleValue()) <= 0.0001;
    }

    // 求平均统计情况
    public static BasicStatistics basicStatistics(double[] a) {

        BasicStatistics basicStatistics = new BasicStatistics();

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        double product = 0;

        for (int i = a.length - 1; i >= 0; i--) {
            double v = a[i];

            sum += v;

            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
            product += Math.log1p(v);
        }


        basicStatistics.minValue = min;
        basicStatistics.maxValue = max;
        basicStatistics.arithmeticAverage = sum / a.length;
        basicStatistics.geometricAverage = Math.pow(Math.E, product);
        return basicStatistics;
    }

    // 堆排序
    public static double[] sort(double[] a, boolean reverse) {

        int rate;

        if (reverse) {
            rate = -1;
        } else {
            rate = 1;
        }

        PriorityQueue<Double> queue = new PriorityQueue<>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return rate * o1.compareTo(o2);
            }
        });

        for (double v : a) {
            queue.add(v);
        }

        double[] result = new double[a.length];
        int i = 0;

        while (!queue.isEmpty()) {
            result[i++] = queue.poll();
        }

        return result;
    }


    public static double[] maxMinNormalization(double[] a) {
        double[] result = new double[a.length];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double v : a) {

            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
        }
        CheckUtil.notCloseTo(max, min);
        for (int i = 0; i < a.length; i++) {
            double v = a[i];
            result[i] = (v - min) / (max - min);
        }

        return result;
    }

}

