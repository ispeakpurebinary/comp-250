package assignment1;

public abstract class Warrior extends Fighter {
    public static double CASTLE_DMG_REDUCTION = 0.2;
    private final int requiredSkillPoints;

    public Warrior(Tile position, double health, int weaponType, 
                  int attackDamage, int cost) {
        super(position, health, weaponType, attackDamage);
        this.requiredSkillPoints = cost;
    }

    public double takeDamage(double rawDamage, int attackerWeaponType) {
        if (getPosition() != null && getPosition().isCastle()) {
            rawDamage *= (1 - CASTLE_DMG_REDUCTION);
        }
        return super.takeDamage(rawDamage, attackerWeaponType);
    }

    public int getTrainingCost() { return requiredSkillPoints; }
}
