package assignment1;

public class Tile {
    private boolean isCastle;
    private boolean isCamp;
    private boolean onThePath;
    private Tile towardTheCastle;
    private Tile towardTheCamp;
    private Warrior warrior;
    private MonsterTroop troop;

    public boolean isCastle() { return isCastle; }
    public boolean isCamp() { return isCamp; }
    public boolean isOnThePath() { return onThePath; }
    public Tile towardTheCastle() { return towardTheCastle; }
    public Tile towardTheCamp() { return towardTheCamp; }


    public Tile() {
        this.troop = new MonsterTroop();
    }

    public Tile(boolean isCastle, boolean isCamp, boolean onThePath, 
                Tile towardTheCastle, Tile towardTheCamp, 
                Warrior warrior, MonsterTroop troop) {
        this.isCastle = isCastle;
        this.isCamp = isCamp;
        this.onThePath = onThePath;
        this.towardTheCastle = towardTheCastle;
        this.towardTheCamp = towardTheCamp;
        this.warrior = warrior;
        this.troop = troop;
    }

    public void buildCastle() {
         isCastle = true; }
    public void buildCamp() { 
        isCamp = true; }

    public void createPath(Tile towardCastle, Tile towardCamp) {
        if ((towardCastle == null && !isCastle) || 
            (towardCamp == null && !isCamp)) {
            throw new IllegalArgumentException("Invalid path configuration");
        }
        this.towardTheCastle = towardCastle;
        this.towardTheCamp = towardCamp;
        this.onThePath = true;
    }

    public boolean addFighter(Fighter fighter) {
        if (fighter instanceof Warrior) {
            if (isCamp || warrior != null) return false;
            warrior = (Warrior) fighter;
            fighter.setPosition(this);
            return true;
        } else if (fighter instanceof Monster) {
            if (!onThePath) return false;
            troop.addMonster((Monster) fighter);
            fighter.setPosition(this);
            return true;
        }
        return false;
    }

    public boolean removeFighter(Fighter fighter) {
        if (fighter instanceof Warrior) {
            if (warrior == fighter) {
                warrior = null;
                fighter.setPosition(null);
                return true;
            }
            return false;
        } else if (fighter instanceof Monster) {
            boolean removed = troop.removeMonster((Monster) fighter);
            if (removed) fighter.setPosition(null);
            return removed;
        }
        return false;
    }

    public Monster[] getMonsters() { return troop.getMonsters(); }
    public Warrior getWarrior() { return warrior; }
    public int getNumOfMonsters() { return troop.sizeOfTroop(); }
    public Monster getMonster() { return troop.getFirstMonster(); }
    public MonsterTroop getTroop() { return troop; }
}
