package finalproject;

import java.util.ArrayList;

public class MyWebGraph {
    public MyHashTable<String, WebVertex> vertexList;

    public MyWebGraph() {
        vertexList = new MyHashTable<>();
    }

    public boolean addVertex(String s) {
        if (vertexList.get(s) != null) {
            return false;
        }
        vertexList.put(s, new WebVertex(s));
        return true;
    }

    public boolean addEdge(String from, String to) {
        WebVertex fromVertex = vertexList.get(from);
        WebVertex toVertex = vertexList.get(to);
        if (fromVertex == null || toVertex == null) {
            return false;
        }
        return fromVertex.addEdge(to);
    }

    public ArrayList<String> getVertices() {
        return vertexList.keySet();
    }

    public ArrayList<String> getEdgesInto(String v) {
        ArrayList<String> edgesInto = new ArrayList<>();
        for (String url : vertexList.keySet()) {
            WebVertex vertex = vertexList.get(url);
            if (vertex.links.contains(v)) {
                edgesInto.add(url);
            }
        }
        return edgesInto;
    }

    public ArrayList<String> getNeighbors(String url) {
        WebVertex vertex = vertexList.get(url);
        return (vertex != null) ? vertex.getNeighbors() : new ArrayList<>();
    }

    public int getOutDegree(String url) {
        WebVertex vertex = vertexList.get(url);
        return (vertex != null) ? vertex.links.size() : 0;
    }

    public void setPageRank(String url, double rank) {
        WebVertex vertex = vertexList.get(url);
        if (vertex != null) {
            vertex.rank = rank;
        }
    }

    public double getPageRank(String url) {
        WebVertex vertex = vertexList.get(url);
        return (vertex != null) ? vertex.rank : 0;
    }

    public boolean setVisited(String url, boolean visited) {
        WebVertex vertex = vertexList.get(url);
        if (vertex != null) {
            vertex.visited = visited;
            return true;
        }
        return false;
    }

    public boolean getVisited(String url) {
        WebVertex vertex = vertexList.get(url);
        return (vertex != null) ? vertex.visited : false;
    }

    public class WebVertex {
        private String url;
        public ArrayList<String> links;
        private boolean visited;
        private double rank;

        public WebVertex(String url) {
            this.url = url;
            this.links = new ArrayList<>();
            this.visited = false;
            this.rank = 0;
        }

        public boolean addEdge(String v) {
            if (!links.contains(v)) {
                links.add(v);
                return true;
            }
            return false;
        }

        public ArrayList<String> getNeighbors() {
            return this.links;
        }

        public boolean containsEdge(String e) {
            return this.links.contains(e);
        }

        public String toString() {
            return this.url + "\t" + this.visited + "\t" + this.rank;
        }
    }
}
