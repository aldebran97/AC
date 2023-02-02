package com.aldebran;

import java.util.Arrays;

/**
 * 相关测试
 *
 * @author aldebran
 * @since 2023-01-30
 */
public class Main {

    public static void main(String[] args) {
        TrieTree trieTree = new TrieTree();
        trieTree.addWords(Arrays.asList("12348", "233", "23", "38"));
//        trieTree.traverse(str -> System.out.println(str));
        trieTree.update();

        System.out.println(trieTree.queryWord("23"));
        System.out.println(trieTree.queryWord("233"));
        System.out.println(trieTree.queryWord("38"));
        System.out.println(trieTree.toWordsList());
    }
}
