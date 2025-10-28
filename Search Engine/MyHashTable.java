package finalproject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class MyHashTable<K, V> implements Iterable<MyPair<K, V>> {
    private int size;
    private int capacity;
    private static final double MAX_LOAD_FACTOR = 0.75;
    private ArrayList<LinkedList<MyPair<K, V>>> buckets;

    public MyHashTable() {
        this(16);
    }

    public MyHashTable(int initialCapacity) {
        this.capacity = initialCapacity;
        this.size = 0;
        this.buckets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buckets.add(new LinkedList<>());
        }
    }

    public void clear() {
        this.size = 0;
        for (LinkedList<MyPair<K, V>> bucket : buckets) {
            bucket.clear();
        }
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public int numBuckets() {
        return this.capacity;
    }

    public ArrayList<LinkedList<MyPair<K, V>>> getBuckets() {
        return this.buckets;
    }

    public int hashFunction(K key) {
        return Math.abs(key.hashCode()) % this.capacity;
    }

    public V put(K key, V value) {
        int index = hashFunction(key);
        LinkedList<MyPair<K, V>> bucket = buckets.get(index);
        for (MyPair<K, V> pair : bucket) {
            if (pair.getKey().equals(key)) {
                V oldValue = pair.getValue();
                pair.setValue(value);
                return oldValue;
            }
        }
        bucket.add(new MyPair<>(key, value));
        size++;
        if ((double) size / capacity > MAX_LOAD_FACTOR) {
            rehash();
        }
        return null;
    }

    public V get(K key) {
        int index = hashFunction(key);
        LinkedList<MyPair<K, V>> bucket = buckets.get(index);
        for (MyPair<K, V> pair : bucket) {
            if (pair.getKey().equals(key)) {
                return pair.getValue();
            }
        }
        return null;
    }

    public V remove(K key) {
        int index = hashFunction(key);
        LinkedList<MyPair<K, V>> bucket = buckets.get(index);
        for (MyPair<K, V> pair : bucket) {
            if (pair.getKey().equals(key)) {
                V value = pair.getValue();
                bucket.remove(pair);
                size--;
                return value;
            }
        }
        return null;
    }

    public void rehash() {
        ArrayList<LinkedList<MyPair<K, V>>> oldBuckets = buckets;
        capacity *= 2;
        buckets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buckets.add(new LinkedList<>());
        }
        size = 0;
        for (LinkedList<MyPair<K, V>> bucket : oldBuckets) {
            for (MyPair<K, V> pair : bucket) {
                put(pair.getKey(), pair.getValue());
            }
        }
    }

    public ArrayList<K> keySet() {
        ArrayList<K> keys = new ArrayList<>();
        for (LinkedList<MyPair<K, V>> bucket : buckets) {
            for (MyPair<K, V> pair : bucket) {
                keys.add(pair.getKey());
            }
        }
        return keys;
    }

    public ArrayList<V> values() {
        ArrayList<V> values = new ArrayList<>();
        for (LinkedList<MyPair<K, V>> bucket : buckets) {
            for (MyPair<K, V> pair : bucket) {
                values.add(pair.getValue());
            }
        }
        return values;
    }

    public ArrayList<MyPair<K, V>> entrySet() {
        ArrayList<MyPair<K, V>> entries = new ArrayList<>();
        for (LinkedList<MyPair<K, V>> bucket : buckets) {
            entries.addAll(bucket);
        }
        return entries;
    }

    @Override
    public Iterator<MyPair<K, V>> iterator() {
        return new MyHashIterator();
    }

    private class MyHashIterator implements Iterator<MyPair<K, V>> {
        private ArrayList<MyPair<K, V>> entries;
        private int currentIndex;

        public MyHashIterator() {
            entries = entrySet();
            currentIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < entries.size();
        }

        @Override
        public MyPair<K, V> next() {
            return entries.get(currentIndex++);
        }
    }
}
