package com.learn.im.common.route.algorithm.consistenthash;


import com.learn.im.common.enums.UserErrorCode;
import com.learn.im.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author lee
 * @description
 */
public class TreeMapConsistentHash extends AbstractConsistentHash {

    private TreeMap<Long, String> treeMap = new TreeMap<>();

    private static final int NODE_SIZE = 2;

    @Override
    protected void add(long key, String value) {
        // 防止key覆盖, 增加额外的拼接 “node+key+i”
        for (int i = 0; i < NODE_SIZE; i++) {
            treeMap.put(super.hash("node" + key + i), value);
        }
        treeMap.put(key, value);
    }


    @Override
    protected String getFirstNodeValue(String value) {

        Long hash = super.hash(value);
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if (!last.isEmpty()) {
            return last.get(last.firstKey());
        }

        if (treeMap.size() == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }

        return treeMap.firstEntry().getValue();
    }

    @Override
    protected void processBefore() {
        treeMap.clear();
    }
}
