package com.example.cardgametest;
import java.lang.reflect.Field;
import java.util.*;

/*
Create a Deck class that will shuffle 6 52-card and has the following functions:
    retrieveTop(); - Retrieve and remove top card from deck
    reset(); - Return all cards to deck and shuffle
    getDeckSize(); - Retrieve the amount of cards in the deck
*/
public class Deck {
    private List<Card> cards;

    public Deck() {
        initializeDeck();
        shuffle();
    }

    // Creates 6 decks of 52 cards
    private void initializeDeck() {
        cards = new ArrayList<Card>();
        for (int i = 0; i < 6; i++) {
            for (int rank = 1; rank <= 13; rank++) {
                int value = rank;
                if (rank > 9) {
                    value = 10;
                }
                for (String suit : Card.SUITS) {
                    cards.add(new Card(rank, value, suit));
                }
            }
        }
    }

    // Returns topmost card from deck
    public Card retrieveTop(){
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty.");
        }
        Card topCard = cards.get(0);
        cards.remove(0);
        return topCard;
    }

    // Resets deck to be completely new
    public void reset() {
        cards.clear();
        initializeDeck();
        shuffle();
    }

    public int getDeckSize() {
        return cards.size();
    }

    // Reshuffles deck
    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> getCards() {
        return cards;
    }

//    public void printCards() {
//        for (int i = 0; i<cards.size(); i++) {
//            System.out.println(cards.get(i).getRank() + " " + cards.get(i).getSuit() + " " + cards.get(i).getValue());
//        }
//    }
}

class Card {
    public static final String[] SUITS = {"clubs", "diamonds", "hearts", "spades"};

    private final int rank;
    private final int value;
    private final String suit;

    public Card(int rank, int value, String suit) {
        this.rank = rank;
        this.value = value;
        this.suit = suit;
    }

    //Create Card Object from String
    public Card(String s) {
        String[] args = s.split(" of ");
        this.suit = args[1];
        switch(args[0]) {
            case "ace":
                this.value = 10;
                this.rank = 1;
                break;
            case "jack":
                this.value = 10;
                this.rank = 11;
                break;
            case "queen":
                this.value = 10;
                this.rank = 12;
                break;
            case "king":
                this.value = 10;
                this.rank = 13;
                break;
            default:
                this.value = Integer.parseInt(args[0]);
                this.rank = Integer.parseInt(args[0]);
        }
    }

    public String getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    public int getCardBack() {
        String filename = "cardbackblack";
        try {
            Field fld = R.drawable.class.getField(filename);
            return fld.getInt(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getCardImage() {
        String filename = "default_" + this.getRank() + "_of_" + this.getSuit();
        try {
            Field fld = R.drawable.class.getField(filename);
            return fld.getInt(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getRank() {
        switch (rank) {
            case 1:
                return "ace";
            case 11:
                return "jack";
            case 12:
                return "queen";
            case 13:
                return "king";
            default:
                return Integer.toString(rank);
        }
    }

    public String toString() {
        return getRank() + " of " + getSuit();
    }
}