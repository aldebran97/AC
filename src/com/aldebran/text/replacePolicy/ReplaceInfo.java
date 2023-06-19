package com.aldebran.text.replacePolicy;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用替换信息
 *
 * @author aldebran
 * @since 2023-06-18
 */
public class ReplaceInfo implements Serializable {

    public String replacement; // 替代字符串

    public Pattern target; // 要替换的模式

    public Pattern pattern; // 替换时的模式，可能是一部分

    public ReplaceInfo(String replacement, Pattern target, Pattern pattern) {
        this.replacement = replacement;
        this.target = target;
        this.pattern = pattern;
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
            sb.append(pattern.matcher(ms).replaceAll(replacement));
            index = ed;
        }
        if (index < text.length()) {
            sb.append(text.substring(index));
        }

        return sb.toString();
    }
}
