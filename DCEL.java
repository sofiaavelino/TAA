import java.util.ArrayList;
import java.util.List;

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
    public void addEdge(Edge e) {
        halfEdges.add(e);
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
            v.leaving = edge;
            addVertex(v);
            addHalfEdge(edge);
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
            addVertex(f);
        }

        // Add edges to DCEL
        for (HalfEdge e : edges) {
            addEdge(f);
        }

        // Add faces to DCEL
        for (Face f : facesArray) {
            addFace(f);
        }
    }
}





/*while we dont get to B:
    edge<- starting_point (A in the beginning)
    old_face<-face(edge)
    origin(next(ha))<- starting_point
    origin(edge)<- starting_point
    next(hA)<-edge
    origin(twin(hA))<- starting_point
    origin(next(twin(edge)))<- starting_point
    newface<-generatenewface
    while edge does not intersect:
        b<-intersection(AB,edge)
        face(edge)<-newface
        edge<-next(edge)
    b<-intersection(AB,edge)
    add_new_edge(b,starting_point,newface,oldface)
    add_new_edge(b,origin(next(edge)),oldface,face(twin(edge)))
    origin(next(edge))<-b
    starting_point<-b*/