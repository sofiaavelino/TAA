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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TAA_proj {

    public static void main(String[] args) throws Exception {
        System.out.println("Insert 0 to generate a permutomino or insert 1 to use a precomputed file");

        Scanner scanner = new Scanner(System.in);
        int userInput = scanner.nextInt();
        scanner.nextLine();
        while (userInput != 0 && userInput != 1) {
            System.out.println("Invalid input. Please try again.");
            userInput = scanner.nextInt();
        }

        String filename = "exemplo_grid";

        if (userInput == 0) {
            System.out.println("Please select the desired number of vertices");

            String polyInput = "1 " + scanner.nextInt();
            scanner.nextLine();
            try {
                // Define the commands to run
                String[] commands = {
                    "gcc -o poly GenPols_RectParts_TAA/swerc_inflate_cut.c",
                    "gcc -o draw GenPols_RectParts_TAA/draw_orthopol.c",
                    "gcc -o grid GenPols_RectParts_TAA/gridnew.c",
                    "./poly > exemplo",
                    "./draw < exemplo > exemplo.tex",
                    "pdflatex exemplo.tex",
                    "./grid < exemplo > exemplo_grid",
                    "./draw < exemplo_grid > exemplo_grid.tex",
                    "pdflatex exemplo_grid.tex"
                };

                // Execute each command
                for (int i = 0; i < 3; i++) {
                    runCommand(commands[i]);
                }

                runCommandWithInput(commands[3], polyInput);
    
                for (int i = 4; i < commands.length; i++) {
                    runCommand(commands[i]);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (userInput == 1) {
            System.out.println("Please insert the name of the file you wish to use:");
            String input = scanner.nextLine();
            filename = input;
        }
        

        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            int n = myReader.nextInt();
            Vertex[] vertices = new Vertex[n];
            List<Vertex[]> partitions = new ArrayList<>();
            HashMap<Vertex,List<Vertex>> nextV = new HashMap<>();
            FileWriter myWriter = new FileWriter("polygon.txt");
           

            for (int i=0; i<n; i++) {
                vertices[i] = new Vertex(myReader.nextDouble(), myReader.nextDouble());
              }

            System.out.println("\nPlease select a guard vertex from the list below and the visibility level you desire. (use format 'x y k'):");
            for (Vertex v : vertices) {
                System.out.print("(" + v.x + "," + v.y + "); ");
            }
            System.out.println();

            int x = scanner.nextInt();
            int y = scanner.nextInt();
            int k_modem = scanner.nextInt();
            scanner.nextLine();

            Vertex guard = null;

            for (Vertex v : vertices) {
                if (v.x == x && v.y == y) {
                    guard = v;
                    break;
                }
            }

            while (guard == null) {
                System.out.println("Invalid guard vertex. Please choose again.");

                x = scanner.nextInt();
                y = scanner.nextInt();
                scanner.nextLine();

                for (Vertex v : vertices) {
                    if (v.x == x && v.y == y) {
                        guard = v;
                        break;
                    }
                }
            }

            while (k_modem % 2 != 0) {
                System.out.println("Invalid visibility, please pick an even number.");
                k_modem = scanner.nextInt();
                scanner.nextLine();
            }

            myWriter.write(guard.x + " " + guard.y + "\n");
            myWriter.write(k_modem + "\n");
            myWriter.write(n + "\n");

            for (int i = 0; i < n; i++) {
                myWriter.write(vertices[i].x + " " + vertices[i].y + "\n");
            }


            DCEL dcel = new DCEL();
            dcel.createDCELFromPolygon(vertices);


            // HERE STARTS THE CODE TO CREATE THE H/V PARTITIONS
            //ADAPTATION WAS NECESSARY AS OUR ADDPARTITION FUNCTION ONLY WORKS WHEN THE EDGES TO ADD HAVE BOTH ENDPOINTS ON THE POLYGON'S EXTERIOR
            while (myReader.hasNextDouble()) {
                Double og_x = myReader.nextDouble();
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
                        exists = true;
                        break;
                    }
                }
                if(exists == false) {
                    List<Vertex> toV = new ArrayList<>();
                    toV.add(end);
                    nextV.put(origin,toV);   
                }
            }


            for  (Vertex v : nextV.keySet()) {
                Vertex origin = v;

                boolean ogInPoly = false;
                for (HalfEdge e : dcel.externalEdges) { // check if end of segment is point in polygon
                    for (Vertex next : nextV.get(v)) {
                        Vertex i = dcel.line_intersect(origin, next, e.origin, e.next.origin);
                        if (i!=null && i.equals(origin)) {
                            ogInPoly = true;
                            break;
                        }
                    }
                }
                if (ogInPoly == false) continue; // origin not in polygon edges

                while (nextV.get(v).size() != 0){ 
                    Vertex end = nextV.get(v).get(0);
                    List<Vertex> vList = nextV.get(v);
                    vList.remove(0); // remove end from list of origin's next vertices
                    nextV.put(v,vList);

                    boolean edgeExists = false;
                    for (int i = 0; i<vertices.length; i++) { // check if edge already exists

                        if (origin.y == end.y && vertices[i].y == vertices[(i+1)%vertices.length].y && end.y == vertices[i].y) {
                            if (Math.max(origin.x,end.x) <= Math.max(vertices[i].x,vertices[(i+1)%vertices.length].x) && Math.min(origin.x,end.x) >= Math.min(vertices[i].x,vertices[(i+1)%vertices.length].x)) {
                                edgeExists = true;
                                break;
                            }
                        }
                        if (origin.x == end.x && vertices[i].x == vertices[(i+1)%vertices.length].x && end.x == vertices[i].x) {
                            if (Math.max(origin.y,end.y) <= Math.max(vertices[i].y,vertices[(i+1)%vertices.length].y) && Math.min(origin.y,end.y) >= Math.min(vertices[i].y,vertices[(i+1)%vertices.length].y)) {
                                edgeExists = true;
                                break;
                            }
                        }
                    }

                    if (edgeExists) continue;

                    boolean reachEdge = false;
                    for (HalfEdge e : dcel.externalEdges) { // check if end of segment is point in polygon edges
                        Vertex i = dcel.line_intersect(origin, end, e.origin, e.next.origin);
                        if (i!=null && i.equals(end)) {
                            reachEdge = true;
                            break;
                        }
                    }

                    Vertex next = null;
                    while (!reachEdge) {
                        List<Vertex> nextVs = null;
                        for (Vertex ver : nextV.keySet()) {
                            if (ver.equals(end)) {
                                nextVs = nextV.get(ver);
                            }
                        }


                        for (Vertex ver: nextVs) {
                            
                            if (v.x == end.x && v.x == ver.x) {
                                if ((end.y > v.y && ver.y > end.y) || (end.y < v.y && ver.y < end.y)) {
                                    next = ver;
                                    end = ver;
                                    break;
                                }
                            }
                            else if (v.y == end.y && v.y == ver.y) {
                                if ((end.x > v.x && ver.x > end.x) || (end.x < v.x && ver.x < end.x)) {
                                    next = ver;
                                    end = ver;
                                    break;
                                }
                            } 
                        }

                        if (next != end) break;

                        for (HalfEdge e : dcel.externalEdges) { // check if end of segment is point in polygon edges
                            Vertex i = dcel.line_intersect(origin, end, e.origin, e.next.origin);
                            if (i!=null && i.equals(end)) {
                                reachEdge = true;
                                break;
                            }
                        }
                    }

                    if (!reachEdge) {continue;}

                    boolean doneP = false;
                    for (Vertex[] p : partitions) {
                        if ((p[0].equals(origin) && p[1].equals(end)) || (p[0].equals(end) && p[1].equals(origin))) { // check if partition has been added before
                            doneP = true;
                            break;
                        }
                    }
                    
                    if (doneP) continue;

                    dcel.addPartition(origin,end);
                    Vertex[] part = {origin, end};
                    partitions.add(part);
                } 
            }


            dcel.computeVisibility(guard);
            dcel.mergeFaces(k_modem, guard, myWriter);
            myWriter.close();
            scanner.close();
            myReader.close();

            executePythonScript("plot.py");

          } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
          }
    }


//------------------------------------------ AUXILIARY SCRIPTS TO EXECUTE PYTHON FILES AND RUN SHELL COMMANDS FROM JAVA ------------------------------------------ //

public static void executePythonScript(String scriptPath) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath);
    processBuilder.inheritIO();
    Process process = processBuilder.start();
}


    private static void runCommand(String command) throws IOException, InterruptedException {
        // Create a ProcessBuilder to run the command
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.redirectErrorStream(true); // Redirect error stream to the output stream

        // Start the process
        Process process = processBuilder.start();

        // Read the output of the command
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // Wait for the process to finish
        int exitCode = process.waitFor();
        System.out.println("Command exited with code: " + exitCode);
    }

    private static void runCommandWithInput(String command, String input) throws IOException, InterruptedException {
        // Create a ProcessBuilder to run the command
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.redirectErrorStream(true); // Redirect error stream to the output stream

        // Start the process
        Process process = processBuilder.start();

        // Provide input to the process
        try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
            writer.write(input);
            writer.flush();
        }

        // Read the output of the command
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // Wait for the process to finish
        int exitCode = process.waitFor();
        System.out.println("Command exited with code: " + exitCode);
    }



}

class Vertex {
    double x, y;

    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Vertex other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static final double EPSILON = 1e-5;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vertex vertex = (Vertex) obj;
        return Math.abs(vertex.x - x) < EPSILON && Math.abs(vertex.y - y) < EPSILON;
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
            return Double.compare(other.centroid.y, this.centroid.y);
        } else {
            return Double.compare(other.centroid.x, this.centroid.x);
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

    // Method to add a new external edge to the DCEL
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
            twinEdge.incidentFace = outerFace;
            edges[i] = edge;
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

    public void addPartition(Vertex origin, Vertex end) {
        if (!vertices.contains(origin)) {
            addVertex(origin);
        }

        HalfEdge h = null;
        Face f = null;

        HalfEdge stop = null;
        List<HalfEdge> intersectionsOg = new ArrayList<>();

        for (HalfEdge e : halfEdges) {
            if (e.incidentFace.counter == 0) continue; //discard HalfEdges incident to external face
            Vertex i = seg_intersect(origin, end, e.origin, e.next.origin);
            if (i != null && i.equals(origin)) {
                intersectionsOg.add(e); // obtain all edges that intersect with the new edge
            }
        }

        int max_intersect = 0;
        for (HalfEdge e : intersectionsOg) {
            int intersections = 1;
            HalfEdge start = e;
            while (e.next != start) { // for each edge that intersects with the segment origin -> end check how many times the latter intersects with the edge's face
                e = e.next;          
                if (seg_intersect(origin, end, e.origin, e.next.origin) != null) {
                    intersections++;
                }
            }
            if (intersections > max_intersect) { // the face with most intersections contains part of origin -> end so we choose the corresponding edge to start
                max_intersect = intersections; 
                h = start;
            }
            
        }

        h.incidentFace = h.next.incidentFace;

        HalfEdge prev = h; //so we can add pointer later

        if (h.origin.equals(origin)) {
            f = new Face(h, faces.size());
            h.incidentFace = f;
            addFace(f);
            prev = h.prev;
        }
        
        else if (h.next.origin.equals(origin)) {
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
            externalEdges.add(nexth);

            h = nexth;
        }

        Vertex intersection = origin;
        HalfEdge next = h; //so we can add pointer later
        h = h.next;           
        
        int passOg = 0;

        while (!intersection.equals(end)) {
            Vertex i = seg_intersect(origin, end, h.origin, h.next.origin);
            if (i != null) {
                if (i.equals(origin)) {passOg ++;}
                intersection = i;
                if (!vertices.contains(i)) {addVertex(i);};
                h.incidentFace = f;
                HalfEdge newh = new HalfEdge(i);
                HalfEdge newh_twin = new HalfEdge(next.origin);

                if (!i.equals(h.next.origin) &&  !i.equals(h.origin)) {
                    HalfEdge nexth = new HalfEdge(i); //add halfedge above intersection point
                    nexth.incidentFace = prev.incidentFace;
                    nexth.incidentFace.outerComponent = nexth;
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

                    if (!intersection.equals(end)) { //check if next is outer face
                        f = new Face(h_twin, faces.size());
                        addFace(f);
                    }
                    else {
                        f = nexth.twin.incidentFace;
                        nexth.twin.next = h_twin; 
                        h_twin.prev = nexth.twin;
                        externalEdges.add(nexth);
                    }

                    addEdge(newh);
                    addEdge(newh_twin);
                    prev = nexth.twin;
                    h = h_twin;

                }
                else {
                    if (i.equals(h.origin)) {h = h.prev;}

                    if (!i.equals(origin) || passOg == 0) {
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
                        addEdge(newh);
                        addEdge(newh_twin);
                    }

                    h = h.twin.prev.twin; 

                    if (!i.equals(end) && !i.equals(origin)) {
                        f = new Face(h, faces.size());
                        addFace(f);
                    }
                    else {
                        f = h.incidentFace;
                    }
                    prev = h.prev;
                }

                next = h;
            }

            h.incidentFace = f;
            h = h.next;
        }
    }

    public void computeVisibility(Vertex guard) {

        List<Vertex> endIntersect = new ArrayList<>();
        List<Vertex[]> partitions = new ArrayList<>();

        for (Vertex v : externalVertices) {

            List<Vertex> intersections = new ArrayList<>();
            if (!v.equals(guard)) { 
                HalfEdge start = null;
                for (HalfEdge edge : externalEdges) {
                    Vertex i = line_intersect(guard, v, edge.origin, edge.next.origin);
                    if (i!=null && i.equals(v)) {
                        start = edge;
                    }
                    else if (i != null && ((guard.y > v.y && i.y < v.y) || (guard.y < v.y && i.y > v.y))) { //intersecao com uma parede
                        intersections.add(i);

                    }
                    else if (i != null && guard.y == i.y) {
                        if ((guard.x > v.x && i.x < v.x) || (guard.x < v.x && i.x > v.x)) {
                            intersections.add(i);
                        }
                    }
                }
                Set<double[]> numIntersect = new HashSet<double[]>(); 
                HalfEdge edge = start.next;
                while (edge != start) {
                    Vertex i = line_intersect(guard, v, edge.origin, edge.next.origin);
                    if (i != null) {
                        double[] vCoord = {i.x,i.y};
                        numIntersect.add(vCoord);
                    }
                    edge = edge.next;
                }
                intersections.add(v);
            }

            Collections.sort(intersections, new VertexComparator(guard)); // sorts the faces in descending order relative to their distance to the guard vertex
            if (intersections.size() > 1 && intersections.get(0).x == intersections.get(1).x && intersections.get(0).y == intersections.get(1).y) {
                intersections.remove(0); // remove repetitions caused by begining at a vertex of the polygon
            } 

            for (int i=0; i<intersections.size()-1; i++) {
                Vertex i1 = intersections.get(i);
                Vertex i2 = intersections.get(i+1);
                boolean edgeExists = false;

                for (Vertex[] vList : partitions) {
                    if ((vList[0].equals(i1) && vList[1].equals(i2)) || (vList[0].equals(i2) && vList[1].equals(i1))) {
                        edgeExists = true; //check if partition was already added
                        break;
                    }
                }

                if (v.x == guard.x || v.y == guard.y) { // vertical or horizontal partitions
                    if (i1.equals(v) || i2.equals(v)) {
                        edgeExists = true;
                            break;
                    }

                    for (HalfEdge e : halfEdges) { 
                        if (e.origin.equals(i2) && e.next.origin.equals(i1)) { //check if edge already exists
                            edgeExists = true;
                            break;
                        }
    
                    }

                    for (Vertex vert : externalVertices) { 
                        if (i2.equals(vert) || i1.equals(vert)) {
                            edgeExists = true;
                            break;
                        }
    
                    }
                }


                boolean edgeInPoly = segInPoly(i2, i1);

                if (!i1.equals(i2) && !edgeExists && edgeInPoly) {
                    addPartition(i2, i1);
                    Vertex[] p = {i1,i2};
                    partitions.add(p);
                    endIntersect.add(i1);
                }
            }
        }
        

        for (Face f : faces) {
            if (f.counter != 0) {
                f.computeCentroid(); //calcular direito centroid
                Vertex c = f.centroid;


                List<Vertex> intersections = new ArrayList<>();
                for (HalfEdge e : externalEdges) {
                    Vertex i = seg_intersect(guard, c, e.origin, e.next.origin);
                    if (i  != null && !i.equals(guard) && !i.equals(c)  && !intersections.contains(i)) {
                        intersections.add(i);
                    }
                }

                f.modem = intersections.size();
                if (f.modem%2!=0) {f.modem++;}
            }
            else {f.modem = 10000000;}
        }
    }

    public void mergeFaces(int k, Vertex guard, FileWriter myWriter) throws Exception {
        List<Face> modemFaces = new ArrayList<>();

        for (Face f : faces) {
            if (f.modem <= k) {
               modemFaces.add(f); // faces com o modem pedido
            }
        }

        if (modemFaces.size() == 0) {
            System.out.println("No regions have this visibility");
            return;
        }

        Collections.sort(modemFaces); // começar da face mais a cima (ou mais à direita em caso de empate) para garantir que tenho pelo menos 1 fronteira com uma face de modem > k
        int region = 0;
        List<Vertex> finalVisRegion = new ArrayList<>();
        List<Integer> finalRegionSizes = new ArrayList<>();

        while (modemFaces.size() != 0) {
            Face startF = modemFaces.get(0);
            modemFaces.remove(0);

            HalfEdge startE = startF.outerComponent;
            HalfEdge edge = startE.next;
            boolean isBarrier = false;
            if (startE.twin.incidentFace.modem > k) {
                isBarrier = true;
            }

            while (isBarrier == false && edge != startE) {
                if (edge.incidentFace != startF) break;
                if (edge.twin.incidentFace.modem > k) { // encontrar uma tal fronteira para iniciar o algoritmo
                    isBarrier = true;
                    startE = edge;
                    break;
                }
                edge = edge.next;
            }


            if (isBarrier == false) {
                continue; // se todos os edges forem adjacentes a uma face de modem <= k, descartar (já contabilizado)
            }

            HalfEdge e = startE.next;

            while (e != startE) {
                if (e.twin.incidentFace.modem <= k) {
                    modemFaces.remove(e.twin.incidentFace);
                    e.prev.next = e.twin.next; // alterar apontadores de uma face para a outra (dar merge)
                    e.prev.incidentFace = startF;
                    e.twin.next.prev = e.prev;
                    e.twin.next.incidentFace = startF;
                    e.next.prev = e.twin.prev;
                    e.next.incidentFace = startF;
                    e.twin.prev.next = e.next;
                    e.twin.prev.incidentFace = startF;

                    HalfEdge rem = e;
                    e = e.twin.next;
                    halfEdges.remove(rem.twin); // apagar edges comuns
                    halfEdges.remove(rem);
                }
                else {
                    e = e.next;
                }
                modemFaces.remove(e.incidentFace);
                e.incidentFace = startF;
            }

            List<Vertex> visRegion = new ArrayList<>();

            visRegion.add(startE.origin);
            e = startE.next;
            while(e != startE) {
                if (visRegion.size() >= 2 && areCollinear(visRegion.get(visRegion.size()-2), visRegion.get(visRegion.size()-1), e.origin)) {
                    visRegion.remove(visRegion.get(visRegion.size()-1));
                }
                visRegion.add(e.origin);
                e = e.next;
            }

            if (visRegion.size() >= 2 && areCollinear(visRegion.get(visRegion.size()-2), visRegion.get(visRegion.size()-1), visRegion.get(0))) {
                visRegion.remove(visRegion.get(visRegion.size()-1));
            }
            if (visRegion.size() >= 2 && areCollinear(visRegion.get(1), visRegion.get(0), visRegion.get(visRegion.size()-1))) {
                visRegion.remove(0);
            }

            int startIndex = 0;
            Vertex startV = visRegion.get(0); 
            for (int i=0; i<visRegion.size(); i++) { // just to start the ccw order from the highest leftmost vertex of the region
                if (visRegion.get(i).x < startV.x) {
                    startIndex = i;
                    startV = visRegion.get(i);
                }
                else if (visRegion.get(i).x == startV.x && visRegion.get(i).y > startV.y) {
                    startIndex = i;
                    startV = visRegion.get(i);
                }
            }

            finalRegionSizes.add(visRegion.size());
            region++;
            System.out.println("\nRegion " + region + ": ");
            for (int i=0; i<visRegion.size(); i++) {
                Vertex v = visRegion.get((startIndex + i) % visRegion.size());
                System.out.println("(" + v.x + "," + v.y + ") ");
                finalVisRegion.add(v);
            }
        }

        myWriter.write(finalRegionSizes.size() + "\n");
        for (int i : finalRegionSizes) {
            myWriter.write(i + "\n");
            for (int j = 0; j < i; j++) {
                myWriter.write(finalVisRegion.get(0).x + " " + finalVisRegion.get(0).y + "\n");
                finalVisRegion.remove(0);
            }
        }
    }


//-------------------------------------------------------- AUXILIARY CODE TO CALCULATE IMPORTANT VALUES -------------------------------------------------------- //



    public void iterateThroughEdges() { // this function was used to test the code in earlier stages
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
        double crossx = 0;
        if (m1 == m2) {return null;}
        else if (b1 != b2) {crossx = (b2 - b1) / (m1 - m2);}
        BigDecimal roundedCrossx = new BigDecimal(crossx);
        roundedCrossx = roundedCrossx.setScale(decimalPlaces, RoundingMode.HALF_UP);
        BigDecimal crossy = new BigDecimal((m1 * crossx + b1));
        crossy = crossy.setScale(decimalPlaces, RoundingMode.HALF_UP);
        Vertex i = new Vertex(roundedCrossx.doubleValue(), crossy.doubleValue()); 

        if (roundedCrossx.doubleValue() <= max_x.doubleValue() && roundedCrossx.doubleValue() >= min_x.doubleValue() && crossy.doubleValue() <= max_y.doubleValue() && crossy.doubleValue() >= min_y.doubleValue()) {
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

        if (v1.x == v2.x) { //check if line is vertical
            if (v3.x == v4.x) {return null;} //if both vertical discard (either concurrent or dont intersect - neither useful)
            BigDecimal y = new BigDecimal(m2 * v1.x + b2);
            y = y.setScale(decimalPlaces, RoundingMode.HALF_UP);
            Vertex i = new Vertex(v1.x, y.doubleValue());
            if (i.x <= max_x.doubleValue() && i.x >= min_x.doubleValue() && i.y <= max_y.doubleValue() && i.y >= min_y.doubleValue()) {
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
                return i;
            }
            return null;
        }

        //for any other case
        double crossx = 0;
        if (m1 == m2) {return null;}
        else if (b1 != b2) {crossx = (b2 - b1) / (m1 - m2);}
        BigDecimal roundedCrossx = new BigDecimal(crossx);
        roundedCrossx = roundedCrossx.setScale(decimalPlaces, RoundingMode.HALF_UP);
        BigDecimal crossy = new BigDecimal((m1 * crossx + b1));
        crossy = crossy.setScale(decimalPlaces, RoundingMode.HALF_UP);
        Vertex i = new Vertex(roundedCrossx.doubleValue(), crossy.doubleValue());   

        if (roundedCrossx.doubleValue() <= max_x.doubleValue() && roundedCrossx.doubleValue() >= min_x.doubleValue() && crossy.doubleValue() <= max_y.doubleValue() && crossy.doubleValue() >= min_y.doubleValue()) {
            return i;
        }
        return null;
    }

    public static boolean areCollinear(Vertex a, Vertex b, Vertex c) {
        // Define a small tolerance value to account for floating-point inaccuracies
        double epsilon = 1e-5;

        // Calculate the cross product of vectors formed by points (a, b) and (a, c)
        double crossProduct = (b.y - a.y) * (c.x - a.x) - (c.y - a.y) * (b.x - a.x);

        // If the absolute value of the cross product is less than epsilon, points are considered collinear
        return Math.abs(crossProduct) < epsilon;
    }

    public boolean segInPoly(Vertex start, Vertex end) { // check if segment is inside the polygon
        Vertex center = new Vertex((start.x + end.x) / 2 + 0.00382, (start.y + end.y) / 2 + 0.00382); // added value just to make lesser odds of corresponding to HV partition line
        Vertex inf = new Vertex(1000000, center.y);

        int intersect = 0;
        for (HalfEdge e : externalEdges) {
            Vertex i = seg_intersect(center, inf, e.origin, e.next.origin);
            if (i != null) {
                if (!i.equals(e.next.origin)) { // para nao contabilizar 2 vezes (como inicio de um edge e fim de outro)
                    intersect++;
                }
            }
        }

        if (intersect % 2 == 0) {
            return false;
        }
        return true;
    }
}