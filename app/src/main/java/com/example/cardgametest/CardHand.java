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
    public void addCard(Card newCard){
        hand.add(newCard);
        size += 1;
    }

    //When player fold, empty their hand
    public void clearHand(){
        hand.clear();
        size = 0;
    }

    //when called, sorts the hand in descending order so aces are last, then it adds the value of
    //cards to a running total and if it hits an ace it checks
    //to see if the running total is greater than 10, if it is than the ace is 1, else the ace is 11
    public int getTotalValue() {
        int value = 0;
        hand.sort(new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                return Integer.compare(o1.getValue(), o2.getValue());
            }
        });

        //Calculate value
        for(int i = 0; i < size; i++){
            if(hand.get(i).getRank() != "Ace"){
                value += hand.get(i).getValue();
            }
            else if (hand.get(i).getRank() == "Ace" && value > 10){
                value += 1;
            }
            else{
                value += 11;
            }
        }

        return value;
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
