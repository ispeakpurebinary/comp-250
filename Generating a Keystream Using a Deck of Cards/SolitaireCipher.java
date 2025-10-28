package assignment2;

public class SolitaireCipher {
    public Deck key;

    public SolitaireCipher(Deck key) {
        this.key = new Deck(key);
    }

    public int[] getKeystream(int size) {
        int[] keystream = new int[size];
        for (int i = 0; i < size; i++) {
            keystream[i] = key.generateNextKeystreamValue();
        }
        return keystream;
    }

    public String encode(String msg) {
        StringBuilder cleaned = new StringBuilder();
        for (char c : msg.toCharArray()) {
            if (Character.isLetter(c)) {
                cleaned.append(Character.toUpperCase(c));
            }
        }
        String processed = cleaned.toString();
        if (processed.isEmpty()) return "";

        int[] keystream = getKeystream(processed.length());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < processed.length(); i++) {
            int shift = keystream[i] % 26;
            char encoded = (char) ((processed.charAt(i) - 'A' + shift + 26) % 26 + 'A');
            result.append(encoded);
        }
        return result.toString();
    }

    public String decode(String msg) {
        StringBuilder cleaned = new StringBuilder();
        for (char c : msg.toCharArray()) {
            if (Character.isLetter(c)) {
                cleaned.append(Character.toUpperCase(c));
            }
        }
        String processed = cleaned.toString();
        if (processed.isEmpty()) return "";

        int[] keystream = getKeystream(processed.length());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < processed.length(); i++) {
            int shift = keystream[i] % 26;
            char decoded = (char) ((processed.charAt(i) - 'A' - shift + 26) % 26 + 'A');
            result.append(decoded);
        }
        return result.toString();
    }
}
