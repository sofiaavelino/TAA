# TAA — k-Modem Illumination in Orthogonal Polygons

This project implements an algorithm to determine the region of an orthogonal polygon that is *illuminated* by a **k-modem** — a wireless modem capable of transmitting a signal through up to *k* walls along a straight line. It models wireless signal coverage in complex indoor environments by combining geometric visibility with wall-penetration constraints.

## ✨ Features
- Compute the illuminated region of a polygon given a k-modem location
- Account for both physical distance and wall penetration
- Efficient polygon partitioning using a **Doubly Connected Edge List (DCEL)**
- Outputs the vertices delimiting illuminated regions in counter-clockwise order

## 🧠 Data Structures
The algorithm relies on the following key structures:

- **Vertex**: Stores `(x, y)` coordinates  
- **Edge**: Includes origin, next, previous, twin, and incident face  
- **Face**: Centroid, ID, modem visibility, and outer component  
- **DCEL**: Contains lists of vertices, edges, external edges, and faces  

The DCEL allows efficient updates as rays cast from the modem introduce new polygon partitions.

## 🔍 Algorithm Overview
1. **Ray Casting from the Modem**  
   - Cast rays from the modem to every vertex in the polygon  
   - Record intersections with polygon edges

2. **Partitioning the Polygon**  
   - Insert new edges connecting intersection points  
   - Update DCEL to form new faces

3. **Visibility Computation**  
   - For each face, count wall crossings along the segment from the modem to the centroid  
   - Determine if the face is visible (≤ *k* wall penetrations)

4. **Region Extraction**  
   - Merge visible faces  
   - Return vertices of the illuminated region(s) in counter-clockwise order

## 🛠️ Implementation
- Language: **Java**  
- Geometric operations and DCEL manipulation implemented from scratch  
- Input: Polygon vertices in counter-clockwise order  
- Output: Illuminated regions and visualizations

## 📄 Report
A full technical write-up of the theory, algorithm design, data structures, and results is provided in:

[**Report_TAA.pdf**](Report_TAA.pdf)

## 🚀 Usage
Clone the repository:

```bash
git clone https://github.com/sofiaavelino/TAA.git
cd TAA

File: TAA_proj.java
Compile: javac TAA_proj.java
Run: java TAA_proj

When running this file you will be prompted the following inputs:

"Insert 0 to generate a permutomino or insert 1 to use a precomputed file"
- Write 0 or 1 and press enter

If you press 0:

	"Please select the desired number of vertices"
	- Insert number of vertices (must be even) and press enter


If you press 1:
	"Please insert the name of the file you wish to use:"
	- Insert the name of a file with the format of the same type as one obtained from "GenPols_RectParts_TAA/gridnew.c" 
	- If you've generated a polygon previously by pressing 0 in this step and wish to use it again with a new guard or vertex insert the name 'exemplo_grid' here, which contains the file created with the information for that polygon


"Please select a guard vertex from the list below and the visibility level you desire. (use format 'x y k', with x,y, and z integers):"
Choose a vertex from the presented ones and insert its coordinates x and y followed by the visibility value restriction in the order x y z

The python file will run automatically from the java file.
You can also run it directly, but have to guarantee the existence of a file named 'polygon.txt' obtained from previously running the java algorithm.


