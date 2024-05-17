import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public Vertex seg_intersect(Vertex v1, Vertex v2, Vertex v3, Vertex v4) { //check if two segments intersect
        double m1 = (v2.y - v1.y) / (v2.x - v1.x);
        double m2 = (v4.y - v3.y) / (v4.x - v3.x);
        double b1 = v2.y - m1 * v2.x;
        double b2 = v4.y - m2 * v4.x;

        BigDecimal max_x = new BigDecimal(Math.min(Math.max(v1.x,v2.x), Math.max(v3.x,v4.x)));
        BigDecimal max_y = new BigDecimal(Math.min(Math.max(v1.y,v2.y), Math.max(v3.y,v4.y)));
        BigDecimal min_x = new BigDecimal(Math.max(Math.min(v1.x,v2.x), Math.min(v3.x,v4.x)));
        BigDecimal min_y = new BigDecimal(Math.max(Math.min(v1.y,v2.y), Math.min(v3.y,v4.y)));

        int decimalPlaces = 6;

        max_x = max_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
        max_y = max_y.setScale(decimalPlaces, RoundingMode.HALF_UP);
        min_x = min_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
        min_y = min_y.setScale(decimalPlaces, RoundingMode.HALF_UP);
        //System.out.println("a");

        /*System.out.println("v1: (" + v1.x + "," + v1.y + ")");
        System.out.println("v2: (" + v2.x + "," + v2.y + ")");
        System.out.println("v3: (" + v3.x + "," + v3.y + ")");
        System.out.println("v4: (" + v4.x + "," + v4.y + ")");
        System.out.println("m: (" + b1 + "," + b2 + ")");
        System.out.println("b: (" + b2 + "," + b1 + ")");*/

        //check if one vertical
        if (v2.x == v1.x) {
            if (v3.x == v4.x) {return null;} //if both vertical discard (either concurrent or dont intersect - neither useful)
            BigDecimal y = new BigDecimal(m2 * v1.x + b2);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v1.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue() && i.y >= min_y.doubleValue()) {
                return i;
            }
            return null;
        }

        if (v4.x == v3.x) {
            if (v1.x == v2.x) {return null;} //if both vertical discard (either concurrent or dont intersect - neither useful)
            BigDecimal y = new BigDecimal(m1 * v3.x + b1);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v3.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue() && i.y >= min_y.doubleValue()) {
                return i;
            }
            return null;
        }

        
        //for any other case
        BigDecimal crossx = new BigDecimal(0);
        if (m1 ==m2) {return null;}
        else if (b1 != b2) {crossx = new BigDecimal(((b2 - b1) / (m1 - m2)));}
        crossx = crossx.setScale(decimalPlaces, RoundingMode.HALF_UP);
        BigDecimal crossy = new BigDecimal((m1 * crossx.doubleValue() + b1));
        crossy = crossy.setScale(decimalPlaces, RoundingMode.HALF_UP);
        Vertex i = new Vertex(crossx.doubleValue(), crossy.doubleValue());  
        /*System.out.println("i: (" + crossx + "," + crossy + ")");
        System.out.println(crossx <= Math.min(Math.max(v1.x,v2.x), Math.max(v3.x,v4.x)));
        System.out.println(crossx >= Math.max(Math.min(v1.x,v2.x), Math.min(v3.x,v4.x)));
        System.out.println(crossy <= Math.min(Math.max(v1.y,v2.y), Math.max(v3.y,v4.y)));
        System.out.println(crossy >= Math.max(Math.min(v1.y,v2.y), Math.min(v3.y,v4.y)));*/

        if (crossx.doubleValue() <= max_x.doubleValue() && crossx.doubleValue() >= min_x.doubleValue() && crossy.doubleValue() <= max_y.doubleValue() && crossy.doubleValue() >= min_y.doubleValue()) {
            return i;
        }
        return null;
    }

    public Vertex line_intersect(Vertex v1, Vertex v2, Vertex v3, Vertex v4) { //check if line intersects segment
        double m1 = (v2.y - v1.y) / (v2.x - v1.x);
        double m2 = (v4.y - v3.y) / (v4.x - v3.x);
        double b1 = v2.y - m1 * v2.x;
        double b2 = v4.y - m2 * v4.x;

        BigDecimal max_x = new BigDecimal(Math.max(v3.x,v4.x));
        BigDecimal max_y = new BigDecimal(Math.max(v3.y,v4.y));
        BigDecimal min_x = new BigDecimal(Math.min(v3.x,v4.x));
        BigDecimal min_y = new BigDecimal(Math.min(v3.y,v4.y));

        int decimalPlaces = 6;

        max_x = max_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
        max_y = max_y.setScale(decimalPlaces, RoundingMode.HALF_UP);
        min_x = min_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
        min_y = min_y.setScale(decimalPlaces, RoundingMode.HALF_UP);

        /*System.out.println("v1: (" + v1.x + "," + v1.y + ")");
        System.out.println("v2: (" + v2.x + "," + v2.y + ")");
        System.out.println("v3: (" + v3.x + "," + v3.y + ")");
        System.out.println("v4: (" + v4.x + "," + v4.y + ")");
        System.out.println("m: (" + m1 + "," + m2 + ")");
        System.out.println("b: (" + b1 + "," + b2 + ")");*/

        if (v1.x == v2.x) { //check if line is vertical
            if (v3.x == v4.x) {return null;} //if both vertical discard (either concurrent or dont intersect - neither useful)
            BigDecimal y = new BigDecimal(m2 * v1.x + b2);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v1.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue() && i.y >= min_y.doubleValue()) {
                //System.out.println("i: (" + i.x + "," + i.y + ")");
                return i;
            }
            return null;
        }

        if (v3.x == v4.x) { //check if segment is vertical
            if (v1.x == v2.x) {return null;} //if both vertical discard (either concurrent or dont intersect - neither useful)
            BigDecimal y = new BigDecimal(m1 * v3.x + b1);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v3.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue() && i.y >= min_y.doubleValue()) {
                //System.out.println("i: (" + i.x + "," + i.y + ")");
                //System.out.println(Math.round((m1 * v3.x + b1)));
                return i;
            }
            return null;
        }

        //for any other case
        BigDecimal crossx = new BigDecimal(0);
        if (m1 ==m2) {return null;}
        else if (b1 != b2) {crossx = new BigDecimal(((b2 - b1) / (m1 - m2)));}
        crossx = crossx.setScale(decimalPlaces, RoundingMode.HALF_UP);
        BigDecimal crossy = new BigDecimal((m1 * crossx.doubleValue() + b1));
        crossy = crossy.setScale(decimalPlaces, RoundingMode.HALF_UP);
        Vertex i = new Vertex(crossx.doubleValue(), crossy.doubleValue());   

        /*System.out.println("i: (" + crossx + "," + crossy + ")");
        System.out.println(crossx <= Math.min(Math.max(v1.x,v2.x), Math.max(v3.x,v4.x)));
        System.out.println(crossx >= Math.max(Math.min(v1.x,v2.x), Math.min(v3.x,v4.x)));
        System.out.println(crossy <= Math.min(Math.max(v1.y,v2.y), Math.max(v3.y,v4.y)));
        System.out.println(crossy >= Math.max(Math.min(v1.y,v2.y), Math.min(v3.y,v4.y)));*/

        if (crossx.doubleValue() <= max_x.doubleValue() && crossx.doubleValue() >= min_x.doubleValue() && crossy.doubleValue() <= max_y.doubleValue() && crossy.doubleValue() >= min_y.doubleValue()) {
            return i;
        }
        return null;
    }

    public void addPartition(Vertex origin, Vertex end, boolean h_v) {
        addVertex(origin);


        HalfEdge h = null;
        Face f = null;

        if (h_v == true) {
            for (HalfEdge e : externalEdges) {
                Vertex i = seg_intersect(origin, end, e.origin, e.next.origin);
                //System.out.println("e: (" + e.origin.x + "," + e.origin.y);
                //System.out.println("e next: (" + e.next.origin.x + "," + e.next.origin.y + ")");
                if (i != null && i.x == origin.x && i.y == origin.y) {
                    h = e;
                    break;
                }
            }
        }
        else {
            List<HalfEdge> intersectionsOg = new ArrayList<>();
            List<HalfEdge> intersectionsEnd = new ArrayList<>();
            for (HalfEdge e : externalEdges) {
                Vertex i = seg_intersect(origin, end, e.origin, e.next.origin);
                //System.out.println("e: (" + e.origin.x + "," + e.origin.y);
                //System.out.println("e next: (" + e.next.origin.x + "," + e.next.origin.y + ")");
                if (i != null && i.x == origin.x && i.y == origin.y) {
                    intersectionsOg.add(e);
                }
                else if (i != null) {
                    intersectionsEnd.add(e);
                }
            }
            for (HalfEdge e1 : intersectionsOg) {
                for (HalfEdge e2 : intersectionsEnd) {
                    if (e1.incidentFace.equals(e2.incidentFace)) {
                        h = e1;
                        break;
                    }
                }
            }
        }
        //System.out.println("h: " + h);
        //System.out.println("h: (" + h.origin.x + "," + h.origin.y + ")");
        //System.out.println("end: (" + end.x + "," + end.y + ")");

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
            origin.addLeaving(nexth);
            nexth.next = h.next;
            h.next.prev = nexth;
            h.next = null; //set later
            nexth.prev = null; //set later

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

            h = nexth;
        }

        Vertex intersection = origin;
        HalfEdge next = h; //so we can add pointer later
        h = h.next;

        while (intersection.x != end.x || intersection.y != end.y) {
            Vertex i = seg_intersect(origin, end, h.origin, h.next.origin);

            if (i != null) {
                intersection = i;
                addVertex(i); //check if vertex doesnt exist yet
                h.incidentFace = f;
                HalfEdge newh = new HalfEdge(i);
                HalfEdge newh_twin = new HalfEdge(next.origin);
                i.addLeaving(newh);
                next.origin.addLeaving(newh_twin);

                if ((i.x != h.next.origin.x || i.y != h.next.origin.y) &&  (i.x != h.origin.x || i.y != h.origin.y)) {
                    HalfEdge nexth = new HalfEdge(i); //add halfedge above intersection point
                    i.addLeaving(nexth);
                    nexth.incidentFace = prev.incidentFace;
                    nexth.incidentFace.outerComponent = nexth;
                    nexth.next = h.next;
                    h.next.prev = nexth;

                    HalfEdge h_twin = new HalfEdge(i); //add twin halfedge to h (below intersection point)
                    i.addLeaving(h_twin);
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
                    f.outerComponent = newh;
                    h.next = newh;
                    newh.prev = h;
                    newh.next = next;
                    next.prev = newh;
                    
                    newh_twin.incidentFace = prev.incidentFace;
                    newh_twin.incidentFace.outerComponent = newh_twin;
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
                    newh_twin.incidentFace.outerComponent = newh_twin;
                    prev.next = newh_twin;
                    newh_twin.next = h.next;
                    h.next.prev = newh_twin;
                    newh_twin.twin = newh;
                    newh.twin = newh_twin;

                    newh.incidentFace = f;
                    f.outerComponent = newh;
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
            f.outerComponent = h;
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

        //System.out.println("a");
        for (Vertex v : externalVertices) {
            BigDecimal max_x = new BigDecimal(Math.max(guard.x,v.x));
            BigDecimal min_x = new BigDecimal(Math.min(guard.x,v.x));

            int decimalPlaces = 6;

            max_x = max_x.setScale(decimalPlaces, RoundingMode.HALF_UP);
            min_x = min_x.setScale(decimalPlaces, RoundingMode.HALF_UP);

            //System.out.println("b");
            List<Vertex> intersections = new ArrayList<>();
            int iToVert = 0;
            if (v.x != guard.x || v.y != guard.y) {
                //System.out.println("v: " + v.x + " " + v.y);
                //System.out.println("c");
                HalfEdge edge = guardEdge;
                for (int j=0; j<externalEdges.size(); j++) {
                    edge = externalEdges.get((externalEdges.indexOf(edge) + 1) % externalEdges.size());
                    Vertex i = line_intersect(guard, v, edge.origin, edge.next.origin);
                    if (i != null && (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue())) {
                        iToVert++;
                        //System.out.println("inot: " + i.x + " " + i.y);
                        //System.out.println("what: " + min_x + " " + max_x);
                    }
                    else if (i != null && (i.x > max_x.doubleValue() || i.x < min_x.doubleValue())) {
                        intersections.add(i);
                        //System.out.println("i: " + i.x + " " + i.y);
                        //System.out.println("e: " + edge.origin.x + " " + edge.origin.y);
                        //System.out.println("enext: " + edge.next.origin.x + " " + edge.next.origin.y);
                    }
                }
                if (iToVert % 2 == 0 && intersections.size() != 0) {
                        intersections.add(v);
                        //System.out.println("i: " + v.x + " " + v.y);
                    }
            }
            //System.out.println("d " + intersections.size());

            for (int i=0; i<intersections.size()/2; i++) {
                //System.out.println("e");
                Vertex i1 = intersections.get(2*i);
                Vertex i2 = intersections.get(2*i+1);
                boolean edgeExists = false;

                for (Vertex vert : externalVertices) {
                    //System.out.println("f");
                    if (vert.x == i1.x && vert.y == i1.y) {
                        for (HalfEdge e : vert.leaving) { //check if partition already exists
                            //System.out.println("g");
                            if (e.origin.x == i1.x && e.origin.y == i1.y && e.next.origin.x == i2.x && e.next.origin.y == i2.y) {
                                edgeExists = true;
                                break;
                            }
                        }
                        break;
                    }
                }



                 //falta condição para contabilizar só cortes depois do vertice corrente

                if ((i1.x != i2.x || i1.y != i2.y) && !edgeExists) {
                    //System.out.println("partition: " + i1.x + " " + i1.y + " " + i2.x + " " + i2.y);
                    addPartition(i1, i2, false);
                    endIntersect.add(i1);
                }
            }
        }

        /*for (Face f : faces) {
            if (f.counter !=0) {
                System.out.println("f" + f.counter);
                HalfEdge startEdge = f.outerComponent;
                HalfEdge e = startEdge;
                System.out.println(e.origin.x + " " + e.origin.y);

                while (!e.next.equals(startEdge)) {
                    e = e.next;
                    System.out.println(e.origin.x + " " + e.origin.y);
                }
            }
        }*/

        //problems: need to redo condition para contabilizar só cortes depois do vertice corrente - not contabilizing the first cut
        // also partition not being done well, two faces registered as the same.. should be fine now


        for (Face f : faces) {
            if (f.counter != 0) {
                //System.out.println("e ");
                f.computeCentroid(); //calcular direito centroid
                //System.out.println("e " + f.centroid);
                Vertex c = f.centroid;
                //System.out.println("c: " + c.x + " " + c.y);


                List<Vertex> intersections = new ArrayList<>();
                for (HalfEdge e : externalEdges) {
                    Vertex i = seg_intersect(guard, c, e.origin, e.next.origin);
                    if (i  != null && (i.x != guard.x || i.y != guard.y) && (i.x != c.x || i.y != c.y)) {
                        intersections.add(i);
                        //System.out.println("i1: " + i.x + " " + i.y);
                        //System.out.println("e: " + e.origin.x + " " + e.origin.y);
                        //System.out.println("enext: " + e.next.origin.x + " " + e.next.origin.y);
                        //System.out.println("e: " + c.x + " " + c.y);
                        //System.out.println("e: " + e.origin.x + " " + e.origin.y);
                        //System.out.println("e: " + e.next.origin.x + " " + e.next.origin.y);
                    }
                }

                f.modem = intersections.size();
                System.out.println("f" + f.counter + " modem: " + f.modem);
            }
        }
    }
}

public class TAA_proj {

    public static void main(String[] args) {
        Vertex[] vertices = new Vertex[8];
        vertices[0] = new Vertex(0.0, 0.0);
        vertices[1] = new Vertex(2.0, 0.0);
        vertices[2] = new Vertex(2.0, 2.0);
        vertices[3] = new Vertex(4.0, 2.0);
        vertices[4] = new Vertex(4.0, 4.0);
        vertices[5] = new Vertex(6.0, 4.0);
        vertices[6] = new Vertex(6.0, 6.0);
        vertices[7] = new Vertex(0.0, 6.0);

        DCEL dcel = new DCEL();
        dcel.createDCELFromPolygon(vertices);

        Vertex origin1 = new Vertex(2.0, 2.0);
        Vertex end1 = new Vertex(2.0, 6.0);
        Vertex origin2 = new Vertex(4.0, 4.0);
        Vertex end2 = new Vertex(4.0, 6.0);
        Vertex origin3 = new Vertex(4.0, 4.0);
        Vertex end3 = new Vertex(0.0, 4.0);
        Vertex origin4 = new Vertex(2.0, 2.0);
        Vertex end4 = new Vertex(0.0, 2.0);
        Vertex end5 = new Vertex(2.0, 2.0);
        Vertex origin5 = new Vertex(0.0, 0.0);
        Vertex origin6 = new Vertex(2.0, 2.0);
        Vertex end6 = new Vertex(4.0, 4.0);

        dcel.addPartition(origin1, end1, true);
        dcel.addPartition(origin2, end2, true);
        dcel.addPartition(origin3, end3, true);
        dcel.addPartition(origin4, end4, true);
        //dcel.addPartition(origin5, end5, false);
        //dcel.addPartition(origin6, end6, false);
        dcel.computeVisibility(vertices[6]);
        //dcel.iterateThroughEdges();
        //Vertex i = dcel.line_intersect(vertices[6], origin5, vertices[1], vertices[2]);
        //System.out.println(i.x + " " + i.y);

        
        /*for (Face f : dcel.faces) {
            System.out.println("f" + f.counter);
            HalfEdge startEdge = f.outerComponent;
            HalfEdge e = startEdge;
            System.out.println(e.origin.x + " " + e.origin.y);

            while (!e.next.equals(startEdge)) {
                e = e.next;
                System.out.println(e.origin.x + " " + e.origin.y);
            }
        }*/
    }


}
