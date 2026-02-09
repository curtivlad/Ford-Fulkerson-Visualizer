package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {
    private NetworkPanel networkPanel;
    private JButton calculateMaxFlowBtn, clearBtn, resetFlowBtn;
    private JLabel statusLabel;

    public Main() {
        setTitle("Ford-Fulkerson - Flux Maxim și Tăietură Minimă");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel pentru rețea
        networkPanel = new NetworkPanel(this);
        add(networkPanel, BorderLayout.CENTER);

        // Panel pentru butoane
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        calculateMaxFlowBtn = new JButton("Calculează Flux Maxim (F)");
        resetFlowBtn = new JButton("Resetează Flux (R)");
        clearBtn = new JButton("Șterge Tot (C)");

        calculateMaxFlowBtn.addActionListener(e -> calculateMaxFlow());
        resetFlowBtn.addActionListener(e -> networkPanel.resetFlow());
        clearBtn.addActionListener(e -> networkPanel.clear());

        // Adăugare taste scurte
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode() == KeyEvent.VK_F) {
                        calculateMaxFlow();
                        return true;
                    } else if (e.getKeyCode() == KeyEvent.VK_R) {
                        networkPanel.resetFlow();
                        return true;
                    } else if (e.getKeyCode() == KeyEvent.VK_C) {
                        networkPanel.clear();
                        return true;
                    }
                }
                return false;
            }
        });

        controlPanel.add(calculateMaxFlowBtn);
        controlPanel.add(resetFlowBtn);
        controlPanel.add(clearBtn);

        add(controlPanel, BorderLayout.NORTH);

        // Status bar
        statusLabel = new JLabel("Click pentru a adăuga nod | Drag de pe un nod pe altul pentru arc | Drag nod în gol pentru mutare");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    public void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void calculateMaxFlow() {
        if (networkPanel.getNodes().size() < 2) {
            JOptionPane.showMessageDialog(this, "Adăugați cel puțin 2 noduri!");
            return;
        }

        String sourceStr = JOptionPane.showInputDialog(this, "Introduceți nodul sursă (0-" + (networkPanel.getNodes().size()-1) + "):");
        String sinkStr = JOptionPane.showInputDialog(this, "Introduceți nodul destinație (0-" + (networkPanel.getNodes().size()-1) + "):");

        if (sourceStr == null || sinkStr == null) return;

        try {
            int source = Integer.parseInt(sourceStr);
            int sink = Integer.parseInt(sinkStr);

            if (source < 0 || source >= networkPanel.getNodes().size() ||
                    sink < 0 || sink >= networkPanel.getNodes().size() || source == sink) {
                JOptionPane.showMessageDialog(this, "Noduri invalide!");
                return;
            }

            FordFulkerson ff = new FordFulkerson(networkPanel.getNodes().size());
            for (Edge edge : networkPanel.getEdges()) {
                ff.addEdge(edge.from, edge.to, edge.capacity, edge.flow);
            }

            int maxFlow = ff.getMaxFlow(source, sink);
            Set<Edge> minCut = ff.getMinCut(source);

            networkPanel.setFlows(ff.getFlows());
            networkPanel.setMinCut(minCut);

            statusLabel.setText("Flux Maxim: " + maxFlow + " | Tăietură minimă evidențiată cu roșu");

            JOptionPane.showMessageDialog(this,
                    "Flux Maxim: " + maxFlow + "\n" +
                            "Tăietură Minimă: " + minCut.size() + " arce\n" +
                            "Capacitate tăietură: " + ff.getMinCutCapacity(minCut),
                    "Rezultat", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Introduceți numere valide!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}

class Node {
    int id;
    int x, y;
    static final int RADIUS = 25;

    public Node(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public boolean contains(int px, int py) {
        return Math.sqrt((px - x) * (px - x) + (py - y) * (py - y)) <= RADIUS;
    }
}

class Edge {
    int from, to;
    int capacity;
    int flow;
    int cost;

    public Edge(int from, int to, int capacity, int flow, int cost) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.flow = flow;
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        return from == edge.from && to == edge.to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}

class NetworkPanel extends JPanel {
    private List<Node> nodes;
    private List<Edge> edges;
    private Map<Edge, Integer> flows;
    private Set<Edge> minCut;

    private Node draggedNode;
    private Node sourceNode;
    private Point dragPoint;
    private Main parentFrame;

    public NetworkPanel(Main parent) {
        this.parentFrame = parent;
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        flows = new HashMap<>();
        minCut = new HashSet<>();

        setBackground(Color.WHITE);

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClicked(e);
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void handleMouseClicked(MouseEvent e) {
        // Click simplu adaugă nod nou dacă nu e pe un nod existent
        if (e.getClickCount() == 1) {
            Node clickedNode = findNode(e.getX(), e.getY());
            if (clickedNode == null) {
                nodes.add(new Node(nodes.size(), e.getX(), e.getY()));
                parentFrame.updateStatus("Nod " + (nodes.size() - 1) + " adăugat");
                repaint();
            }
        }
        // Double-click pe nod îl șterge
        else if (e.getClickCount() == 2) {
            Node clickedNode = findNode(e.getX(), e.getY());
            if (clickedNode != null) {
                deleteNode(clickedNode);
            }
        }
    }

    private void handleMousePressed(MouseEvent e) {
        draggedNode = findNode(e.getX(), e.getY());
        if (draggedNode != null) {
            sourceNode = draggedNode;
            dragPoint = e.getPoint();
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (draggedNode != null) {
            dragPoint = e.getPoint();
            repaint();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (draggedNode != null) {
            Node targetNode = findNode(e.getX(), e.getY());

            // Dacă target este un alt nod diferit → creează arc
            if (targetNode != null && targetNode != sourceNode) {
                createEdge(sourceNode.id, targetNode.id);
                parentFrame.updateStatus("Arc creat: " + sourceNode.id + " → " + targetNode.id);
            }
            // Dacă target este gol → mută nodul
            else if (targetNode == null) {
                sourceNode.x = e.getX();
                sourceNode.y = e.getY();
                parentFrame.updateStatus("Nod " + sourceNode.id + " mutat");
            }

            draggedNode = null;
            sourceNode = null;
            dragPoint = null;
            repaint();
        }
    }

    private void createEdge(int from, int to) {
        // Verifică dacă arcul există deja
        for (Edge edge : edges) {
            if (edge.from == from && edge.to == to) {
                JOptionPane.showMessageDialog(this, "Arcul există deja!");
                return;
            }
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField capacityField = new JTextField("10");
        JTextField flowField = new JTextField("0");

        panel.add(new JLabel("Capacitate:"));
        panel.add(capacityField);
        panel.add(new JLabel("Flux inițial:"));
        panel.add(flowField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Arc " + from + " → " + to, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int capacity = Integer.parseInt(capacityField.getText());
                int flow = Integer.parseInt(flowField.getText());

                if (capacity < 0 || flow < 0 || flow > capacity) {
                    JOptionPane.showMessageDialog(this, "Valori invalide! (capacitate ≥ 0, 0 ≤ flux ≤ capacitate)");
                    return;
                }

                edges.add(new Edge(from, to, capacity, flow, 0));
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Introduceți numere valide!");
            }
        }
    }

    private void deleteNode(Node node) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Ștergeți nodul " + node.id + " și toate arcele conectate?",
                "Confirmare", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Șterge toate arcele care conțin acest nod
            edges.removeIf(edge -> edge.from == node.id || edge.to == node.id);

            // Șterge nodul
            int deletedId = node.id;
            nodes.remove(node);

            // Re-indexează nodurile și arcele
            for (int i = 0; i < nodes.size(); i++) {
                int oldId = nodes.get(i).id;
                nodes.get(i).id = i;

                // Actualizează arcele
                for (Edge edge : edges) {
                    if (edge.from == oldId) edge.from = i;
                    if (edge.to == oldId) edge.to = i;
                }
            }

            parentFrame.updateStatus("Nod " + deletedId + " șters");
            repaint();
        }
    }

    private Node findNode(int x, int y) {
        for (Node node : nodes) {
            if (node.contains(x, y)) {
                return node;
            }
        }
        return null;
    }

    public void setFlows(Map<Edge, Integer> flows) {
        this.flows = new HashMap<>(flows);
        repaint();
    }

    public void setMinCut(Set<Edge> minCut) {
        this.minCut = new HashSet<>(minCut);
        repaint();
    }

    public void resetFlow() {
        flows.clear();
        minCut.clear();
        for (Edge edge : edges) {
            edge.flow = 0;
        }
        parentFrame.updateStatus("Fluxuri resetate");
        repaint();
    }

    public void clear() {
        nodes.clear();
        edges.clear();
        flows.clear();
        minCut.clear();
        parentFrame.updateStatus("Rețea ștearsă - Click pentru a adăuga noduri");
        repaint();
    }

    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Desenare arce
        for (Edge edge : edges) {
            Node fromNode = nodes.get(edge.from);
            Node toNode = nodes.get(edge.to);

            boolean isMinCut = minCut.contains(edge);
            int currentFlow = flows.getOrDefault(edge, edge.flow);

            g2.setColor(isMinCut ? Color.RED : Color.BLACK);
            g2.setStroke(new BasicStroke(isMinCut ? 3 : 2));

            drawArrow(g2, fromNode.x, fromNode.y, toNode.x, toNode.y);

            // Label cu capacitate/flux
            int midX = (fromNode.x + toNode.x) / 2;
            int midY = (fromNode.y + toNode.y) / 2;

            String label = currentFlow + "/" + edge.capacity;

            g2.setColor(Color.BLUE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);

            // Dreptunghi alb în spate pentru lizibilitate
            g2.setColor(Color.WHITE);
            g2.fillRect(midX + 3, midY - 15, labelWidth + 4, 16);

            g2.setColor(Color.BLUE);
            g2.drawString(label, midX + 5, midY - 5);
        }

        // Desenare linie de drag (când tragem de pe un nod)
        if (draggedNode != null && dragPoint != null && sourceNode != null) {
            g2.setColor(new Color(100, 100, 100, 150));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g2.drawLine(sourceNode.x, sourceNode.y, dragPoint.x, dragPoint.y);
        }

        // Desenare noduri
        for (Node node : nodes) {
            if (node == draggedNode) {
                g2.setColor(Color.YELLOW);
            } else {
                g2.setColor(Color.LIGHT_GRAY);
            }
            g2.fillOval(node.x - Node.RADIUS, node.y - Node.RADIUS,
                    2 * Node.RADIUS, 2 * Node.RADIUS);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(node.x - Node.RADIUS, node.y - Node.RADIUS,
                    2 * Node.RADIUS, 2 * Node.RADIUS);

            g2.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            String id = String.valueOf(node.id);
            g2.drawString(id, node.x - fm.stringWidth(id)/2, node.y + fm.getAscent()/2 - 2);
        }
    }

    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);

        int startX = (int)(x1 + Node.RADIUS * Math.cos(angle));
        int startY = (int)(y1 + Node.RADIUS * Math.sin(angle));
        int endX = (int)(x2 - Node.RADIUS * Math.cos(angle));
        int endY = (int)(y2 - Node.RADIUS * Math.sin(angle));

        g2.drawLine(startX, startY, endX, endY);

        int arrowSize = 10;
        int dx = (int)(arrowSize * Math.cos(angle - Math.PI / 6));
        int dy = (int)(arrowSize * Math.sin(angle - Math.PI / 6));
        g2.drawLine(endX, endY, endX - dx, endY - dy);

        dx = (int)(arrowSize * Math.cos(angle + Math.PI / 6));
        dy = (int)(arrowSize * Math.sin(angle + Math.PI / 6));
        g2.drawLine(endX, endY, endX - dx, endY - dy);
    }
}