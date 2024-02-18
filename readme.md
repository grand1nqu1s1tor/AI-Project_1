# 3D Puzzle Solver

## Overview
This project is a Java application that implements the A* search algorithm to solve a 3D puzzle. 
The objective of the code is to navigate a 3D grid to reach a goal state from a given initial state 
by moving the blank tile in one of six possible directions (North, South, East, West, Up, Down). 
Each move has an associated cost, and the path with the lowest total cost is selected as the solution.

## Features
- A* search algorithm implementation for optimal path finding.
- 3D grid representation of puzzle states.
- Heuristic function based on Manhattan distance for cost estimation.

## How to Run

### Prerequisites
- Java Development Kit (JDK) installed on your machine.

### Setup
1. Clone the repository to your local machine.
2. Navigate to the directory where the project is located.

### Running the Program
To run the program, compile the `PuzzleSolver.java` file and execute the Java program:

```bash
javac PuzzleSolver.java
java PuzzleSolver
```

The program expects to find an input file named `input.txt` with the initial and goal states defined, 
and it will produce an output file named `output.txt` containing the solution path and metrics.

## Input and Output File Format

### Input
The input file should contain two sections separated by an empty line, where each section represents 
a 3D state of the puzzle with each layer of the grid separated by an empty line.

### Output
The output file will include:
- The initial and goal states as read from the input.
- The depth level of the solution.
- The total number of nodes generated during the search.
- The sequence of actions representing the solution path.
- The sequence of F values for the nodes along the solution path.

## Contributing
Contributions are welcome. Please feel free to fork the repository and submit pull requests. 
For major changes, open an issue first to discuss what you would like to change.

## DS Used
Algorithms like A* use priority queues to determine the shortest path on a graph. Nodes or vertices 
are explored based on their priority, which is usually a combination of the cost to reach the node and 
an estimate of the cost to reach the goal from that node. This ensures that the most promising paths are explored first.

## License
[MIT](https://choosealicense.com/licenses/mit/)
