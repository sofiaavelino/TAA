import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;
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

    public Face(HalfEdge outerComponent) {
        this.outerComponent = outerComponent;
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
        Face face = new Face(null);
        Face outerFace = new Face (null);
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
        HalfEdge h = halfEdges.get(0);
        HalfEdge h1 = null;

        System.out.println("Fist cycle");

        while (h != h1) { 
            h1 = halfEdges.get(0);
            //System.out.println(h);
            System.out.println("Origin: (" + h.origin.x + ", " + h.origin.y + ")");
            System.out.println("Twin Origin: (" + h.twin.origin.x + ", " + h.twin.origin.y + ")");
            h = h.next;
        }

        h1 = null;
        h = halfEdges.get(9);

        System.out.println("\nSecond cycle");

        while (h != h1) { 
            h1 = halfEdges.get(9);
            //System.out.println(h);
            System.out.println("Origin: (" + h.origin.x + ", " + h.origin.y + ")");
            System.out.println("Twin Origin: (" + h.twin.origin.x + ", " + h.twin.origin.y + ")");
            h = h.next;
        }
    }

    public Vertex intersection(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        double m1 = (v2.y - v1.y) / (v2.x - v1.x);
        double m2 = (v4.y - v3.y) / (v4.x - v3.x);

        double b1 = v2.y - m1 * v2.x;
        double b2 = v4.y - m2 * v4.x;

        if (m1 == m2) {return null;}

        if (Double.isInfinite(m2)) {
            if ((m1 * v3.x + b1) < Math.max(v4.y,v3.y) && (m1 * v3.x + b1) > Math.min(v4.y,v3.y)) {
                Vertex i = new Vertex(v3.x, m1 * v3.x + b1);
                return i;
            }
            else {
                return null;
            }
        }

        else if (Double.isInfinite(m1)) {
            if ((m2 * v1.x + b2) < Math.max(v1.y,v2.y) && (m2 * v1.x + b2) > Math.min(v1.y,v2.y)) {
                Vertex i = new Vertex(v1.x, m2 * v1.x + b2);
                return i;
            }
            else {
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
            if (intersection(origin, end, e.origin, e.next.origin) != null) {
                h = e;
            }
        }

        HalfEdge prev = h; //so we can add pointer later

        if (h.origin == origin) {
            f = new Face(h);
            h.incidentFace = f;
            addFace(f);
        }
        
        else if (h.next.origin == origin) {
            h = h.next;
            f = new Face(h);
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

            f = new Face(nexth);
            nexth.incidentFace = f;
            nexth_twin.incidentFace = h.twin.incidentFace;
            nexth.twin = nexth_twin;
            nexth_twin.twin = nexth;

            addEdge(nexth);
            addEdge(nexth_twin);
            addFace(f);

            h = nexth;
        }

        Vertex intersection = origin;
        HalfEdge next = h; //so we can add pointer later
        h = h.next;

        while (intersection.x != end.x && intersection.y != end.y) {
            Vertex i = intersection(origin, end, h.origin, h.next.origin);

            if (i != null) {
                intersection = i;
                addVertex(i);
                h.incidentFace = f;

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

                HalfEdge newh = new HalfEdge(i);
                newh.incidentFace = f;
                h.next = newh;
                newh.prev = h;
                newh.next = next;
                next.prev = newh;
                
                HalfEdge newh_twin = new HalfEdge(next.origin);
                newh_twin.incidentFace = prev.incidentFace;
                newh_twin.prev = prev;
                prev.next = newh_twin;
                newh_twin.next = nexth;
                nexth.prev = newh_twin;
                newh_twin.twin = newh;
                newh.twin = newh_twin;

                if (nexth.twin.incidentFace.outerComponent != null) {
                    f = new Face(h_twin);
                    addFace(f);
                }
                else {
                    f = nexth.twin.incidentFace;
                }
                h = h_twin;

                addEdge(nexth);
                addEdge(h_twin);
                addEdge(newh);
                addEdge(newh_twin);
            }
            
            h.incidentFace = f;
            h = h.next;
        }
    }
}

public class TAA_proj {

    public static void main(String[] args) {
        Vertex[] vertices = new Vertex[4];
        vertices[0] = new Vertex(0.0, 0.0);
        vertices[1] = new Vertex(3.0, 0.0);
        vertices[2] = new Vertex(3.0, 3.0);
        vertices[3] = new Vertex(0.0, 3.0);

        DCEL dcel = new DCEL();
        dcel.createDCELFromPolygon(vertices);

        Vertex origin = new Vertex(0.0, 1.0);
        Vertex end = new Vertex(3.0, 2.0);

        dcel.addPartition(origin, end);
        dcel.iterateThroughEdges();
    }
}

