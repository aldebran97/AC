package com.aldebran.text.replacePolicy;

import java.util.regex.Pattern;

/**
 * 简单替换策略
 *
 * @author aldebran
 * @since 2023-06-18
 */
public class SimpleReplacePolicy extends ReplacePolicy {

    public Pattern pattern;

    public String replacement;

    @Override
    public String replace(String text) {
        return pattern.matcher(text).replaceAll(replacement);
    }
}
