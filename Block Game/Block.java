package assignment3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Block {
    private int xCoord;
    private int yCoord;
    private int size;
    private int level;
    private int maxDepth;
    private Color color;
    private Block[] children;

    public static Random gen = new Random();

    public Block() {}

    public Block(int x, int y, int size, int lvl, int maxD, Color c, Block[] subBlocks) {
        this.xCoord = x;
        this.yCoord = y;
        this.size = size;
        this.level = lvl;
        this.maxDepth = maxD;
        this.color = c;
        this.children = subBlocks;
    }

    public Block(int lvl, int maxDepth) {
        this.level = lvl;
        this.maxDepth = maxDepth;
        if (lvl < maxDepth) {
            double probability = Math.exp(-0.25 * lvl);
            if (gen.nextDouble() < probability) {
                this.children = new Block[4];
                for (int i = 0; i < 4; i++) {
                    this.children[i] = new Block(lvl + 1, maxDepth);
                }
                this.color = null;
            } else {
                this.color = GameColors.BLOCK_COLORS[gen.nextInt(GameColors.BLOCK_COLORS.length)];
                this.children = new Block[0];
            }
        } else {
            this.color = GameColors.BLOCK_COLORS[gen.nextInt(GameColors.BLOCK_COLORS.length)];
            this.children = new Block[0];
        }
    }

    public void updateSizeAndPosition(int size, int xCoord, int yCoord) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive.");
        }
        int requiredDivisions = this.maxDepth - this.level;
        int minSize = (int) (size / Math.pow(2, requiredDivisions));
        if (size % minSize != 0) {
            throw new IllegalArgumentException("Size must be divisible by 2^" + requiredDivisions);
        }

        this.size = size;
        this.xCoord = xCoord;
        this.yCoord = yCoord;

        if (children.length == 4) {
            int childSize = size / 2;
            children[0].updateSizeAndPosition(childSize, xCoord + childSize, yCoord); // UR
            children[1].updateSizeAndPosition(childSize, xCoord, yCoord); // UL
            children[2].updateSizeAndPosition(childSize, xCoord, yCoord + childSize); // LL
            children[3].updateSizeAndPosition(childSize, xCoord + childSize, yCoord + childSize); // LR
        }
    }

    public ArrayList<BlockToDraw> getBlocksToDraw() {
        ArrayList<BlockToDraw> blocks = new ArrayList<>();
        if (children.length == 0) {
            blocks.add(new BlockToDraw(color, xCoord, yCoord, size, 0));
            blocks.add(new BlockToDraw(GameColors.FRAME_COLOR, xCoord, yCoord, size, 3));
        } else {
            for (Block child : children) {
                blocks.addAll(child.getBlocksToDraw());
            }
        }
        return blocks;
    }

    public Block getSelectedBlock(int x, int y, int lvl) {
        if (x < xCoord || x >= xCoord + size || y < yCoord || y >= yCoord + size) {
            return null;
        }
        if (lvl < level || lvl > maxDepth) {
            throw new IllegalArgumentException("Invalid level");
        }

        if (level == lvl) {
            return this;
        }

        if (children.length == 0) {
            return this;
        }

        for (Block child : children) {
            Block selected = child.getSelectedBlock(x, y, lvl);
            if (selected != null) {
                return selected;
            }
        }

        return null;
    }

    public void reflect(int direction) {
        if (direction != 0 && direction != 1) {
            throw new IllegalArgumentException("Invalid direction");
        }
        if (children.length == 0) {
            return;
        }

        Block[] newChildren = new Block[4];
        if (direction == 0) { // Reflect over main diagonal (swap UL with LR)
            newChildren[0] = children[0]; // UR remains
            newChildren[1] = children[3]; // UL becomes LR
            newChildren[2] = children[2]; // LL remains
            newChildren[3] = children[1]; // LR becomes UL
        } else { // Reflect over anti-diagonal (swap UR with LL)
            newChildren[0] = children[2]; // UR becomes LL
            newChildren[1] = children[1]; // UL remains
            newChildren[2] = children[0]; // LL becomes UR
            newChildren[3] = children[3]; // LR remains
        }
        children = newChildren;

        updateSizeAndPosition(size, xCoord, yCoord);
        for (Block child : children) {
            child.reflect(direction);
        }
    }

    public void rotate(int direction) {
        if (direction != 0 && direction != 1) {
            throw new IllegalArgumentException("Invalid direction");
        }
        if (children.length == 0) {
            return;
        }

        Block[] newChildren = new Block[4];
        if (direction == 0) { // Clockwise
            newChildren[0] = children[1];
            newChildren[1] = children[2];
            newChildren[2] = children[3];
            newChildren[3] = children[0];
        } else { // Counter-clockwise
            newChildren[0] = children[3];
            newChildren[1] = children[0];
            newChildren[2] = children[1];
            newChildren[3] = children[2];
        }
        children = newChildren;

        updateSizeAndPosition(size, xCoord, yCoord);
        for (Block child : children) {
            child.rotate(direction);
        }
    }

    public boolean smash() {
        if (level == 0 || level >= maxDepth) {
            return false;
        }
        children = new Block[4];
        for (int i = 0; i < 4; i++) {
            children[i] = new Block(level + 1, maxDepth);
        }
        color = null;
        updateSizeAndPosition(size, xCoord, yCoord);
        return true;
    }

    public boolean condense() {
        if (level == 0 || children.length != 4) {
            return false;
        }

        Map<Color, Integer> colorCounts = new HashMap<>();
        for (Block child : children) {
            countColors(child, colorCounts);
        }

        Color mostCommon = null;
        int maxCount = 0;
        boolean tie = false;
        for (Map.Entry<Color, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostCommon = entry.getKey();
                maxCount = entry.getValue();
                tie = false;
            } else if (entry.getValue() == maxCount) {
                tie = true;
            }
        }

        if (tie || mostCommon == null) {
            return false;
        }

        children = new Block[0];
        color = mostCommon;
        return true;
    }

    private void countColors(Block block, Map<Color, Integer> counts) {
        if (block.children.length == 0) {
            int area = (int) Math.pow(4, block.maxDepth - block.level);
            counts.put(block.color, counts.getOrDefault(block.color, 0) + area);
        } else {
            for (Block child : block.children) {
                countColors(child, counts);
            }
        }
    }

    public Color[][] flatten() {
        int unitSize = (int) Math.pow(2, maxDepth - level);
        Color[][] grid = new Color[unitSize][unitSize];
        if (children.length == 0) {
            for (int i = 0; i < unitSize; i++) {
                for (int j = 0; j < unitSize; j++) {
                    grid[i][j] = color;
                }
            }
        } else {
            int half = unitSize / 2;
            Color[][] ur = children[0].flatten();
            Color[][] ul = children[1].flatten();
            Color[][] ll = children[2].flatten();
            Color[][] lr = children[3].flatten();

            for (int i = 0; i < half; i++) {
                System.arraycopy(ur[i], 0, grid[i], half, half);
                System.arraycopy(ul[i], 0, grid[i], 0, half);
                System.arraycopy(ll[i], 0, grid[i + half], 0, half);
                System.arraycopy(lr[i], 0, grid[i + half], half, half);
            }
        }
        return grid;
    }

    public int getXCoord() { return xCoord; }
    public int getYCoord() { return yCoord; }
    public int getSize() { return size; }
    public int getLevel() { return level; }
    public int getMaxDepth() { return maxDepth; }
    public Color getColor() { return color; }
    public Block[] getChildren() { return children; }

    public String toString() {
        return String.format("pos=(%d,%d), size=%d, level=%d", xCoord, yCoord, size, level);
    }

    public void printBlock() {
        printBlockIndented(0);
    }

    private void printBlockIndented(int indent) {
        String tabs = "";
        for (int i = 0; i < indent; i++) tabs += "\t";
        if (children.length == 0) {
            System.out.println(tabs + GameColors.colorToString(color) + ", " + this);
        } else {
            System.out.println(tabs + this);
            for (Block child : children) child.printBlockIndented(indent + 1);
        }
    }

    private static void coloredPrint(String message, Color color) {
        System.out.print(GameColors.colorToANSIColor(color));
        System.out.print(message);
        System.out.print(GameColors.ANSI_RESET);
    }

    public void printColoredBlock() {
        Color[][] colorArray = this.flatten();
        for (Color[] colors : colorArray) {
            for (Color value : colors) {
                String colorName = GameColors.colorToString(value).toUpperCase();
                if (colorName.isEmpty()) {
                    colorName = "\u2588";
                } else {
                    colorName = colorName.substring(0, 1);
                }
                coloredPrint(colorName, value);
            }
            System.out.println();
        }
    }

    public BlockToDraw getHighlightedFrame() {
        return new BlockToDraw(GameColors.HIGHLIGHT_COLOR, xCoord, yCoord, size, 5);
    }
}
