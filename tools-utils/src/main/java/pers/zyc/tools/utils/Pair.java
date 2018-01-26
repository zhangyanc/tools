package pers.zyc.tools.utils;

/**
 * @author zhangyancheng
 */
public class Pair<K, V> {
    private K key;
    private V value;

    public K key() {
        return key;
    }

    public void key(K key) {
        this.key = key;
    }

    public V value() {
        return value;
    }

    public void value(V value) {
        this.value = value;
    }
}
