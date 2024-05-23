package com.aldebran.text.util;

import com.aldebran.text.ac.AC;
import com.aldebran.text.similarity.TextSimilaritySearch;
import com.aldebran.text.ac.ACPlus;

import java.io.*;
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

    private static void mkdir(File folder) {
        if (!folder.exists()) {
            boolean mkResult = folder.mkdirs();
            assert mkResult;
        }
    }

    // 保存任何对象
    public static void save(ObjectOutputStream objectOutputStream, Object obj) throws IOException {
        objectOutputStream.writeObject(obj);
    }

    public static void saveHashMap(ObjectOutputStream objectOutputStream, HashMap map, long unitSize) throws IOException {
        objectOutputStream.writeObject(HASH_MAP_START);
        IterableInfo iterableInfo = hashMapToIterableInfo(map);
        saveIterable(objectOutputStream, iterableInfo, unitSize);
        objectOutputStream.writeObject(HASH_MAP_END);
    }

    public static IterableInfo hashMapToIterableInfo(HashMap hashMap) {

        IterableInfo result = new IterableInfo();
        result.size = hashMap.size();
        result.iterable = new Iterable() {

            Iterator<Map.Entry> entries = hashMap.entrySet().iterator();

            @Override
            public Iterator iterator() {
                return new Iterator() {
                    @Override
                    public boolean hasNext() {
                        return entries.hasNext();
                    }

                    @Override
                    public Object next() {
                        Map.Entry entry = entries.next();
                        ArrayList list = new ArrayList();
                        list.add(entry.getKey());
                        list.add(entry.getValue());
                        return list;
                    }
                };
            }
        };

        return result;
    }


    // 保存ArrayList
    public static void saveIterable(ObjectOutputStream objectOutputStream, IterableInfo iterableInfo, long unitSize) throws IOException {
        if (iterableInfo.size < unitSize) {
            objectOutputStream.writeObject(ARRAY_LIST_START);
            ArrayList thisList = new ArrayList();
            for (Object obj : iterableInfo.iterable) {
                thisList.add(obj);
            }
            save(objectOutputStream, thisList);
            objectOutputStream.writeObject(ARRAY_LIST_END);
        } else {
            objectOutputStream.writeObject(ARRAY_LIST_START);
            ArrayList thisList = new ArrayList();
            for (Object obj : iterableInfo.iterable) {
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

    public static HashMap readHashMap(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        CheckUtil.Assert(HASH_MAP_START.equals(objectInputStream.readObject()));
        HashMap resultMap = new HashMap();
        readArrayList_(objectInputStream, new Consumer<ArrayList>() {
            @Override
            public void accept(ArrayList arrayList) {
                resultMap.put(arrayList.get(0), arrayList.get(1));
            }
        });
        CheckUtil.Assert(HASH_MAP_END.equals(objectInputStream.readObject()));
        return resultMap;
    }

    public static HashMap readHashMap(ObjectInputStream objectInputStream, HashMap outMap) throws IOException, ClassNotFoundException {
        CheckUtil.Assert(HASH_MAP_START.equals(objectInputStream.readObject()));
        HashMap resultMap = outMap;
        readArrayList_(objectInputStream, new Consumer<ArrayList>() {
            @Override
            public void accept(ArrayList arrayList) {
                resultMap.put(arrayList.get(0), arrayList.get(1));
            }
        });
        CheckUtil.Assert(HASH_MAP_END.equals(objectInputStream.readObject()));
        return resultMap;
    }

    // 读ArrayList
    public static ArrayList readArrayList(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        ArrayList list = new ArrayList();
        readArrayList_(objectInputStream, object -> {
            System.out.println("read: " + object);
            list.add(object);
        });
        return list;
    }

    public static void readArrayList_(ObjectInputStream objectInputStream, Consumer consumer) throws IOException, ClassNotFoundException {
        CheckUtil.Assert(ARRAY_LIST_START.equals(objectInputStream.readObject()));
        Object readObject;
        while ((readObject = objectInputStream.readObject()) != null) {
            if (readObject instanceof String) {
                CheckUtil.Assert(ARRAY_LIST_END.equals(readObject));
                break;
            } else {
                assert readObject instanceof ArrayList;
                ArrayList thisList = ((ArrayList) readObject);
                for (Object o : thisList) {
                    consumer.accept(o);
                }

            }
        }
    }


    // 保存AC自动机-单线程
    public static void saveACSingleThread(File saveFolder, AC ac, long unitSize) throws IOException {
        if (saveFolder.isFile()) {
            throw new IOException("保存位置必须是目录，而不能是文件！");
        }
        mkdir(saveFolder);
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
        File idNodeMapFile = new File(saveFolder, "idNodeMap");
        File idParentIdMapFile = new File(saveFolder, "idParentIdMap");
        File idMissIdMapFile = new File(saveFolder, "idMissIdMap");
        File nextIdFile = new File(saveFolder, "nextId");
        // id-node-map
        try (FileOutputStream fileOutputStream = new FileOutputStream(idNodeMapFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            saveHashMap(objectOutputStream, idNodeMap, unitSize);
        }
        idNodeMap.clear();
        // id-parent-map
        try (FileOutputStream fileOutputStream = new FileOutputStream(idParentIdMapFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            saveHashMap(objectOutputStream, idParentIdMap, unitSize);
        }
        idParentIdMap.clear();
        // id-miss-map
        try (FileOutputStream fileOutputStream = new FileOutputStream(idMissIdMapFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            saveHashMap(objectOutputStream, idMissIdMap, unitSize);
        }
        idMissIdMap.clear();
        try (FileOutputStream fileOutputStream = new FileOutputStream(nextIdFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            objectOutputStream.writeLong(ac.nextId);
        }
    }

    // 保存AC自动机-单线程
    public static void saveACMultipleThreads(File saveFolder, AC ac, long unitSize) throws IOException, InterruptedException {
        if (saveFolder.isFile()) {
            throw new IOException("保存位置必须是目录，而不能是文件！");
        }
        mkdir(saveFolder);
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
        File idNodeMapFile = new File(saveFolder, "idNodeMap");
        File idParentIdMapFile = new File(saveFolder, "idParentIdMap");
        File idMissIdMapFile = new File(saveFolder, "idMissIdMap");
        File nextIdFile = new File(saveFolder, "nextId");

        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        Thread t1 = new Thread(() -> {
            // id-node-map
            try (FileOutputStream fileOutputStream = new FileOutputStream(idNodeMapFile);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
                saveHashMap(objectOutputStream, idNodeMap, unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
            idNodeMap.clear();
        });
        t1.start();
        threads.add(t1);

        Thread t2 = new Thread(() -> {
            // id-parent-map
            try (FileOutputStream fileOutputStream = new FileOutputStream(idParentIdMapFile);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
                saveHashMap(objectOutputStream, idParentIdMap, unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
            idParentIdMap.clear();
        });
        t2.start();
        threads.add(t2);

        Thread t3 = new Thread(() -> {
            // id-miss-map
            try (FileOutputStream fileOutputStream = new FileOutputStream(idMissIdMapFile);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
                saveHashMap(objectOutputStream, idMissIdMap, unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
            idMissIdMap.clear();
        });
        t3.start();
        threads.add(t3);

        try (FileOutputStream fileOutputStream = new FileOutputStream(nextIdFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            objectOutputStream.writeLong(ac.nextId);
        }

        for (Thread thread : threads) {
            thread.join();
        }

        if (!exceptions.isEmpty()) {
            throw new IOException("保存AC失败!", exceptions.get(0)); // 仅携带第一个子错误也能够说明问题！
        }

    }

    public static HashMap<Long, List<Long>> idParentIdMapToIdChildIdsMap(HashMap<Long, Long> idParentIdMap) {
        HashMap<Long, List<Long>> result = new HashMap<>();
        idParentIdMapToIdChildIdsMap(idParentIdMap, result);
        return result;
    }

    public static HashMap<Long, List<Long>> idParentIdMapToIdChildIdsMap(HashMap<Long, Long> idParentIdMap, HashMap<Long, List<Long>> outMap) {
        HashMap<Long, List<Long>> result = outMap;
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


    private static AC buildAC(long nextId,
                              HashMap<Long, AC.ACNode> idNodeMap,
                              HashMap<Long, Long> idParentIdMap,
                              HashMap<Long, List<Long>> idChildIdsMap,
                              HashMap<Long, Long> idMissIdMap,
                              boolean isACPlus
    ) {
        // 构建AC自动机
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
        ac.traverse_(acNode -> {
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
        });
        return ac;
    }

    public static AC loadACSingleThread(File saveFolder, boolean isACPlus) throws IOException, ClassNotFoundException {
        File idNodeMapFile = new File(saveFolder, "idNodeMap");
        File idParentIdMapFile = new File(saveFolder, "idParentIdMap");
        File idMissIdMapFile = new File(saveFolder, "idMissIdMap");
        File nextIdFile = new File(saveFolder, "nextId");
        assert idMissIdMapFile.isFile();
        assert idNodeMapFile.isFile();
        assert idMissIdMapFile.isFile();
        assert nextIdFile.isFile();
        HashMap<Long, Long> idParentIdMap = new HashMap<>();
        HashMap<Long, AC.ACNode> idNodeMap = new HashMap<>();
        HashMap<Long, Long> idMissIdMap = new HashMap<>();

        // id-node-map
        try (FileInputStream fileInputStream = new FileInputStream(idNodeMapFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            readHashMap(objectInputStream, idNodeMap);
        }

        // id-parent-map
        try (FileInputStream fileInputStream = new FileInputStream(idParentIdMapFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            readHashMap(objectInputStream, idParentIdMap);
        }

        // id-childs-map
        HashMap<Long, List<Long>> idChildIdsMap = idParentIdMapToIdChildIdsMap(idParentIdMap);

        // id-miss-map
        try (FileInputStream fileInputStream = new FileInputStream(idMissIdMapFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            readHashMap(objectInputStream, idMissIdMap);
        }

        Long nextId = null;
        try (FileInputStream fileInputStream = new FileInputStream(nextIdFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            nextId = objectInputStream.readLong();
        }

        return buildAC(nextId, idNodeMap, idParentIdMap, idChildIdsMap, idMissIdMap, isACPlus);
    }

    public static AC loadACMultipleThreads(File saveFolder, boolean isACPlus) throws IOException, InterruptedException {
        File idNodeMapFile = new File(saveFolder, "idNodeMap");
        File idParentIdMapFile = new File(saveFolder, "idParentIdMap");
        File idMissIdMapFile = new File(saveFolder, "idMissIdMap");
        File nextIdFile = new File(saveFolder, "nextId");
        assert idMissIdMapFile.isFile();
        assert idNodeMapFile.isFile();
        assert idMissIdMapFile.isFile();
        assert nextIdFile.isFile();
        HashMap<Long, Long> idParentIdMap = new HashMap<>();
        HashMap<Long, AC.ACNode> idNodeMap = new HashMap<>();
        HashMap<Long, Long> idMissIdMap = new HashMap<>();

        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        Thread t1 = new Thread(() -> {
            // id-node-map
            try (FileInputStream fileInputStream = new FileInputStream(idNodeMapFile);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
                readHashMap(objectInputStream, idNodeMap);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });

        t1.start();
        threads.add(t1);

        HashMap<Long, List<Long>> idChildIdsMap = new HashMap<>();

        Thread t2 = new Thread(() -> {
            // id-parent-map
            try (FileInputStream fileInputStream = new FileInputStream(idParentIdMapFile);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
                readHashMap(objectInputStream, idParentIdMap);

                // id-childs-map
                idParentIdMapToIdChildIdsMap(idParentIdMap, idChildIdsMap);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });

        t2.start();
        threads.add(t2);

        Thread t3 = new Thread(() -> {
            // id-miss-map
            try (FileInputStream fileInputStream = new FileInputStream(idMissIdMapFile);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
                readHashMap(objectInputStream, idMissIdMap);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });

        t3.start();
        threads.add(t3);


        Long nextId = null;
        try (FileInputStream fileInputStream = new FileInputStream(nextIdFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            nextId = objectInputStream.readLong();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        return buildAC(nextId, idNodeMap, idParentIdMap, idChildIdsMap, idMissIdMap, isACPlus);
    }

    public static void saveTextSimilaritySearchSingleThread(File saveFolder, TextSimilaritySearch lib, long unitSize) throws IOException {
        if (saveFolder.isFile()) {
            throw new IOException("保存位置必须是目录，而不能是文件！");
        }
        mkdir(saveFolder);

        File gramIdfMapFile = new File(saveFolder, "gramIdfMap");
        gramIdfMapFile.createNewFile();
        File idTextMapFile = new File(saveFolder, "idTextMap");
        File contentGramTextIdsMapFile = new File(saveFolder, "contentGramTextIdsMap");
        File titleGramTextIdsMapFile = new File(saveFolder, "titleGramTextIdsMap");
        File contentACFolder = new File(saveFolder, "contentAC");
        File titleACFolder = new File(saveFolder, "titleAC");
        File simpleLibFile = new File(saveFolder, "simpleLib");

        // gram->idf
        try (FileOutputStream fileOutputStream = new FileOutputStream(gramIdfMapFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            saveHashMap(objectOutputStream, lib.wordIdfMap, unitSize);
        }

        // id->text
        try (FileOutputStream fileOutputStream = new FileOutputStream(idTextMapFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            saveHashMap(objectOutputStream, lib.idTextMap, unitSize);
        }

        // contentGramTextIdsMap
        try (FileOutputStream fileOutputStream = new FileOutputStream(contentGramTextIdsMapFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            saveIterable(objectOutputStream, gramTextIdsFlatten(lib.contentWordTextIdsMap), unitSize);
        }

        // titleGramTextIdsMap
        try (FileOutputStream fileOutputStream = new FileOutputStream(titleGramTextIdsMapFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            saveIterable(objectOutputStream, gramTextIdsFlatten(lib.titleWordTextIdsMap), unitSize);
        }

        // title ac
        saveACSingleThread(titleACFolder, lib.titleAC, unitSize);

        // content ac
        saveACSingleThread(contentACFolder, lib.contentAC, unitSize);


        try (FileOutputStream fileOutputStream = new FileOutputStream(simpleLibFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            objectOutputStream.writeObject(lib);
        }

    }


    public static void saveTextSimilaritySearchMultipleThreads(File saveFolder, TextSimilaritySearch lib, long unitSize) throws IOException, InterruptedException {
        if (saveFolder.isFile()) {
            throw new IOException("保存位置必须是目录，而不能是文件！");
        }
        mkdir(saveFolder);

        File gramIdfMapFile = new File(saveFolder, "gramIdfMap");
        File idTextMapFile = new File(saveFolder, "idTextMap");
        File contentGramTextIdsMapFile = new File(saveFolder, "contentGramTextIdsMap");
        File titleGramTextIdsMapFile = new File(saveFolder, "titleGramTextIdsMap");
        File contentACFolder = new File(saveFolder, "contentAC");
        File titleACFolder = new File(saveFolder, "titleAC");
        File simpleLibFile = new File(saveFolder, "simpleLib");

        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        Thread t1 = new Thread(() -> {
            // gram->idf
            try (FileOutputStream fileOutputStream = new FileOutputStream(gramIdfMapFile);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
                saveHashMap(objectOutputStream, lib.wordIdfMap, unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });
        t1.start();
        threads.add(t1);

        Thread t2 = new Thread(() -> {
            // id->text
            try (FileOutputStream fileOutputStream = new FileOutputStream(idTextMapFile);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
                saveHashMap(objectOutputStream, lib.idTextMap, unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });
        t2.start();
        threads.add(t2);

        Thread t3 = new Thread(() -> {
            // contentGramTextIdsMap
            try (FileOutputStream fileOutputStream = new FileOutputStream(contentGramTextIdsMapFile);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
                saveIterable(objectOutputStream, gramTextIdsFlatten(lib.contentWordTextIdsMap), unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });
        t3.start();
        threads.add(t3);


        Thread t4 = new Thread(() -> {
            // titleGramTextIdsMap
            try (FileOutputStream fileOutputStream = new FileOutputStream(titleGramTextIdsMapFile);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
                saveIterable(objectOutputStream, gramTextIdsFlatten(lib.titleWordTextIdsMap), unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });
        t4.start();
        threads.add(t4);

        Thread t5 = new Thread(() -> {
            // title ac
            try {
                saveACMultipleThreads(titleACFolder, lib.titleAC, unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });
        t5.start();
        threads.add(t5);

        Thread t6 = new Thread(() -> {
            // content ac
            try {
                saveACMultipleThreads(contentACFolder, lib.contentAC, unitSize);
            } catch (Exception e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        });
        t6.start();
        threads.add(t6);

        // 简单的部分由主线程完成了
        try (FileOutputStream fileOutputStream = new FileOutputStream(simpleLibFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
            objectOutputStream.writeObject(lib);
        }

        for (Thread thread : threads) {
            thread.join();
        }

        if (!exceptions.isEmpty()) {
            throw new IOException("保存TextSimilaritySearch失败!", exceptions.get(0)); // 仅携带第一个子错误也能够说明问题！
        }
    }


    public static IterableInfo gramTextIdsFlatten(HashMap<String, Set<String>> gramTextIdsMap) {
        IterableInfo iterableInfo = new IterableInfo();
        iterableInfo.size = 0;
        for (Set<String> value : gramTextIdsMap.values()) {
            iterableInfo.size += value.size();
        }
//        System.out.println("gramTextIdsFlatten iterableInfo size: " + iterableInfo.size);

        Iterator<Map.Entry<String, Set<String>>> entryIterator = gramTextIdsMap.entrySet().iterator();
        iterableInfo.iterable = new Iterable() {

            int c = 0;

            List<String> vs = null;

            int vs_c = 0;

            String key = null;

            @Override
            public Iterator iterator() {
                return new Iterator() {
                    @Override
                    public boolean hasNext() {
                        return c < iterableInfo.size;
                    }

                    @Override
                    public Object next() {
                        if (key == null || vs == null || vs_c >= vs.size()) {
                            Map.Entry<String, Set<String>> entry = entryIterator.next();
                            key = entry.getKey();
                            vs = new ArrayList<>();
                            vs.addAll(entry.getValue());
                            vs_c = 0;
                        }
                        ArrayList thisResult = new ArrayList();
                        thisResult.add(key);
                        thisResult.add(vs.get(vs_c++));
                        c++;
//                        System.out.printf("gramTextIdsFlatten yield, kv: %s%n", thisResult);
                        return thisResult;
                    }
                };
            }
        };

        return iterableInfo;
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

    public static HashMap<String, Set<String>> flattenToGramTextIds(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        HashMap<String, Set<String>> result = new HashMap<>();
        readArrayList_(objectInputStream, new Consumer() {
            @Override
            public void accept(Object o) {
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
        });

        return result;
    }

    public static TextSimilaritySearch loadTextSimilaritySearchSingleThread(File saveFolder) throws IOException, ClassNotFoundException {

        if (!saveFolder.isDirectory()) {
            throw new IOException("保存目录不存在！");
        }

        File gramIdfMapFile = new File(saveFolder, "gramIdfMap");
        assert gramIdfMapFile.isFile();
        File idTextMapFile = new File(saveFolder, "idTextMap");
        assert idTextMapFile.isFile();
        File contentGramTextIdsMapFile = new File(saveFolder, "contentGramTextIdsMap");
        assert contentGramTextIdsMapFile.isFile();
        File titleGramTextIdsMapFile = new File(saveFolder, "titleGramTextIdsMap");
        assert titleGramTextIdsMapFile.isFile();
        File contentACFolder = new File(saveFolder, "contentAC");
        assert contentACFolder.isDirectory();
        File titleACFolder = new File(saveFolder, "titleAC");
        assert titleACFolder.isDirectory();
        File simpleLibFile = new File(saveFolder, "simpleLib");
        assert simpleLibFile.isFile();

        TextSimilaritySearch lib = null;

        // lib
        try (FileInputStream fileInputStream = new FileInputStream(simpleLibFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            lib = (TextSimilaritySearch) objectInputStream.readObject();
        }


        // gramIdfMap
        try (FileInputStream fileInputStream = new FileInputStream(gramIdfMapFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            lib.wordIdfMap = readHashMap(objectInputStream);
        }

        // idTextMap
        try (FileInputStream fileInputStream = new FileInputStream(idTextMapFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            lib.idTextMap = readHashMap(objectInputStream);
        }

        // contentGramTextIdsMap
        try (FileInputStream fileInputStream = new FileInputStream(contentGramTextIdsMapFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            lib.contentWordTextIdsMap = flattenToGramTextIds(objectInputStream);
        }

        // titleGramTextIdsMap
        try (FileInputStream fileInputStream = new FileInputStream(titleGramTextIdsMapFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            lib.titleWordTextIdsMap = flattenToGramTextIds(objectInputStream);
        }

        lib.titleAC = (ACPlus) loadACSingleThread(titleACFolder, true);
        lib.contentAC = (ACPlus) loadACSingleThread(contentACFolder, true);

        lib.setSaveFolder(saveFolder);

        return lib;
    }

    public static TextSimilaritySearch loadTextSimilaritySearchMultipleThreads(File saveFolder) throws IOException, ClassNotFoundException, InterruptedException {

        if (!saveFolder.isDirectory()) {
            throw new IOException("保存目录不存在！");
        }

        File gramIdfMapFile = new File(saveFolder, "gramIdfMap");
        assert gramIdfMapFile.isFile();
        File idTextMapFile = new File(saveFolder, "idTextMap");
        assert idTextMapFile.isFile();
        File contentGramTextIdsMapFile = new File(saveFolder, "contentGramTextIdsMap");
        assert contentGramTextIdsMapFile.isFile();
        File titleGramTextIdsMapFile = new File(saveFolder, "titleGramTextIdsMap");
        assert titleGramTextIdsMapFile.isFile();
        File contentACFolder = new File(saveFolder, "contentAC");
        assert contentACFolder.isDirectory();
        File titleACFolder = new File(saveFolder, "titleAC");
        assert titleACFolder.isDirectory();
        File simpleLibFile = new File(saveFolder, "simpleLib");
        assert simpleLibFile.isFile();

        TextSimilaritySearch lib = null;

        // lib
        try (FileInputStream fileInputStream = new FileInputStream(simpleLibFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
            lib = (TextSimilaritySearch) objectInputStream.readObject();
        }

        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        TextSimilaritySearch finalLib = lib;

        Thread t1 = new Thread(() -> {
            // gramIdfMap
            try (FileInputStream fileInputStream = new FileInputStream(gramIdfMapFile);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
                finalLib.wordIdfMap = readHashMap(objectInputStream);
            } catch (Exception e) {
                synchronized (e) {
                    exceptions.add(e);
                }
            }
        });

        t1.start();
        threads.add(t1);

        Thread t2 = new Thread(() -> {
            // idTextMap
            try (FileInputStream fileInputStream = new FileInputStream(idTextMapFile);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
                finalLib.idTextMap = readHashMap(objectInputStream);
            } catch (Exception e) {
                synchronized (e) {
                    exceptions.add(e);
                }
            }
        });

        t2.start();
        threads.add(t2);

        Thread t3 = new Thread(() -> {
            // contentGramTextIdsMap
            try (FileInputStream fileInputStream = new FileInputStream(contentGramTextIdsMapFile);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
                finalLib.contentWordTextIdsMap = flattenToGramTextIds(objectInputStream);
            } catch (Exception e) {
                synchronized (e) {
                    exceptions.add(e);
                }
            }
        });

        t3.start();
        threads.add(t3);

        Thread t4 = new Thread(() -> {
            // titleGramTextIdsMap
            try (FileInputStream fileInputStream = new FileInputStream(titleGramTextIdsMapFile);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);) {
                finalLib.titleWordTextIdsMap = flattenToGramTextIds(objectInputStream);
            } catch (Exception e) {
                synchronized (e) {
                    exceptions.add(e);
                }
            }
        });

        t4.start();
        threads.add(t4);

        Thread t5 = new Thread(() -> {
            try {
                finalLib.titleAC = (ACPlus) loadACMultipleThreads(titleACFolder, true);
            } catch (Exception e) {
                synchronized (e) {
                    exceptions.add(e);
                }
            }
        });

        t5.start();
        threads.add(t5);

        Thread t6 = new Thread(() -> {
            try {
                finalLib.contentAC = (ACPlus) loadACMultipleThreads(contentACFolder, true);
            } catch (Exception e) {
                synchronized (e) {
                    exceptions.add(e);
                }
            }
        });

        t6.start();
        threads.add(t6);

        for (Thread thread : threads) {
            thread.join();
        }

        if (!exceptions.isEmpty()) {
            throw new IOException("加载TextSimilaritySearch失败!", exceptions.get(0)); // 仅携带第一个子错误也能够说明问题！
        }

        lib.setSaveFolder(saveFolder);

        return lib;
    }

}