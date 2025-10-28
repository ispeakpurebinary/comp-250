package finalproject;

import java.util.ArrayList;

public class Sorting {
    public static <K, V extends Comparable<V>> ArrayList<K> slowSort(MyHashTable<K, V> results) {
        ArrayList<K> sortedUrls = new ArrayList<>(results.keySet());
        int N = sortedUrls.size();
        for (int i = 0; i < N - 1; i++) {
            for (int j = 0; j < N - i - 1; j++) {
                if (results.get(sortedUrls.get(j)).compareTo(results.get(sortedUrls.get(j + 1))) < 0) {
                    K temp = sortedUrls.get(j);
                    sortedUrls.set(j, sortedUrls.get(j + 1));
                    sortedUrls.set(j + 1, temp);
                }
            }
        }
        return sortedUrls;
    }

    public static <K, V extends Comparable<V>> ArrayList<K> fastSort(MyHashTable<K, V> results) {
        ArrayList<MyPair<K, V>> entries = results.entrySet();
        mergeSort(entries, 0, entries.size() - 1);
        ArrayList<K> sortedKeys = new ArrayList<>();
        for (MyPair<K, V> entry : entries) {
            sortedKeys.add(entry.getKey());
        }
        return sortedKeys;
    }

    private static <K, V extends Comparable<V>> void mergeSort(ArrayList<MyPair<K, V>> list, int l, int r) {
        if (l < r) {
            int m = l + (r - l) / 2;
            mergeSort(list, l, m);
            mergeSort(list, m + 1, r);
            merge(list, l, m, r);
        }
    }

    private static <K, V extends Comparable<V>> void merge(ArrayList<MyPair<K, V>> list, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;

        ArrayList<MyPair<K, V>> L = new ArrayList<>(n1);
        ArrayList<MyPair<K, V>> R = new ArrayList<>(n2);

        for (int i = 0; i < n1; i++) {
            L.add(list.get(l + i));
        }
        for (int j = 0; j < n2; j++) {
            R.add(list.get(m + 1 + j));
        }

        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if (L.get(i).getValue().compareTo(R.get(j).getValue()) >= 0) {
                list.set(k, L.get(i));
                i++;
            } else {
                list.set(k, R.get(j));
                j++;
            }
            k++;
        }

        while (i < n1) {
            list.set(k, L.get(i));
            i++;
            k++;
        }

        while (j < n2) {
            list.set(k, R.get(j));
            j++;
            k++;
        }
    }
}
