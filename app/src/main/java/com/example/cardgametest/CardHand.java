package com.example.cardgametest;

import android.widget.ImageView;
import android.widget.LinearLayout;

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
    private int totalValue;

    //Initializes the hand passed in cards
    public CardHand(){
        hand = new ArrayList<Card>(2);
        size = 0;
        totalValue = 0;
    }

    //When player hits, add a new card to their hand
    public void addCard(Card newCard){
        hand.add(newCard);
        size += 1;
    }

    //When player fold, empty their hand
    public void foldHand(){
        hand.clear();
        size = 0;
        totalValue = 0;
    }

    public ArrayList<Card> retrieveHand(){
        return hand;
    }
    public Card retrieveFirstCard(){
        return hand.get(0);
    }
    public Card get(int i){
        return hand.get(i);
    }

    public int size(){
        return size;
    }
}
