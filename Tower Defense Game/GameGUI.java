package assignment1;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class GameGUI extends JFrame {
    public static class NumberOverlayIcon implements Icon {
        private final Icon baseIcon;
        private final String numberText;

        public NumberOverlayIcon(Icon baseIcon, int number) {
            this.baseIcon = baseIcon;
            this.numberText = String.valueOf(number);
        }

        @Override
        public int getIconWidth() {
            return baseIcon.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return baseIcon.getIconHeight();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Paint the full base icon (monster icon)
            baseIcon.paintIcon(c, g, x, y);
            // Draw a semi-transparent overlay covering the entire icon
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(x, y, getIconWidth(), getIconHeight());

            // Draw the monster count centered in the icon
            g.setColor(Color.WHITE);
            Font font = new Font("Arial", Font.BOLD, 14);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(numberText);
            int textHeight = fm.getAscent();
            int textX = x + (getIconWidth() - textWidth) / 2;
            int textY = y + (getIconHeight() + textHeight) / 2;
            g.drawString(numberText, textX, textY);
        }
    }

    public static class MonsterCornerOverlayIcon implements Icon {
        private final Icon baseIcon;      // The underlying tile image (castle, camp, warrior, etc.)
        private final Icon monsterIcon;   // The monster icon to overlay (will be scaled down)
        private final int monsterCount;   // The number of monsters on the tile
        private final int overlaySize;    // The size (in pixels) of the overlay (e.g. 16)

        public MonsterCornerOverlayIcon(Icon baseIcon, Icon monsterIcon, int monsterCount, int overlaySize) {
            this.baseIcon = baseIcon;
            // Scale the monster icon to the desired overlay size.
            this.monsterIcon = scaleIcon(monsterIcon, overlaySize, overlaySize);
            this.monsterCount = monsterCount;
            this.overlaySize = overlaySize;
        }

        private Icon scaleIcon(Icon icon, int width, int height) {
            if (icon instanceof ImageIcon) {
                Image img = ((ImageIcon) icon).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
            return icon;
        }

        @Override
        public int getIconWidth() {
            return baseIcon.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return baseIcon.getIconHeight();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Paint the base icon first.
            baseIcon.paintIcon(c, g, x, y);

            // Position the overlay in the top-right corner.
            int overlayX = x + baseIcon.getIconWidth() - overlaySize;

            // Paint the scaled monster icon in that corner.
            monsterIcon.paintIcon(c, g, overlayX, y);

            // Draw a semi-transparent rectangle over just the bottom half of the overlay.
            int rectHeight = overlaySize / 2;
            int rectY = y + overlaySize - rectHeight;
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(overlayX, rectY, overlaySize, rectHeight);

            // Draw the monster count centered in the rectangle.
            String countText = String.valueOf(monsterCount);
            Font font = new Font("Arial", Font.BOLD, 10);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(countText);
            int textHeight = fm.getAscent();
            int textX = overlayX + (overlaySize - textWidth) / 2;
            int textY = rectY + (rectHeight + textHeight) / 2 - 2;
            g.setColor(Color.WHITE);
            g.drawString(countText, textX, textY);
        }
    }

    // Grid dimensions and icon/button size.
    private static int INITIAL_ROWS, INITIAL_COLS;
    private static int ROWS, COLS;
    private static int CASTLE_ROW = 0, CASTLE_COL = 0;
    private static int CAMP_ROW, CAMP_COL;

    // The model: a 2D array of Tile objects.
    private final Tile[][] board;
    // The view: a parallel 2D array of JButtons.
    private final JButton[][] gridButtons;
    private static final int ICON_SIZE = 32;

    // Enumeration for tool selection.
    private enum Tool { TILE, MONSTER, AXEBRINGER, BLADESWORN, LANCEFORGED }
    private Tool selectedTool = Tool.TILE;

    // Load and scale icons.
    private static final ImageIcon TILE_ICON        = loadAndScaleIcon("images/tile.png");
    private static final ImageIcon CASTLE_ICON      = loadAndScaleIcon("images/castle.png");
    private static final ImageIcon CAMP_ICON        = loadAndScaleIcon("images/camp.png");
    private static final ImageIcon MONSTER_ICON     = loadAndScaleIcon("images/monster.png");
    private static final ImageIcon AXEBRINGER_ICON  = loadAndScaleIcon("images/axebringer.png");
    private static final ImageIcon BLADESWORN_ICON  = loadAndScaleIcon("images/bladesworn.png");
    private static final ImageIcon LANCEFORGED_ICON = loadAndScaleIcon("images/lanceforged.png");
    private static final ImageIcon EXPLOSION_ICON   = loadAndScaleIcon("images/attack.png");

    private int totalNumMonstersToSpawn = 0;
    private final int STARTING_SKILL_POINTS = 120;
    private int currentSkillPoints = STARTING_SKILL_POINTS;
    private final JLabel skillPointsLabel;

    private JTextArea logTextArea;

    /**
     * Utility method to scale an icon to ICON_SIZE×ICON_SIZE.
     */
    private static ImageIcon loadAndScaleIcon(String filepath) {
        java.net.URL imgURL = GameGUI.class.getResource(filepath);
        if (imgURL == null) {
            System.err.println("Couldn't find file: " + filepath);
            return null;
        }
        ImageIcon icon = new ImageIcon(imgURL);
        // Print initial width (this may be -1 if not loaded)
        Image img = icon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(img);
        if (scaledIcon.getIconWidth() <= 0) {
            System.err.println("Error: Failed to load image for icon.");
        }
        return scaledIcon;
    }

    public GameGUI() {
        setTitle("Tower Defence!");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize the model and view arrays.
        updateGridSize(INITIAL_ROWS, INITIAL_COLS);
        board = new Tile[ROWS][COLS];
        gridButtons = new JButton[ROWS][COLS];
        // Create the grid panel (center) using a GridLayout.
        // Panels.
        // Center: grid
        JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                //board[r][c] = new Tile();
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
                final int row = r, col = c;
                btn.addActionListener(_ -> cellClicked(row, col));
                gridButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }
        // Designate special cells.
        board[0][0] = new Tile();
        board[0][0].buildCastle();
        board[ROWS - 1][COLS - 1] = new Tile();
        board[ROWS - 1][COLS - 1].buildCamp();
        updateCell(0, 0);
        updateCell(ROWS - 1, COLS - 1);
        add(gridPanel, BorderLayout.CENTER);

        // Create the toolbar panel (side) with radio buttons.
        // East: toolbar with radio buttons
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
        // Set a preferred size so that it is visible.
        Dimension toolbarSize = new Dimension(ICON_SIZE * 2, ROWS * ICON_SIZE);
        toolbarPanel.setPreferredSize(toolbarSize);
        toolbarPanel.setMinimumSize(toolbarSize);
        // Add a border.
        toolbarPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        Dimension radioSize = new Dimension(ICON_SIZE*2, ICON_SIZE*2);

        // Toolbar radio buttons.
        JRadioButton tileRadio = new JRadioButton();
        tileRadio.setIcon(TILE_ICON);
        tileRadio.setPreferredSize(radioSize);
        tileRadio.setToolTipText("Change to tile placement.");

        JRadioButton monsterRadio = new JRadioButton();
        monsterRadio.setIcon(MONSTER_ICON);
        monsterRadio.setPreferredSize(radioSize);
        monsterRadio.setToolTipText("Change to monster placement.");

        JRadioButton axebringerRadio = new JRadioButton();
        axebringerRadio.setIcon(AXEBRINGER_ICON);
        axebringerRadio.setPreferredSize(radioSize);
        axebringerRadio.setToolTipText("Change to Axebringer placement.");

        JRadioButton bladeswornRadio = new JRadioButton();
        bladeswornRadio.setIcon(BLADESWORN_ICON);
        bladeswornRadio.setPreferredSize(radioSize);
        bladeswornRadio.setToolTipText("Change to Bladesworn placement.");

        JRadioButton lanceforgedRadio = new JRadioButton();
        lanceforgedRadio.setIcon(LANCEFORGED_ICON);
        lanceforgedRadio.setPreferredSize(radioSize);
        lanceforgedRadio.setToolTipText("Change to Lanceforged placement.");

        // Default selection: "Tile" (TILE mode).
        tileRadio.setSelected(true);
        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(tileRadio);
        toolGroup.add(monsterRadio);
        toolGroup.add(axebringerRadio);
        toolGroup.add(bladeswornRadio);
        toolGroup.add(lanceforgedRadio);

        // Update selectedTool on selection.
        tileRadio.addActionListener(_ -> selectedTool = Tool.TILE);
        monsterRadio.addActionListener(_ -> selectedTool = Tool.MONSTER);
        axebringerRadio.addActionListener(_ -> selectedTool = Tool.AXEBRINGER);
        bladeswornRadio.addActionListener(_ -> selectedTool = Tool.BLADESWORN);
        lanceforgedRadio.addActionListener(_ -> selectedTool = Tool.LANCEFORGED);

        // Add radio buttons to toolbar.
        toolbarPanel.add(tileRadio);
        toolbarPanel.add(monsterRadio);
        toolbarPanel.add(axebringerRadio);
        toolbarPanel.add(bladeswornRadio);
        toolbarPanel.add(lanceforgedRadio);

        // Place toolbar on the right side.
        add(toolbarPanel, BorderLayout.EAST);

        // Create the control panel (bottom) with the "Play Game" button.
        // South: "Play Game" button
        JPanel controlPanel = new JPanel();
        JButton playGameButton = new JButton("Play Game");
        playGameButton.addActionListener(_ -> playGame());
        controlPanel.add(playGameButton);
        add(controlPanel, BorderLayout.SOUTH);

        JButton nextTurnButton = new JButton("Next Turn");
        nextTurnButton.addActionListener(_ -> playGameTurn());
        controlPanel.add(nextTurnButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Display the current skill points.
        skillPointsLabel = new JLabel("Skill Points: " + currentSkillPoints);
        controlPanel.add(skillPointsLabel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // minimum height and width
        setMinimumSize(new Dimension(ICON_SIZE * 10, ICON_SIZE * 10));

        createLogWindow();
        appendToLog("Starting up.");
    }

    private void updateGridSize(int rows, int cols) {
        ROWS = rows;
        COLS = cols;
        CASTLE_ROW = 0;
        CASTLE_COL = 0;
        CAMP_ROW = ROWS - 1;
        CAMP_COL = COLS - 1;
    }

    private void createLogWindow() {
        JFrame logFrame = new JFrame("Game Log");

        // Create a non-editable text area wrapped in a scroll pane.
        logTextArea = new JTextArea(15, 50);
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        logFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        logFrame.pack();

        // Position the log window below the main window.
        Point mainLoc = this.getLocation();
        Dimension mainSize = this.getSize();

        int newX = mainLoc.x + (mainSize.width - logFrame.getWidth()) / 2;
        int newY = mainLoc.y + mainSize.height;  // position directly below the main window

        logFrame.setLocation(newX, newY);

        logFrame.setVisible(true);
    }

    public void appendToLog(String message) {
        // Append the new message and a newline
        logTextArea.append(message + "\n");
        // Scroll to the bottom so the latest message is visible.
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    private void updateSkillPoints(double delta) {
        currentSkillPoints += (int) delta;
        skillPointsLabel.setText("Skill Points: " + currentSkillPoints);
        appendToLog("Your skill points are now: " + currentSkillPoints);
    }

    /**
     * Updates the icon of the grid cell at (row, col) based on the Tile’s state.
     */
    private void updateCell(int row, int col) {
        Tile tile = board[row][col];
        if (tile == null) {
            return;
        }
        JButton btn = gridButtons[row][col];

        // Determine the base icon for the tile.
        Icon baseIcon = getBaseIcon(tile);

        // If monsters are present...
        if (tile.getNumOfMonsters() > 0) {
            int count = tile.getNumOfMonsters();
            // If the tile already has a castle, camp, or warrior,
            // overlay a small monster icon in the corner.
            if (tile.isCastle() || tile.isCamp() || tile.getWarrior() != null) {
                Icon compositeIcon = new MonsterCornerOverlayIcon(baseIcon, MONSTER_ICON, count, 16);
                btn.setIcon(compositeIcon);
            } else {
                // Otherwise, the tile is "empty" and the monster icon should cover the full tile.
                Icon fullMonsterIcon = new NumberOverlayIcon(MONSTER_ICON, count);
                btn.setIcon(fullMonsterIcon);
            }
        } else {
            // No monsters: just show the base icon.
            btn.setIcon(baseIcon);
        }
    }

    private static Icon getBaseIcon(Tile tile) {
        Icon baseIcon;
        if (tile.isCastle()) {
            baseIcon = CASTLE_ICON;
        } else if (tile.isCamp()) {
            baseIcon = CAMP_ICON;
        } else if (tile.getWarrior() != null) {
            Warrior w = tile.getWarrior();
            baseIcon = switch (w) {
                case Axebringer _ -> AXEBRINGER_ICON;
                case Bladesworn _ -> BLADESWORN_ICON;
                case Lanceforged _ -> LANCEFORGED_ICON;
                case null, default -> TILE_ICON;
            };
        } else {
            baseIcon = TILE_ICON;
        }
        return baseIcon;
    }

    /**
     * Called when a grid cell is clicked. Depending on the selected tool,
     * either creates a tile or attempts to add a fighter.
     */
    private void cellClicked(int row, int col) {
        Tile tile = board[row][col];
        if (tile == null)
        {
            board[row][col] = new Tile();
            // Check if a full path from castle to camp now exists.
            if (pathFromCastleToCampExists()) {
                appendToLog("A path between the castle and camp now exists, so you can start the game.");
                updateAllPathPointers();
                // Also update the display for all cells, if needed.
                for (int r = 0; r < ROWS; r++) {
                    for (int c = 0; c < COLS; c++) {
                        updateCell(r, c);
                    }
                }
                // random number of monsters to spawn from 1 to 12
                totalNumMonstersToSpawn = (int) (Math.random() * 12) + 1;
                appendToLog(totalNumMonstersToSpawn + " monsters will spawn this game!");
            }
            tile = board[row][col];
        }
        boolean success = false;

        appendToLog("Selected tool: " + selectedTool + " and position: (" + row + ", " + col + ")");

        switch (selectedTool) {
            case TILE:
                appendToLog("Placing a tile.");
                if (tile.getWarrior() != null) {
                    tile.removeFighter(tile.getWarrior());
                }
                for (Monster m : tile.getMonsters()) {
                    tile.removeFighter(m);
                }
                success = true;
                break;
            case MONSTER:
                appendToLog("Placing a monster.");
                if (!tile.isOnThePath()) {
                    JOptionPane.showMessageDialog(this, "Error: Monsters can only be placed on the path.");
                    return;
                }
                try {
                    new Monster(tile, 100, 1, 10);
                    success = true;
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
                break;
            case AXEBRINGER:
                appendToLog("Placing an Axebringer.");
                if (currentSkillPoints < Axebringer.BASE_COST) {
                    JOptionPane.showMessageDialog(this, "You need " + Axebringer.BASE_COST + " skill points to create an Axebringer.");
                    return;
                }
                try {
                    new Axebringer(tile);
                    success = true;
                    updateSkillPoints(-Axebringer.BASE_COST);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
                break;
            case BLADESWORN:
                appendToLog("Placing a Bladesworn.");
                if (currentSkillPoints < Bladesworn.BASE_COST) {
                    JOptionPane.showMessageDialog(this, "You need " + Bladesworn.BASE_COST + " skill points to create a Bladesworn.");
                    return;
                }
                try {
                    new Bladesworn(tile);
                    success = true;
                    updateSkillPoints(-Bladesworn.BASE_COST);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
                break;
            case LANCEFORGED:
                appendToLog("Placing a Lanceforged.");
                if (currentSkillPoints < Lanceforged.BASE_COST) {
                    JOptionPane.showMessageDialog(this, "You need " + Lanceforged.BASE_COST + " skill points to create a Lanceforged.");
                    return;
                }
                String piercingStr = JOptionPane.showInputDialog(this, "Enter piercing power:");
                String rangeStr = JOptionPane.showInputDialog(this, "Enter action range:");
                if (piercingStr == null || rangeStr == null) return;
                try {
                    int piercing = Integer.parseInt(piercingStr);
                    int range = Integer.parseInt(rangeStr);
                    new Lanceforged(tile, piercing, range);
                    success = true;
                    updateSkillPoints(-Lanceforged.BASE_COST);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number format.");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
                break;
        }
        if (success) {
            updateCell(row, col);
        }
    }

    private int getTotalNumMonsters() {
        int total = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile tile = board[r][c];
                if (tile != null) {
                    total += tile.getNumOfMonsters();
                }
            }
        }
        return total;
    }

    private void playGame() {
        // Start the first asynchronous turn.
        playGameTurnAsync(gameEnded -> {
            if (!gameEnded) {
                // Schedule the next turn after a 500 ms delay.
                Timer delayTimer = new Timer(500, e -> {
                    ((Timer)e.getSource()).stop(); // stop the timer (non-repeating)
                    playGame(); // start the next turn
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            } else {
                appendToLog("Game ended!");
            }
        });
    }

    private void updateGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                updateCell(r, c);
            }
        }
    }

    private boolean gameOver = false;
    private boolean checkIfGameOver() {
        // check if game won
        if (getTotalNumMonsters() == 0) {
            JOptionPane.showMessageDialog(this, "The Warriors have successfully defended the castle!");
            gameOver = true;
            return true;
        }

        // check if game lost -- monsters have reached the castle
        if (board[CASTLE_ROW][CASTLE_COL].getNumOfMonsters() > 0 && board[CASTLE_ROW][CASTLE_COL].getWarrior() == null) {
            gameOver = true;
            JOptionPane.showMessageDialog(this, "The Monsters have breached the castle walls!");
            return true;
        }
        return false;
    }

    /**
     * This method collects all fighters from all cells, calls takeAction on each,
     * and then updates every cell. Returns true if the game has ended.
     */
    private void playGameTurn() {
        playGameTurnAsync(gameEnded -> {
            if (gameEnded) {
                appendToLog("Game ended!");
            }
        });
    }

    private void playGameTurnAsync(Consumer<Boolean> turnCompleteCallback) {
        startTurn();  // Collect the fighters for this turn.

        Timer actionTimer = new Timer(500, null);
        actionTimer.addActionListener(_ -> {
            if (gameOver) {
                return;
            }
            if (currentFighterIndex < turnFighters.size()) {
                // Process one fighter's action.
                playSingleAction();

                if (checkIfGameOver()) {
                    // Game over; stop the timer.
                    actionTimer.stop();
                    turnCompleteCallback.accept(true);
                }
            } else {
                // All fighter actions have been processed; stop the timer.
                actionTimer.stop();
                // Check if the game is over.
                boolean gameEnded = checkIfGameOver();
                // Invoke the callback with the result.
                turnCompleteCallback.accept(gameEnded);
            }
        });
        actionTimer.start();
    }

    private final List<Fighter> turnFighters = new ArrayList<>();
    private List<Integer> defeatedFighters = new ArrayList<>();
    private int currentFighterIndex = 0;
    private final HashMap<Tile, List<Integer>> tileMap = new HashMap<>();

    private void startTurn() {
        appendToLog("Starting a new turn.");
        defeatedFighters = new ArrayList<>();

        // decide number of monsters to spawn, from 1 to 5, max of totalNumMonstersToSpawn.
        int numMonstersToSpawn = Math.min((int) (Math.random() * 5) + 1, totalNumMonstersToSpawn);
        totalNumMonstersToSpawn -= numMonstersToSpawn;
        if (totalNumMonstersToSpawn > 0) {
            appendToLog("Spawning " + numMonstersToSpawn + " monsters at the monster camp this turn.");
        }
        for (int i = 0; i < numMonstersToSpawn; i++) {
            Tile tile = board[CAMP_ROW][CAMP_COL];
            if (tile != null) {
                try {
                    new Monster(tile, 100, 1, 10);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Failed to spawn monster: " + ex.getMessage());
                }
            }
        }

        turnFighters.clear();
        appendToLog("Collecting a list of all fighters in the game.");
        // Scan the board for non-null tiles and collect the fighter (if any) and all monsters.
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile tile = board[r][c];
                if (tile == null) {
                    continue;
                }
                tileMap.put(tile, Arrays.asList(r, c));
                if (tile.getWarrior() != null) {
                    turnFighters.add(tile.getWarrior());
                }
                for (Monster m : tile.getMonsters()) {
                    if (m != null) {
                        turnFighters.add(m);
                    }
                }
            }
        }
        currentFighterIndex = 0;
    }

    /**
     * Processes a single fighter's action. Calls takeAction() on one fighter, updates the
     * corresponding cell, and if the fighter gains skill points (indicating an attack) it
     * shows an explosion image briefly before restoring the normal icon.
     */
    private void playSingleAction() {
        if (currentFighterIndex >= turnFighters.size()) {
            // All fighter actions for this turn have been processed.
            return;
        }

        Fighter f = turnFighters.get(currentFighterIndex);
        if (defeatedFighters.contains(System.identityHashCode(f))) {
            // Skip this fighter if it has already been defeated.
            currentFighterIndex++;
            return;
        }

        int gridDelay = 0;
        Tile tile = f.getPosition();
        if (tile != null) {
            appendToLog("Processing action for " + f);

            Warrior tileWarrior = tile.getWarrior();
            Monster[] monsters = tile.getMonsters();

            int skillPoints = f.takeAction();
            List<Integer> pos = tileMap.get(tile);
            int r = pos.get(0);
            int c = pos.get(1);

            if (skillPoints > 0) {
                appendToLog("A warrior attacked a monster and gained " + skillPoints + " skill points!");
                // Show the explosion image for 500ms.
                gridButtons[r][c].setIcon(EXPLOSION_ICON);
                gridButtons[r][c].repaint();

                Timer explosionTimer = new Timer(500, _ -> updateCell(r, c));
                gridDelay = 500;
                explosionTimer.setRepeats(false);
                explosionTimer.start();
                updateSkillPoints(skillPoints);
            } else {
                // No explosion needed; update the cell normally.
                updateCell(r, c);
            }

            // Check if the fighter has been defeated.
            if (tileWarrior != null && tileWarrior.getHealth() <= 0) {
                // add the ID of the defeated fighter.
                defeatedFighters.add(System.identityHashCode(tileWarrior));
                appendToLog("Warrior " + tileWarrior + " has been defeated!");
            }

            for (Monster monster : monsters) {
                // check if this monster is in curMonsters.
                if (monster == null) { continue; }
                if (monster.getHealth() <= 0) {
                    defeatedFighters.add(System.identityHashCode(monster));
                    appendToLog("Monster " + monster + " has been defeated!");
                }
            }
        }

        currentFighterIndex++;

        Timer globalUpdateTimer = new Timer(100+gridDelay, _ -> updateGrid());
        globalUpdateTimer.setRepeats(false);
        globalUpdateTimer.start();

    }

    private Set<Point> collectPathCells() {
        Set<Point> pathCells = new HashSet<>();
        boolean[][] visited = new boolean[ROWS][COLS];
        Queue<Point> queue = new LinkedList<>();
        queue.offer(new Point(CASTLE_ROW, CASTLE_COL));
        visited[CASTLE_ROW][CASTLE_COL] = true;

        int[][] moves = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            pathCells.add(p);
            int r = p.x, c = p.y;
            for (int[] move : moves) {
                int nr = r + move[0], nc = c + move[1];
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS &&
                        !visited[nr][nc] && board[nr][nc] != null) {
                    visited[nr][nc] = true;
                    queue.offer(new Point(nr, nc));
                }
            }
        }
        return pathCells;
    }

    private void updateAllPathPointers() {
        int[][] distToCastle = new int[ROWS][COLS];
        int[][] distToCamp = new int[ROWS][COLS];
        // Initialize all distances to a very high value.
        for (int i = 0; i < ROWS; i++) {
            Arrays.fill(distToCastle[i], Integer.MAX_VALUE);
            Arrays.fill(distToCamp[i], Integer.MAX_VALUE);
        }

        // Run BFS from the castle to compute distances.
        bfsFromPoint(CASTLE_ROW, CASTLE_COL, distToCastle);
        // Run BFS from the camp to compute distances.
        bfsFromPoint(CAMP_ROW, CAMP_COL, distToCamp);

        // Now update tiles on the shortest path.
        Set<Point> pathCells = collectPathCells();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile tile = board[r][c];
                if (tile == null || !pathCells.contains(new Point(r, c)))
                    continue;  // Only update tiles that are part of the path.
                // For endpoints, the pointer in one direction remains null.
                Tile pointerToCastle = tile.isCastle() ? null : findBestNeighborForPointer(r, c, distToCastle);
                Tile pointerToCamp   = tile.isCamp()   ? null : findBestNeighborForPointer(r, c, distToCamp);
                try {
                    tile.createPath(pointerToCastle, pointerToCamp);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Failed to update tile (" + r + "," + c + "): " + ex.getMessage());
                }
            }
        }
    }

    /**
     * A BFS that fills in the distance array with the minimum number of steps from the start point.
     * Only tiles marked as on the path are considered.
     */
    private void bfsFromPoint(int startRow, int startCol, int[][] dist) {
        Queue<int[]> queue = new LinkedList<>();
        dist[startRow][startCol] = 0;
        queue.offer(new int[] {startRow, startCol});
        int[][] moves = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int r = cur[0], c = cur[1], d = dist[r][c];
            for (int[] move : moves) {
                int nr = r + move[0], nc = c + move[1];
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS && board[nr][nc] != null && dist[nr][nc] > d + 1) {
                    dist[nr][nc] = d + 1;
                    queue.offer(new int[] {nr, nc});
                }
            }
        }
    }

    /**
     * For the tile at (row, col), returns the neighbor (among the four cardinal directions)
     * that has the smallest distance (from the provided distance array) compared to the current tile.
     */
    private Tile findBestNeighborForPointer(int row, int col, int[][] dist) {
        Tile bestNeighbor = null;
        int bestDist = dist[row][col];
        int[][] moves = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
        for (int[] move : moves) {
            int nr = row + move[0], nc = col + move[1];
            if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS && board[nr][nc] != null) {
                if (dist[nr][nc] < bestDist) {
                    bestDist = dist[nr][nc];
                    bestNeighbor = board[nr][nc];
                }
            }
        }
        return bestNeighbor;
    }

    private boolean pathFromCastleToCampExists() {
        // Ensure both endpoints have been placed.
        if (board[CASTLE_ROW][CASTLE_COL] == null || board[CAMP_ROW][CAMP_COL] == null) {
            return false;
        }

        // Create a visited array.
        boolean[][] visited = new boolean[ROWS][COLS];
        Queue<int[]> queue = new LinkedList<>();

        // Start from the castle.
        visited[CASTLE_ROW][CASTLE_COL] = true;
        queue.offer(new int[] { CASTLE_ROW, CASTLE_COL });

        // Define moves for the four cardinal directions.
        int[][] moves = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int r = cur[0], c = cur[1];

            // If we have reached the camp, a path exists.
            if (r == CAMP_ROW && c == CAMP_COL) {
                return true;
            }

            // Check all four neighbors.
            for (int[] move : moves) {
                int nr = r + move[0];
                int nc = c + move[1];
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS &&
                        !visited[nr][nc] && board[nr][nc] != null) {
                    visited[nr][nc] = true;
                    queue.offer(new int[] { nr, nc });
                }
            }
        }

        // If we finish the BFS without reaching the camp, then no full path exists.
        return false;
    }


    /**
     * Main method. Before launching the GUI, we initialize static simulation parameters.
     */
    public static void main(String[] args) {

        // Initialize static parameters for fighters.

        INITIAL_ROWS = 6;
        INITIAL_COLS = 6;

        Axebringer.BASE_HEALTH = 120;
        Axebringer.BASE_COST = 50;
        Axebringer.BASE_ATTACK_DAMAGE = 30;

        Bladesworn.BASE_HEALTH = 100;
        Bladesworn.BASE_COST = 40;
        Bladesworn.BASE_ATTACK_DAMAGE = 25;

        Lanceforged.BASE_HEALTH = 110;
        Lanceforged.BASE_COST = 60;
        Lanceforged.BASE_ATTACK_DAMAGE = 20;

        Warrior.CASTLE_DMG_REDUCTION = 0.5;
        Monster.BERSERK_THRESHOLD = 100;

        SwingUtilities.invokeLater(GameGUI::new);
    }
}