package main.java.com.aldebran.text;

import java.io.Serializable;

/**
 * 常量定义类
 *
 * @author aldebran
 */
public class Constants implements Serializable {

    public static final String WORD_PREFIX_POSTFIX = "B"; // 替换词语时候的前后字符

    public static final String NUMBER_REPLACEMENT = "0"; // 替换数字的字符


    public static final double CRITICAL_CONTENT_HIT_COUNT = 2;

    public static final double CRITICAL_TITLE_HIT_COUNT = 2;

    public static final double CRITICAL_SCORE = 0.5;

    public static final double CONTENT_K = 1;

    public static final double TITLE_K = 3;

    public static final double BM25_K = 1.2;

    public static final double BM25_B = 0.1;

    public static final double ITEM_SCORE_DIFF = 10;

    public static final int MULTIPLE_THREAD_SEARCH_MIN_TEXTS_COUNT = 300000; // 启用多线程搜索的最小文本数量

}
