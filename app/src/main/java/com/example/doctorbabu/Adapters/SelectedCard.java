package com.example.doctorbabu.Adapters;

import java.util.ArrayList;

public class SelectedCard {
    ArrayList<String> cards = new ArrayList<>();
    private SelectedCard(){

    }

    public static SelectedCard instance = null;
    public static SelectedCard getInstance(){
        if(instance == null){
            instance = new SelectedCard();
        }
        return instance;
    }

    public ArrayList<String> getCards() {
        return cards;
    }

    public void setCards(ArrayList<String> cards) {
        this.cards = cards;
    }

    public void resetCards(){
        cards.clear();
    }
}
