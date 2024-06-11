import matplotlib.pyplot as plt

def read_polygons(filename):
    with open(filename, 'r') as file:
        lines = file.readlines()
    
    # Read the number of vertices for the first polygon
    n1 = int(lines[0].strip())
    
    # Read the vertices for the first polygon
    polygon1 = []
    for i in range(1, n1 + 1):
        x, y = map(float, lines[i].strip().split())
        polygon1.append((x, y))
    
    # Read the number of vertices for the second polygon
    n2 = int(lines[n1 + 1].strip())
    
    # Read the vertices for the second polygon
    polygon2 = []
    for i in range(n1 + 2, n1 + 2 + n2):
        x, y = map(float, lines[i].strip().split())
        polygon2.append((x, y))
    
    return polygon1, polygon2

def plot_polygons(polygon1, polygon2):
    # Unpack the vertices for plotting
    x1, y1 = zip(*polygon1)
    x2, y2 = zip(*polygon2)
    
    # Close the polygons by adding the first point at the end
    x1, y1 = list(x1) + [x1[0]], list(y1) + [y1[0]]
    x2, y2 = list(x2) + [x2[0]], list(y2) + [y2[0]]
    
    # Plot the polygons
    plt.figure()
    plt.plot(x1, y1, 'k-', label='Polygon 1')  # Black line for the first polygon
    plt.plot(x2, y2, 'r-', label='Polygon 2')  # Red line for the second polygon
    
    plt.fill(x1, y1, 'black', alpha=0.5)
    plt.fill(x2, y2, 'red', alpha=0.5)
    
    plt.legend()
    plt.show()

# Example usage
filename = 'polygons.txt'  # Replace with the path to your file

# If the file is in the same directory as the script:
# filename = 'polygons.txt'

# If the file is in a different directory, provide the absolute path:
# filename = '/path/to/your/polygons.txt'

polygon1, polygon2 = read_polygons(filename)
plot_polygons(polygon1, polygon2)
