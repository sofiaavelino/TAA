#include <stdio.h>
#include <stdlib.h>

#define PRINTHEDGES
// to print the half-edges

typedef enum{NOTVISITED,VISITED} LABELVIS;
typedef enum{NORTH,SOUTH,EAST,WEST} WROSE;
char Windrose[4]={'n','s','e','w'};
WROSE OppositeDir[4] = {SOUTH,NORTH,WEST,EAST};

typedef struct gridnode{
  int point[2];
  LABELVIS visited;
  struct piece *idrpiece;
  struct gridnode *adjs[4];
} GNODE;

typedef struct segment{
  GNODE *start, *final;
  struct segment *nxt, *prev;
} SEGMENT;

typedef struct vertex {
  int label;
  int point[2];
  GNODE *posgrid;
  struct vertex *nxt, *prev;
} VERTEX;

typedef struct piece {
  GNODE *gridvert;
  LABELVIS visited;
  int label;
  struct piece *nxt, *prev;
} PIECE;

#define IDRPIECE(P) ((P) -> idrpiece)
#define IDVERT(P) ((P)->gridvert)
#define POINT_X(V) ((V)->point[0])
#define POINT_Y(V) ((V)->point[1])
#define LABEL(V) ((V)->label)
#define POSGRID(V) ((V)->posgrid)
#define START(S) ((S)->start)
#define FINAL(S) ((S)->final)
#define EMPTY(P) ((P)== NULL)
#define ADJ(V,D) ((V)->adjs[(D)])
#define COORD(P,C) ((P)->point[(C)])
#define NXT(P) ((P)->nxt)
#define PREV(P) ((P)->prev)
#define INDEXCOORD(D) ((D)==SOUTH?1:0)
#define EXTREME(S,D) (((D)==NORTH || (D)==WEST)? START((S)): FINAL((S)))
#define PERPENDIR(D) (((D)==EAST || (D) == WEST)? SOUTH:EAST)
#define OPPOSITEDIR(D) (OppositeDir[(D)])

void printNodes(GNODE *,WROSE);
void printnode(GNODE *);
void printGridByEdges(SEGMENT *,WROSE,WROSE);

VERTEX *new_vertex(int,int, VERTEX *, VERTEX *,int);
VERTEX *readpolygon_ccw(int);

WROSE position_NSEW(VERTEX *,VERTEX *);
GNODE *new_gridnode(int,int);
GNODE *initgrid(VERTEX *, int);
SEGMENT *insertSort(SEGMENT *,SEGMENT *,int);
SEGMENT *edges(VERTEX *, int, int);
SEGMENT *new_segment(GNODE *, GNODE *,SEGMENT *, SEGMENT *);
SEGMENT *locate(SEGMENT *, int, SEGMENT *, int *);
void coords(GNODE *,GNODE *,int, int *, int *);
void sweep(SEGMENT *,WROSE);
SEGMENT *update(SEGMENT *,SEGMENT *,WROSE,int,SEGMENT *);
GNODE *break_edge(WROSE,SEGMENT *,int,SEGMENT *,WROSE);
void link_gridnode(GNODE *, GNODE *, WROSE, WROSE);



/* ----- CREATING NEW SEGMENTS AND NEW GRID NODES  ---------------- */

SEGMENT *new_segment(GNODE *s, GNODE *f,SEGMENT *prev, SEGMENT *nxt)
{
  SEGMENT *aux = (SEGMENT *) malloc(sizeof(SEGMENT));
  FINAL(aux) = f;   START(aux) = s; 
  PREV(aux) = prev; NXT(aux) = nxt;
  return aux;
}

GNODE *new_gridnode(int x, int y) {
  int pos;
  GNODE *aux = (GNODE *) malloc(sizeof(GNODE));
  POINT_X(aux) = x;
  POINT_Y(aux) = y;
  for (pos=0; pos<4; pos++)
    ADJ(aux,pos) = NULL;
  //  LABEL(aux) = NOTVISITED;
  IDRPIECE(aux) = NULL;
  return aux;
}

/* ---------   CREATE INITIAL GRID (MIMICS POLYGON) ----- */

WROSE position_NSEW(VERTEX *vi,VERTEX *vj) {
  // X increases from left to right
  // Y increases from top to bottom
  if (POINT_X(vi) == POINT_X(vj)) 
    if (POINT_Y(vi) < POINT_Y(vj)) 
      return SOUTH;  // vj is to the south of vi
    else return NORTH;
  else if (POINT_X(vi) < POINT_X(vj)) return EAST;
  else return WEST;
}

GNODE *initgrid(VERTEX *polygon, int n) {
  VERTEX *v;
  GNODE *grid, *auxprev, *aux; 
  WROSE pos;

  grid = new_gridnode(POINT_X(polygon),POINT_Y(polygon));
  POSGRID(polygon) = grid;
  auxprev = grid;
  v = polygon;
  while (--n!=0) {
    v = NXT(v);
    aux = new_gridnode(POINT_X(v),POINT_Y(v));
    POSGRID(v) = aux;
    pos = position_NSEW(PREV(v),v);
    ADJ(auxprev,pos) = aux;
    pos = OPPOSITEDIR(pos);
    ADJ(aux,pos) = auxprev;
    auxprev = aux;
  }    
  pos = position_NSEW(v,polygon);
  ADJ(aux,pos) = grid;
  pos = OPPOSITEDIR(pos);
  ADJ(grid,pos) = aux;
  return grid;
}

/*-------------------------------------------------------------------  */


/*--- FINDING V-EDGES (H-EDGES) LIST ORDERED BY LEFT-TO-RIGHT --- \
\     (TOP-TO-BOTTOM) SWEEP                                  --- */

SEGMENT *edges(VERTEX *polygon, int n, int coord)
{ 
  SEGMENT *sortlist = NULL, *aux;
  if (COORD(polygon,coord) != COORD(NXT(polygon),coord))
    polygon = NXT(polygon);
  do {
    aux = (SEGMENT *) malloc(sizeof(SEGMENT));
    if (COORD(polygon,1-coord) < COORD(NXT(polygon),1-coord)) {
      START(aux) = POSGRID(polygon);
      FINAL(aux) = POSGRID(NXT(polygon));
    } else {
      FINAL(aux) = POSGRID(polygon);
      START(aux) = POSGRID(NXT(polygon));
    }
    sortlist = insertSort(sortlist,aux,coord);
    polygon = NXT(NXT(polygon));
  }  while ((n=n-2) != 0);
  return sortlist;
}

// non-optimal sorting
SEGMENT *insertSort(SEGMENT *list,SEGMENT *newSeg,int coord)
{ // for orthogonal polygons in general position
  if (!EMPTY(list) && COORD(START(list),coord) < COORD(START(newSeg),coord)) {
    NXT(list) = insertSort(NXT(list),newSeg,coord);
    return list;
  }
  // COORD(START(list),coord) > COORD(START(newSeg),coord))
  NXT(newSeg) = list;
  return newSeg;
}

/*-----------------------------------------------------------  */

/*---------------- FINDING PI-RCUT GRID BY SWEEPING --------------  */

void sweep(SEGMENT *edges,WROSE perpd) 
{ // sweepV  c = 1 (perpd=SOUTH); otherwise c = 0 (perpd=EAST)
  int event, c=INDEXCOORD(perpd);
  SEGMENT *aibi;

  SEGMENT *sweepst = new_segment(START(edges),FINAL(edges),NULL,NULL);
  while (!EMPTY((edges = NXT(edges)))) {
    aibi = locate(sweepst,1-c,edges,&event);
    sweepst = update(sweepst,aibi,perpd,event,edges);
  }
}

// non-optimal structures for locate procedure  
SEGMENT *locate(SEGMENT *sweepst, int coord, SEGMENT *e, int *event) 
{
  int a, b, ai, bi;
  if (EMPTY(sweepst)){
    *event = 5; 
    return sweepst;
  }
  a = COORD(START(e),coord); b = COORD(FINAL(e),coord);
  for (; ;) {
    bi = COORD(FINAL(sweepst),coord);
    if (a > bi) 
      if (!EMPTY(NXT(sweepst)))
	sweepst = NXT(sweepst);
      else {
	*event = 9; // end line
	return sweepst;
      }
    else {
      ai = COORD(START(sweepst),coord);
      if (ai == a) {
	if (bi == b) *event = 8;
	else *event = 6;
      } else if (b == ai) *event = 2;
      else if (bi == a) {
	if (!EMPTY(NXT(sweepst)) && COORD(START(NXT(sweepst)),coord) == b)
	  *event = 4;
	else *event = 3;
      } else if (bi == b) *event = 7;
      else if (ai < a && bi > b) 
	*event = 1;
      else  *event = 5;
      return  sweepst;
    }
  }
}

SEGMENT *update(SEGMENT *swst,SEGMENT *aibi,WROSE perpd,int event,SEGMENT *e){
  SEGMENT *newsg;
  GNODE *gnode;
  int c = INDEXCOORD(perpd);
  WROSE d = PERPENDIR(perpd);
  switch(event) {
  case 5: 
    if (EMPTY(aibi)) 
      return new_segment(START(e),FINAL(e),NULL,NULL);
    newsg = new_segment(START(e),FINAL(e),PREV(aibi),aibi);
    if (!EMPTY(PREV(aibi))) { // bug 2018
      NXT(PREV(newsg)) = newsg;
      PREV(aibi) = newsg;
      return swst;
    } else {
      PREV(aibi) = newsg;
      return newsg;
    }
  case 2: 
    gnode = break_edge(perpd,aibi,c,e,d);
    START(aibi) = START(e);
    FINAL(aibi) = gnode;
    return swst;
  case 3:
    gnode = break_edge(perpd,aibi,c,e,OPPOSITEDIR(d));
    FINAL(aibi) = FINAL(e);
    START(aibi) = gnode;
    return swst;
  case 4:
    gnode = break_edge(perpd,aibi,c,e,OPPOSITEDIR(d));
    START(aibi) = gnode;
    aibi = NXT(aibi);
    gnode = break_edge(perpd,aibi,c,e,d);
    FINAL(PREV(aibi)) = gnode;
    NXT(PREV(aibi)) = NXT(aibi);
    if (!EMPTY(NXT(aibi))) PREV(NXT(aibi))=PREV(aibi);
    free(aibi);
    return swst;
  case 6:
    if (perpd == EAST) // V-sweep
      START(aibi) = ADJ(FINAL(e),WEST);
    gnode = break_edge(perpd,aibi,c,e,d);
    FINAL(aibi) = gnode;
    START(aibi) = FINAL(e);
    return swst;
  case 7:
    if (perpd == EAST) // V-sweep
      FINAL(aibi) = ADJ(START(e),WEST);
    gnode = break_edge(perpd,aibi,c,e,OPPOSITEDIR(d));
    START(aibi) = gnode;
    FINAL(aibi) = START(e);
    return swst;
  case 8:
    if (!EMPTY(PREV(aibi))) 
      NXT(PREV(aibi)) = NXT(aibi);
    else swst = NXT(aibi);
    if (!EMPTY(NXT(aibi))) PREV(NXT(aibi)) = PREV(aibi);
    free(aibi);
    return swst;
 case 1: 
    NXT(aibi) = new_segment(START(aibi),FINAL(aibi),aibi,NXT(aibi));
    if (!EMPTY(NXT(NXT(aibi)))) PREV(NXT(NXT(aibi))) = NXT(aibi);
    if (perpd == EAST) 
      FINAL(aibi) = ADJ(START(e),WEST);
    gnode = break_edge(perpd,aibi,c,e,OPPOSITEDIR(d));
    if (perpd == EAST) 
      START(NXT(aibi)) = ADJ(FINAL(e),WEST);
    FINAL(NXT(aibi)) = break_edge(perpd,NXT(aibi),c,e,d); 
    START(aibi) = gnode;
    FINAL(aibi) = START(e);
    START(NXT(aibi)) = FINAL(e);
    return swst;
  default: // case 9: 
    NXT(aibi) = new_segment(START(e),FINAL(e),aibi,NULL);
    PREV(NXT(aibi)) = aibi;
    return swst;
  }
}

void coords(GNODE *node,GNODE *cordnode,int coord, int *x, int *y)
{ 
  if (coord) {
    *x = POINT_X(node);  *y = POINT_Y(cordnode);
  } else {
    *x = POINT_X(cordnode); *y = POINT_Y(node);
  }
}

GNODE *break_edge(WROSE perpd,SEGMENT *aibi,int coord,SEGMENT *e,WROSE d)
{ int x, y;
  WROSE symd = OPPOSITEDIR(d),symperpd;
  GNODE *gnode, *start, *final, *cordstart;

  symperpd = OPPOSITEDIR(perpd),
  final = EXTREME(aibi,d);
  if (perpd!=SOUTH) 
    start = EXTREME(aibi,symd);
  else start = ADJ(final,symd);
  cordstart = EXTREME(e,d);
  do{
    start = ADJ(start,d);
    coords(start,cordstart,coord,&x,&y);
    gnode = new_gridnode(x,y);
    link_gridnode(gnode,cordstart,d,symd);
    link_gridnode(gnode,start,perpd,symperpd);
    cordstart = gnode;
  }  while(start != final);
  return gnode;
}

void link_gridnode(GNODE *newgnode, GNODE *node, WROSE d, WROSE symd)
{
  ADJ(newgnode,d) = ADJ(node,d);
  if (!EMPTY(ADJ(node,d)))  ADJ(ADJ(node,d),symd) = newgnode;
  ADJ(node,d) = newgnode;
  ADJ(newgnode,symd) = node;
}

/*-----------------------------------------------------  */
/*

PIECE *RPieces = NULL;
int NRPieces = 0;

void new_rpieces(GNODE *s, GNODE *f)
{ PIECE *piece;
  while (s != f) {
    piece = (PIECE *) malloc(sizeof(PIECE));
    IDVERT(piece) = s;
    NXT(piece) = RPieces;
    PREV(piece) = NULL;
    LABEL(piece) = ++NRPieces;
    if (!EMPTY(RPieces)) PREV(RPieces) = piece;
    RPieces = Piece;
    s = ADJ(s,SOUTH);
  }
}


*/



/* ---------   READING POLYGON ORDERED COUNTERCLOCKWISE ---------- */

VERTEX *new_vertex(int x,int y, VERTEX *prox, VERTEX *prev,int label)
{
  VERTEX *v = (VERTEX *) malloc (sizeof(VERTEX));
  POINT_X(v) = x;
  POINT_Y(v) = y;
  NXT(v) = prox;
  PREV(v) = prev;
  LABEL(v) = label;
  return v;
}

    
VERTEX *readpolygon_ccw(int n) {
  int x,y;
  VERTEX *polygon, *aux;
  scanf("%d %d", &x, &y);
  polygon = new_vertex(x,y,NULL,NULL,n);
  aux = polygon;
  while (--n != 0) {
    scanf("%d %d", &x, &y);
    aux = new_vertex(x,y,NULL,aux,n);
    NXT(PREV(aux)) = aux;
  }
  NXT(aux) = polygon;
  PREV(polygon) = aux;
  return polygon;
}





/*--------------------   OUTPUT  ------------------ */

void outputGrid(VERTEX *polygon, int n)
{ 
  while (n) {
    printnode(POSGRID(polygon));
    polygon = NXT(polygon);
    n--;
  }
}

void outputEdges(SEGMENT *edges)
{
  while(!EMPTY(edges)) {
    printf("(%d,%d)", POINT_X(START(edges)),POINT_Y(START(edges)));
    printf(" -- (%d,%d)\n", POINT_X(FINAL(edges)),POINT_Y(FINAL(edges)));
    edges = NXT(edges);
  }
}

void printNodes(GNODE *gnode,WROSE dir)
{ 
  while(!EMPTY((gnode= ADJ(gnode,dir))))
    printnode(gnode);
}

#ifndef PRINTHEDGES
void printnode(GNODE *gn)
{ int d;
  printf("(%d,%d)", POINT_X(gn), POINT_Y(gn));
  for (d=0; d<4; d++) 
    if (!EMPTY(ADJ(gn,d)))
      printf("\t%c:(%d,%d)",Windrose[d],POINT_X(ADJ(gn,d)),POINT_Y(ADJ(gn,d)));
    else printf("\t%c: --- ",Windrose[d]);
  putchar(10);
}
#endif


#ifdef PRINTHEDGES
void printnode(GNODE *gn)
{ int d;
  for (d=0; d<4; d++) 
    if (!EMPTY(ADJ(gn,d)))
      printf("%d %d %d %d\n",POINT_X(gn),POINT_Y(gn),POINT_X(ADJ(gn,d)),POINT_Y(ADJ(gn,d)));
}

#endif


void printGridByEdges(SEGMENT *edges,WROSE backward, WROSE forward)
{ GNODE *gnode;
  while (!EMPTY(edges)) {
    gnode = START(edges);
    printNodes(gnode,backward);
    printnode(gnode);
    printNodes(gnode,forward);
    edges = NXT(edges);
  }
  //  putchar(10);
}



void write_polygon(VERTEX *pol,int n) {
  printf("%d\n",n);
  while(n-- > 0) {
    printf("%d %d\n",POINT_X(pol),POINT_Y(pol));
    pol = NXT(pol);
  }
}



int main()
{ 
  int n;
  VERTEX *pol; 
  GNODE *grid;
  SEGMENT *vedges, *hedges;
  scanf("%d",&n);
  pol = readpolygon_ccw(n);
  write_polygon(pol,n);
  grid = initgrid(pol,n);
  //outputGrid(pol,n);
  vedges =  edges(pol,n,0);
  hedges =  edges(pol,n,1);
  //  outputEdges(vedges);
  // putchar(10);
  //outputEdges(hedges);
  sweep(hedges,SOUTH);
  printGridByEdges(hedges,WEST,EAST);
  sweep(vedges,EAST);
  printGridByEdges(hedges,WEST,EAST);

  return 0;
}




