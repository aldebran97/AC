package com.aldebran;

import java.util.*;
import java.util.function.Consumer;

/**
 * 词语查找树
 *
 * @author aldebran
 * @since 2023-01-30
 */
public class TrieTree {

    // 词语查找树结点定义
    public static class TrieTreeNode {
        public String charContent; // 字符内容，之所以不用char，因为考虑了特殊字符，两字节无法表示的情况
        public List<TrieTreeNode> children; // 子结点
        public TrieTreeNode mismatchPointer; // 失配结点
        public TrieTreeNode parent; // 父结点
        public boolean isWordEnd; // 是否可作为词尾，用于处理包含关系

        public TrieTreeNode(String charContent, List<TrieTreeNode> children, TrieTreeNode mismatchPointer, TrieTreeNode parent) {
            this.charContent = charContent;
            this.children = children;
            this.mismatchPointer = mismatchPointer;
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "TrieTreeNode{" +
                    "charContent='" + charContent + '\'' +
                    ", parent=" + parent +
                    ", isWordEnd=" + isWordEnd +
                    '}';
        }
    }

    // 用于输出词语
    public interface WordWriter {
        void write(String word);
    }

    public TrieTreeNode root = new TrieTreeNode("", new ArrayList<>(), null, null);

    // 添加词
    public void addWords(List<String> words) {
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            TrieTreeNode current = root;
            for (int j = 0; j < word.length(); j++) {
                String charContent = word.substring(j, j + 1);
//                System.out.println(charContent);
                TrieTreeNode find = null;
                // TODO 字查找可转换为二分插入、查找或者其他带有索引的方法
                for (TrieTreeNode child : current.children) {
                    if (charContent.equals(child.charContent)) {
                        find = child;
                        break;
                    }
                }
                if (find == null) {
                    find = new TrieTreeNode(charContent, new ArrayList<>(), null, current);
                    current.children.add(find);
                }
                current = find;
                if (j == word.length() - 1) current.isWordEnd = true;
            }

        }
    }


    // 遍历
    public void traverse_(Consumer<TrieTreeNode> pVisit) {
        Queue<TrieTreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TrieTreeNode c = queue.remove();
            pVisit.accept(c);
            for (TrieTreeNode child : c.children) {
                queue.add(child);
            }
        }
    }

    public void traverse(Consumer<String> pVisit) {
        traverse_(trieTreeNode -> pVisit.accept(trieTreeNode.charContent));
    }

    // 更新失配结点
    public void update() {
        traverse_(trieTreeNode -> {
            if (trieTreeNode != root && trieTreeNode.mismatchPointer == null) {
                String first = trieTreeNode.charContent;
                for (TrieTreeNode child : root.children) {
                    if (child.charContent.equals(first)) {
                        trieTreeNode.mismatchPointer = child;
                        break;
                    }
                }

            }
        });
    }

    // 特里树转词
    public void toWords(WordWriter wordWriter) {
        traverse_(node -> {
                    if (node.isWordEnd) {
                        StringBuilder sb = new StringBuilder();
                        while (node != root) {
                            sb.append(node.charContent);
                            node = node.parent;
                        }
                        sb = sb.reverse();
                        wordWriter.write(sb.toString());
                    }
                }
        );
    }

    public List<String> toWordsList() {
        List<String> result = new ArrayList<>();
        toWords(word -> {
            result.add(word);
        });
        return result;
    }

    // 匹配词
    public boolean queryWord(String word) {
        TrieTreeNode current = root;
        for (int i = 0; i < word.length(); i++) {
//            System.out.println(current.charContent);
            String charContent = word.substring(i, i + 1);
            TrieTreeNode find = null;
            // TODO 字查找可转换为二分插入、查找或者其他带有索引的方法
            for (TrieTreeNode child : current.children) {
                if (charContent.equals(child.charContent)) {
                    find = child;
                    break;
                }
            }
            if (find == null) {
                if (current.mismatchPointer == null) return false;
                current = current.mismatchPointer;
                i -= 1;
            } else {
                current = find;
            }

        }
        return current.isWordEnd;
    }
}
