import java.util.ArrayList;
import java.util.List;


public class TAA{
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
        dcel.iterateThroughEdges();
        // System.out.println("Testing for edge 1: Previous origin " +
        // dcel.halfEdges.get(1).prev.origin.x +
        // ", " + dcel.halfEdges.get(1).prev.origin.y); // exemplo da edge 1
        // System.out.println("Testing for edge 1: Next origin " +
        // dcel.halfEdges.get(1).next.origin.x + ",
        // " + dcel.halfEdges.get(1).next.origin.y);
         //Vertex test = new Vertex(5.0, 1.0);
         //System.out.println(isPointInsideRectangle(test,
         //new Vertex[] { dcel.vertices.get(4), dcel.vertices.get(5),
         //dcel.vertices.get(6),
         //dcel.vertices.get(7) }));
    }

    public static boolean isPointInsideRectangle(Vertex point, Vertex[] rectangle) { // verifica se um ponto está dentro de ret
        return (point.x >= rectangle[0].x && point.x <= rectangle[2].x && point.y >= rectangle[0].y
                && point.y <= rectangle[2].y);
    }
}

class Vertex {
    double x, y;
    // HalfEdge leaving; // reference to one of the half-edges leaving this vertex

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
    List<Face> faces;

    public DCEL() {
        vertices = new ArrayList<>();
        halfEdges = new ArrayList<>();
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
            twinEdge.incidentFace = null; // Will be set later
            edges[i] = edge;
            // v.leaving = edge;
            addVertex(v);
            addEdge(edge);
        }

        // Linking next and previous half-edges
        for (int i = 0; i < n; i++) {
            edges[i].next = edges[(i + 1) % n];
            edges[(i + 1) % n].prev = edges[i];
        }

        // Set outer component for the face
        face.outerComponent = edges[0];
        facesArray[0] = face;

        // Add vertexes to DCEL
        for (Vertex v : polygonVertices) {
            addVertex(v);
        }

        // Add edges to DCEL
        for (HalfEdge e : edges) {
            addEdge(e);
        }

        // Add faces to DCEL
        for (Face f : facesArray) {
            addFace(f);
        }
    }

    public void iterateThroughEdges() {
        System.out.println("Pontos de cada edge:");
        for (HalfEdge edge : halfEdges) {
            System.out.println("Origin: (" + edge.origin.x + ", " + edge.origin.y + ")");
            System.out.println("Twin Origin: (" + edge.twin.origin.x + ", " + edge.twin.origin.y + ")");
        }
    }
}
