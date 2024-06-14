import matplotlib.pyplot as plt

def read_polygons(filename):
    with open(filename, 'r') as file:
        lines = file.readlines()
    
    point_x, point_y = map(float, lines[0].strip().split())
    k = int(lines[1].strip())
    n1 = int(lines[2].strip())
    polygon1 = []
    for i in range(3, 3 + n1):
        x, y = map(float, lines[i].strip().split())
        polygon1.append((x, y))
    index = 3 + n1
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
    
    return (point_x, point_y), k, polygon1, additional_polygons

def get_bounds(polygon1, additional_polygons):
    all_points = polygon1[:]
    for polygon in additional_polygons:
        all_points.extend(polygon)
    
    x_coords = [p[0] for p in all_points]
    y_coords = [p[1] for p in all_points]
    
    min_x, max_x = min(x_coords), max(x_coords)
    min_y, max_y = min(y_coords), max(y_coords)
    
    return min_x, max_x, min_y, max_y

def plot_polygons(point, k, polygon1, additional_polygons):
    plt.figure()
    
    x1, y1 = zip(*polygon1)
    x1, y1 = list(x1) + [x1[0]], list(y1) + [y1[0]]
    plt.plot(x1, y1, 'k-', label='Polygon')
    plt.fill(x1, y1, 'black', alpha=0.5)
    
    for polygon in additional_polygons:
        x, y = zip(*polygon)
        x, y = list(x) + [x[0]], list(y) + [y[0]]
        plt.plot(x, y, 'r-')
        plt.fill(x, y, 'red', alpha=0.5)
        
    
    plt.plot([], [], 'r-', label = 'Visibility (k={})'.format(k))
    plt.plot(point[0], point[1], 'bo', label='Guard')
    plt.title('Plot of the visibility zone with k={}'.format(k))
    
    min_x, max_x, min_y, max_y = get_bounds(polygon1, additional_polygons)
    plt.xlim(min_x - 2, max_x + 2)
    plt.ylim(min_y - 2, max_y + 2)  
    
    plt.legend(fontsize='small')
    plt.show()

filename = 'polygon.txt'
point, k, polygon1, additional_polygons = read_polygons(filename)
plot_polygons(point, k, polygon1, additional_polygons)
