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
    private ArrayList<Card> secondHand;
    private int size;
    private boolean split = false;

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
        ArrayList<Card> temp = hand;
        temp.sort(new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                return Integer.compare(o2.getValue(), o1.getValue());
            }
        });

        //Calculate value
        for(int i = 0; i < size; i++){
            if(temp.get(i).getRank() != "Ace"){
                value += temp.get(i).getValue();
            }
            else if (temp.get(i).getRank() == "Ace" && value > 10){
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
    public ArrayList<Card> retrieveHand(int n){
        if(n == 0){
            return hand;
        } else{
            return secondHand;
        }
    }
    public Card retrieveFirstCard(){
        return hand.get(0);
    }
    public Card get(int i){
        return hand.get(i);
    }
    public boolean isPair(){
        boolean pair = false;
        if(hand.size() == 2 && hand.get(0).getValue() == hand.get(1).getValue()){
            pair = true;
        }
        return pair;
    }
    public boolean splitHand(){
        if(isPair() && size == 2) {
            secondHand = new ArrayList<Card>(2);
            Card temp = hand.remove(0);
            secondHand.add(temp);
            split = true;
            return true;
        }
        return false;
    }

    public boolean isSplit(){
        return split;
    }

    public int size(){
        return size;
    }
}
