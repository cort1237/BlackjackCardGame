package com.example.cardgametest;

import android.content.Context;
import android.widget.ImageView;

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
    public static final String[] SUITS = {"Clubs", "Diamonds", "Hearts", "Spades"};

    private final int rank;
    private final int value;
    private final String suit;

    public Card(int rank, int value, String suit) {
        this.rank = rank;
        this.value = value;
        this.suit = suit;
    }

    public String getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    public String getRank() {
        switch (rank) {
            case 1:
                return "Ace";
            case 11:
                return "Jack";
            case 12:
                return "Queen";
            case 13:
                return "King";
            default:
                return Integer.toString(rank);
        }
    }
}