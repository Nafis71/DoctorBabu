package com.example.doctorbabu.Adapters;

import java.util.ArrayList;

public class selectedCard {
    ArrayList<String> cards = new ArrayList<>();
    private selectedCard(){

    }

    public static selectedCard instance = null;
    public static selectedCard getInstance(){
        if(instance == null){
            instance = new selectedCard();
        }
        return instance;
    }

    public ArrayList<String> getCards() {
        return cards;
    }

    public void setCards(ArrayList<String> cards) {
        this.cards = cards;
    }
}
