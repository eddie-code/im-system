package com.learn.im.common.route.algorithm.consistenthash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author lee
 * @description
 */
public abstract class AbstractConsistentHash {

    //add
    protected abstract void add(long key, String value);

    //sort
    protected void sort() {
        // TODO
    }

    //获取节点 get
    protected abstract String getFirstNodeValue(String value);

    /**
     * 处理之前事件
     */
    protected abstract void processBefore();

    /**
     * 传入节点列表以及客户端信息获取一个服务节点
     *
     * @param values
     * @param key
     * @return
     */
    public synchronized String process(List<String> values, String key) {
        // 清空treeMap; 因为每次的节点都是动态, 有多有少
        processBefore();
        for (String value : values) {
            // 添加元素; 需要注意的是, 这里有虚拟节点的概念, 所以会比实际的节点多, 会在6个当中挑一个, 如果不清空的话, 有可能会拿到旧的节点
            add(hash(value), value);
        }
        // 排序没有实现
        sort();
        return getFirstNodeValue(key);
    }


    //hash

    /**
     * hash 运算
     *
     * @param value
     * @return
     */
    public Long hash(String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes = null;
        try {
            keyBytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown string :" + value, e);
        }

        md5.update(keyBytes);
        byte[] digest = md5.digest();

        // hash code, Truncate to 32-bits
        long hashCode = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);

        long truncateHashCode = hashCode & 0xffffffffL;
        return truncateHashCode;
    }

}
