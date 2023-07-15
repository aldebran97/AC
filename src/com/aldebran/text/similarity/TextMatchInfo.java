package com.aldebran.text.similarity;

import java.util.HashMap;
import java.util.Map;

public class TextMatchInfo {

    public FullText text;

    public Map<String, Integer> hitContentGramCountMap = new HashMap<>();

    public Map<String, Integer> hitTitleGramCountMap = new HashMap<>();
}
