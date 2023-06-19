package com.aldebran.text.replacePolicy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordReplaceInfo extends ReplaceInfo {
    public WordReplaceInfo() {
        super("", Pattern.compile("[a-z]+"), Pattern.compile("[a-z]+"));
    }

    public String replace(String text) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = target.matcher(text);
        int index = 0;
        while (matcher.find()) {
            int st = matcher.start();
            int ed = matcher.end();
            String ms = text.substring(st, ed);
            sb.append(text.substring(index, st));
            sb.append("B" + ms + "B");
            index = ed;
        }
        if (index < text.length()) {
            sb.append(text.substring(index));
        }

        return sb.toString();
    }
}
