// Adapted to SWERC 2014


/*---------------------------------------------------------------------------*\

   Copyright (c) 2004-2014 Ana Paula Tomas  apt _AT_ dcc.fc.up.pt
	DCC-FCUP & LIACC, Universidade do Porto

#    This program is free software; you can redistribute it and/or modify
#      it under the terms of the GNU General Public License as published by
#      the Free Software Foundation; either version 2 of the License, or
#      (at your option) any later version.
#
#      This program is distributed in the hope that it will be useful,
#      but WITHOUT ANY WARRANTY; without even the implied warranty of
#      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#      GNU General Public License for more details.
#
#      You should have received a copy of the GNU General Public License
#      along with this program.

\*---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------\
 

   Generating Grid-Orthogonal Polygons by Inflate-Cut

   Reference:
     Ana Paula Tom�s and Ant�nio Leslie Bajuelos (2004)
     Quadratic-Time Linear-Space Algorithms for Generating Orthogonal
     Polygons with a Given Number of Vertices. LNCS 3045, 117-126, 2004.
     http://www.dcc.fc.up.pt/~apt/genpoly/index.html


   File: distr_Inflate.c
   Last changed: March 2009

   Input: the number of polygons that will be generated
          and their number of vertices

   Output: the number of vertices 
           and the coordinates in CCW order 
           --- wrt (O,(1,0),(0,-1))  (i.e., (East,South)) coordinate system
           if SYM_STANDARD_Y_AXIS active
           --- wrt (O,(1,0),(0,1)) (i.e., (East,North)) coordinate system
           if STANDARD_Y_AXIS active

                                                                                                                          
 REMARK:
 *** if we use SYM_STANDARD_Y_AXIS but represent the result as if
 the coordinates were in the canonical cartesian system,
 the sequence of vertices of the polygon will be in CW order.

\-----------------------------------------------------------------------*/


#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define SWERC 
// to create permutominoes with vertex coordinates starting at 0

// comment/uncomment one of the two following lines to control output
//#define SYM_STANDARD_Y_AXIS
#define STANDARD_Y_AXIS


typedef struct count {
  int val;
  struct count *nxt;
} *PCOUNTS, COUNTER;

typedef struct verts {
  int x, y;
  struct verts *nxt, *prev;
} *PVERTS, VERT;

typedef struct edges {
  PVERTS pvert;
  int line;
  struct edges *nxt;
} *PEDGES, EDGE;


PCOUNTS RowCounters;

PVERTS Polygon;

int AreaPolygon;

#define EDGENXT(P) ((P) ->nxt)
#define EDGEVERT_0(P) ((P) ->pvert)
#define EDGEVERT_1(P) (VERTNXT(EDGEVERT_0(P)))
#define EDGELINE(P) ((P) -> line)
 
#define VERTNXT(P)  ((P)->nxt)
#define VERTPREV(P)  ((P)->prev)
#define VERTX(P) ((P)->x)
#define VERTY(P) ((P)->y)

#define COUNTVALUE(P) ((P)->val)
#define COUNTNXT(P) ((P)->nxt)


typedef enum{FALSE,TRUE,MAYBE} BOOL3;

typedef struct rects {
  PVERTS pv;
  BOOL3 type;
} RECTS;

  
RECTS Rects[8];
int NRects;

#define MYMIN(A,B) (((A) < (B))? (A) : (B))
#define MYMAX(A,B) (((A) > (B))? (A) : (B))
#define HORIZONTAL_EDGE(V1,V2) (VERTY((V1)) == VERTY((V2))? 1: 0)
#define VERTICAL_EDGE(V1,V2) (VERTX((V1)) == VERTX((V2))? 1: 0)


// finds list of edges that intersect ray
PEDGES intersectEdges(PVERTS, int, BOOL3 (*)(PVERTS,int));
BOOL3 intersectH(PVERTS, int);
BOOL3 intersectV(PVERTS, int);

// creates a new edge
PEDGES newEdge(PVERTS, int);
PEDGES insertEdge(PEDGES,PVERTS);

// applies inflate
void inflate(int, int, PCOUNTS, PEDGES);
void updateVertices(int,int);  // inflates polygon
void insert_new_row(PCOUNTS);  // inserts a new counter
void insert_new_column(PEDGES);  // just updates row counters 

// finds the selected cell
PEDGES findCell(int, int *, int *, PCOUNTS *);
// finds list of H-edges that intersect ray and the H-edges that enclose cell
PEDGES findHedges(int, int, PEDGES *);

// finds rectangles defined an edge shot
void candRect(PEDGES, BOOL3, int, int);
// ckecks whether a point is in a rectangle
BOOL3 in_rectangle(PVERTS, PVERTS, int, int);
// checks whether the rectangle may be cut
BOOL3 ok_rectangle(PVERTS,int,int);
// selects a rectangle for cutting
BOOL3 selectRectangle(PEDGES, PEDGES, int, int, PVERTS *);
// cuts a rectangle
void cutRectangle(PVERTS, int, int);

// creates an n-vertex grid ogon by Inflate-Cut
void inflate_cut_generator(int);
void square1();  // creates the initial square
BOOL3 inflatecut(int); // applies one Inflate-Cut transformation

// writes a grid-ogon
void outputPolygon(PVERTS,int);  
      // (X,-Y) cartesian coordinate system if SYM_STANDARD_Y_AXIS defined
      // the usual cartesian coordinate system if STANDARD_Y_AXIS defined
      // (see above)

// creates a permutation of the cells for random selection
void onepermutation(int,int *);

// creates a new vertex
PVERTS  newVertex(int, int, PVERTS, PVERTS);

// frees dynamic memory
void free_edgelist(PEDGES);
void free_polygon(PVERTS);
void free_counters(PCOUNTS);


/*--------------------------------------------------------------------------\
   Example  SYM_STANDARD_Y_AXIS
  
Input
2 6

Output

6
1 1
1 3
2 3
2 2
3 2
3 1


6
2 1
2 2
1 2
1 3
3 3
3 1

   Example  STANDARD_Y_AXIS


Output

6
1 3
1 1
2 1
2 2
3 2
3 3


6
2 3
2 2
1 2
1 1
3 1
3 3

\-----------------------------------------------------------------------*/


int main()
{ int n, npols, auxpols;
 
  srand( (unsigned int)time( NULL));
#ifndef SWERC 
  printf("Number of Polygons? Number of Vertices?\n");
#endif
  // npols: the number of polygons that will be generated
  // n: the number of vertices of each polygon
  scanf("%d %d", &npols,&n);
  auxpols = npols;
  if (n && npols) {
    while(npols) {
      npols--;
      inflate_cut_generator(n);  // creates and writes polygon
#ifndef SWERC 
      putchar(10);  putchar(10);
#endif
    }
  }
  return 0;
}


BOOL3 intersectH(PVERTS pvert, int y) 
{
  if (VERTY(pvert) > y) {
    if (VERTY(VERTNXT(pvert)) <= y) return TRUE;
  } else if (VERTY(VERTNXT(pvert)) > y) return TRUE;
  return FALSE;
}


BOOL3 intersectV(PVERTS pvert, int x) 
{
  if (VERTX(pvert) > x) {
    if (VERTX(VERTNXT(pvert)) <= x) return TRUE;
  } else if (VERTX(VERTNXT(pvert)) > x) return TRUE;
  return FALSE;
}


PEDGES newEdge(PVERTS pv, int c)
{
  PEDGES pnew = (PEDGES) malloc(sizeof(EDGE));
  EDGENXT(pnew)  = NULL;
  EDGEVERT_0(pnew)  = pv;  // oriented CCW 
  EDGELINE(pnew) = c;
  return pnew;
}


PEDGES insertEdge(PEDGES plst, PVERTS pv) 
{ 
  int c; 
  PEDGES newE, aux = plst, paux = NULL;

  if (VERTX(pv) == VERTX(VERTNXT(pv)))
    c = VERTX(pv);
  else c = VERTY(pv);

  newE = newEdge(pv,c);

  while (aux != NULL && EDGELINE(aux) < c) {
    paux = aux;
    aux = EDGENXT(aux);
  }
  if (paux == NULL) 
    plst = newE;
  else EDGENXT(paux) = newE;
  EDGENXT(newE) = aux;
  return plst;
}


PEDGES intersectEdges(PVERTS pvert, int c, BOOL3 (*testint)(PVERTS,int))
{
  PVERTS pv = pvert;
  PEDGES pe = NULL;

  do {
    if (testint(pv,c) == TRUE) 
      pe = insertEdge(pe,pv);
    pv = VERTNXT(VERTNXT(pv));
  } while (pv != pvert);

  return pe;
}


/*-----  INFLATING ---------------------  */
void insert_new_row(PCOUNTS pi)
{
  PCOUNTS newc = (PCOUNTS) malloc(sizeof(COUNTER));
  COUNTVALUE(newc) = COUNTVALUE(pi);
  AreaPolygon += COUNTVALUE(pi);
  COUNTNXT(newc) = COUNTNXT(pi);
  COUNTNXT(pi) = newc;
}

void updateVertices(int icell, int jcell)
{ // updating vertices due to the insertion of new row
  // and new column
  PVERTS pv = Polygon;
  do {
    if (VERTX(pv) > jcell) VERTX(pv)++;
    if (VERTY(pv) > icell) VERTY(pv)++;
    pv = VERTNXT(pv);
  } while (pv != Polygon);
}

void insert_new_column(PEDGES phE)
{
  // assumes vertices already updated

  PCOUNTS pcount = RowCounters;
  int yhE, y=1;

  while (phE != NULL) {
    yhE =  VERTY(EDGEVERT_0(phE));
    while (y < yhE) {
      pcount = COUNTNXT(pcount);
      y++;
    }
    phE = EDGENXT(phE);
    yhE = VERTY(EDGEVERT_0(phE));
    do {
      COUNTVALUE(pcount)++;  AreaPolygon++;
      y++;
      pcount = COUNTNXT(pcount);
    } while (y < yhE);
    phE = EDGENXT(phE);
  }
}

void inflate(int icell, int jcell, PCOUNTS pRowI, PEDGES phE)
{
  insert_new_row(pRowI);

  updateVertices(icell, jcell);

  insert_new_column(phE);
}


/*-----  END INFLATING ---------------------  */

 

/* ------  FINDING the row and column of the selected cell ---- */

PEDGES findCell(int cell, int *i, int *j, PCOUNTS *ppcount)
{
  int count = 0;
  PEDGES pvEaux, aux;
  PCOUNTS pcount = RowCounters;
  
  *i=1;

  while (count+COUNTVALUE(pcount) < cell) {
    count += COUNTVALUE(pcount);
    pcount = COUNTNXT(pcount);
    (*i)++;
  }
  
  *ppcount = pcount;
  
  pvEaux = intersectEdges(Polygon,*i,intersectH);
  
  while(count+EDGELINE(EDGENXT(pvEaux))-EDGELINE(pvEaux)< cell) {
    count += (EDGELINE(EDGENXT(pvEaux))-EDGELINE(pvEaux));
    aux =  pvEaux;
    pvEaux = EDGENXT(EDGENXT(pvEaux));
    free(EDGENXT(aux)); free(aux); 
  }
  
  *j = EDGELINE(pvEaux)+cell-count-1;
  aux = EDGENXT(EDGENXT(pvEaux)); free_edgelist(aux);
  return pvEaux;  
  // pvEaux and EDGENXT(pvEaux) identify the closest vertical edges 
  // to the left and to the right of the selected cell
}


PEDGES findHedges(int j, int i, PEDGES *pphE)
{
  PEDGES phEaux = intersectEdges(VERTNXT(Polygon),j,intersectV);
  *pphE  = phEaux;
  while (EDGELINE(EDGENXT(phEaux)) < i)  phEaux = EDGENXT(EDGENXT(phEaux));
  return phEaux;
}  
  


BOOL3 in_rectangle(PVERTS pv, PVERTS pw, int xc, int yc)
{
  int xmin, xmax, ymin, ymax, xpv=2*VERTX(pv), ypv=2*VERTY(pv);
  xmin = MYMIN(xc,2*VERTX(pw)); xmax = MYMAX(xc,2*VERTX(pw));
  ymin = MYMIN(yc,2*VERTY(pw)); ymax = MYMAX(yc,2*VERTY(pw));

  if (xpv >= xmin && xpv <= xmax && ypv >= ymin &&  ypv <= ymax)
    return TRUE;
  return FALSE;
}

void candRect(PEDGES pe, BOOL3 last, int xc, int yc)
{
  if (EDGEVERT_0(pe) != Rects[NRects].pv) {
    Rects[++NRects].pv = EDGEVERT_0(pe);
    if (in_rectangle(Rects[NRects-1].pv,EDGEVERT_0(pe),xc,yc) == TRUE)
      Rects[NRects].type = FALSE;
    else Rects[NRects].type = MAYBE;
    if (in_rectangle(EDGEVERT_0(pe),Rects[NRects-1].pv,xc,yc) == TRUE)
      Rects[NRects-1].type = FALSE;
  } else Rects[NRects].type = TRUE;

  if (last == TRUE) {
    if (EDGEVERT_1(pe) != Rects[0].pv) {
      Rects[++NRects].pv = EDGEVERT_1(pe);
      if (in_rectangle(Rects[0].pv,EDGEVERT_1(pe),xc,yc) == TRUE)
	Rects[NRects].type = FALSE;
      else Rects[NRects].type = MAYBE;
      if (in_rectangle(EDGEVERT_1(pe),Rects[0].pv,xc,yc) == TRUE)
	Rects[0].type = FALSE;
    } else Rects[0].type = TRUE;
  }  else {
    Rects[++NRects].pv = EDGEVERT_1(pe);
    Rects[NRects].type = MAYBE;
  }
}

BOOL3 ok_rectangle(PVERTS pw,int xc,int yc)
{ 
  PVERTS pv=Polygon;
  int xmin, xmax, ymin, ymax, xpv, ypv;
  xmin = MYMIN(xc,2*VERTX(pw)); xmax = MYMAX(xc,2*VERTX(pw));
  ymin = MYMIN(yc,2*VERTY(pw)); ymax = MYMAX(yc,2*VERTY(pw));

  do {
    if (pv != pw) {
      xpv = VERTX(pv)*2; ypv = VERTY(pv)*2;
      if (xpv >= xmin && xpv <= xmax && ypv >= ymin &&  ypv <= ymax)
	return FALSE;
    }
    pv = VERTNXT(pv);
  } while (pv != Polygon);
  return TRUE;
}

BOOL3 selectRectangle(PEDGES left, PEDGES up, int i, int j, PVERTS *ppw)
{
  int vrtyc = 2*i+1, vrtxc = 2*j+1, kr, countRects = 0;

  Rects[0].pv = EDGEVERT_0(EDGENXT(left));
  Rects[0].type = Rects[1].type = MAYBE;
  Rects[1].pv = EDGEVERT_1(EDGENXT(left));
  NRects = 1;

  candRect(up,FALSE,vrtxc,vrtyc);
  candRect(left,FALSE,vrtxc,vrtyc);
  candRect(EDGENXT(up),TRUE,vrtxc,vrtyc);
  NRects++;

  for (kr = 0; kr < NRects; kr++)
    if (Rects[kr].type == MAYBE) {
      if ( (Rects[kr].type = ok_rectangle(Rects[kr].pv,vrtxc,vrtyc))==TRUE)
	countRects++;
    } else if (Rects[kr].type == TRUE) countRects++;
  
  if (!countRects) return FALSE;

  // selected Rectangle to cut
 
  NRects = 1+(rand() % countRects);


  kr=-1; countRects = 0; 
  while(countRects < NRects)
    if (Rects[++kr].type == TRUE) countRects++;
  *ppw = Rects[kr].pv;
  return TRUE;
}


PVERTS  newVertex(int x, int y, PVERTS prevV, PVERTS nxtV)
{
  PVERTS newV = (PVERTS) malloc(sizeof(VERT));
  VERTPREV(newV) = prevV;  VERTNXT(newV) = nxtV;
  VERTX(newV) = x;   VERTY(newV) = y; 
  return newV;
}


void cutRectangle(PVERTS pw, int icell, int jcell)
{
  int y = 1, xmin, xmax, ymin, ymax, xw, yw, xc, yc; 
  PCOUNTS pcount = RowCounters;
  PVERTS s1, s0;

  xw = VERTX(pw); yw = VERTY(pw);
  xc = jcell+1; yc = icell+1;

  xmin = MYMIN(xc,xw);   xmax = MYMAX(xc,xw); 
  ymin = MYMIN(yc,yw);   ymax = MYMAX(yc,yw); 

  for (y=1; y < ymin; y++, pcount = COUNTNXT(pcount));
  for (; y < ymax; y++,  pcount = COUNTNXT(pcount))  
    COUNTVALUE(pcount) = COUNTVALUE(pcount)-(xmax-xmin);
  
     
  if (HORIZONTAL_EDGE(pw,VERTPREV(pw))) {
    s0 = newVertex(xc,yw,NULL,NULL);  s1 = newVertex(xw,yc,NULL,NULL); 
    if (Polygon == pw) Polygon = s0; 
  } else {
    s1 = newVertex(xc,yw,NULL,NULL);  s0 = newVertex(xw,yc,NULL,NULL); 
  }

  VERTX(pw) = xc;  VERTY(pw) = yc;
  
  VERTNXT(VERTPREV(pw)) = s0; VERTPREV(s0) = VERTPREV(pw);
  VERTNXT(s0) = pw;  
  VERTNXT(s1) = VERTNXT(pw);
  VERTPREV(VERTNXT(pw)) = s1;
  VERTPREV(s1) = pw;  
  VERTPREV(pw) = s0; VERTNXT(pw) = s1;
  AreaPolygon =   AreaPolygon -(xmax-xmin)*(ymax-ymin);
}
  

    
  
void square1()
{ 
  PVERTS aux;
  Polygon = aux = newVertex(1,1,NULL,NULL);
  VERTNXT(aux) = newVertex(1,2,aux,NULL);
  aux = VERTNXT(aux);
  VERTNXT(aux) = newVertex(2,2,aux,NULL);
  aux = VERTNXT(aux); 
  VERTNXT(aux) = newVertex(2,1,aux,Polygon);
  VERTPREV(Polygon) = VERTNXT(aux);

  RowCounters = (PCOUNTS) malloc(sizeof(COUNTER));
  COUNTVALUE(RowCounters) = 1;
  COUNTNXT(RowCounters) = NULL;
  AreaPolygon = 1;
}



void inflate_cut_generator(int n)
{
  int r = n/2-2, cell;
  int *permut = (int *) malloc(sizeof(int)*n*n);
  square1();
  while (r>0) {
    onepermutation(AreaPolygon,permut); 
    for (cell=0; inflatecut(permut[cell]) == FALSE; cell++);
    r--;
  }
  /*--------------- OUTPUT -------------- */
  outputPolygon(Polygon,n);  
  /*-------------------------------------*/
  free(permut); free_polygon(Polygon);  free_counters(RowCounters);
}


void onepermutation(int AreaPolygon, int *permut)
{
  int i, k, aux= AreaPolygon, aux2;
  for(i=0; i<AreaPolygon; i++)
    permut[i] = i+1;

  for (i=0; i<AreaPolygon-1; i++) {
    k = i+rand() % aux;
    if (k != i) {
      aux2 = permut[k];
      permut[k] = permut[i];
      permut[i] = aux2;
    } 
    aux--;
  }
}

BOOL3 inflatecut(int cell)
{ // one step
  int icell, jcell;
  PEDGES leftVedge, pHEdges, upHedge;
  PCOUNTS cellrow; 
  PVERTS pw;

  leftVedge = findCell(cell,&icell,&jcell,&cellrow);
  upHedge = findHedges(jcell,icell,&pHEdges);
  if (selectRectangle(leftVedge,upHedge,icell,jcell,&pw) == TRUE) {
    inflate(icell,jcell,cellrow,pHEdges);
    cutRectangle(pw,icell,jcell);
    free_edgelist(pHEdges);
    return TRUE;
  }
  free_edgelist(pHEdges);
  return FALSE;
}


#ifdef STANDARD_Y_AXIS
void outputPolygon(PVERTS polygon, int n)
{
  PVERTS p = polygon;
#ifdef SWERC
  printf("%d",n);
#else
  printf("%d\n",n);
#endif
  do {
#ifdef SWERC
    printf(" %d %d", VERTX(p)-1,n/2+1-VERTY(p)-1);
#else
    printf("%d %d\n", VERTX(p),n/2+1-VERTY(p));
#endif
    p = VERTNXT(p);
  } while (p!=polygon);
  //  printf("\n Area = %d\n", AreaPolygon);
#ifdef SWERC
  printf("\n");
#endif
}
#endif

#ifdef SYM_STANDARD_Y_AXIS
void outputPolygon(PVERTS polygon, int n)
{
  PVERTS p = polygon;
  printf("%d\n",n);
  do {
    printf("%d %d\n", VERTX(p),VERTY(p));
    p = VERTNXT(p);
  } while (p!=polygon);
  //  printf("\n Area = %d\n", AreaPolygon);
}
#endif

/*
void outputPolygon(PVERTS polygon,int n)
{
  PVERTS p = polygon;
  //  printf("%d\n",n);
  printf("\n[p(%d,%d)", VERTX(p),VERTY(p));
  while ( (p =  VERTNXT(p))!= polygon) {
    printf(",p(%d,%d)", VERTX(p),VERTY(p));
  } 
  printf("].\n");
  //  printf("\n Area = %d\n", AreaPolygon);
}
*/



// -------------   Auxiliary (free dynamic memory) ---------------------
void free_edgelist(PEDGES e)
{
  if (e == NULL) return;
  free_edgelist(EDGENXT(e));
  free(e);
}

void free_polygon(PVERTS p)
{ PVERTS aux;
  VERTNXT(VERTPREV(p)) = NULL;
  do {
    aux = VERTNXT(p);
    free(p);
    p = aux;
  } while (p != NULL);
}
   
void free_counters(PCOUNTS p)
{
  if (p == NULL) return;
  free_counters(COUNTNXT(p));
  free(p);
}


