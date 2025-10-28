package assignment1;
public class Axebringer extends Warrior {
    public static double BASE_HEALTH = 120;
    public static int BASE_COST = 100;
    public static int WEAPON_TYPE = 2;
    public static int BASE_ATTACK_DAMAGE;
    private int cooldown;

    public Axebringer(Tile position) {
        super(position, BASE_HEALTH, WEAPON_TYPE, BASE_ATTACK_DAMAGE, BASE_COST);
    }

    public int takeAction() {
        if (cooldown > 0) {
            cooldown--;
            return 0;
        }

        Tile current = getPosition();
        if (current == null) return 0;

        int points = 0;
        Monster m = current.getMonster();
        if (m != null) {
            double damage = m.takeDamage(getAttackDamage(), WEAPON_TYPE);
            points = (int) ((getAttackDamage() / damage) + 1);
        } else {
            Tile next = current.towardTheCamp();
            if (next != null && !next.isCamp() && next.getMonster() != null) {
                double damage = next.getMonster().takeDamage(getAttackDamage(), WEAPON_TYPE);
                points = (int) ((getAttackDamage() / damage) + 1);
                cooldown = 1;
            }
        }
        return points;
    }
}
