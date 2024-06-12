package com.aldebran.text.text;

import java.util.Map;

public class ShowText {

    public String id;

    public String title;

    public String content;

    public Map<String, Object> metaData;

    @Override
    public String toString() {
        return "ShowText{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", metaData=" + metaData +
                '}';
    }
}
