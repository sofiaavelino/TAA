import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.awt.geom.Line2D;
import java.lang.Math;

public class TAA {
    public static void main(String[] args) {
        Vertex[] vertices = new Vertex[24];
        vertices[0] = new Vertex(0.0, 0.0);
        vertices[1] = new Vertex(3.0, 0.0);
        vertices[2] = new Vertex(3.0, 1.0);
        vertices[3] = new Vertex(4.0, 1.0);
        vertices[4] = new Vertex(4.0, 0.0);
        vertices[5] = new Vertex(6.0, 0.0);
        vertices[6] = new Vertex(6.0, 2.0);
        vertices[7] = new Vertex(5.0, 2.0);
        vertices[8] = new Vertex(5.0, 3.0);
        vertices[9] = new Vertex(7.0, 3.0);
        vertices[10] = new Vertex(7.0, 5.0);
        vertices[11] = new Vertex(4.0, 5.0);
        vertices[12] = new Vertex(4.0, 6.0);
        vertices[13] = new Vertex(7.0, 6.0);
        vertices[14] = new Vertex(7.0, 9.0);
        vertices[15] = new Vertex(6.0, 9.0);
        vertices[16] = new Vertex(6.0, 10.0);
        vertices[17] = new Vertex(4.0, 10.0);
        vertices[18] = new Vertex(4.0, 9.0);
        vertices[19] = new Vertex(1.0, 9.0);
        vertices[20] = new Vertex(1.0, 6.0);
        vertices[21] = new Vertex(2.0, 6.0);
        vertices[22] = new Vertex(2.0, 4.0);
        vertices[23] = new Vertex(0.0, 4.0);

        DCEL dcel = new DCEL();
        dcel.createDCELFromPolygon(vertices);

        Vertex origin1 = new Vertex(0.0, 1.0);
        Vertex end1 = new Vertex(3.0, 2.0);
        Vertex origin2 = new Vertex(1.5, 3.0);
        Vertex end2 = new Vertex(1.5, 0.0);

        dcel.addPartition(origin1, end1);
        dcel.addPartition(origin2, end2);
        dcel.iterateThroughEdges();
    }

    /*
     * TODO:
     * LINHAS:
     * -55
     * -97
     */
    public static List<Face> computeVisibility(Vertex v, Face face) {
        List<Face> visibilityRegion = new ArrayList<>();
        Queue<Face> queue = new LinkedList<>();

        for (Face adjacentFace : face.getAdjacentFaces()) {
            queue.add(adjacentFace);
            adjacentFace.markAsVisible();
        }

        while (!queue.isEmpty()) {
            Face currentFace = queue.poll();
            if (!currentFace.visited) {
                currentFace.markAsVisited();
                int quadrant = currentFace.getQuadrant(v);
                double d1 = currentFace.centroid.x - v.x;
                double d2 = currentFace.centroid.y - v.y;
                // visiblePiece();
                currentFace.markAsVisible();
                Face esquerda, baixo, cima, direita;
                if (currentFace.outerComponent.origin.x == currentFace.outerComponent.origin.next.x) {
                    if (currentFace.outerComponent.origin.y < currentFace.outerComponent.origin.next.y) { // vertical
                                                                                                          // esquerda
                        esquerda = currentFace.outerComponent.twin.incidentFace;
                        cima = currentFace.outerComponent.next.twin.incidentFace;
                        direita = currentFace.outerComponent.next.next.twin.incidentFace;
                        baixo = currentFace.outerComponent.next.next.next.twin.incidentFace;
                    } else {// direita
                        direita = currentFace.outerComponent.twin.incidentFace;
                        baixo = currentFace.outerComponent.next.twin.incidentFace;
                        esquerda = currentFace.outerComponent.next.next.twin.incidentFace;
                        cima = currentFace.outerComponent.next.next.next.twin.incidentFace;
                    }
                } else {
                    if (currentFace.outerComponent.origin.x < currentFace.outerComponent.origin.next.x) { // horizontal
                                                                                                          // de cima
                        cima = currentFace.outerComponent.twin.incidentFace;
                        direita = currentFace.outerComponent.next.twin.incidentFace;
                        baixo = currentFace.outerComponent.next.next.twin.incidentFace;
                        esquerda = currentFace.outerComponent.next.next.next.twin.incidentFace;
                    } else {// baixo
                        baixo = currentFace.outerComponent.twin.incidentFace;
                        esquerda = currentFace.outerComponent.next.twin.incidentFace;
                        cima = currentFace.outerComponent.next.next.twin.incidentFace;
                        direita = currentFace.outerComponent.next.next.next.twin.incidentFace;
                    }
                }

                if (d2 > d1) { // determinar a prox face a analisar dependendo das direcoes
                    if ((-d2) < 0)
                        queue.add(baixo);
                    else
                        queue.add(cima);
                } else {
                    if ((-d1) < 0)
                        queue.add(esquerda);
                    else
                        queue.add(direita);
                }
                // identificar edge que a face partilha com q e ver se o modem a consegue ver
            }
        }

        return visibilityRegion;
    }
    /*
     * ROUGH PSEUDOCODE FOR VISIBILITY
     * Function visibility_propagation(V, p, Q):
     * Initialize an empty queue
     * For each piece q ∈ Q adjacent to p:
     * Mark q as totally visible
     * Enqueue q
     * While the queue is not empty:
     * Dequeue a piece q from the queue
     * Determine the quadrant of q relative to p
     * Set the search directions d1 and d2 based on the quadrant
     * Compute the visible section Vs(q, p) of q using the visibility cone defined
     * by p and the visible parts of the edges shared by q and its neighbours in the
     * directions –d1 and –d2
     * Mark q as visited
     * For each piece q' adjacent to q in directions d1 and d2:
     * If q' is not already visited and q' shares an edge with q that is visible
     * from p:
     * Mark q' as partially visible if Vs(q', p) is a proper subset of q'
     * Otherwise, mark q' as totally visible
     * Enqueue q'
     */
}

class Vertex {
    double x, y;
    HalfEdge leaving; // reference to one of the half-edges leaving this vertex

    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

class HalfEdge {
    Vertex origin;
    HalfEdge twin;
    Face incidentFace;
    HalfEdge next;
    HalfEdge prev;

    public HalfEdge(Vertex origin) {
        this.origin = origin;
    }
}

class Face {
    HalfEdge outerComponent; // reference to one of the half-edges that bound the face
    Vertex centroid;
    boolean visited;
    boolean visible;

    public Face(HalfEdge outerComponent) {
        this.outerComponent = outerComponent;
        this.visible = false;
        this.visited = false;
    }

    public void computeCentroid() { // computes the center of the face
        if (outerComponent.origin.x == outerComponent.next.origin.x) { // edge vertical
            centroid.y = outerComponent.origin.y + (outerComponent.origin.y - outerComponent.next.origin.y) / 2;
            centroid.x = outerComponent.origin.next.x
                    + (outerComponent.origin.next.x - outerComponent.next.next.origin.x) / 2;
        } else { // edge horizontal
            centroid.x = outerComponent.origin.x + (outerComponent.origin.x - outerComponent.next.origin.x) / 2;
            centroid.y = outerComponent.origin.next.y
                    + (outerComponent.origin.next.y - outerComponent.next.next.origin.y) / 2;
        }
    }

    public void markAsVisible() {
        this.visible = true;
    }

    public void markAsVisited() {
        this.visited = true;
    }

    public List<Face> getAdjacentFaces() {
        HalfEdge temp = outerComponent;
        List<Face> result = new ArrayList<>();
        do {
            if (temp.twin.incidentFace != null)
                result.add(temp.twin.incidentFace);
            temp = temp.next;
        } while (temp != outerComponent);
        return result;
    }

    public int getQuadrant(Vertex p) {
        // upper-left - 1
        // upper-right - 2
        // lower-left - 3
        // lower-right - 4
        if (centroid.x < p.x) { // either upper-left or lower-left quadrant
            if (centroid.y > p.x) // upper left
                return 1;
            else // lower left
                return 3;
        } else { // upper or lower right
            if (centroid.y > p.x) // upper right
                return 2;
            else // lower right
                return 4;
        }
    }

    public Vertex getDirections(Vertex modem) { // retorna um vertice mas na verdade sao direções, cuidado para nao usar
                                                // para um vertice mesmo
        Vertex result = centroid;
        result.x -= modem.x;
        result.y -= modem.y;
        return result;
    }
}

class DCEL {
    List<Vertex> vertices;
    List<HalfEdge> halfEdges;
    List<HalfEdge> externalEdges;
    List<Face> faces;

    public DCEL() {
        vertices = new ArrayList<>();
        halfEdges = new ArrayList<>();
        externalEdges = new ArrayList<>();
        faces = new ArrayList<>();
    }

    // Method to add a new vertex to the DCEL
    public void addVertex(Vertex v) {
        vertices.add(v);
    }

    // Method to add a new edge to the DCEL
    public void addEdge(HalfEdge e) {
        halfEdges.add(e);
    }

    // Method to add a new edge to the DCEL
    public void addExternalEdge(HalfEdge e) {
        externalEdges.add(e);
    }

    // Method to add a new face to the DCEL
    public void addFace(Face f) {
        faces.add(f);
    }

    // Method to create a DCEL from an array of vertices representing a polygon in
    // counter-clockwise order
    public void createDCELFromPolygon(Vertex[] polygonVertices) {
        int n = polygonVertices.length;
        HalfEdge[] edges = new HalfEdge[n];
        Face face = new Face(null);
        Face outerFace = new Face(null);
        Face[] facesArray = new Face[n];

        // Create half-edges and vertices
        for (int i = 0; i < n; i++) {
            Vertex v = polygonVertices[i];
            Vertex nextVertex = polygonVertices[(i + 1) % n];
            HalfEdge edge = new HalfEdge(v);
            HalfEdge twinEdge = new HalfEdge(nextVertex);
            edge.twin = twinEdge;
            twinEdge.twin = edge;
            edge.incidentFace = face;
            twinEdge.incidentFace = outerFace; // Will be set later
            edges[i] = edge;
            v.leaving = edge;
            addVertex(v);
            addEdge(edge);
            addEdge(twinEdge);
            externalEdges.add(edge);
        }

        // Linking next and previous half-edges
        for (int i = 0; i < n; i++) {
            edges[i].next = edges[(i + 1) % n];
            edges[(i + 1) % n].prev = edges[i];
            edges[i].twin.prev = edges[(i + 1) % n].twin;
            edges[(i + 1) % n].twin.next = edges[i].twin;
        }

        // Set outer component for the face
        face.outerComponent = edges[0];
        facesArray[0] = face;

        // Add faces to DCEL
        for (Face f : facesArray) {
            addFace(f);
        }
    }

    public void iterateThroughEdges() {
        System.out.println("Pontos de cada edge:");
        for (int i = 0; i < halfEdges.size() / 2; i++) {
            HalfEdge h = halfEdges.get(2 * i);
            HalfEdge h1 = null;

            System.out.println("\n" + (i + 1) + "th cycle");

            while (h != h1) {
                h1 = halfEdges.get(2 * i); // note: isto podia estar fora do while
                System.out.println("Origin: (" + h.origin.x + ", " + h.origin.y + ")");
                // System.out.println(h.incidentFace);
                System.out.println("Twin Origin: (" + h.twin.origin.x + ", " + h.twin.origin.y + ")");
                // System.out.println(h.twin.incidentFace);
                h = h.next;
            }
        }
    }

    public int edgeVisibility(Vertex modem, HalfEdge targetEdge) {
        Vertex p1 = targetEdge.origin;
        Vertex p2 = targetEdge.next.origin;
        int count = 0;
        for (HalfEdge edge : halfEdges) {
            if (intersection(edge.origin, edge.next.origin, p1, p2) != null)
                count++;
        }
        return count;
    }

    public Vertex intersection(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        double m1 = (v2.y - v1.y) / (v2.x - v1.x);
        double m2 = (v4.y - v3.y) / (v4.x - v3.x);

        double b1 = v2.y - m1 * v2.x;
        double b2 = v4.y - m2 * v4.x;

        if (m1 == m2) {
            return null;
        }

        if (Double.isInfinite(m2)) {
            if ((m1 * v3.x + b1) <= Math.max(v4.y, v3.y) && (m1 * v3.x + b1) >= Math.min(v4.y, v3.y)) {
                Vertex i = new Vertex(v3.x, m1 * v3.x + b1);
                return i;
            } else {
                return null;
            }
        }

        else if (Double.isInfinite(m1)) {
            if ((m2 * v1.x + b2) <= Math.max(v1.y, v2.y) && (m2 * v1.x + b2) >= Math.min(v1.y, v2.y)) {
                Vertex i = new Vertex(v1.x, m2 * v1.x + b2);
                return i;
            } else {
                return null;
            }
        }

        double crossx = (b2 - b1) / (m1 - m2);
        double crossy = m1 * crossx + b1;

        Vertex i = new Vertex(crossx, crossy);

        double dotprod = (i.x - v3.x) * (v4.x - v3.x) + (i.y - v3.y) * (v4.y - v3.y);
        double len = (v4.x - v3.x) * (v4.x - v3.x) + (v4.y - v3.y) * (v4.y - v3.y);

        if (dotprod < 0 || dotprod > len) {
            return null;
        }

        return i;
    }

    public void addPartition(Vertex origin, Vertex end) {
        addVertex(origin);

        HalfEdge h = null;
        Face f = null;
        for (HalfEdge e : externalEdges) {
            Vertex i = intersection(origin, end, e.origin, e.next.origin);
            if (i != null && i.x == origin.x && i.y == origin.y) {
                h = e;
            }
        }

        HalfEdge prev = h; // so we can add pointer later

        if (h.origin.x == origin.x && h.origin.y == origin.y) {
            f = new Face(h, faces.size());
            h.incidentFace = f;
            addFace(f);
            prev = h.prev;
        }

        else if (h.next.origin.x == origin.x && h.next.origin.y == origin.y) {
            h = h.next;
            f = new Face(h, faces.size());
            h.incidentFace = f;
            addFace(f);
        }

        else {
            HalfEdge nexth = new HalfEdge(origin);
            nexth.next = h.next;
            h.next.prev = nexth;
            h.next = null; // set later
            nexth.prev = null; // set later

            HalfEdge nexth_twin = new HalfEdge(h.twin.origin);
            h.twin.origin = origin;
            nexth_twin.prev = h.twin.prev;
            h.twin.prev.next = nexth_twin;
            nexth_twin.next = h.twin;
            h.twin.prev = nexth_twin;

            f = new Face(nexth, faces.size());
            nexth.incidentFace = f;
            nexth_twin.incidentFace = h.twin.incidentFace;
            nexth.twin = nexth_twin;
            nexth_twin.twin = nexth;

            addEdge(nexth);
            addEdge(nexth_twin);
            addFace(f);

            System.out.println(nexth.origin.x + " " + nexth.origin.y);
            System.out.println(nexth.incidentFace.counter);

            h = nexth;
        }

        Vertex intersection = origin;
        HalfEdge next = h; // so we can add pointer later
        h = h.next;

        while (intersection.x != end.x || intersection.y != end.y) {
            Vertex i = intersection(origin, end, h.origin, h.next.origin);

            if (i != null) {
                intersection = i;
                System.out.println(i.x + " " + i.y);
                addVertex(i); // check if vertex doesnt exist yet
                h.incidentFace = f;
                HalfEdge newh = new HalfEdge(i);
                HalfEdge newh_twin = new HalfEdge(next.origin);

                if ((i.x != h.next.origin.x || i.y != h.next.origin.y) && (i.x != h.origin.x || i.y != h.origin.y)) {
                    HalfEdge nexth = new HalfEdge(i); // add halfedge above intersection point
                    nexth.incidentFace = prev.incidentFace;
                    nexth.next = h.next;
                    h.next.prev = nexth;

                    HalfEdge h_twin = new HalfEdge(i); // add twin halfedge to h (below intersection point)
                    h_twin.next = h.twin.next;
                    h.twin.next.prev = h_twin;
                    h.twin.next = null; // to be set later
                    h_twin.prev = null; // to be set later
                    nexth.twin = h.twin;
                    h.twin.twin = nexth;
                    h.twin = h_twin;
                    h_twin.twin = h;
                    addEdge(nexth);
                    addEdge(h_twin);

                    newh.incidentFace = f;
                    h.next = newh;
                    newh.prev = h;
                    newh.next = next;
                    next.prev = newh;

                    newh_twin.incidentFace = prev.incidentFace;
                    newh_twin.prev = prev;
                    prev.next = newh_twin;
                    newh_twin.next = nexth;
                    nexth.prev = newh_twin;
                    newh_twin.twin = newh;
                    newh.twin = newh_twin;

                    if (nexth.twin.incidentFace.counter != 0) { // check if next is outer face
                        f = new Face(h_twin, faces.size());
                        addFace(f);
                    } else {
                        f = nexth.twin.incidentFace;
                        nexth.twin.next = h_twin;
                        h_twin.prev = nexth.twin;
                    }

                    prev = nexth.twin;
                    h = h_twin;
                } else {

                    newh_twin.prev = prev;
                    newh_twin.incidentFace = prev.incidentFace;
                    prev.next = newh_twin;
                    newh_twin.next = h.next;
                    h.next.prev = newh_twin;
                    newh_twin.twin = newh;
                    newh.twin = newh_twin;

                    newh.incidentFace = f;
                    h.next = newh;
                    newh.prev = h;
                    newh.next = next;
                    next.prev = newh;

                    prev = h;

                    f = h.twin.incidentFace;
                    h = h.twin;
                }

                next = h;
                addEdge(newh);
                addEdge(newh_twin);
            }

            h.incidentFace = f;
            h = h.next;
        }

        /*
         * for (HalfEdge e : halfEdges) {
         * System.out.println(e.origin.x + " " + e.origin.y);
         * System.out.println(e.next.origin.x + " " + e.next.origin.y);
         * System.out.println(e.incidentFace.counter);
         * }
         */
    }
}
