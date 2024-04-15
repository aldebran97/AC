package com.aldebran.text.text;

import java.io.Serializable;

/**
 * 多字段文本
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class FullText implements Serializable {

    public String id;

    public BasicText titleText;

    public BasicText contentText;

    public double articleWeight = 1; // 文章附加权重

    public int totalWordsCountRepeat; // 标题和文本的词语次数之和


}
