package com.aldebran;

import java.util.regex.Pattern;

/**
 * 替换策略
 *
 * @author aldebran
 * @since 2023-06-18
 */
public abstract class ReplacePolicy {

    public abstract String replace(String text);

}
