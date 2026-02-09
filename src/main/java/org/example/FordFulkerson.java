package org.example;

import java.util.*;

public class FordFulkerson {
    private int vertices;
    private List<List<EdgeFF>> graph;
    private Map<Edge, Integer> flows;

    static class EdgeFF {
        int to;
        int capacity;
        int flow;
        EdgeFF reverse;

        EdgeFF(int to, int capacity, int flow) {
            this.to = to;
            this.capacity = capacity;
            this.flow = flow;
        }
    }

    public FordFulkerson(int vertices) {
        this.vertices = vertices;
        this.graph = new ArrayList<>();
        this.flows = new HashMap<>();

        for (int i = 0; i < vertices; i++) {
            graph.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int capacity, int initialFlow) {
        EdgeFF forward = new EdgeFF(to, capacity, initialFlow);
        EdgeFF backward = new EdgeFF(from, 0, 0);

        forward.reverse = backward;
        backward.reverse = forward;

        graph.get(from).add(forward);
        graph.get(to).add(backward);
    }

    public int getMaxFlow(int source, int sink) {
        int maxFlow = 0;

        while (true) {
            // BFS pentru a găsi un drum de augmentare
            int[] parent = new int[vertices];
            EdgeFF[] parentEdge = new EdgeFF[vertices];
            Arrays.fill(parent, -1);

            Queue<Integer> queue = new LinkedList<>();
            queue.offer(source);
            parent[source] = source;

            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.poll();

                for (EdgeFF edge : graph.get(u)) {
                    if (parent[edge.to] == -1 && edge.capacity > edge.flow) {
                        parent[edge.to] = u;
                        parentEdge[edge.to] = edge;
                        queue.offer(edge.to);
                    }
                }
            }

            // Dacă nu există drum de augmentare, returnăm fluxul maxim
            if (parent[sink] == -1) {
                break;
            }

            // Găsim capacitatea reziduală minimă de-a lungul drumului
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                EdgeFF edge = parentEdge[v];
                pathFlow = Math.min(pathFlow, edge.capacity - edge.flow);
            }

            // Actualizăm fluxurile de-a lungul drumului
            for (int v = sink; v != source; v = parent[v]) {
                EdgeFF edge = parentEdge[v];
                edge.flow += pathFlow;
                edge.reverse.flow -= pathFlow;
            }

            maxFlow += pathFlow;
        }

        // Salvăm fluxurile finale
        for (int u = 0; u < vertices; u++) {
            for (EdgeFF edge : graph.get(u)) {
                if (edge.capacity > 0 && edge.flow > 0) {
                    flows.put(new Edge(u, edge.to, edge.capacity, 0, 0), edge.flow);
                }
            }
        }

        return maxFlow;
    }

    public Set<Edge> getMinCut(int source) {
        Set<Edge> minCut = new HashSet<>();
        boolean[] reachable = new boolean[vertices];

        // BFS pentru a găsi nodurile accesibile din sursa în graful rezidual
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(source);
        reachable[source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (EdgeFF edge : graph.get(u)) {
                if (!reachable[edge.to] && edge.capacity > edge.flow) {
                    reachable[edge.to] = true;
                    queue.offer(edge.to);
                }
            }
        }

        // Tăietura minimă constă din arcele care merg din partea accesibilă
        // în partea neaccesibilă
        for (int u = 0; u < vertices; u++) {
            if (reachable[u]) {
                for (EdgeFF edge : graph.get(u)) {
                    if (!reachable[edge.to] && edge.capacity > 0) {
                        minCut.add(new Edge(u, edge.to, edge.capacity, 0, 0));
                    }
                }
            }
        }

        return minCut;
    }

    public int getMinCutCapacity(Set<Edge> minCut) {
        int capacity = 0;
        for (Edge edge : minCut) {
            capacity += edge.capacity;
        }
        return capacity;
    }

    public Map<Edge, Integer> getFlows() {
        return new HashMap<>(flows);
    }
}