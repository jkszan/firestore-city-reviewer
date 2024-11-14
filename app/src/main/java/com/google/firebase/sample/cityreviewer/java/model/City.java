package com.google.firebase.sample.cityreviewer.java.model;

//import com.google.firebase.firestore.IgnoreExtraProperties;


public class City {
    public static final String FIELD_CITY = "city";
    public static final String FIELD_COUNTRY = "country";
    public static final String FIELD_RATING = "rating";
    public static final String FIELD_AUTHOR = "author";

    private String author;
    private String city;
    private String country;
    private int rating;

    private String description;

    private String photo;

    public City (){}

    public City(String author, String city, String country, int rating, String description, String photo) {
        this.author = author;
        this.city = city;
        this.country = country;
        this.rating = rating;
        this.photo = photo;
        this.description = description;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getDescription(){
        return this.description;
    }

}