package assignment1;

public class MonsterTroop {
    private Monster[] monsters = new Monster[0];
    private int numOfMonsters = 0; // Corrected field name

    public int sizeOfTroop() {
        return numOfMonsters;
    }

    public Monster[] getMonsters() {
        Monster[] result = new Monster[numOfMonsters];
        System.arraycopy(monsters, 0, result, 0, numOfMonsters);
        return result;
    }

    public Monster getFirstMonster() {
        return numOfMonsters > 0 ? monsters[0] : null;
    }

    public void addMonster(Monster m) {
        if (numOfMonsters == monsters.length) {
            int newCapacity = Math.max(1, monsters.length * 2);
            Monster[] newArray = new Monster[newCapacity];
            System.arraycopy(monsters, 0, newArray, 0, numOfMonsters);
            monsters = newArray;
        }
        monsters[numOfMonsters++] = m;
    }

    public boolean removeMonster(Monster m) {
        for (int i = 0; i < numOfMonsters; i++) {
            if (monsters[i] == m) {
                System.arraycopy(monsters, i + 1, monsters, i, numOfMonsters - i - 1);
                monsters[--numOfMonsters] = null;
                return true;
            }
        }
        return false;
    }
}
