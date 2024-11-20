package com.google.firebase.sample.cityreviewer.java.model;

//import com.google.firebase.firestore.IgnoreExtraProperties;


import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class City {
    public static final String FIELD_CITY = "city";
    public static final String FIELD_COUNTRY = "country";
    public static final String FIELD_RATING = "rating";
    public static final String FIELD_AUTHOR = "author";

    private String author;
    private String author_uid;
    private String city;
    private String country;
    private float rating;

    private Bitmap[] photos;
    private ArrayList<String> photoDescriptions;

    private String description;

    public City (){}

    public City(String author_uid, String author, String city, String country, float rating, String description, Bitmap[] photos, String[] photoDescriptions) {
        this.author_uid = author_uid;
        this.author = author;
        this.city = city;
        this.country = country;
        this.rating = rating;
        this.description = description;
        ArrayList<Bitmap> photoList = new ArrayList<>();
        this.photoDescriptions = new ArrayList<String>();
        for (int i = 0; i < photos.length; i++){
            if (photos[i] != null){
                photoList.add(photos[i]);
                this.photoDescriptions.add(photoDescriptions[i]);
            }
        }
        this.photos = new Bitmap[photoList.size()];
        this.photos = photoList.toArray(this.photos);
    }

    public Bitmap[] getPhotos(){
        return this.photos;
    }


    public ArrayList<String> getPhotoDescriptions(){
        return this.photoDescriptions;
    }

    public String getUID(){
        return this.author_uid;
    }

    public void setUID(String author_uid){
        this.author_uid = author_uid;
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