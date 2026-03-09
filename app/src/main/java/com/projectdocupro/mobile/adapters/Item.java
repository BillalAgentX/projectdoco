package com.projectdocupro.mobile.adapters;

public class Item {

    String animalName;
    int animalImage;

    public Item(String animalName, int animalImage) {
        this.animalImage = animalImage;
        this.animalName = animalName;
    }

    public String getAnimalName() {
        return animalName;
    }

    public int getAnimalImage() {
        return animalImage;
    }
}