package assignment1;

public abstract class Fighter {
    private Tile position;
    private double health;
    private final int weaponType;
    private final int attackDamage;

    public Fighter(Tile position, double health, int weaponType, int attackDamage) {
        this.position = position;
        this.health = health;
        this.weaponType = weaponType;
        this.attackDamage = attackDamage;
        if (position != null && !position.addFighter(this)) {
            throw new IllegalArgumentException("Cannot place fighter on tile");
        }
    }

    public double takeDamage(double rawDamage, int attackerWeaponType) {
        double multiplier = 1.0;
        if (attackerWeaponType > weaponType) multiplier = 1.5;
        else if (attackerWeaponType < weaponType) multiplier = 0.5;
        
        double actualDamage = rawDamage * multiplier;
        health -= actualDamage;
        if (health <= 0 && position != null) {
            position.removeFighter(this);
        }
        return actualDamage;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fighter other = (Fighter) obj;
        return position == other.position && 
               Math.abs(health - other.health) <= 0.001;
    }

    public abstract int takeAction();

    // Getters and setters
    public final Tile getPosition() { return position; }
    public final void setPosition(Tile position) { this.position = position; }
    public final double getHealth() { return health; }
    public final int getWeaponType() { return weaponType; }
    public final int getAttackDamage() { return attackDamage; }
}
