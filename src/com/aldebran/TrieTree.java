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

        public Map<String, TrieTreeNode> childContentChildMap = new HashMap<>(); // value不用index

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
                    ", parent=" + (parent == null ? "null" : parent.charContent) +
                    ", mismatchPointer=" + (mismatchPointer == null ? "null" : mismatchPointer.charContent) +
                    ", isWordEnd=" + isWordEnd +
                    '}';
        }
    }

    // 用于输出词语
    public interface WordWriter {
        void write(String word);
    }

    public static class MatchResult {
        public String word;
        public int index;

        @Override
        public String toString() {
            return "MatchResult{" +
                    "word='" + word + '\'' +
                    ", index=" + index +
                    '}';
        }
    }

    public TrieTreeNode root = new TrieTreeNode("", new ArrayList<>(), null, null);

    // 添加词
    public void addWords(List<String> words) {
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            TrieTreeNode current = root;
            for (int j = 0; j < word.length(); j++) {
                String charContent = word.substring(j, j + 1);
                TrieTreeNode find = null;
                // 字查找可转换为二分插入、查找或者其他带有索引的方法
                find = current.childContentChildMap.get(charContent);

                if (find == null) {
                    find = new TrieTreeNode(charContent, new ArrayList<>(), null, current);
                    current.children.add(find);
                    current.childContentChildMap.put(charContent, find);
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
                    if (child.charContent.equals(first) && child != trieTreeNode) {
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

    // 是否包含词
    public boolean containsWord(String word) {
        TrieTreeNode current = root;
        for (int i = 0; i < word.length(); i++) {
            String charContent = word.substring(i, i + 1);
            TrieTreeNode find = null;
            // 字查找可转换为二分插入、查找或者其他带有索引的方法
            find = current.childContentChildMap.get(charContent);

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

    public List<MatchResult> indexOf(String text) {

        List<MatchResult> results = new ArrayList<>();

        int p = 0;
        TrieTreeNode current = root;
        while (p < text.length()) {
            String charContent = text.substring(p, p + 1);
//            System.out.printf("node: %s, p: %s, char: %s%n", current.charContent, p, charContent);
            TrieTreeNode find = current.childContentChildMap.get(charContent);
            if (find != null) {
                current = find;
                p++;
            } else {
                if (current == root) {
                    p++;
                } else {
                    int move = 0;
                    int max = -1;

                    TrieTreeNode c1 = current;
                    TrieTreeNode lastMismatchPointer = null;
                    while (c1 != root) {
                        if (c1.mismatchPointer != null) {
                            lastMismatchPointer = c1.mismatchPointer;
                            if (move > max) {
                                max = move;
                            }
                        }
                        c1 = c1.parent;
                        move++;
                    }
//                    System.out.println("lastMismatchPointer" + " " + (lastMismatchPointer != null ? lastMismatchPointer.charContent : null));
                    if (max == -1) {
                        current = root;
                    } else {
                        p -= max;
                        current = lastMismatchPointer;
                    }
                }
            }

            if (current.isWordEnd) {
                StringBuilder sb = new StringBuilder();
                TrieTreeNode node = current;
                while (node != root) {
                    sb.append(node.charContent);
                    node = node.parent;
                }
                sb = sb.reverse();
                MatchResult matchResult = new MatchResult();
                matchResult.word = sb.toString();
                matchResult.index = p - sb.length();
                results.add(matchResult);
            }
        }


        return results;
    }
}
