package main.java.com.aldebran.text.similarity;

import com.aldebran.text.text.FullText;

import java.util.HashMap;
import java.util.Map;

/**
 * 多字段匹配信息
 */
public class MultipleFieldMatchInfo {

    public FullText text;

    SingleFieldMatchInfo titleMatchInfo = new SingleFieldMatchInfo();

    SingleFieldMatchInfo contentMatchInfo = new SingleFieldMatchInfo();



}
