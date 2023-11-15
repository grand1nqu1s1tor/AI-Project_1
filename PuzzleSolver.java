//Submitted by Dipesh Parwani(N15729998) & Eshaan Raj Sharma(N15107439)

import java.io.*;
import java.util.*;

public class PuzzleSolver {

    // Possible movement directions in a 3D grid.
    private static final int[][][] DIRECTIONS = {
            {{-1, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}
    };

    // A class to represent a state in the puzzle including its location and Cost f.
    private static class PuzzleNode {
        // The current configuration of the puzzle as a 3D array.
        int[][][] state;
        // Reference to the parent node from which this node was expanded.
        PuzzleNode parent;
        // The action taken to get from the parent node to this node.
        char action;
        // Total cost (f = g + h) to determine node selection.
        int g;
        int h;
        int f;
        // Coordinates of the blank space
        int blankX, blankY, blankZ;

        public PuzzleNode(int[][][] state, int blankX, int blankY, int blankZ) {
            this.state = state;
            this.blankX = blankX;
            this.blankY = blankY;
            this.blankZ = blankZ;
        }
    }

    // Point of Entry.
    public static void main(String[] args) throws Exception {
        // Define the file prefix for input and output
        String inputFilePrefix = "input";
        String outputFilePrefix = "output";

        // Define an array with the specific names of your input files
        String[] inputFileNames = {inputFilePrefix + ".txt", inputFilePrefix + "1.txt", inputFilePrefix + "2.txt", inputFilePrefix + "3.txt"};

        // Iterate over the array of file names
        for (String inputFileName : inputFileNames) {
            // Construct the output file name by replacing "input" with "output" in the input file name
            String outputFileName = inputFileName.replace(inputFilePrefix, outputFilePrefix);

            // Read the initial and goal states from the input file
            int[][][] initialState = readInputFile(inputFileName, 0, 11);
            int[][][] goalState = readInputFile(inputFileName, 13, 23);

            // Print the initial and goal states
            print3DArray(initialState);
            print3DArray(goalState);

            // Perform the A* algorithm
            Object[] resultAStar = AStar(initialState, goalState);

            // Extract the results from the A* algorithm
            int totalNodes = (int) resultAStar[1];
            List<Integer> fValues = (List<Integer>) resultAStar[2];

            // Print the results
            System.out.println((List<Character>) resultAStar[0] + "\nTotal Nodes: " + totalNodes + "\nF Values: " + fValues);

            // Write the results to the output file
            writeOutputFile(outputFileName, initialState, goalState, (List<Character>) resultAStar[0], totalNodes, fValues);
        }
    }


    // Converts a direction index into the corresponding action character.
    private static char getAction(int index) {
        char[] actions = {'N', 'S', 'E', 'W', 'D', 'U'};
        return actions[index];
    }

    // Writes the results to an output file.
    private static void writeOutputFile(String filename, int[][][] initialState, int[][][] goalState,
                                        List<Character> actions, int nodesGenerated, List<Integer> fValues) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {

            // Copy initial state and goal state
            writeStateToFile(bw, initialState);
            bw.newLine();
            writeStateToFile(bw, goalState);
            bw.newLine();

            // Write depth level
            bw.write(String.valueOf(actions.size()));
            bw.newLine();

            // Total number of nodes generated
            bw.write(String.valueOf(nodesGenerated));
            bw.newLine();

            // Write Actions
            for (char action : actions) {
                bw.write(action + " ");
            }
            bw.newLine();

            // Write F values
            for (int value : fValues) {
                bw.write(value + " ");
            }
        }
    }

    // Reads a 3D puzzle state from a file between specified line numbers.

    public static int[][][] readInputFile(String filename, int startLine, int endLine) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(filename));

        // Initializes a 3x3x3 integer array to hold the state of the puzzle.
        int[][][] state = new int[3][3][3];

        String line;
        int z = 0, x = 0;  // z tracks the depth layer in the 3D array, x tracks the row.
        int lineNumber = 0;

        // Reads the file line by line until the end of the file is reached.
        while ((line = br.readLine()) != null) {
            lineNumber++;  // Increments the line counter.

            // Skips the lines before the start line.
            if (lineNumber < startLine) {
                continue;
            }

            // Detects an empty line indicating a new layer in the 3D array, resets x to 0, increments z.
            if (line.trim().isEmpty()) {
                z++;
                x = 0;
                continue;
            }

            // Splits the line into individual numbers using whitespace as a delimiter.
            String[] values = line.split("\\s+");

            // Parses the numbers and stores them in the current layer and row of the 3D array.
            for (int y = 0; y < 3; y++) {
                state[x][y][z] = Integer.parseInt(values[y]);
            }
            // Moves to the next row in the current layer.
            x++;

            // Breaks the loop if the end line has been reached.
            if (lineNumber >= endLine) {
                break;
            }
        }
        br.close();
        // Returns the populated 3D array.
        return state;
    }

    // Helper method to write a 3D state to the file.
    private static void writeStateToFile(BufferedWriter bw, int[][][] state) throws IOException {
        for (int z = 0; z < 3; z++) {
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    bw.write(state[x][y][z] + " ");
                }
                bw.newLine();
            }
            if (z < 2)
                bw.newLine(); // No extra newline for the last layer
        }
    }

    // Finds the blank tile in the 3D puzzle.
    private static int[] findBlankTile(int[][][] state) {
        for (int z = 0; z < 3; z++)
            for (int x = 0; x < 3; x++)
                for (int y = 0; y < 3; y++)
                    if (state[x][y][z] == 0)    // Since blank tile is represented by a 0
                        return new int[]{x, y, z};
        return null;
    }

    // Finds the position of a given value in the 3D matrix.
    private static int[] findPosition(int[][][] matrix, int value) {
        for (int z = 0; z < matrix.length; z++) {
            for (int x = 0; x < matrix[z].length; x++) {
                for (int y = 0; y < matrix[z][x].length; y++) {
                    if (matrix[z][x][y] == value) {
                        return new int[]{z, x, y};
                    }
                }
            }
        }
        return null;
    }

    // Generates a list of all valid neighbor states from the current state.
    private static List<PuzzleNode> getNeighbors(PuzzleNode node) {
        List<PuzzleNode> neighbors = new ArrayList<>(); // Initialize the list to hold the neighbor nodes.

        // Loop through each possible direction of movement.
        for (int i = 0; i < DIRECTIONS[0].length; i++) {
            // Calculate the new coordinates for the blank space after moving it in the
            // current direction.
            int newX = node.blankX + DIRECTIONS[0][i][0];
            int newY = node.blankY + DIRECTIONS[0][i][1];
            int newZ = node.blankZ + DIRECTIONS[0][i][2];

            if (isValid(newX, newY, newZ)) {
                // Create a deep copy of the current state to avoid mutations when we swap
                // tiles.
                int[][][] newState = copyState(node.state);

                // Swap the blank space with the target position to generate the new state.
                swap(newState, node.blankX, node.blankY, node.blankZ, newX, newY, newZ);

                // Instantiate a new PuzzleNode with the new state and blank space coordinates.
                PuzzleNode neighbor = new PuzzleNode(newState, newX, newY, newZ);

                // Link the current node as the parent of the new node for path tracking.
                neighbor.parent = node;

                // Record the action that led to this new state
                neighbor.action = getAction(i);

                // Add the newly created neighbor node to the list of neighbors.
                neighbors.add(neighbor);
            }
        }

        // Return the complete list of neighbor states.
        return neighbors;
    }

    // Validates if the given coordinates are within the bounds of the 3D grid.
    private static boolean isValid(int x, int y, int z) {
        return x >= 0 && x < 3 && y >= 0 && y < 3 && z >= 0 && z < 3;
    }

    // Swaps two tiles in the 3D state array.
    private static void swap(int[][][] state, int x1, int y1, int z1, int x2, int y2, int z2) {
        int temp = state[x1][y1][z1];
        state[x1][y1][z1] = state[x2][y2][z2];
        state[x2][y2][z2] = temp;
    }

    // Creates a  copy of a 3D state array.
    private static int[][][] copyState(int[][][] state) {
        int[][][] newState = new int[3][3][3];
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                System.arraycopy(state[x][y], 0, newState[x][y], 0, 3);
        return newState;
    }

    // Calculates the heuristic value (Manhattan distance in this case) for A* algorithm.
    private static int calculateHeuristic(int[][][] state, int[][][] goal) {
        int distance = 0; // Initialize the Manhattan distance to 0.

        // Iterate over each level of the 3D puzzle.
        for (int z = 0; z < state.length; z++) {
            for (int x = 0; x < state[z].length; x++) {
                for (int y = 0; y < state[z][x].length; y++) {

                    if (state[x][y][z] != 0 && state[z][x][y] != goal[z][x][y]) {
                        // Find the expected position of the current tile in the goal state.
                        int[] position = findPosition(goal, state[z][x][y]);

                        // If the position is not null, meaning the tile exists in the goal state.
                        if (position != null) {
                            // Calculate the Manhattan distance for the current tile.
                            distance += Math.abs(z - position[0])
                                    + Math.abs(x - position[1])
                                    + Math.abs(y - position[2]);

                        } else {
                            // Output an error if the tile is not found in the goal state.
                            System.out.println("Tile not found in goal state at x:" + x + " y:" + y + " z:" + z);
                        }
                    }
                }
            }
        }
        return distance;
    }

    // Prints out a 3D array in a readable format.
    private static void print3DArray(int[][][] arr) {
        for (int z = 0; z < arr[0][0].length; z++) {
            System.out.println("Layer " + (z + 1) + ":");
            for (int x = 0; x < arr.length; x++) {
                for (int y = 0; y < arr[x].length; y++) {
                    System.out.printf("%4d", arr[x][y][z]);
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    // Reconstructs the path from the goal node to the initial state.
    private static Object[] reconstructPath(PuzzleNode node) {
        List<Character> path = new ArrayList<>();
        List<Integer> fValues = new ArrayList<>();

        while (node.parent != null) {
            path.add(node.action);

            fValues.add(node.f);
            node = node.parent;
        }
        fValues.add(node.f); // Adding the f(n) for the start node

        Collections.reverse(path);
        Collections.reverse(fValues);

        System.out.println(fValues);
        return new Object[]{path, fValues};
    }

    // The A-Star search Algorithm Implementation.
    private static Object[] AStar(int[][][] initialState, int[][][] goalState) {
        // Initialize a priority queue to manage nodes to be explored with a comparator based on f values
        PriorityQueue<PuzzleNode> openList = new PriorityQueue<>(Comparator.comparingInt(node -> node.f));

        // A map to keep track of the best path to a given state
        Map<String, PuzzleNode> allNodesMap = new HashMap<>();

        // A set to keep track of states that have been visited and expanded
        Set<String> closedSet = new HashSet<>();

        // Find the initial blank tile position
        int[] blankPosition = findBlankTile(initialState);
        // Create the start node with the initial state
        PuzzleNode start = new PuzzleNode(initialState, blankPosition[0], blankPosition[1], blankPosition[2]);
        start.g = 0; // Cost from the start node to itself
        start.h = calculateHeuristic(start.state, goalState); // Estimated cost from this node to the goal
        start.f = start.g + start.h; // Total estimated cost

        // Add the start node to the open set
        openList.add(start);
        // Record the start node in the map of all nodes
        allNodesMap.put(Arrays.deepToString(start.state), start);

        int nodesGenerated = 0; // Counter for the total nodes generated

        // Main loop to explore nodes until the queue is empty
        while (!openList.isEmpty()) {
            // Retrieve and remove the node with the lowest f value from the queue
            PuzzleNode current = openList.poll();
            // Mark the current state as processed
            String currentStateStr = Arrays.deepToString(current.state);
            closedSet.add(currentStateStr);

            // Check if the current state is the goal state
            if (Arrays.deepEquals(current.state, goalState)) {
                // Reconstruct the path and the sequence of f values to reach the goal
                Object[] pathAndFValues = reconstructPath(current);
                return new Object[]{pathAndFValues[0], nodesGenerated, pathAndFValues[1]};
            }

            // Explore the neighbors of the current node
            for (PuzzleNode neighbor : getNeighbors(current)) {
                String neighborStateStr = Arrays.deepToString(neighbor.state);
                // Count each unique state generated
                if (!allNodesMap.containsKey(neighborStateStr)) {
                    nodesGenerated++;
                }
                // Skip this neighbor if it's already been processed
                if (closedSet.contains(neighborStateStr)) {
                    continue;
                }
                // Calculate the cost values for the neighbor
                neighbor.g = current.g + 1; // Cost from start to neighbor through current
                neighbor.h = calculateHeuristic(neighbor.state, goalState); // Heuristic cost from neighbor to goal
                neighbor.f = neighbor.g + neighbor.h; // Total estimated cost

                // Check if this is the FIRST time we see this state or if this path is BETTER
                PuzzleNode existingNode = allNodesMap.get(neighborStateStr);
                if (existingNode == null || neighbor.g < existingNode.g) {
                    openList.add(neighbor); // Add the neighbor to the queue

                    allNodesMap.put(neighborStateStr, neighbor); // Record this path as the best for this state
                }
            }
        }
        // Return null if the goal state is not reachable from the initial state
        return null;
    }


}