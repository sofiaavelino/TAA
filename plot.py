import matplotlib.pyplot as plt

def read_polygons(filename):
    with open(filename, 'r') as file:
        lines = file.readlines()
    
    # Read the first polygon
    n1 = int(lines[0].strip())
    polygon1 = []
    for i in range(1, n1 + 1):
        x, y = map(float, lines[i].strip().split())
        polygon1.append((x, y))
    
    # Read the number of additional polygons
    index = n1 + 1
    num_additional_polygons = int(lines[index].strip())
    
    additional_polygons = []
    index += 1
    for _ in range(num_additional_polygons):
        n = int(lines[index].strip())
        polygon = []
        for i in range(index + 1, index + 1 + n):
            x, y = map(float, lines[i].strip().split())
            polygon.append((x, y))
        additional_polygons.append(polygon)
        index += n + 1
    
    return polygon1, additional_polygons

def plot_polygons(polygon1, additional_polygons):
    plt.figure()
    
    # Plot the first polygon (in the back, black)
    x1, y1 = zip(*polygon1)
    x1, y1 = list(x1) + [x1[0]], list(y1) + [y1[0]]
    plt.plot(x1, y1, 'k-', label='Polygon 1')
    plt.fill(x1, y1, 'black', alpha=0.5)
    
    # Plot additional polygons (in the front, red)
    for i, polygon in enumerate(additional_polygons):
        x, y = zip(*polygon)
        x, y = list(x) + [x[0]], list(y) + [y[0]]
        plt.plot(x, y, 'r-', label=f'Polygon {i + 2}')
        plt.fill(x, y, 'red', alpha=0.5)
    
    plt.legend()
    plt.show()

filename = 'polygons.txt'  # MUDAR PATH DO FICHEIRO SE NECESSARIO ---------------------------------------------------------------------------------------------------------------------------
polygon1, additional_polygons = read_polygons(filename)
plot_polygons(polygon1, additional_polygons)
