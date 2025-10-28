package finalproject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class SearchEngine {
    public MyHashTable<String, ArrayList<String>> wordIndex;
    public MyWebGraph internet;
    public XmlParser parser;

    public SearchEngine(String filename) throws Exception {
        this.wordIndex = new MyHashTable<>();
        this.internet = new MyWebGraph();
        this.parser = new XmlParser(filename);
    }

    public void crawlAndIndex(String url) {
        HashSet<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(url);
        visited.add(url);
        internet.addVertex(url);

        while (!queue.isEmpty()) {
            String currentUrl = queue.poll();
            internet.setVisited(currentUrl, true);

            try {
                ArrayList<String> words = parser.getContent(currentUrl);
                for (String word : words) {
                    word = word.toLowerCase();
                    ArrayList<String> urls = wordIndex.get(word);
                    if (urls == null) {
                        urls = new ArrayList<>();
                        wordIndex.put(word, urls);
                    }
                    if (!urls.contains(currentUrl)) {
                        urls.add(currentUrl);
                    }
                }

                ArrayList<String> links = parser.getLinks(currentUrl);
                for (String link : links) {
                    internet.addVertex(link);
                    if (internet.addEdge(currentUrl, link)) {
                        if (!visited.contains(link)) {
                            visited.add(link);
                            queue.add(link);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void assignPageRanks(double epsilon) {
        ArrayList<String> vertices = internet.getVertices();
        ArrayList<Double> prevRanks = new ArrayList<>();
        for (String url : vertices) {
            internet.setPageRank(url, 1.0);
            prevRanks.add(1.0);
        }

        boolean converged = false;
        while (!converged) {
            ArrayList<Double> newRanks = computeRanks(vertices);
            converged = true;
            for (int i = 0; i < vertices.size(); i++) {
                double diff = Math.abs(newRanks.get(i) - prevRanks.get(i));
                if (diff >= epsilon) {
                    converged = false;
                    break;
                }
            }
            prevRanks = newRanks;
        }
    }

    public ArrayList<Double> computeRanks(ArrayList<String> vertices) {
        ArrayList<Double> newRanks = new ArrayList<>();
        double damping = 0.5;
        for (String url : vertices) {
            ArrayList<String> edgesInto = internet.getEdgesInto(url);
            double sum = 0.0;
            for (String w : edgesInto) {
                int outDegree = internet.getOutDegree(w);
                if (outDegree > 0) {
                    sum += internet.getPageRank(w) / outDegree;
                }
            }
            double rank = (1 - damping) + damping * sum;
            newRanks.add(rank);
        }

        for (int i = 0; i < vertices.size(); i++) {
            internet.setPageRank(vertices.get(i), newRanks.get(i));
        }
        return newRanks;
    }

    public ArrayList<String> getResults(String query) {
        query = query.toLowerCase();
        ArrayList<String> urls = wordIndex.get(query);
        if (urls == null) {
            return new ArrayList<>();
        }

        MyHashTable<String, Double> urlRanks = new MyHashTable<>();
        for (String url : urls) {
            urlRanks.put(url, internet.getPageRank(url));
        }

        return Sorting.fastSort(urlRanks);
    }
}
