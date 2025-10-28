package assignment3;

import java.awt.Color;

public class BlobGoal extends Goal {

    public BlobGoal(Color c) {
        super(c);
    }

    @Override
    public int score(Block board) {
        Color[][] grid = board.flatten();
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        int maxBlob = 0;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (!visited[i][j] && targetGoal.equals(grid[i][j])) {
                    int size = undiscoveredBlobSize(i, j, grid, visited);
                    maxBlob = Math.max(maxBlob, size);
                }
            }
        }

        return maxBlob;
    }

    public int undiscoveredBlobSize(int i, int j, Color[][] unitCells, boolean[][] visited) {
        if (i < 0 || i >= unitCells.length || j < 0 || j >= unitCells[0].length) return 0;
        if (visited[i][j] || unitCells[i][j] == null || !unitCells[i][j].equals(targetGoal)) return 0;

        visited[i][j] = true;
        return 1 + 
            undiscoveredBlobSize(i - 1, j, unitCells, visited) + 
            undiscoveredBlobSize(i + 1, j, unitCells, visited) + 
            undiscoveredBlobSize(i, j - 1, unitCells, visited) + 
            undiscoveredBlobSize(i, j + 1, unitCells, visited);
    }

    @Override
    public String description() {
        return "Create the largest connected blob of " + GameColors.colorToString(targetGoal) + " blocks.";
    }
}
