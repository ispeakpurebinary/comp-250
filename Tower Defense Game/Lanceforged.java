package assignment1;

public class Lanceforged extends Warrior {
    public static double BASE_HEALTH;
    public static int BASE_COST;
    public static int WEAPON_TYPE = 3;
    public static int BASE_ATTACK_DAMAGE;
    private int piercingPower;
    private int actionRange;

    public Lanceforged(Tile position, int piercing, int range) {
        super(position, BASE_HEALTH, WEAPON_TYPE, BASE_ATTACK_DAMAGE, BASE_COST);
        this.piercingPower = piercing;
        this.actionRange = range;
    }


    public int takeAction() {
        Tile current = getPosition();
        if (current == null) return 0;

        Tile target = findTarget(current);
        if (target == null) return 0;

        Monster[] monsters = target.getMonsters();
        int count = Math.min(piercingPower, monsters.length);
        double total = 0;

        for (int i = 0; i < count; i++) {
            double damage = monsters[i].takeDamage(getAttackDamage(), WEAPON_TYPE);
            total += (getAttackDamage() / damage) + 1;
        }

        return count > 0 ? (int) (total / count) : 0;
    }

    private Tile findTarget(Tile start) {
        Tile current = start;
        for (int i = 0; i < actionRange; i++) {
            current = current.towardTheCamp();
            if (current == null || current.isCamp()) break;
            if (current.getNumOfMonsters() > 0) return current;
        }
        return null;
    }
}
