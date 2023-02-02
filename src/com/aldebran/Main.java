package com.aldebran;

import java.util.Arrays;

/**
 * 相关测试
 *
 * @author aldebran
 * @since 2023-01-30
 */
public class Main {

    public static void main(String[] args) throws Exception {
        TrieTree trieTree = new TrieTree();
        trieTree.addWords(Arrays.asList("12348", "2344", "38"));
        trieTree.update();

        trieTree.traverse_(str -> System.out.println(str));

        System.out.println(trieTree.containsWord("12348"));
        System.out.println(trieTree.containsWord("2344"));
        System.out.println(trieTree.containsWord("23"));
        System.out.println(trieTree.containsWord("38"));
        System.out.println(trieTree.toWordsList());


        System.out.println(trieTree.indexOf("12343382344038"));
    }
}
