package com.aldebran.text.similarity;

public class SimilaritySearchResult {

    public String id;

    public String title;

    public String text;

    public double score;

    @Override
    public String toString() {
        return "SimilaritySearchResult{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", score=" + score +
                '}';
    }
}
