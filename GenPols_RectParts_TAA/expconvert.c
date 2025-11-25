
//------------------------------------------------------------------------
// A.P.Tomás
// changed 2024 to handle polygons for which ymin can be anything
// (previously it was assumed to be zero to conform to the constraints
// defined to SWERC 2014 problem --  "Playing with Geometry"
// 
// inserts some empty grid lines to obtain a ortho-polygon without collinear
// edges from a permutomino 
//--------------------------------------------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <time.h>


// comment/uncomment the following line to transform to be used by gridnew.c
#define  OTHERREF


#define MAXVERTS 500
#define MAXCOORDS 3000

#define VERTICAL 1
#define HORIZONTAL 0

// defines the maximum number of grid lines introduced in a single step
#define MAXGLINESPERSTEP 4
// upper bound on the number of empty grid lines
#define MAXEMPTY 30
//#define MAXEMPTY 3000
#define RANDEMPTY


#define MIN(X,Y)  ((X) <(Y)? (X):(Y))
#define MAX(X,Y)  ((X) >(Y)? (X):(Y))


typedef struct ponto {
  int x,y;
} PONTO;

#define XCOORD(P) ((P).x)
#define YCOORD(P) ((P).y)

// canonical cartesian coordinates (X,Y) and CCW order
// or (X,-Y) ref and CW order
void write_polygon(int n, PONTO pol[]);

// (X,-Y) ref and CCW order
void write_polygon_other(int n, PONTO pol[]);

// leftmost vertex on the bottom horizontal edge 
void start_vertex(int n,PONTO pol[]) {
  int i, j, ymin, imin;
  PONTO aux[n];

  ymin = YCOORD(pol[0]);
  imin = 0;
  for (i=1;i<n;i++)
    if (YCOORD(pol[i]) < ymin) {
      ymin = YCOORD(pol[0]);
      imin = i;
    }

  if (YCOORD(pol[imin]) != YCOORD(pol[(imin+1)%n]))
    imin = (imin+1)%n;

  
  // imin is the index of leftmost vertex on the bottom edge
  if (imin != 0) {
    for(j=0; j < n; j=j+2) {
      aux[j]=pol[imin];
      aux[j+1] = pol[(imin+1)%n];
      imin = (imin+2)%n;
    }
    for(j=0;j<n;j++)
      pol[j]=aux[j];
  }

}

// to insert k horizontal grid lines shifting y=a by k positions
void insert_H_gridlines(int k, int a,PONTO pol[],int n) 
{
  int i;
  if (a==0) a = 1;
  for(i=0;i<n;i++)
    if (YCOORD(pol[i]) >= a) 
      YCOORD(pol[i]) += k;
}


// to insert k vertical grid lines shifting x=a by k positions
void insert_V_gridlines(int k, int a,PONTO pol[],int n) 
{
  int i;

  if (a==0) a = 1;
  for(i=0;i<n;i++)
    if (XCOORD(pol[i]) >= a) 
      XCOORD(pol[i]) += k;
}



// to get a rectilinear polygon from a permutomino by introducing 
// a total of k empty grid lines (either horizontal or vertical) 
void permutomino2polygon(int k, PONTO pol[],int n) {
  int max[2], direction, kaux, maxpstep, edge;

  max[HORIZONTAL] = max[VERTICAL] = n/2-1; 
  // max[HORIZONTAL]  the current maximum y-value
  // max[VERTICAL]  the current maximum x-value
  // for the insertion of horizontal and vertical lines

  // check that k is feasible
  if (2*MAXCOORDS-max[HORIZONTAL]-max[VERTICAL] < k) {
    fprintf(stderr,"ERROR: cannot insert %d empty lines\n",k);
    exit(0);
  }

  while (k != 0) {
    if (max[VERTICAL] == MAXCOORDS) direction = HORIZONTAL;
    else if (max[HORIZONTAL] == MAXCOORDS)  direction = VERTICAL;
    else direction = rand()%2;

    maxpstep = MIN(k,MAXGLINESPERSTEP);
    maxpstep = MIN(maxpstep,MAXCOORDS-max[direction]);

    if (maxpstep > 1) 
      kaux = 1+rand()%maxpstep;
    else kaux = 1;

    edge = rand()%(n/2);   // random edge (in the same direction)

    // new empty lines follow immediately the selected edge 
    if (direction == HORIZONTAL)
      insert_H_gridlines(kaux,YCOORD(pol[2*edge]),pol,n);
    else  
      insert_V_gridlines(kaux,XCOORD(pol[2*edge+1]),pol,n);

    max[direction] += kaux;
    k -= kaux;
  }
}

void write_polygon(int n, PONTO pol[]) 
{
  int i;

  printf("%d",n);  

  for(i=0; i<n; i++)
    printf(" %d %d",XCOORD(pol[i]),YCOORD(pol[i]));  


  printf("\n");
}



void write_polygon_other(int n, PONTO pol[]) 
{
  int i;

  printf("%d",n);  

  for(i=n-1; i>=0; i--)
    printf(" %d %d",XCOORD(pol[i])+1,YCOORD(pol[i])+1);  


  printf("\n");
}


int main(int argc, char *argv[])
{ int n, k, i;
  PONTO pol[MAXVERTS];

/*   if (argc < 2) { */
/*     fprintf(stderr,"Usage: %s [nlines]\n",argv[0]); */
/*     exit(0); */
/*   } */
/*   // exemplo:   transform 3 < polygon */
/*   // parameter defines max number of lines to insert */

  srand( (unsigned int)time( NULL));

  // inputs a permutomino (e.g., create by inflate-cut)
  scanf("%d",&n);
  for(i=0; i<n; i++)
    scanf("%d %d",&XCOORD(pol[i]),&YCOORD(pol[i]));

  // reduces to canonical definition, with pol[0] left vertex of bottom edge
  start_vertex(n,pol);
  

  k = 2*MAXCOORDS-(n-2);  // max number of empty grid lines
  k = MIN(2*MAXEMPTY,k);
#ifdef RANDEMPTY
  k = 1+rand()%k;   // select randomly the number to insert
#endif
  permutomino2polygon(k,pol,n);
  fprintf(stderr,"Trace:..... %d linhas vazias\n",k);

#ifdef OTHERREF
  write_polygon_other(n,pol);
#else
  write_polygon(n,pol);
#endif


  return 0;
}
