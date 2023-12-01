package com.example.cardgametest;
import java.util.*;


/*
Create a CardHand class that will store the values of the players hand and implements the following:
    retrieveHand(): return the cards in the player hands
    foldHand(): folds the players cards, stops them from playing
    hit(): draws another card into the players hand
 */
public class CardHand {
    private final ArrayList<Card> hand;
    private final ArrayList<Card> secondHand;
    private int size;
    private int secondHandSize;
    private boolean split = false;

    //Initializes the hand passed in cards
    public CardHand(){
        hand = new ArrayList<>(2);
        secondHand = new ArrayList<>(2);
        size = 0;
    }

    //When player hits, add a new card to their hand
    //modified addCard to be used when hands are split
    public void addCard(Card newCard, int n){
        if(n == 0){
            hand.add(newCard);
            size = hand.size();
        }
        else{
            secondHand.add(newCard);
            secondHandSize = secondHand.size();
        }
    }


    //When player fold, empty their hand
    public void clearHand(){
        hand.clear();
        secondHand.clear();
        split = false;
        size = 0;
    }

    //when called, sorts the hand in descending order so aces are last, then it adds the value of
    //cards to a running total and if it hits an ace it checks
    //to see if the running total is greater than 10, if it is than the ace is 1, else the ace is 11
    public int getTotalValue() {
        int value = 0;
        int aceCounter = 0;
        ArrayList<Card> temp = new ArrayList<>(hand);
        temp.sort((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()));

        //Calculate value
        for(int i = 0; i < size; i++){
            if(!temp.get(i).getRank().equals("ace")){
                value += temp.get(i).getValue();
            }
            else{
                aceCounter += 1;
            }
        }
        if(aceCounter != 0){
            if(value < 11){
                value += 11;
                aceCounter = aceCounter -1;
            }
            value += aceCounter;
        }

        return value;
    }

    //copy of getTotalValue that is only used for split hands
    public int getTotalValue(int n) {
        int value = 0;
        ArrayList<Card> temp;
        if(n == 0) {
            temp = new ArrayList<>(hand);
        } else {
            temp = new ArrayList<>(secondHand);
        }

        //Sort Aces to back using getRank
        temp.sort((o1, o2) -> {
            if(o1.getRank().equals("ace")){
                return 1;
            }
            else if(o2.getRank().equals("ace")){
                return -1;
            }
            else{
                return Integer.compare(o2.getValue(), o1.getValue());
            }
        });

        //Calculate value for all but aces
        int i = 0;
        while(i < temp.size()){
            if(temp.get(i).getRank().equals("ace"))
                break;
            value += temp.get(i).getValue();
            i++;
        }
        //Calculate value for aces making the best combo for the player
        for(int j = i; j < temp.size(); j++){
            int needed = 21 - value;
            int cardsLeft = temp.size() - j + 1;

            //If the player can fit an 11 in their hand with any remaining aces being 1, then the ace is 11
            if(needed > 11 && (needed-11-(cardsLeft-1)) >= 0){
                value += 11;
            }
            //Else the ace is 1
            else{
                value += 1;
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
    public Card get(int i){
        return hand.get(i);
    }
    public boolean isPair(){
        return hand.size() == 2 && hand.get(0).getValue() == hand.get(1).getValue();
    }
    public boolean splitHand(){
        if(isPair() && size == 2) {
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

    public int size(int s){
        if(s == 0)
            return size;
        else
            return secondHandSize;
    }

    public int size(){
        return size(0);
    }
}
