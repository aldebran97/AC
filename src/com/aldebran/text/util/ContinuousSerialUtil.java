package com.aldebran.text.util;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.similarity.FullText;
import com.aldebran.text.similarity.TextSimilaritySearch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * 持续的序列化工具
 *
 * @author aldebran
 * @since 2023-09-26
 */
public class ContinuousSerialUtil {

    private static final String HASH_MAP_START = "HASH_MAP_START";

    private static final String HASH_MAP_END = "HASH_MAP_END";

    private static final String AC_START = "AC_START";

    private static final String AC_END = "AC_END";


    private static final String ARRAY_LIST_START = "ARRAY_LIST_START";

    private static final String ARRAY_LIST_END = "ARRAY_LIST_END";

    private static final String TextSimilaritySearch_START = "TextSimilaritySearch_START";
    private static final String TextSimilaritySearch_END = "TextSimilaritySearch_END";

    // 保存任何对象
    public static void save(ObjectOutputStream objectOutputStream, Object obj) throws IOException {
        objectOutputStream.writeObject(obj);
    }

    // 保存HashMap
//    public static void saveHashMap(ObjectOutputStream objectOutputStream, HashMap map, long unitSize) throws IOException {
//        if (map.size() < unitSize) {
//            objectOutputStream.writeObject(HASH_MAP_START);
//            save(objectOutputStream, map);
//            objectOutputStream.writeObject(HASH_MAP_END);
//        } else {
//            objectOutputStream.writeObject(HASH_MAP_START);
//            HashMap thisMap = new HashMap();
//            for (Object k : map.entrySet()) {
//                if (thisMap.size() >= unitSize) {
//                    save(objectOutputStream, thisMap);
//                    thisMap.clear();
//                }
//                thisMap.put(k, map.get(k));
//            }
//            if (!thisMap.isEmpty()) {
//                save(objectOutputStream, thisMap);
//            }
//            objectOutputStream.writeObject(HASH_MAP_END);
//
//        }
//    }

    public static void saveHashMap(ObjectOutputStream objectOutputStream, HashMap map, long unitSize) throws IOException {
        objectOutputStream.writeObject(HASH_MAP_START);
        List result = hashMapToList(map);
        ArrayList keys = (ArrayList) result.get(0);
        ArrayList values = (ArrayList) result.get(1);
        saveArrayList(objectOutputStream, keys, unitSize);
        saveArrayList(objectOutputStream, values, unitSize);
        objectOutputStream.writeObject(HASH_MAP_END);
    }

    public static List hashMapToList(HashMap hashMap) {
        ArrayList keys = new ArrayList();
        ArrayList values = new ArrayList();
        for (Object e : hashMap.entrySet()) {
            Map.Entry eO = (Map.Entry) e;
            keys.add(eO.getKey());
            values.add(eO.getValue());
        }
        return Arrays.asList(keys, values);
    }


    public static HashMap kvListToMap(ArrayList keys, ArrayList values) {
        HashMap hashMap = new HashMap(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            hashMap.put(keys.get(i), values.get(i));
        }
        return hashMap;
    }

    // 保存ArrayList
    public static void saveArrayList(ObjectOutputStream objectOutputStream, ArrayList list, long unitSize) throws IOException {
        if (list.size() < unitSize) {
            objectOutputStream.writeObject(ARRAY_LIST_START);
            save(objectOutputStream, list);
            objectOutputStream.writeObject(ARRAY_LIST_END);
        } else {
            objectOutputStream.writeObject(ARRAY_LIST_START);
            ArrayList thisList = new ArrayList();
            for (Object obj : list) {
                if (thisList.size() >= unitSize) {
                    save(objectOutputStream, thisList);
                    thisList = new ArrayList();
                }
                thisList.add(obj);
            }
            if (!thisList.isEmpty()) {
                save(objectOutputStream, thisList);
            }
            objectOutputStream.writeObject(ARRAY_LIST_END);
        }
    }

    // 读HashMap
//    public static HashMap readHashMap(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
//        CheckUtil.Assert(HASH_MAP_START.equals(objectInputStream.readObject()));
//
//        HashMap resultMap = new HashMap();
//        Object readObject;
//        while ((readObject = objectInputStream.readObject()) != null) {
//            if (readObject instanceof String) {
//                CheckUtil.Assert(HASH_MAP_END.equals(readObject));
//                break;
//            } else {
//                assert readObject instanceof HashMap;
//                HashMap thisMap = ((HashMap) readObject);
//                resultMap.putAll(thisMap);
//            }
//        }
//        return resultMap;
//    }

    public static HashMap readHashMap(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        CheckUtil.Assert(HASH_MAP_START.equals(objectInputStream.readObject()));
        ArrayList keys = readArrayList(objectInputStream);
        ArrayList values = readArrayList(objectInputStream);
        HashMap resultMap = kvListToMap(keys, values);
        CheckUtil.Assert(HASH_MAP_END.equals(objectInputStream.readObject()));
        return resultMap;
    }

    // 读ArrayList
    public static ArrayList readArrayList(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        CheckUtil.Assert(ARRAY_LIST_START.equals(objectInputStream.readObject()));
        ArrayList list = new ArrayList();
        Object readObject;
        while ((readObject = objectInputStream.readObject()) != null) {
            if (readObject instanceof String) {
                CheckUtil.Assert(ARRAY_LIST_END.equals(readObject));
                break;
            } else {
                assert readObject instanceof ArrayList;
                ArrayList thisList = ((ArrayList) readObject);
                list.addAll(thisList);
            }
        }
        return list;
    }


    // 保存AC，平坦化
    public static void saveAC(ObjectOutputStream objectOutputStream, AC ac, long unitSize) throws IOException {
        objectOutputStream.writeObject(AC_START);
        HashMap<Long, AC.ACNode> idNodeMap = new HashMap<>();
//        HashMap<Long, List<Long>> idChildIdsMap = new HashMap<>();
        HashMap<Long, Long> idParentIdMap = new HashMap<>();
        HashMap<Long, Long> idMissIdMap = new HashMap<>();

        ac.traverse_(acNode -> {
            AC.ACNode newNode = new AC.ACNode(acNode.charContent, null, null, acNode.id);
            newNode.word = acNode.word;
            idNodeMap.put(acNode.id, newNode);
//            ArrayList<Long> childIds = new ArrayList<>();
//            for (AC.ACNode child : acNode.childContentChildMap.values()) {
//                childIds.add(child.id);
//            }
//            idChildIdsMap.put(acNode.id, childIds);
            if (acNode.parent != null) {
                idParentIdMap.put(acNode.id, acNode.parent.id);
            }
            if (acNode.mismatchPointer != null) {
                idMissIdMap.put(acNode.id, acNode.mismatchPointer.id);
            }
        });
//        System.out.println("write idNodeMap: " + idNodeMap);
//        System.out.println("write idParentIdMap: " + idParentIdMap);
//        System.out.println("write idMissIdMap: " + idMissIdMap);
        saveHashMap(objectOutputStream, idNodeMap, unitSize);
//        saveHashMap(objectOutputStream, idChildIdsMap, unitSize);
        saveHashMap(objectOutputStream, idParentIdMap, unitSize);
        saveHashMap(objectOutputStream, idMissIdMap, unitSize);
        objectOutputStream.writeLong(ac.nextId);

        objectOutputStream.writeObject(AC_END);

    }

    public static HashMap<Long, List<Long>> idParentIdMapToIdChildIdsMap(HashMap<Long, Long> idParentIdMap) {
        HashMap<Long, List<Long>> result = new HashMap<>();
        for (Map.Entry<Long, Long> entry : idParentIdMap.entrySet()) {
            long id = entry.getKey();
            long parentId = entry.getValue();
            List<Long> childIds = result.get(parentId);
            if (childIds == null) {
                childIds = new ArrayList<>();
                result.put(parentId, childIds);
            }
            childIds.add(id);
        }
        return result;
    }


    // 读取AC or ACPlus
    private static AC loadAC_(ObjectInputStream objectInputStream, boolean isACPlus) throws IOException, ClassNotFoundException {
        CheckUtil.Assert(AC_START.equals(objectInputStream.readObject()));
        HashMap<Long, AC.ACNode> idNodeMap = readHashMap(objectInputStream);

//        HashMap<Long, List<Long>> idChildIdsMap = readHashMap(objectInputStream);
        HashMap<Long, Long> idParentIdMap = readHashMap(objectInputStream);
        HashMap<Long, List<Long>> idChildIdsMap = idParentIdMapToIdChildIdsMap(idParentIdMap);
        HashMap<Long, Long> idMissIdMap = readHashMap(objectInputStream);

//        System.out.println("read idNodeMap: " + idNodeMap);
//        System.out.println("read idChildIdsMap: " + idChildIdsMap);
//        System.out.println("read idParentIdMap: " + idParentIdMap);
//        System.out.println("read idMissIdMap: " + idMissIdMap);
        long nextId = objectInputStream.readLong();

        AC ac = null;
        if (isACPlus) {
            ac = new ACPlus();
        } else {
            ac = new AC();
        }

        AC.ACNode root = idNodeMap.get(0L);
        assert root != null;
        ac.root = root;
        ac.nextId = nextId;
        ac.traverse_(new Consumer<AC.ACNode>() {
            @Override
            public void accept(AC.ACNode acNode) {
//                System.out.println(acNode.id);
                List<Long> childIds = idChildIdsMap.get(acNode.id);
                if (childIds != null) {
                    for (long childId : childIds) {
                        AC.ACNode childNode = idNodeMap.get(childId);
                        acNode.childContentChildMap.put(childNode.charContent, childNode);
                    }
                }

                Long parentId = idParentIdMap.get(acNode.id);
                if (parentId != null) {
                    acNode.parent = idNodeMap.get(parentId);
                }
                Long missId = idMissIdMap.get(acNode.id);
                if (missId != null) {
                    acNode.mismatchPointer = idNodeMap.get(missId);
                }
            }
        });
        CheckUtil.Assert(AC_END.equals(objectInputStream.readObject()));
        return ac;
    }

    // 读取AC
    public static AC loadAC(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        return loadAC_(objectInputStream, false);
    }

    // 读取AC Plus
    public static ACPlus loadACPlus(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        return (ACPlus) loadAC_(objectInputStream, true);
    }

    // 保存相似库
    public static void saveTextSimilaritySearch(ObjectOutputStream objectOutputStream, TextSimilaritySearch lib, long unitSize) throws IOException {
        objectOutputStream.writeObject(TextSimilaritySearch_START);
        TextSimilaritySearch newLib = new TextSimilaritySearch(lib.criticalContentHitCount,
                lib.criticalTitleHitCount, lib.criticalScore, lib.contentK, lib.titleK, lib.hitGramsCountLogA,
                lib.gramsCountLogA, lib.idfGrowthK, lib.n, lib.libName);
        newLib.gramAvgIdf = lib.gramAvgIdf;
        newLib.gramMinIdf = lib.gramMinIdf;
        newLib.gramMaxIdf = lib.gramMaxIdf;
        newLib.maxTitleAvgIdf = lib.maxTitleAvgIdf;
        newLib.minTitleAvgIdf = lib.minTitleAvgIdf;
        newLib.avgTitleAvgIdf = lib.avgTitleAvgIdf;

        newLib.maxContentAvgIdf = lib.maxContentAvgIdf;
        newLib.minContentAvgIdf = lib.minContentAvgIdf;
        newLib.avgContentAvgIdf = lib.avgContentAvgIdf;
        newLib.titleIdfRate = lib.titleIdfRate;
        newLib.basicGrowthValue = lib.basicGrowthValue;
        newLib.titleGramsCountSum = lib.titleGramsCountSum;
        newLib.contentGramsCountSum = lib.contentGramsCountSum;

        newLib.scoreCalculator = lib.scoreCalculator;
        newLib.avgIdfGrowthCalculator = lib.avgIdfGrowthCalculator;
        newLib.textPreprocess = lib.textPreprocess;

        save(objectOutputStream, newLib);
        saveHashMap(objectOutputStream, lib.gramIdfMap, unitSize);
        saveHashMap(objectOutputStream, lib.idTextMap, unitSize);

        saveArrayList(objectOutputStream, gramTextIdsFlatten(lib.contentGramTextIdsMap), unitSize);
//        saveHashMap(objectOutputStream, lib.contentGramTextIdsMap, unitSize);

        saveArrayList(objectOutputStream, gramTextIdsFlatten(lib.titleGramTextIdsMap), unitSize);
//        saveHashMap(objectOutputStream, lib.titleGramTextIdsMap, unitSize);

        saveAC(objectOutputStream, lib.titleAC, unitSize);
        saveAC(objectOutputStream, lib.contentAC, unitSize);

        objectOutputStream.writeObject(TextSimilaritySearch_END);
    }

    public static ArrayList gramTextIdsFlatten(HashMap<String, Set<String>> gramTextIdsMap) {
        ArrayList list = new ArrayList();
        for (Map.Entry<String, Set<String>> entry : gramTextIdsMap.entrySet()) {
            String gram = entry.getKey();
            for (String textId : entry.getValue()) {
                ArrayList pair = new ArrayList();
                pair.add(gram);
                pair.add(textId);
                list.add(pair);
            }
        }
        return list;
    }

    public static HashMap<String, Set<String>> flattenToGramTextIds(ArrayList list) {
        HashMap<String, Set<String>> result = new HashMap<>();
        for (Object o : list) {
            ArrayList pair = (ArrayList) o;
            String gram = (String) pair.get(0);
            String textId = (String) pair.get(1);
            Set<String> textIds = result.get(gram);
            if (textIds == null) {
                textIds = new HashSet<>();
                result.put(gram, textIds);
            }
            textIds.add(textId);
        }
        return result;
    }

    // 加载相似库
    public static TextSimilaritySearch loadTextSimilaritySearch(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        CheckUtil.Assert(TextSimilaritySearch_START.equals(objectInputStream.readObject()));
        Object obj = objectInputStream.readObject();
        CheckUtil.Assert(obj instanceof TextSimilaritySearch);
        TextSimilaritySearch lib = (TextSimilaritySearch) obj;
        lib.gramIdfMap = readHashMap(objectInputStream);
        lib.idTextMap = readHashMap(objectInputStream);
        lib.contentGramTextIdsMap = flattenToGramTextIds(readArrayList(objectInputStream));
//        lib.contentGramTextIdsMap = readHashMap(objectInputStream);
        lib.titleGramTextIdsMap = flattenToGramTextIds(readArrayList(objectInputStream));
//        lib.titleGramTextIdsMap = readHashMap(objectInputStream);
        lib.titleAC = loadAC(objectInputStream);
        lib.contentAC = loadAC(objectInputStream);

        CheckUtil.Assert(TextSimilaritySearch_END.equals(objectInputStream.readObject()));
        return lib;
    }
}