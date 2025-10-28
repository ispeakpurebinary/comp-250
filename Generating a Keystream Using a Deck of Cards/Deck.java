package assignment2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deck {
    public static String[] suitsInOrder = {"clubs", "diamonds", "hearts", "spades"};
    public static Random gen = new Random();

    public int numOfCards;
    public Card head;

    public abstract class Card {
        public Card next;
        public Card prev;
        public abstract Card getCopy();
        public abstract int getValue();
    }

    public class PlayingCard extends Card {
        public String suit;
        public int rank;

        public PlayingCard(String s, int r) {
            suit = s;
            rank = r;
        }

        public PlayingCard getCopy() {
            return new PlayingCard(suit, rank);
        }

        public int getValue() {
            for (int i = 0; i < suitsInOrder.length; i++) {
                if (suitsInOrder[i].equals(suit)) {
                    return rank + 13 * i;
                }
            }
            return 0;
        }

        public String toString() {
            if (rank == 1) {
                return "A" + Character.toUpperCase(suit.charAt(0));
            } else if (rank > 10) {
                String[] face = {"J", "Q", "K"};
                return face[rank - 11] + Character.toUpperCase(suit.charAt(0));
            }
            return rank + "" + Character.toUpperCase(suit.charAt(0));
        }
    }

    public class Joker extends Card {
        public String redOrBlack;

        public Joker(String c) {
            if (!c.equalsIgnoreCase("red") && !c.equalsIgnoreCase("black")) {
                throw new IllegalArgumentException();
            }
            redOrBlack = c.toLowerCase();
        }

        public Joker getCopy() {
            return new Joker(redOrBlack);
        }

        public int getValue() {
            return numOfCards - 1;
        }

        public String getColor() {
            return redOrBlack;
        }

        public String toString() {
            return (redOrBlack.charAt(0) + "J").toUpperCase();
        }
    }

    public Deck(int numOfCardsPerSuit, int numOfSuits) {
        if (numOfCardsPerSuit < 1 || numOfCardsPerSuit > 13) throw new IllegalArgumentException();
        if (numOfSuits < 1 || numOfSuits > suitsInOrder.length) throw new IllegalArgumentException();

        head = null;
        numOfCards = 0;
        for (int i = 0; i < numOfSuits; i++) {
            String suit = suitsInOrder[i];
            for (int rank = 1; rank <= numOfCardsPerSuit; rank++) {
                addCard(new PlayingCard(suit, rank));
            }
        }
        addCard(new Joker("red"));
        addCard(new Joker("black"));
    }

    public Deck(Deck d) {
        head = null;
        numOfCards = 0;

        if (d.head != null) {
            Card current = d.head;
            do {
                if (current instanceof PlayingCard) {
                    PlayingCard pc = (PlayingCard) current;
                    addCard(this.new PlayingCard(pc.suit, pc.rank));
                } else if (current instanceof Joker) {
                    Joker j = (Joker) current;
                    addCard(this.new Joker(j.redOrBlack));
                }
                current = current.next;
            } while (current != d.head);
        }
    }

    public Deck() {
        head = null;
        numOfCards = 0;
    }

    public void addCard(Card c) {
        if (head == null) {
            head = c;
            c.next = c;
            c.prev = c;
        } else {
            Card tail = head.prev;
            tail.next = c;
            c.prev = tail;
            c.next = head;
            head.prev = c;
        }
        numOfCards++;
    }

    public void shuffle() {
        if (numOfCards <= 1) return;

        Card[] cards = new Card[numOfCards];
        Card current = head;
        for (int i = 0; i < numOfCards; i++) {
            cards[i] = current;
            current = current.next;
        }

        for (int i = numOfCards - 1; i > 0; i--) {
            int j = gen.nextInt(i + 1);
            Card temp = cards[i];
            cards[i] = cards[j];
            cards[j] = temp;
        }

        head = null;
        numOfCards = 0;
        for (Card card : cards) {
            card.next = null;
            card.prev = null;
            addCard(card);
        }
    }

    public Joker locateJoker(String color) {
        if (head == null) return null;
        Card current = head;
        do {
            if (current instanceof Joker) {
                Joker j = (Joker) current;
                if (j.getColor().equalsIgnoreCase(color)) {
                    return j;
                }
            }
            current = current.next;
        } while (current != head);
        return null;
    }

    public void moveCard(Card c, int p) {
        if (c == null || numOfCards <= 1 || p <= 0) return;
        int actualPos = p % (numOfCards - 1); 
        if (actualPos == 0) return;

        if (c == head) {
            Card current = head.next;
            for (int i = 0; i < actualPos; i++) {
                Card next = current.next;
                current.prev.next = current.next;
                current.next.prev = current.prev;
                addCard(current);
                numOfCards--; 
                current = next;
            }
        } else {
            c.prev.next = c.next;
            c.next.prev = c.prev;

            Card newPrev = c;
            for (int i = 0; i < actualPos; i++) {
                newPrev = newPrev.next;
            }

            Card newNext = newPrev.next;
            newPrev.next = c;
            c.prev = newPrev;
            c.next = newNext;
            newNext.prev = c;
        }
    }

    public void tripleCut(Card firstCard, Card secondCard) {
        if (head == null || numOfCards <= 3 || firstCard == secondCard) return;

        Card beforeFirst = firstCard.prev;
        Card afterSecond = secondCard.next;

        beforeFirst.next = afterSecond;
        afterSecond.prev = beforeFirst;

        Card headPrev = head.prev;
        headPrev.next = firstCard;
        firstCard.prev = headPrev;

        secondCard.next = head;
        head.prev = secondCard;

        head = (afterSecond == head) ? firstCard : afterSecond;
    }

    public void countCut() {
        if (numOfCards <= 1) return;
        Card lastCard = head.prev;
        int k = lastCard.getValue() % numOfCards;
        if (k == 0) return;

        // Find the split point
        Card splitPoint = head;
        for (int i = 0; i < k; i++) {
            splitPoint = splitPoint.next;
        }

        Card topStart = head;
        Card topEnd = splitPoint.prev;

        // Disconnect the top part
        topStart.prev.next = splitPoint;
        splitPoint.prev = topStart.prev;

        // Insert the top part before lastCard
        Card beforeLast = lastCard.prev;
        beforeLast.next = topStart;
        topStart.prev = beforeLast;
        topEnd.next = lastCard;
        lastCard.prev = topEnd;

        // Update head
        head = splitPoint;
    }

    public Card lookUpCard() {
        if (numOfCards == 0) return null;
        int count = head.getValue() % numOfCards;
        if (count == 0) count = numOfCards;

        Card current = head;
        for (int i = 1; i < count; i++) {
            current = current.next;
        }
        Card result = current.next;
        return (result instanceof Joker) ? null : result;
    }

    public int generateNextKeystreamValue() {
        while (true) {
            // Step 1: Move Red Joker
            Joker red = locateJoker("red");
            moveCard(red, 1);

            // Step 2: Move Black Joker
            Joker black = locateJoker("black");
            moveCard(black, 2);

            // Step 3: Triple Cut
            Card current = head;
            Card firstJoker = null;
            Card secondJoker = null;
            do {
                if (current instanceof Joker) {
                    if (firstJoker == null) {
                        firstJoker = current;
                    } else {
                        secondJoker = current;
                        break;
                    }
                }
                current = current.next;
            } while (current != head);

            // Handle case with two jokers
            if (secondJoker == null) {
                current = head;
                do {
                    if (current instanceof Joker) {
                        if (firstJoker == null) {
                            firstJoker = current;
                        } else {
                            secondJoker = current;
                            break;
                        }
                    }
                    current = current.next;
                } while (current != head);
            }

            // Determine order of jokers
            boolean firstBeforeSecond = false;
            current = head;
            do {
                if (current == firstJoker) {
                    firstBeforeSecond = true;
                    break;
                } else if (current == secondJoker) {
                    break;
                }
                current = current.next;
            } while (current != head);

            if (!firstBeforeSecond) {
                Card temp = firstJoker;
                firstJoker = secondJoker;
                secondJoker = temp;
            }

            tripleCut(firstJoker, secondJoker);

            // Step 4: Count Cut
            countCut();

            // Step 5: Look Up Card
            Card resultCard = lookUpCard();
            if (resultCard != null) {
                return resultCard.getValue();
            }
        }
    }

    public String toString() {
        if (head == null) return "";
        StringBuilder sb = new StringBuilder();
        Card current = head;
        do {
            sb.append(current.toString()).append(" ");
            current = current.next;
        } while (current != head);
        return sb.toString().trim();
    }
}
