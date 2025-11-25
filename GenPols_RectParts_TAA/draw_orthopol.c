#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define MAXVERTS 500

typedef struct ponto {
  int x,y;
} PONTO;

#define XCOORD(P) ((P).x)
#define YCOORD(P) ((P).y)

#define ABS(X) ((X) < 0 ? -(X) : (X))



void drawOrthoPolygon(FILE *fr,PONTO p[],int n)
{
  // supoe-se que p aponta um vértice de um polígono ortogonal
  // com n vertices; as coordenadas dos vertices são inteiros
  // não negativos

  int dx, dy, s;
  int i, maxx=0, maxy=0;

  for(i=0;i<n;i++) {
    if(XCOORD(p[i]) > maxx) maxx = XCOORD(p[i]);
    if(YCOORD(p[i]) > maxy) maxy = YCOORD(p[i]);
  }

  i=0;

  fprintf(fr,"\\begin{picture}(%d,%d)\n",maxx+1,maxy+1);

  do {
    dx = XCOORD(p[(i+1)%n])-XCOORD(p[i]);
    dy = YCOORD(p[(i+1)%n])-YCOORD(p[i]);
    if (dx) {
      s = ABS(dx);
      dx = dx/s;
    } else {
      s = ABS(dy);
      dy = dy/s;
    }
    if (i==0) 
      printf("\\put(%d,%d){\\mbox{*}}\n",XCOORD(p[i]),YCOORD(p[i]));
   fprintf(fr,"\\put(%d,%d){\\line(%d,%d){%d}}\n",XCOORD(p[i]),YCOORD(p[i]),dx,dy,s);
    i = (i+1)%n;
  } while (i != 0);

  fprintf(fr,"\\end{picture}\n");
}


int main(int argc,char *argv[])
{ 
  int n, i;
  FILE *fresult;
  PONTO pol[MAXVERTS];

  if (argc == 2) {
    if ((fresult = fopen(argv[1],"w")) == NULL) {
      fprintf(stderr,"ERRO NA ABERTURA DO FICHEIRO:  %s\n",argv[1]);
      exit(EXIT_FAILURE);
    }
  } else fresult = stdout;


  scanf("%d", &n);
  for(i=0;i<n;i++) 
    scanf("%d%d",&XCOORD(pol[i]),&YCOORD(pol[i]));

  // TOLATEX
  fprintf(fresult,"\\documentclass{article}\n");
  fprintf(fresult,"\\setlength{\\unitlength}{0,3cm}\n");
  fprintf(fresult,"\\begin{document}\n\n\n");

  drawOrthoPolygon(fresult,pol,n);
   
  
  fprintf(fresult,"\n\n\\end{document}\n");


  if (argc == 2) fclose(fresult);
  return EXIT_SUCCESS;
}
