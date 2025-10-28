package assignment1;

public class Bladesworn extends Warrior {
    public static double BASE_HEALTH;
    public static int BASE_COST;
    public static int WEAPON_TYPE = 3;
    public static int BASE_ATTACK_DAMAGE;

    public Bladesworn(Tile position) {
        super(position, BASE_HEALTH, WEAPON_TYPE, BASE_ATTACK_DAMAGE, BASE_COST);
    }

    public int takeAction() {
        Tile current = getPosition();
        if (current == null) return 0;

        int points = 0;
        Monster m = current.getMonster();
        if (m != null) {
            double damage = m.takeDamage(getAttackDamage(), WEAPON_TYPE);
            points = (int) ((getAttackDamage() / damage) + 1);
        } else {
            Tile next = current.towardTheCamp();
            if (next != null && !next.isCamp() && next.getWarrior() == null) {
                current.removeFighter(this);
                next.addFighter(this);
            }
        }
        return points;
    }
}
