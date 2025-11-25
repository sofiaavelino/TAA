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


"Please select a guard vertex from the list below and the visibility level you desire. (use format 'x y k'):"
Choose a vertex from the presented ones and insert its coordinates x and y followed by the visibility value restriction in the order x y z

The python file will run automatically from the java file.
You can also run it directly, but have to guarantee the existence of a file named 'polygon.txt' obtained from previously running the java algorithm.


