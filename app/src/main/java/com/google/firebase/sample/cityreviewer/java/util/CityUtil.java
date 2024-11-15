package com.google.firebase.sample.cityreviewer.java.util;

import android.content.Context;

import com.google.firebase.sample.cityreviewer.R;
import com.google.firebase.sample.cityreviewer.java.model.City;

import java.util.Arrays;
import java.util.Random;

public class CityUtil {

    public static City getRandom(Context context) {
        City city = new City();
        Random random = new Random();


        String[] cities = context.getResources().getStringArray(R.array.cities);
        cities = Arrays.copyOfRange(cities, 1, cities.length);
        int index = random.nextInt(cities.length);
        city.setCity(cities[index]);

        String[] countries = context.getResources().getStringArray(R.array.countries);
        countries = Arrays.copyOfRange(countries, 1, countries.length);
        int countryIndex = random.nextInt(countries.length);
        city.setCountry(countries[countryIndex]);

        int rating = random.nextInt(10) + 1;
        float pointRating = (float) rating / 2;
        city.setRating(pointRating);

        city.setAuthor("Anonymous");
        city.setDescription("Sample Description");

        return city;

    }

}
