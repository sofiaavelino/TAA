import matplotlib.pyplot as plt

def read_polygons(filename):
    with open(filename, 'r') as file:
        lines = file.readlines()
    n1 = int(lines[0].strip())
    polygon1 = []
    for i in range(1, n1 + 1):
        x, y = map(float, lines[i].strip().split())
        polygon1.append((x, y))
    n2 = int(lines[n1 + 1].strip())
    polygon2 = []
    for i in range(n1 + 2, n1 + 2 + n2):
        x, y = map(float, lines[i].strip().split())
        polygon2.append((x, y))
    return polygon1, polygon2

def plot_polygons(polygon1, polygon2):
    x1, y1 = zip(*polygon1)
    x2, y2 = zip(*polygon2)
    x1, y1 = list(x1) + [x1[0]], list(y1) + [y1[0]]
    x2, y2 = list(x2) + [x2[0]], list(y2) + [y2[0]]
    plt.figure()
    plt.plot(x1, y1, 'k-', label='Polygon 1')
    plt.plot(x2, y2, 'r-', label='Polygon 2')
    plt.fill(x1, y1, 'black', alpha=0.5)
    plt.fill(x2, y2, 'red', alpha=0.5)
    plt.legend()
    plt.show()

filename = 'polygons.txt' # MUDAR PATH DO FICHEIRO SE NECESSARIO ---------------------------------------------------------------------------------------------------------------------------
polygon1, polygon2 = read_polygons(filename)
plot_polygons(polygon1, polygon2)
