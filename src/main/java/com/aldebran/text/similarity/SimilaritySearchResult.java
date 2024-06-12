package com.aldebran.text.similarity;

import java.util.HashMap;
import java.util.Map;

public class SimilaritySearchResult {

    public String id;

    public String title;

    public String content;

    public double score;

    public Map<String, Object> metaData; // 元数据

    @Override
    public String toString() {
        return "SimilaritySearchResult{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", score=" + score +
                '}';
    }
}
