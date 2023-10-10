package com.example.cardgametest;

import java.util.*;


/*
Create a CardHand class that will store the values of the players hand and implements the following:
    retrieveHand(): return the cards in the player hands
    foldHand(): folds the players cards, stops them from playing
    hit(): draws another card into the players hand
 */
public class CardHand {
    private ArrayList<Card> hand;
    private int size;

    //Initializes the hand passed in cards
    public CardHand(){
        hand = new ArrayList<Card>(2);
        size = 0;
    }

    //When player hits, add a new card to their hand
    public void hit(Card newCard){
        hand.add(newCard);
        size += 1;
    }

    //When player fold, empty their hand
    public void foldHand(){
        hand.clear();
    }

    public ArrayList<Card> retrieveHand(){
        return hand;
    }

    public int size(){
        return size;
    }
}
