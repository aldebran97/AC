package com.aldebran.text.similarity;

import java.io.Serializable;
import java.util.Objects;

public class Text implements Serializable {

    public String source;

    public String result;

    public String id;

    public String title;

    @Override
    public String toString() {
        return "Text{" +
                "source='" + source + '\'' +
                ", result='" + result + '\'' +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Text text = (Text) o;
        return Objects.equals(id, text.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
