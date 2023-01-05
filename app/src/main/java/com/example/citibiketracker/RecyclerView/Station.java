package com.example.citibiketracker.RecyclerView;

public class Station {
    String name;
    String ID;
    Boolean isFavorite;

    public Station(String name, String ID) {
        this.name = name;
        this.ID = ID;
        this.isFavorite=false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public String setFavorite(Boolean favorite) {
        isFavorite = favorite;
        return null;
    }
}
