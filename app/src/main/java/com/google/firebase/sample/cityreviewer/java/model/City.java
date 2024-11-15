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
    private float rating;

    private String description;

    //private String photo;

    public City (){}

    public City(String author, String city, String country, float rating, String description) {
        this.author = author;
        this.city = city;
        this.country = country;
        this.rating = rating;
        //this.photo = photo;
        this.description = description;
    }

    public String getAuthor(){
        return this.author;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getDescription(){
        return this.description;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getRating(){
        return this.rating;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry(){
        return this.country;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity(){
        return this.city;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getPhoto() {
        return null;
    }
}