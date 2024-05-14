import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

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
    int counter;

    public Face(HalfEdge outerComponent, int counter) {
        this.outerComponent = outerComponent;
        this.counter = counter;
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

    // Method to create a DCEL from an array of vertices representing a polygon in counter-clockwise order
    public void createDCELFromPolygon(Vertex[] polygonVertices) {
        int n = polygonVertices.length;
        HalfEdge[] edges = new HalfEdge[n];
        Face face = new Face(null, 1);
        Face outerFace = new Face (null, 0);

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

        addFace(face);
        addFace(outerFace);
    }

    public void iterateThroughEdges() {
        System.out.println("Pontos de cada edge:");
        for (int i=0; i<halfEdges.size()/2; i++) {
            HalfEdge h = halfEdges.get(2*i);
            HalfEdge h1 = null;

            System.out.println("\n" + (i+1) + "th cycle");

            while (h != h1) { 
                h1 = halfEdges.get(2*i);
                System.out.println("Origin: (" + h.origin.x + ", " + h.origin.y + ")");
                System.out.println("Orgin face: f" + h.incidentFace.counter);
                System.out.println("Twin Origin: (" + h.twin.origin.x + ", " + h.twin.origin.y + ")");
                System.out.println("Twin face: f" + h.twin.incidentFace.counter);
                h = h.next;
            }
        }
    }

    public Vertex intersection(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        double m1 = (v2.y - v1.y) / (v2.x - v1.x);
        double m2 = (v4.y - v3.y) / (v4.x - v3.x);
        double b1 = v2.y - m1 * v2.x;
        double b2 = v4.y - m2 * v4.x;

        //check if one vertical
        if (v2.x == v1.x) {
            Vertex i = new Vertex(v1.x, m2 * v1.x + b2);
            if (i.x <= Math.min(Math.max(v1.x,v2.x), Math.max(v3.x,v4.x)) && i.x >= Math.max(Math.min(v1.x,v2.x), Math.min(v3.x,v4.x)) && i.y <= Math.min(Math.max(v1.y,v2.y), Math.max(v3.y,v4.y)) && i.y >= Math.max(Math.min(v1.y,v2.y), Math.min(v3.y,v4.y))) {
                return i;
            }
            else {
                return null;
            }
        }

        if (v4.x == v3.x) {
            Vertex i = new Vertex(v3.x, m1 * v3.x + b1);
            if (i.x <= Math.min(Math.max(v1.x,v2.x), Math.max(v3.x,v4.x)) && i.x >= Math.max(Math.min(v1.x,v2.x), Math.min(v3.x,v4.x)) && i.y <= Math.min(Math.max(v1.y,v2.y), Math.max(v3.y,v4.y)) && i.y >= Math.max(Math.min(v1.y,v2.y), Math.min(v3.y,v4.y))) {
                return i;
            }
            else {
                return null;
            }
        }
        
        //for any other case
        double crossx = (b2 - b1) / (m1 - m2);
        double crossy = m1 * crossx + b1;
        Vertex i = new Vertex(crossx, crossy);

        if (crossx <= Math.min(Math.max(v1.x,v2.x), Math.max(v3.x,v4.x)) && crossx >= Math.max(Math.min(v1.x,v2.x), Math.min(v3.x,v4.x)) && crossy <= Math.min(Math.max(v1.y,v2.y), Math.max(v3.y,v4.y)) && crossy >= Math.max(Math.min(v1.y,v2.y), Math.min(v3.y,v4.y))) {
            return i;
        }
        return null;
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

        HalfEdge prev = h; //so we can add pointer later

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
            h.next = null; //set later
            nexth.prev = null; //set later

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
        HalfEdge next = h; //so we can add pointer later
        h = h.next;

        while (intersection.x != end.x || intersection.y != end.y) {
            Vertex i = intersection(origin, end, h.origin, h.next.origin);

            if (i != null) {
                intersection = i;
                System.out.println(i.x + " " + i.y);
                addVertex(i); //check if vertex doesnt exist yet
                h.incidentFace = f;
                HalfEdge newh = new HalfEdge(i);
                HalfEdge newh_twin = new HalfEdge(next.origin);

                if ((i.x != h.next.origin.x || i.y != h.next.origin.y) &&  (i.x != h.origin.x || i.y != h.origin.y)) {
                    HalfEdge nexth = new HalfEdge(i); //add halfedge above intersection point
                    nexth.incidentFace = prev.incidentFace;
                    nexth.next = h.next;
                    h.next.prev = nexth;

                    HalfEdge h_twin = new HalfEdge(i); //add twin halfedge to h (below intersection point)
                    h_twin.next = h.twin.next;
                    h.twin.next.prev = h_twin;
                    h.twin.next = null; //to be set later
                    h_twin.prev = null; //to be set later
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

                    if (nexth.twin.incidentFace.counter != 0) { //check if next is outer face
                        f = new Face(h_twin, faces.size());
                        addFace(f);
                    }
                    else {
                        f = nexth.twin.incidentFace;
                        nexth.twin.next = h_twin; 
                        h_twin.prev = nexth.twin;
                    }

                    prev = nexth.twin;
                    h = h_twin;
                }
                else {

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

        /*for (HalfEdge e : halfEdges) {
            System.out.println(e.origin.x + " " + e.origin.y);
            System.out.println(e.next.origin.x + " " + e.next.origin.y);
            System.out.println(e.incidentFace.counter);
        }*/
    }
}

public class TAA_proj {

    public static void main(String[] args) {
        Vertex[] vertices = new Vertex[12];
        vertices[0] = new Vertex(2.0, 1.0);
        vertices[1] = new Vertex(4.0, 1.0);
        vertices[2] = new Vertex(4.0, 2.0);
        vertices[3] = new Vertex(6.0, 2.0);
        vertices[4] = new Vertex(6.0, 4.0);
        vertices[5] = new Vertex(5.0, 4.0);
        vertices[6] = new Vertex(5.0, 6.0);
        vertices[7] = new Vertex(3.0, 6.0);
        vertices[8] = new Vertex(3.0, 5.0);
        vertices[9] = new Vertex(1.0, 5.0);
        vertices[10] = new Vertex(1.0, 3.0);
        vertices[11] = new Vertex(2.0, 3.0);

        DCEL dcel = new DCEL();
        dcel.createDCELFromPolygon(vertices);

        Vertex origin1 = new Vertex(3.0, 5.0);
        Vertex end1 = new Vertex(5.0, 5.0);
        Vertex origin2 = new Vertex(2.0, 5.0);
        Vertex end2 = new Vertex(2.0, 3.0);

        dcel.addPartition(origin1, end1);
        dcel.addPartition(origin2, end2);
        dcel.iterateThroughEdges();
    }

}
