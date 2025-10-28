package assignment1;

public class Monster extends Fighter {
    public static int BERSERK_THRESHOLD = 5;
    private int rageLevel;

    public Monster(Tile position, double health, int weaponType, int attackDamage) {
        super(position, health, weaponType, attackDamage);
    }

    //public final Tile getPosition() { return super.getPosition(); }

    public double takeDamage(double rawDamage, int attackerWeaponType) {
        double actualDamage = super.takeDamage(rawDamage, attackerWeaponType);
        int diff = attackerWeaponType - getWeaponType();
        if (diff > 0) rageLevel += diff;
        return actualDamage;
    }

        public int takeAction() {
        int actions = rageLevel >= BERSERK_THRESHOLD ? 2 : 1;
        int actionsTaken = 0;
        
        while (actionsTaken < actions) {
            Tile currentTile = getPosition();
            if (currentTile == null) break;

            Warrior w = currentTile.getWarrior();
            if (w != null) {
                w.takeDamage(getAttackDamage(), getWeaponType());
                currentTile.getTroop().removeMonster(this);
                currentTile.getTroop().addMonster(this);
            } else {
                Tile nextTile = currentTile.towardTheCastle();
                if (nextTile != null && nextTile.addFighter(this)) {
                    currentTile.removeFighter(this);
                }
            }
            actionsTaken++;
        }
        
        if (actions > 1) rageLevel = 0;
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        Monster other = (Monster) obj;
        return this.getAttackDamage() == other.getAttackDamage();
    }
}
