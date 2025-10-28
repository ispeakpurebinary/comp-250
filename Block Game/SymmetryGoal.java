package assignment3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SymmetryGoal extends Goal {
    private int direction;

    public SymmetryGoal(int direction) {
        super(null);
        this.direction = direction;
    }

    @Override
    public int score(Block board) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(board);
        int total = 0;

        int boardSize = board.getSize();
        while (!blocks.isEmpty()) {
            Block current = blocks.remove(0);
            if (current.getChildren().length == 0) {
                int x = current.getXCoord();
                int y = current.getYCoord();
                int size = current.getSize();
                int mirrorX, mirrorY;

                if (direction == 0) { // Horizontal
                    mirrorX = x;
                    mirrorY = boardSize - y - size;
                } else { // Vertical
                    mirrorX = boardSize - x - size;
                    mirrorY = y;
                }

                Block mirror = board.getSelectedBlock(mirrorX, mirrorY, current.getLevel());
                if (mirror != null && mirror.getColor() != null && mirror.getColor().equals(current.getColor())) {
                    total += (int) Math.pow(4, board.getMaxDepth() - current.getLevel());
                }
            } else {
                for (Block child : current.getChildren()) {
                    blocks.add(child);
                }
            }
        }

        return total;
    }

    @Override
    public String description() {
        return "Achieve symmetry along the " + (direction == 0 ? "horizontal axis" : "vertical axis");
    }
}
