import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.lang.Math;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class TAA_proj {
    /*
     * public static void computeHVPartitions(DCEL dcel) {
     * HalfEdge guardEdge = null;
     * for (HalfEdge e : dcel.externalEdges) {
     * if (e.origin.x == guard.x && e.origin.y == guard.y) {
     * guardEdge = e;
     * break;
     * }
     * }
     * 
     * List<Vertex> endIntersect = new ArrayList<>();
     * 
     * //System.out.println("a");
     * for (Vertex v : dcel.externalVertices) {
     * if (guard.x == v.x || guard.y == v.y) {
     * List<Vertex> intersections = new ArrayList<>();
     * if (v.x != guard.x || v.y != guard.y) {
     * System.out.println("v: " + v.x + " " + v.y);
     * HalfEdge start = null;
     * for (HalfEdge edge : externalEdges) {
     * Vertex i = line_intersect(guard, v, edge.origin, edge.next.origin);
     * if (i!=null && i.x == v.x && i.y ==v.y) {
     * start = edge;
     * }
     * else if (i != null && ((guard.y > v.y && i.y < v.y) || (guard.y < v.y && i.y
     * > v.y))) { //intersecao com uma parede
     * if (guard.x != v.x) { //ignorar casos de particoes verticais (ja
     * contabilizadas)
     * intersections.add(i);
     * }
     * 
     * }
     * }
     * Set<double[]> numIntersect = new HashSet<double[]>();
     * HalfEdge edge = start.next;
     * while (edge != start) {
     * Vertex i = line_intersect(guard, v, edge.origin, edge.next.origin);
     * if (i != null) {
     * double[] vCoord = {i.x,i.y};
     * numIntersect.add(vCoord);
     * }
     * edge = edge.next;
     * }
     * if (numIntersect.size() > 1) {
     * intersections.add(v);
     * System.out.println("i: " + v.x + " " + v.y);
     * }
     * //intersections.add(v);
     * //System.out.println("i: " + v.x + " " + v.y);
     * }
     * Collections.sort(intersections, new VertexComparator(guard));
     * if (intersections.size() > 1 && intersections.get(0).x ==
     * intersections.get(1).x && intersections.get(0).y == intersections.get(1).y)
     * intersections.remove(0);
     * //System.out.println("d " + intersections.size());
     * for (Vertex vi : intersections) {
     * System.out.println("a" + vi.x + " " + vi.y);
     * }
     * 
     * for (int i=0; i<intersections.size()/2; i++) {
     * Vertex i1 = intersections.get(2*i);
     * Vertex i2 = intersections.get(2*i+1);
     * System.out.println("e");
     * boolean edgeExists = false;
     * 
     * for (HalfEdge e : halfEdges) {
     * //System.out.println("e: " + e.origin.x + " " + e.origin.y + " " +
     * e.next.origin.x + " " + e.next.origin.y);
     * if (e.origin.x == i2.x && e.origin.y == i2.y && e.next.origin.x == i1.x &&
     * e.next.origin.y == i1.y) { //check if partition already exists
     * edgeExists = true;
     * break;
     * }
     * 
     * }
     * 
     * 
     * 
     * //falta condição para contabilizar só cortes depois do vertice corrente
     * 
     * if ((i1.x != i2.x || i1.y != i2.y) && !edgeExists) {
     * System.out.println("partition: " + i2.x + " " + i2.y + " " + i1.x + " " +
     * i1.y);
     * addPartition(i2, i1);
     * endIntersect.add(i1);
     * }
     * }
     * }
     * }
     * }
     */

    public static void main(String[] args) throws IOException {
        try {
            File myObj = new File("exemplo_grid");
            Scanner myReader = new Scanner(myObj);
            int n = myReader.nextInt();
            Vertex[] vertices = new Vertex[n];
            List<Vertex[]> partitions = new ArrayList<>();
            HashMap<Vertex, List<Vertex>> nextV = new HashMap<>();
            FileWriter myWriter = new FileWriter("polygon.txt");
            myWriter.write(n + "\n");
            for (int i = 0; i < n; i++) {
                vertices[i] = new Vertex(myReader.nextDouble(), myReader.nextDouble());
                myWriter.write(vertices[i].x + " " + vertices[i].y + "\n");
            }

            DCEL dcel = new DCEL();
            dcel.createDCELFromPolygon(vertices);

            while (myReader.hasNextLine()) {
                Double og_x = myReader.nextDouble();
                if (og_x == null)
                    break;
                Double og_y = myReader.nextDouble();
                Double end_x = myReader.nextDouble();
                Double end_y = myReader.nextDouble();
                Vertex origin = new Vertex(og_x, og_y);
                Vertex end = new Vertex(end_x, end_y);

                boolean exists = false;
                for (Vertex v : nextV.keySet()) {
                    if (v.equals(origin)) {
                        List<Vertex> toV = nextV.get(v);
                        toV.add(end);
                        nextV.put(v, toV);
                        // System.out.println("AAA" + toV.size());
                        exists = true;
                        break;
                    }
                }
                if (exists == false) {
                    // System.out.println("a");
                    List<Vertex> toV = new ArrayList<>();
                    toV.add(end);
                    nextV.put(origin, toV);
                }
            }

            for (Vertex v : nextV.keySet()) {
                Vertex origin = v;

                boolean ogInPoly = false;
                for (HalfEdge e : dcel.externalEdges) { // check if end of segment is point in polygon
                    for (Vertex next : nextV.get(v)) {
                        Vertex i = dcel.line_intersect(origin, next, e.origin, e.next.origin);
                        if (i != null && i.equals(origin)) {
                            ogInPoly = true;
                            break;
                        }
                    }
                }
                if (ogInPoly == false)
                    continue; // origin not in polygon edges

                // if (partitions.get(originCoords) == null) continue; // origin not vertex from
                // polygon
                // if (origin.x == end.x && partitions.get(originCoords)[1] == 1) continue; // v
                // partition already done for vertex
                // if (origin.y == end.y && partitions.get(originCoords)[0] == 1) continue; // h
                // partition already done for vertex

                while (nextV.get(v).size() != 0) {
                    Vertex end = nextV.get(v).get(0);
                    List<Vertex> vList = nextV.get(v);
                    vList.remove(0); // remove end from list of origin's next vertices
                    nextV.put(v, vList);

                    boolean edgeExists = false;
                    for (int i = 0; i < vertices.length; i++) { // check if edge already exists
                        // Vertex intersect = dcel.line_intersect(origin, end, vertices[i],
                        // vertices[(i+1)%vertices.length]);

                        if (origin.y == end.y && vertices[i].y == vertices[(i + 1) % vertices.length].y
                                && end.y == vertices[i].y) {
                            if (Math.max(origin.x, end.x) <= Math.max(vertices[i].x,
                                    vertices[(i + 1) % vertices.length].x)
                                    && Math.min(origin.x, end.x) >= Math.min(vertices[i].x,
                                            vertices[(i + 1) % vertices.length].x)) {
                                edgeExists = true;
                                break;
                            }
                        }
                        if (origin.x == end.x && vertices[i].x == vertices[(i + 1) % vertices.length].x
                                && end.x == vertices[i].x) {
                            if (Math.max(origin.y, end.y) <= Math.max(vertices[i].y,
                                    vertices[(i + 1) % vertices.length].y)
                                    && Math.min(origin.y, end.y) >= Math.min(vertices[i].y,
                                            vertices[(i + 1) % vertices.length].y)) {
                                edgeExists = true;
                                break;
                            }
                        }
                    }

                    if (edgeExists)
                        continue;

                    boolean reachEdge = false;
                    for (HalfEdge e : dcel.externalEdges) { // check if end of segment is point in polygon edges
                        Vertex i = dcel.line_intersect(origin, end, e.origin, e.next.origin);
                        if (i != null && i.equals(end)) {
                            reachEdge = true;
                            break;
                        }
                    }
                    // boolean partitionDone = false;
                    // System.out.println("a" + end.x + " " + end.y);
                    // if (nextV.get(end) != null && nextV.get(end).size() != 0)
                    // {System.out.println(nextV.get(end).get(0));}
                    Vertex next = null;
                    while (!reachEdge) {
                        List<Vertex> nextVs = null;
                        for (Vertex ver : nextV.keySet()) {
                            if (ver.equals(end)) {
                                nextVs = nextV.get(ver);
                            }
                        }

                        System.out.println(nextVs.size());

                        for (Vertex ver : nextVs) {
                            // System.out.println("V " + v.x + " " + v.y);
                            // System.out.println(ver.x + " " + ver.y);
                            // System.out.println("dist" + v.distanceTo(ver) + " " + (v.distanceTo(end) + 1
                            // ));
                            if (v.distanceTo(ver) == v.distanceTo(end) + 1) {
                                next = ver;
                                end = ver;
                                // break;
                            }
                            // next = false;
                        }

                        if (next != end)
                            break;

                        /*
                         * if (nextVs == null || nextVs.size() == 0) {
                         * partitionDone = true;
                         * List<Vertex> ogNextV = nextV.get(v);
                         * ogNextV.remove(end);
                         * nextV.put(v, ogNextV);
                         * break;
                         * }
                         * Collections.sort(nextVs, new VertexComparator(origin));
                         * end = nextVs.get(0); // next vertex furthest away
                         * System.out.println("a" + end.x + " " + end.y);
                         */

                        for (HalfEdge e : dcel.externalEdges) { // check if end of segment is point in polygon edges
                            Vertex i = dcel.line_intersect(origin, end, e.origin, e.next.origin);
                            if (i != null && i.equals(end)) {
                                reachEdge = true;
                                break;
                            }
                        }
                    }

                    if (!reachEdge) {
                        continue;
                    }

                    /*
                     * HalfEdge start = dcel.externalEdges.get(0).twin;
                     * if (start.origin.equals(origin) && start.next.origin.equals(end)) {
                     * edgeExists = true;
                     * break;
                     * }
                     * if (start.origin.equals(end) && start.next.origin.equals(origin)) {
                     * edgeExists = true;
                     * break;
                     * }
                     * HalfEdge e = start.next;
                     * while (!edgeExists && e != start) { // check if segment already exists
                     * System.out.println("e: " + e.origin.x + " " + e.origin.y + " " +
                     * e.next.origin.x + " " + e.next.origin.y);
                     * if (e.origin.equals(origin) && e.next.origin.equals(end)) {
                     * edgeExists = true;
                     * break;
                     * }
                     * if (e.origin.equals(end) && e.next.origin.equals(origin)) {
                     * edgeExists = true;
                     * break;
                     * }
                     * e = e.next;
                     * }
                     */

                    boolean doneP = false;
                    for (Vertex[] p : partitions) {
                        if ((p[0].equals(origin) && p[1].equals(end)) || (p[0].equals(end) && p[1].equals(origin))) {
                            doneP = true;
                            break;
                        }
                    }

                    if (doneP)
                        continue;

                    // if (!edgeExists) {
                    System.out.println("Partition: " + origin.x + " " + origin.y + " " + end.x + " " + end.y);
                    dcel.addPartition(origin, end);
                    Vertex[] part = { origin, end };
                    partitions.add(part);
                    // }
                }

                /*
                 * if (origin.y == end.y) { // horizontal partition
                 * Set<Vertex> rIntersect = new HashSet<Vertex>();
                 * Set<Vertex> lIntersect = new HashSet<Vertex>();
                 * for (HalfEdge e : dcel.externalEdges) {
                 * Vertex i = dcel.line_intersect(origin, end, e.origin, e.next.origin);
                 * if (i!=null && i.x > origin.x) { // intersect to the right of vertex
                 * rIntersect.add(i);
                 * }
                 * else if (i!=null && i.x < origin.x) { // intersect to the left of vertex
                 * lIntersect.add(i);
                 * }
                 * }
                 * rIntersect.add(origin);
                 * lIntersect.add(origin);
                 * 
                 * dcel.Collections.sort(rIntersect, new VertexComparator(origin));
                 * dcel.Collections.sort(lIntersect, new VertexComparator(origin));
                 * 
                 * 
                 * boolean edgeExists = false;
                 * 
                 * for (HalfEdge e : dcel.externalEdges) {
                 * if (e.origin.x == )
                 * }
                 * }
                 * 
                 * 
                 */
            }
            myReader.close();

            Vertex origin1 = new Vertex(3.0, 3.0);
            Vertex end1 = new Vertex(3.0, 6.0);
            Vertex origin2 = new Vertex(3.0, 3.0);
            Vertex end2 = new Vertex(0.0, 3.0);

            // dcel.addPartition(origin1, end1);
            // dcel.addPartition(origin2, end2);

            dcel.computeVisibility(vertices[0]);
            System.out.println(vertices[0].x + " " + vertices[0].y);
            List<Vertex> visRegion = dcel.mergeFaces(2);
            myWriter.write(visRegion.size() + "\n");
            for (Vertex cv : visRegion) {
                myWriter.write(cv.x + " " + cv.y + "\n");
            }
            myWriter.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        }

        // vertices[0] = new Vertex(0.0, 0.0);
        // vertices[1] = new Vertex(3.0, 0.0);
        // vertices[2] = new Vertex(3.0, 3.0);
        // vertices[3] = new Vertex(6.0, 3.0);
        // vertices[4] = new Vertex(6.0, 6.0);
        // vertices[5] = new Vertex(0.0, 6.0);
        // vertices[6] = new Vertex(6.0, 6.0);
        // vertices[7] = new Vertex(0.0, 6.0);
        // vertices[8] = new Vertex(10.0, 10.0);
        // vertices[9] = new Vertex(1.0, 10.0);
        // vertices[10] = new Vertex(1.0, 1.0);
        // vertices[11] = new Vertex(0.0, 1.0);

        /*
         * Vertex origin1 = new Vertex(3.0, 3.0);
         * Vertex end1 = new Vertex(3.0, 6.0);
         * Vertex origin2 = new Vertex(3.0, 3.0);
         * Vertex end2 = new Vertex(0.0, 3.0);
         * Vertex origin3 = new Vertex(2.0, 2.0);
         * Vertex end3 = new Vertex(2.0, 6.0);
         * Vertex origin4 = new Vertex(4.0, 4.0);
         * Vertex end4 = new Vertex(4.0, 6.0);
         * Vertex origin5 = new Vertex(8.0, 8.0);
         * Vertex end5 = new Vertex(1.0, 8.0);
         * Vertex origin6 = new Vertex(6.0, 5.0);
         * Vertex end6 = new Vertex(1.0, 5.0);
         * Vertex origin7 = new Vertex(5.0, 1.0);
         * Vertex end7 = new Vertex(1.0, 1.0);
         * 
         * Vertex origin8 = new Vertex(5.0, 1.0);
         * Vertex end8 = new Vertex(4.444444, 0.0);
         * Vertex origin9 = new Vertex(6.0, 5.0);
         * Vertex end9 = new Vertex(2.0, 0.0);
         * Vertex origin10 = new Vertex(8.0, 8.0);
         * Vertex end10 = new Vertex(1.0, 1.0);
         * Vertex origin11 = new Vertex(1.0, 1.0);
         * Vertex end11 = new Vertex(0.0, 0.0);
         * Vertex origin12 = new Vertex(1.0, 1.0);
         * Vertex end12 = new Vertex(8.0, 8.0);
         * 
         * 
         * dcel.addPartition(origin1, end1);
         * dcel.addPartition(origin2, end2);
         */
        // dcel.addPartition(origin3, end3);
        // dcel.addPartition(origin4, end4);
        // dcel.addPartition(origin5, end5);
        // dcel.addPartition(origin6, end6);
        // dcel.addPartition(origin7, end7);
        // dcel.addPartition(origin8, end8);
        // dcel.addPartition(origin9, end9);
        // dcel.addPartition(origin10, end10);
        // dcel.addPartition(origin11, end11);
        // dcel.addPartition(origin12, end12);

        /*
         * for (HalfEdge e : dcel.externalEdges) {
         * System.out.println("e: " + e.origin.x + " " + e.origin.y);
         * System.out.println("enext: " +e.next.origin.x + " " + e.next.origin.y);
         * }
         */

        // dcel.computeVisibility(vertices[5]);
        // dcel.iterateThroughEdges();
        // Vertex i = dcel.line_intersect(vertices[8], origin7, a, vertices[1]);
        // System.out.println(i.x + " " + i.y);

        /*
         * for (Face f : dcel.faces) {
         * int c = 0;
         * System.out.println("f" + f.counter);
         * HalfEdge startEdge = f.outerComponent;
         * HalfEdge e = startEdge;
         * System.out.println(e.origin.x + " " + e.origin.y);
         * //System.out.println(e.next.origin.x + " " + e.next.origin.y);
         * 
         * while (!e.next.equals(startEdge)) {
         * c++;
         * e = e.next;
         * System.out.println(e.origin.x + " " + e.origin.y + " " + e.next.origin.x +
         * " " + e.next.origin.y);
         * //System.out.println(startEdge.origin.x + " " + startEdge.origin.y + " " +
         * startEdge.next.origin.x + " " + startEdge.next.origin.y);
         * //if (c>10) break;
         * }
         * 
         * }
         */
    }

}

class Vertex {
    double x, y;
    List<HalfEdge> leaving; // reference to the half-edges leaving this vertex

    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
        this.leaving = new ArrayList<>();
    }

    public void addLeaving(HalfEdge e) {
        this.leaving.add(e);
    }

    public double distanceTo(Vertex other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vertex vertex = (Vertex) obj;
        return Double.compare(vertex.x, x) == 0 && Double.compare(vertex.y, y) == 0;
    }

}

class VertexComparator implements Comparator<Vertex> {
    private Vertex guard;

    public VertexComparator(Vertex guard) {
        this.guard = guard;
    }

    @Override
    public int compare(Vertex v1, Vertex v2) {
        double dist1 = v1.distanceTo(guard);
        double dist2 = v2.distanceTo(guard);

        return Double.compare(dist2, dist1);
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

class Face implements Comparable<Face> {
    HalfEdge outerComponent; // reference to one of the half-edges that bound the face
    Vertex centroid;
    int counter;
    int modem;

    public Face(HalfEdge outerComponent, int counter) {
        this.outerComponent = outerComponent;
        this.counter = counter;
    }

    public void computeCentroid() { // computes the center of the face
        HalfEdge startEdge = outerComponent;
        HalfEdge e = startEdge;
        double x = e.origin.x;
        double y = e.origin.y;
        int count = 1;

        while (!e.next.equals(startEdge)) {
            e = e.next;
            x += e.origin.x;
            y += e.origin.y;
            count++;
        }

        x /= count;
        y /= count;

        Vertex c = new Vertex(x, y);
        centroid = c;
    }

    @Override
    public int compareTo(Face other) {
        if (this.centroid.y != other.centroid.y) {
            return Double.compare(this.centroid.y, other.centroid.y);
        } else {
            return Double.compare(this.centroid.x, other.centroid.x);
        }
    }
}

class DCEL {
    List<Vertex> vertices;
    List<Vertex> externalVertices;
    List<HalfEdge> halfEdges;
    List<HalfEdge> externalEdges;
    List<Face> faces;

    public DCEL() {
        vertices = new ArrayList<>();
        externalVertices = new ArrayList<>();
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
        Face face = new Face(null, 1);
        Face outerFace = new Face(null, 0);

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
            v.addLeaving(edge);
            addVertex(v);
            externalVertices.add(v);
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
        outerFace.outerComponent = edges[0].twin;

        addFace(face);
        addFace(outerFace);
    }

    public void iterateThroughEdges() {
        System.out.println("Pontos de cada edge:");
        for (int i = 0; i < halfEdges.size() / 2; i++) {
            HalfEdge h = halfEdges.get(2 * i);
            HalfEdge h1 = null;

            System.out.println("\n" + (i + 1) + "th cycle");

            while (h != h1) {
                h1 = halfEdges.get(2 * i);
                System.out.println("Origin: (" + h.origin.x + ", " + h.origin.y + ")");
                System.out.println("Orgin face: f" + h.incidentFace.counter);
                System.out.println("Twin Origin: (" + h.twin.origin.x + ", " + h.twin.origin.y + ")");
                System.out.println("Twin face: f" + h.twin.incidentFace.counter);
                h = h.next;
            }
        }
    }

    public Vertex seg_intersect(Vertex v1, Vertex v2, Vertex v3, Vertex v4) { // check if two segments intersect
        double m1 = (v2.y - v1.y) / (v2.x - v1.x);
        double m2 = (v4.y - v3.y) / (v4.x - v3.x);
        double b1 = v2.y - m1 * v2.x;
        double b2 = v4.y - m2 * v4.x;

        BigDecimal max_x = new BigDecimal(Math.min(Math.max(v1.x, v2.x), Math.max(v3.x, v4.x)));
        BigDecimal max_y = new BigDecimal(Math.min(Math.max(v1.y, v2.y), Math.max(v3.y, v4.y)));
        BigDecimal min_x = new BigDecimal(Math.max(Math.min(v1.x, v2.x), Math.min(v3.x, v4.x)));
        BigDecimal min_y = new BigDecimal(Math.max(Math.min(v1.y, v2.y), Math.min(v3.y, v4.y)));

        int decimalPlaces = 6;

        max_x = max_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
        max_y = max_y.setScale(decimalPlaces, RoundingMode.HALF_UP);
        min_x = min_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
        min_y = min_y.setScale(decimalPlaces, RoundingMode.HALF_UP);
        // System.out.println("a");

        /*
         * System.out.println("v1: (" + v1.x + "," + v1.y + ")");
         * System.out.println("v2: (" + v2.x + "," + v2.y + ")");
         * System.out.println("v3: (" + v3.x + "," + v3.y + ")");
         * System.out.println("v4: (" + v4.x + "," + v4.y + ")");
         * System.out.println("m: (" + b1 + "," + b2 + ")");
         * System.out.println("b: (" + b2 + "," + b1 + ")");
         */

        // check if one vertical
        if (v2.x == v1.x) {
            if (v3.x == v4.x) {
                return null;
            } // if both vertical discard (either concurrent or dont intersect - neither
              // useful)
            BigDecimal y = new BigDecimal(m2 * v1.x + b2);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v1.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue()
                    && i.y >= min_y.doubleValue()) {
                return i;
            }
            return null;
        }

        if (v4.x == v3.x) {
            if (v1.x == v2.x) {
                return null;
            } // if both vertical discard (either concurrent or dont intersect - neither
              // useful)
            BigDecimal y = new BigDecimal(m1 * v3.x + b1);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v3.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue()
                    && i.y >= min_y.doubleValue()) {
                return i;
            }
            return null;
        }

        // for any other case
        double crossx = 0;
        if (m1 == m2) {
            return null;
        } else if (b1 != b2) {
            crossx = (b2 - b1) / (m1 - m2);
        }
        BigDecimal roundedCrossx = new BigDecimal(crossx);
        roundedCrossx = roundedCrossx.setScale(decimalPlaces, RoundingMode.HALF_UP);
        BigDecimal crossy = new BigDecimal((m1 * crossx + b1));
        crossy = crossy.setScale(decimalPlaces, RoundingMode.HALF_UP);
        Vertex i = new Vertex(roundedCrossx.doubleValue(), crossy.doubleValue());
        /*
         * System.out.println("i: (" + crossx + "," + crossy + ")");
         * System.out.println(crossx <= Math.min(Math.max(v1.x,v2.x),
         * Math.max(v3.x,v4.x)));
         * System.out.println(crossx >= Math.max(Math.min(v1.x,v2.x),
         * Math.min(v3.x,v4.x)));
         * System.out.println(crossy <= Math.min(Math.max(v1.y,v2.y),
         * Math.max(v3.y,v4.y)));
         * System.out.println(crossy >= Math.max(Math.min(v1.y,v2.y),
         * Math.min(v3.y,v4.y)));
         */

        if (roundedCrossx.doubleValue() <= max_x.doubleValue() && roundedCrossx.doubleValue() >= min_x.doubleValue()
                && crossy.doubleValue() <= max_y.doubleValue() && crossy.doubleValue() >= min_y.doubleValue()) {
            return i;
        }
        return null;
    }

    public Vertex line_intersect(Vertex v1, Vertex v2, Vertex v3, Vertex v4) { // check if line intersects segment
        double m1 = (v2.y - v1.y) / (v2.x - v1.x);
        double m2 = (v4.y - v3.y) / (v4.x - v3.x);
        double b1 = v2.y - m1 * v2.x;
        double b2 = v4.y - m2 * v4.x;

        BigDecimal max_x = new BigDecimal(Math.max(v3.x, v4.x));
        BigDecimal max_y = new BigDecimal(Math.max(v3.y, v4.y));
        BigDecimal min_x = new BigDecimal(Math.min(v3.x, v4.x));
        BigDecimal min_y = new BigDecimal(Math.min(v3.y, v4.y));

        int decimalPlaces = 6;

        max_x = max_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
        max_y = max_y.setScale(decimalPlaces, RoundingMode.HALF_UP);
        min_x = min_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
        min_y = min_y.setScale(decimalPlaces, RoundingMode.HALF_UP);

        // System.out.println("v1: (" + v1.x + "," + v1.y + ")");
        // System.out.println("v2: (" + v2.x + "," + v2.y + ")");
        // System.out.println("v3: (" + v3.x + "," + v3.y + ")");
        // System.out.println("v4: (" + v4.x + "," + v4.y + ")");
        // System.out.println("m: (" + m1 + "," + m2 + ")");
        // System.out.println("b: (" + b1 + "," + b2 + ")");

        if (v1.x == v2.x) { // check if line is vertical
            if (v3.x == v4.x) {
                return null;
            } // if both vertical discard (either concurrent or dont intersect - neither
              // useful)
            BigDecimal y = new BigDecimal(m2 * v1.x + b2);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v1.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue()
                    && i.y >= min_y.doubleValue()) {
                // System.out.println("i: (" + i.x + "," + i.y + ")");
                return i;
            }
            return null;
        }

        if (v3.x == v4.x) { // check if segment is vertical
            if (v1.x == v2.x) {
                return null;
            } // if both vertical discard (either concurrent or dont intersect - neither
              // useful)
            BigDecimal y = new BigDecimal(m1 * v3.x + b1);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v3.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue()
                    && i.y >= min_y.doubleValue()) {
                // System.out.println("i: (" + i.x + "," + i.y + ")");
                // System.out.println(Math.round((m1 * v3.x + b1)));
                return i;
            }
            return null;
        }

        // for any other case
        double crossx = 0;
        if (m1 == m2) {
            return null;
        } else if (b1 != b2) {
            crossx = (b2 - b1) / (m1 - m2);
        }
        BigDecimal roundedCrossx = new BigDecimal(crossx);
        roundedCrossx = roundedCrossx.setScale(decimalPlaces, RoundingMode.HALF_UP);
        BigDecimal crossy = new BigDecimal((m1 * crossx + b1));
        crossy = crossy.setScale(decimalPlaces, RoundingMode.HALF_UP);
        Vertex i = new Vertex(roundedCrossx.doubleValue(), crossy.doubleValue());

        // System.out.println("i: (" + crossx + "," + crossy + ")");
        // System.out.println(crossx <= Math.min(Math.max(v1.x,v2.x),
        // Math.max(v3.x,v4.x)));
        // System.out.println(crossx >= Math.max(Math.min(v1.x,v2.x),
        // Math.min(v3.x,v4.x)));
        // System.out.println(crossy <= Math.min(Math.max(v1.y,v2.y),
        // Math.max(v3.y,v4.y)));
        // System.out.println(crossy >= Math.max(Math.min(v1.y,v2.y),
        // Math.min(v3.y,v4.y)));*/

        if (roundedCrossx.doubleValue() <= max_x.doubleValue() && roundedCrossx.doubleValue() >= min_x.doubleValue()
                && crossy.doubleValue() <= max_y.doubleValue() && crossy.doubleValue() >= min_y.doubleValue()) {
            return i;
        }
        return null;
    }

    public static boolean areCollinear(Vertex a, Vertex b, Vertex c) {
        // Calculate the cross product of vectors formed by points (a, b) and (a, c)
        double crossProduct = (b.y - a.y) * (c.x - a.x) - (c.y - a.y) * (b.x - a.x);

        // If cross product is zero, points are collinear
        return crossProduct == 0;
    }

    public void addPartition(Vertex origin, Vertex end) {
        addVertex(origin);
        /*
         * 
         * List<HalfEdge> intersectionsOg = new ArrayList<>();
         * List<HalfEdge> intersectionsEnd = new ArrayList<>();
         * for (HalfEdge e : externalEdges) {
         * Vertex i = seg_intersect(origin, end, e.origin, e.next.origin);
         * //System.out.println("e: (" + e.origin.x + "," + e.origin.y);
         * //System.out.println("e next: (" + e.next.origin.x + "," + e.next.origin.y +
         * ")");
         * if (i != null && i.x == origin.x && i.y == origin.y) {
         * intersectionsOg.add(e);
         * }
         * else if (i != null) {
         * intersectionsEnd.add(e);
         * }
         * }
         * 
         * for (HalfEdge e1 : intersectionsOg) { //case if partition is applied only to
         * one face - review this
         * for (HalfEdge e2 : intersectionsEnd) {
         * if (e1.incidentFace.equals(e2.incidentFace) && e1.incidentFace.counter!=0) {
         * h = e1;
         * break;
         * }
         * }
         * }
         * 
         * int index = 0;
         * while (h == null) {
         * HalfEdge i = intersectionsOg.get(index);
         * if (i.incidentFace.counter!=0) {
         * h = i;
         * System.out.println(i.incidentFace.counter);
         * }
         * index++;
         * }
         */

        HalfEdge h = null;
        Face f = null;

        HalfEdge stop = null;
        List<HalfEdge> intersectionsOg = new ArrayList<>();

        for (HalfEdge e : externalEdges) {
            Vertex i = seg_intersect(origin, end, e.origin, e.next.origin);
            if (i != null && i.x == origin.x && i.y == origin.y) {
                intersectionsOg.add(e);
            }
        }

        int max_intersect = 0;
        for (HalfEdge e : intersectionsOg) {
            int intersections = 1;
            HalfEdge start = e;
            System.out.println(start.origin.x + " " + start.origin.y);
            while (e.next != start) {
                e = e.next;
                if (seg_intersect(origin, end, e.origin, e.next.origin) != null) {
                    intersections++;
                    System.out.println(e.origin.x + " " + e.origin.y);
                }
            }
            if (intersections > max_intersect) {
                max_intersect = intersections;
                h = start;
            }

        }

        /*
         * for (HalfEdge e : externalEdges) {
         * Vertex i = seg_intersect(origin, end, e.origin, e.next.origin);
         * if (i != null && i.x == end.x && i.y == end.y) {
         * stop = e.twin;
         * break;
         * }
         * }
         * System.out.println(stop);
         * 
         * for (int j=0; j<externalEdges.size(); j++) {
         * System.out.println(stop.origin.x + " " + stop.origin.y);
         * stop = stop.next;
         * Vertex i = seg_intersect(origin, end, stop.origin, stop.next.origin);
         * if (i!=null && i.x == origin.x && i.y == origin.y) {
         * System.out.println("a" + stop.next.origin.x + " " + stop.next.origin.y);
         * h = stop.twin;
         * if (i.x == stop.next.origin.x && i.y == stop.next.origin.y) {
         * h = stop.next.twin;
         * }
         * break;
         * }
         * }
         */

        System.out.println("new");
        System.out.println("h: (" + h.origin.x + "," + h.origin.y + ")");
        System.out.println("hnext: (" + h.next.origin.x + "," + h.next.origin.y + ")");
        System.out.println("hnext: (" + h.next.next.origin.x + "," + h.next.next.origin.y + ")");

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
            System.out.println(f.counter + "f");
            addFace(f);
        }

        else {
            HalfEdge nexth = new HalfEdge(origin);
            origin.addLeaving(nexth);
            nexth.next = h.next;
            h.next.prev = nexth;
            h.next = null; // set later
            nexth.prev = null; // set later

            HalfEdge nexth_twin = new HalfEdge(h.twin.origin);
            h.twin.origin.addLeaving(nexth_twin);
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
            externalEdges.add(nexth);

            h = nexth;
            System.out.println(h.origin.x + " " + h.origin.y);
            System.out.println("b" + h.next.origin.x + " " + h.next.origin.y);
        }

        Vertex intersection = origin;
        HalfEdge next = h; // so we can add pointer later
        h = h.next;

        int passOg = 0;

        while (intersection.x != end.x || intersection.y != end.y) {
            Vertex i = seg_intersect(origin, end, h.origin, h.next.origin);
            // System.out.println(h.origin.x + " " + h.origin.y);
            // System.out.println("a" + h.next.origin.x + " " + h.next.origin.y);

            if (i != null) {
                if (i.x == origin.x && i.y == origin.y) {
                    passOg = 1;
                }
                // System.out.println("i: "+ i.x + " " + i.y);
                intersection = i;
                addVertex(i); // check if vertex doesnt exist yet
                h.incidentFace = f;
                HalfEdge newh = new HalfEdge(i);
                HalfEdge newh_twin = new HalfEdge(next.origin);
                i.addLeaving(newh);
                next.origin.addLeaving(newh_twin);

                if ((i.x != h.next.origin.x || i.y != h.next.origin.y) && (i.x != h.origin.x || i.y != h.origin.y)) {
                    System.out.println("used 1");
                    HalfEdge nexth = new HalfEdge(i); // add halfedge above intersection point
                    i.addLeaving(nexth);
                    nexth.incidentFace = prev.incidentFace;
                    nexth.incidentFace.outerComponent = nexth;
                    nexth.next = h.next;
                    h.next.prev = nexth;

                    HalfEdge h_twin = new HalfEdge(i); // add twin halfedge to h (below intersection point)
                    i.addLeaving(h_twin);
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
                    f.outerComponent = newh;
                    h.next = newh;
                    newh.prev = h;
                    newh.next = next;
                    next.prev = newh;

                    newh_twin.incidentFace = prev.incidentFace;
                    System.out.println("prev: " + prev.origin.x + " " + prev.origin);
                    newh_twin.incidentFace.outerComponent = newh_twin;
                    newh_twin.prev = prev;
                    prev.next = newh_twin;
                    newh_twin.next = nexth;
                    nexth.prev = newh_twin;
                    newh_twin.twin = newh;
                    newh.twin = newh_twin;

                    if (intersection.x != end.x || intersection.y != end.y) { // check if next is outer face
                        f = new Face(h_twin, faces.size());
                        addFace(f);
                    } else {
                        f = nexth.twin.incidentFace;
                        nexth.twin.next = h_twin;
                        h_twin.prev = nexth.twin;
                        externalEdges.add(nexth);
                        // System.out.println("added: " + nexth.origin.x + " " + nexth.origin.y);
                    }

                    addEdge(newh);
                    addEdge(newh_twin);
                    prev = nexth.twin;
                    h = h_twin;

                } else if (i.x != h.origin.x || i.y != h.origin.y) {
                    // System.out.println("used 2");

                    if (i.x != origin.x || i.y != origin.y || passOg == 0) {
                        newh_twin.prev = prev;
                        newh_twin.incidentFace = prev.incidentFace;
                        System.out.println("f" + prev.incidentFace.counter);
                        newh_twin.incidentFace.outerComponent = newh_twin;
                        prev.next = newh_twin;
                        newh_twin.next = h.next;
                        // System.out.println(h.next.origin.x + " " + h.next.origin.y + " " +
                        // h.next.next.origin.x + " " + h.next.next.origin.y);
                        h.next.prev = newh_twin;
                        newh_twin.twin = newh;
                        newh.twin = newh_twin;

                        newh.incidentFace = f;
                        f.outerComponent = newh;
                        h.next = newh;
                        newh.prev = h;
                        newh.next = next;
                        next.prev = newh;
                        addEdge(newh);
                        addEdge(newh_twin);
                    }

                    h = h.twin.prev.twin; // recheck if this makes sense for intersection at (internal) vertex

                    if ((i.x != end.x || i.y != end.y) && (i.x != origin.x || i.y != origin.y)) {
                        f = new Face(h, faces.size());
                        addFace(f);
                    } else {
                        f = h.incidentFace;
                    }
                    prev = h.prev;
                } else {
                    System.out.println("used 3");
                    newh.incidentFace = f;
                    f.outerComponent = newh;
                    h.prev.next = newh;
                    newh.prev = h.prev;
                    newh.next = next;
                    next.prev = newh;

                    newh_twin.prev = prev;
                    newh_twin.incidentFace = prev.incidentFace;
                    newh_twin.incidentFace.outerComponent = newh_twin;
                    prev.next = newh_twin;
                    newh_twin.next = h;
                    h.prev = newh_twin;
                    newh_twin.twin = newh;
                    newh.twin = newh_twin;
                    addEdge(newh);
                    addEdge(newh_twin);

                    h = h.prev.twin.prev.twin; // recheck if this makes sense for intersection at (internal) vertex

                    if (i.x != end.x || i.y != end.y) {
                        f = new Face(h, faces.size());
                        addFace(f);
                    } else {
                        f = h.incidentFace;
                    }
                    prev = h.prev;

                }

                next = h;
            }

            h.incidentFace = f;
            // f.outerComponent = h;
            h = h.next;
        }
    }

    public void computeVisibility(Vertex guard) {
        HalfEdge guardEdge = null;
        for (HalfEdge e : externalEdges) {
            if (e.origin.x == guard.x && e.origin.y == guard.y) {
                guardEdge = e;
                break;
            }
        }

        List<Vertex> endIntersect = new ArrayList<>();

        // System.out.println("a");
        for (Vertex v : externalVertices) {
            // BigDecimal max_x = new BigDecimal(Math.max(guard.x,v.x));
            // BigDecimal min_x = new BigDecimal(Math.min(guard.x,v.x));

            // int decimalPlaces = 6;

            // max_x = max_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
            // min_x = min_x.setScale(decimalPlaces, RoundingMode.HALF_UP);

            // System.out.println("b");
            List<Vertex> intersections = new ArrayList<>();
            if (v.x != guard.x || v.y != guard.y) {
                System.out.println("v: " + v.x + " " + v.y);
                HalfEdge start = null;
                for (HalfEdge edge : externalEdges) {
                    Vertex i = line_intersect(guard, v, edge.origin, edge.next.origin);
                    if (i != null && i.x == v.x && i.y == v.y) {
                        start = edge;
                    } else if (i != null && ((guard.y > v.y && i.y < v.y) || (guard.y < v.y && i.y > v.y))) { // intersecao
                                                                                                              // com uma
                                                                                                              // parede
                        if (guard.x != v.x) { // ignorar casos de particoes verticais (ja contabilizadas)
                            intersections.add(i);
                        }

                    }
                }
                Set<double[]> numIntersect = new HashSet<double[]>();
                HalfEdge edge = start.next;
                while (edge != start) {
                    Vertex i = line_intersect(guard, v, edge.origin, edge.next.origin);
                    if (i != null) {
                        double[] vCoord = { i.x, i.y };
                        numIntersect.add(vCoord);
                    }
                    edge = edge.next;
                }
                if (numIntersect.size() > 1) {
                    intersections.add(v);
                    System.out.println("i: " + v.x + " " + v.y);
                }
                // intersections.add(v);
                // System.out.println("i: " + v.x + " " + v.y);
            }
            Collections.sort(intersections, new VertexComparator(guard));
            if (intersections.size() > 1 && intersections.get(0).x == intersections.get(1).x
                    && intersections.get(0).y == intersections.get(1).y)
                intersections.remove(0);
            // System.out.println("d " + intersections.size());
            for (Vertex vi : intersections) {
                System.out.println("a" + vi.x + " " + vi.y);
            }

            for (int i = 0; i < intersections.size() / 2; i++) {
                Vertex i1 = intersections.get(2 * i);
                Vertex i2 = intersections.get(2 * i + 1);
                System.out.println("e");
                boolean edgeExists = false;

                for (HalfEdge e : halfEdges) {
                    // System.out.println("e: " + e.origin.x + " " + e.origin.y + " " +
                    // e.next.origin.x + " " + e.next.origin.y);
                    if (e.origin.x == i2.x && e.origin.y == i2.y && e.next.origin.x == i1.x
                            && e.next.origin.y == i1.y) { // check if partition already exists
                        edgeExists = true;
                        break;
                    }

                }

                // falta condição para contabilizar só cortes depois do vertice corrente

                if ((i1.x != i2.x || i1.y != i2.y) && !edgeExists) {
                    System.out.println("partition: " + i2.x + " " + i2.y + " " + i1.x + " " + i1.y);
                    addPartition(i2, i1);
                    endIntersect.add(i1);
                }
            }
        }

        /// *
        for (Face f : faces) {
            int c = 0;
            if (f.counter != 0) {
                System.out.println("f" + f.counter);
                HalfEdge startEdge = f.outerComponent;
                HalfEdge e = startEdge;
                System.out.println(e.origin.x + " " + e.origin.y);

                while (!e.next.equals(startEdge)) {
                    c++;
                    e = e.next;
                    System.out.println(e.origin.x + " " + e.origin.y);
                    if (c > 20)
                        break;
                }
            }
        } // */

        // problems: need to redo condition para contabilizar só cortes depois do
        // vertice corrente - not contabilizing the first cut
        // also partition not being done well, two faces registered as the same.. should
        // be fine now

        /// *
        for (Face f : faces) {
            if (f.counter != 0) {
                // System.out.println("e ");
                f.computeCentroid(); // calcular direito centroid
                // System.out.println("e " + f.centroid);
                Vertex c = f.centroid;
                // System.out.println("c: " + c.x + " " + c.y);

                List<Vertex> intersections = new ArrayList<>();
                for (HalfEdge e : externalEdges) {
                    Vertex i = seg_intersect(guard, c, e.origin, e.next.origin);
                    if (i != null && (i.x != guard.x || i.y != guard.y) && (i.x != c.x || i.y != c.y)) {
                        intersections.add(i);
                        // System.out.println("i1: " + i.x + " " + i.y);
                        // System.out.println("e: " + e.origin.x + " " + e.origin.y);
                        // System.out.println("enext: " + e.next.origin.x + " " + e.next.origin.y);
                        // System.out.println("e: " + c.x + " " + c.y);
                        // System.out.println("e: " + e.origin.x + " " + e.origin.y);
                        // System.out.println("e: " + e.next.origin.x + " " + e.next.origin.y);
                    }
                }

                f.modem = intersections.size();
                if (f.modem % 2 != 0) {
                    f.modem++;
                }
                System.out.println("f" + f.counter + " modem: " + f.modem);
            } else {
                f.modem = 10000000;
            }
        } // */
    }

    public List<Vertex> mergeFaces(int k) {
        List<Face> modemFaces = new ArrayList<>();
        List<Vertex> visRegion = new ArrayList<>();

        for (Face f : faces) {
            if (f.modem <= k) {
                modemFaces.add(f); // faces com o modem pedido
            }
        }

        if (modemFaces.size() == 0) {
            System.out.println("No regions have this visibility");
            return null;
        }

        Collections.sort(modemFaces, new FaceComparator()); // quero começar da face mais a cima (ou mais à direita em
                                                            // caso de empate) para garantir que tenho pelo menos 1
                                                            // fronteira com uma face de modem > k
        int region = 0;

        while (modemFaces.size() != 0) {
            region++;
            Face startF = modemFaces.get(0);
            modemFaces.remove(0);

            HalfEdge startE = startF.outerComponent;
            boolean isBarrier = false;

            while (isBarrier == false) {
                if (startE.twin.incidentFace.modem > k) { // encontrar uma tal fronteira para iniciar o algoritmo
                    isBarrier = true;
                }
                startE = startE.next;
            }

            HalfEdge e = startE.next;

            while (e != startE) {
                if (e.twin.incidentFace.modem <= k) {
                    e.prev.next = e.twin.next; // alterar apontadores de uma face par a outra (dar merge)
                    e.twin.next.prev = e.prev;
                    e.next.prev = e.twin.prev;
                    e.twin.prev.next = e.next;
                    e = e.twin.next;
                    halfEdges.remove(e.prev.twin); // apagar edges comuns
                    halfEdges.remove(e.prev);
                    modemFaces.remove(e.incidentFace);
                } else {
                    e = e.next;
                }
                e.incidentFace = e.prev.incidentFace;
            }

            visRegion.add(startE.origin);
            // System.out.println("(" + startE.origin.x + "," + startE.origin.y + ") ");
            e = startE.next;
            while (e != startE) {
                if (visRegion.size() >= 2 && areCollinear(visRegion.get(visRegion.size() - 2),
                        visRegion.get(visRegion.size() - 1), e.origin)) {
                    visRegion.remove(visRegion.get(visRegion.size() - 1));
                }
                visRegion.add(e.origin);
                // System.out.println("(" + e.origin.x + "," + e.origin.y + ") ");
                e = e.next;
            }

            if (visRegion.size() >= 2 && areCollinear(visRegion.get(visRegion.size() - 2),
                    visRegion.get(visRegion.size() - 1), visRegion.get(0))) {
                visRegion.remove(visRegion.get(visRegion.size() - 1));
            }

            System.out.println("Region " + region + ": ");
            for (Vertex v : visRegion) {
                System.out.println("(" + v.x + "," + v.y + ") ");
            }
            System.out.println();

        }
        return visRegion;
    }
}