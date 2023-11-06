import java.io.*;
import java.util.*;

public class PuzzleSolver {

    // Possible movement directions in a 3D grid.
    private static final int[][][] DIRECTIONS = {
            { { -1, 0, 0 }, { 1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } }
    };

    // A class to represent a state in the puzzle including its location and Cost f.
    private static class PuzzleNode {
        int[][][] state;
        PuzzleNode parent;
        char action;
        int g;
        int h;
        int f;
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
        char[] actions = { 'N', 'S', 'E', 'W', 'D', 'U' };
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
        int[][][] state = new int[3][3][3];
        String line;
        int z = 0, x = 0;
        int lineNumber = 0;

        while ((line = br.readLine()) != null) {
            lineNumber++;

            if (lineNumber < startLine) {
                continue;
            }

            if (line.trim().isEmpty()) {
                z++;
                x = 0;
                continue;
            }

            String[] values = line.split("\\s+");

            for (int y = 0; y < 3; y++) {
                state[x][y][z] = Integer.parseInt(values[y]);
            }
            x++;

            if (lineNumber >= endLine) {
                break;
            }
        }
        br.close();
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
                    if (state[x][y][z] == 0)
                        return new int[] { x, y, z };
        return null;
    }

    // Finds the position of a given value in the 3D matrix.
    private static int[] findPosition(int[][][] matrix, int value) {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                for (int z = 0; z < 3; z++)
                    if (matrix[x][y][z] == value)
                        return new int[] { x, y, z };
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

    // Creates a deep copy of a 3D state array.
    private static int[][][] copyState(int[][][] state) {
        int[][][] newState = new int[3][3][3];
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                System.arraycopy(state[x][y], 0, newState[x][y], 0, 3);
        return newState;
    }

    // Calculates the heuristic value (Manhattan distance in this case) for A*
    // algorithm.
    private static int calculateHeuristic(int[][][] state, int[][][] goal) {
        int distance = 0; // Initialize the Manhattan distance to 0.

        // Iterate over each level of the 3D puzzle.
        for (int z = 0; z < state.length; z++) {
            for (int x = 0; x < state[z].length; x++) {
                for (int y = 0; y < state[z][x].length; y++) {
                    if (state[x][y][z] != 0 && state[x][y][z] != goal[x][y][z]) {
                        // Find the expected position of the current tile in the goal state.
                        int[] position = findPosition(goal, state[x][y][z]);

                        // If the position is not null, meaning the tile exists in the goal state.
                        if (position != null) {
                            // Calculate the Manhattan distance for the current tile.
                            distance += Math.abs(x - position[0]) // Distance on the x-axis.
                                    + Math.abs(y - position[1]) // Distance on the y-axis.
                                    + Math.abs(z - position[2]); // Distance on the z-axis.
                        } else {
                            // Output an error if the tile is not found in the goal state.
                            System.out.println("Tile not found in goal state at x:" + x + " y:" + y + " z:" + z);
                        }
                    }
                }
            }
        }

        // Return the total Manhattan distance for the entire state.
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
            path.add(node.action); // Assuming you have an 'action' field in PuzzleNode.
            fValues.add(node.f);
            node = node.parent;
        }
        fValues.add(node.f); // Adding the f(n) for the start node

        Collections.reverse(path);
        Collections.reverse(fValues);

        return new Object[] { path, fValues };
    }

    // The A* search algorithm to find the shortest path from initial to goal state.
    private static Object[] AStar(int[][][] initialState, int[][][] goalState) {
        PriorityQueue<PuzzleNode> openList = new PriorityQueue<>(Comparator.comparingInt(node -> node.f));
        Map<String, PuzzleNode> allNodesMap = new HashMap<>(); // This will store the best node for each state.
        Set<String> closedSet = new HashSet<>();

        int[] blankPosition = findBlankTile(initialState);
        PuzzleNode start = new PuzzleNode(initialState, blankPosition[0], blankPosition[1], blankPosition[2]);
        start.g = 0;
        start.h = calculateHeuristic(start.state, goalState);
        start.f = start.g + start.h;

        openList.add(start);
        allNodesMap.put(Arrays.deepToString(start.state), start); // Add the start node to the map.

        int nodesGenerated = 0;

        while (!openList.isEmpty()) {
            PuzzleNode current = openList.poll();
            String currentStateStr = Arrays.deepToString(current.state);
            closedSet.add(currentStateStr); // Add to closedSet.

            if (Arrays.deepEquals(current.state, goalState)) {
                Object[] pathAndFValues = reconstructPath(current);
                List<Character> path = (List<Character>) pathAndFValues[0];
                List<Integer> fValues = (List<Integer>) pathAndFValues[1];
                return new Object[] { path, nodesGenerated, fValues };
            }

            for (PuzzleNode neighbor : getNeighbors(current)) {

                String neighborStateStr = Arrays.deepToString(neighbor.state);

                if (!allNodesMap.containsKey(neighborStateStr)) {
                    // Increment the counter here only if the node is new (not expanded before)
                    nodesGenerated++;
                }
                if (closedSet.contains(neighborStateStr)) {
                    continue; // Skip this neighbor since it's already expanded.
                }

                neighbor.g = current.g + 1;
                neighbor.h = calculateHeuristic(neighbor.state, goalState);
                neighbor.f = neighbor.g + neighbor.h;

                PuzzleNode existingNode = allNodesMap.get(neighborStateStr);

                if (existingNode == null || neighbor.g < existingNode.g) { // If not in allNodesMap or has better path
                    openList.add(neighbor); // Add to openList.
                    allNodesMap.put(neighborStateStr, neighbor); // Update the node in allNodesMap with the better path.
                }
            }
        }
        return null; // In case the goal is not reachable.
    }

}