package main.java.com.aldebran.text.similarity;

import com.aldebran.text.text.BasicText;

import java.util.HashMap;
import java.util.Map;

/**
 * 单字段匹配信息
 */
public class SingleFieldMatchInfo {

    public Map<String, Integer> hitWordCountMap = new HashMap<>();

    public BasicText basicText;

    public double[] idfs = new double[0];

    public double[] tfs = new double[0];

}
