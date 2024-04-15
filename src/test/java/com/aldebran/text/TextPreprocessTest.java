package com.aldebran.text;

import com.aldebran.text.preprocess.TextPreprocess;

/**
 * 文本预处理测试
 *
 * @author aldebran
 */
public class TextPreprocessTest {

    public static void main(String[] args) throws Exception {

        TextPreprocess textPreprocess = new TextPreprocess();

        System.out.println(textPreprocess.preprocess("其中古籍文献近200万册，数字资源总量超过1000TB，是亚洲规模最大的图书馆，居世界国家图书馆第三位；"));
    }
}
