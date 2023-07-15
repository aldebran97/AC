package com.aldebran.text.similarity;

import java.io.Serializable;

/**
 * 文本对象
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class FullText implements Serializable {

    public String id;

    public BasicText titleText;

    public BasicText contentText;

    public double articleWeight = 0.5; // 文章附加权重
}
