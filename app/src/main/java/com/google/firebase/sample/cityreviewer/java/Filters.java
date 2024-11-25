package com.google.firebase.sample.cityreviewer.java;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.sample.cityreviewer.R;
import com.google.firebase.sample.cityreviewer.java.model.City;
import com.google.firebase.sample.cityreviewer.java.util.CityUtil;
import com.google.firebase.firestore.Query;

/**
 * Object for passing filters around.
 */
public class Filters {

    private String city = null;
    private String country = null;
    private String author = null;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(City.FIELD_TIME);
        filters.setSortDirection(Query.Direction.DESCENDING);

        return filters;
    }

    public boolean hasCountry() {
        return !(TextUtils.isEmpty(country));
    }

    public boolean hasCity() {
        return !(TextUtils.isEmpty(city));
    }

    public boolean hasAuthor() {
        return !(TextUtils.isEmpty(author));
    }

    public boolean hasSortBy() {
        return !(TextUtils.isEmpty(sortBy));
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (country == null && city == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_cities));
            desc.append("</b>");
        }

        if (city != null) {
            desc.append("<b>");
            desc.append(city);
            desc.append("</b>");
        }


        if (country != null && city != null) {
            desc.append(" in ");
        }


        if (country != null) {
            desc.append("<b>");
            desc.append(country);
            desc.append("</b>");
        }
        if (author != null) {
            desc.append(" by ");
            desc.append("<b>");
            desc.append(author);
            desc.append("</b>");
        }

        return desc.toString();
    }

    public String getOrderDescription(Context context) {
        if (City.FIELD_RATING.equals(sortBy)) {
            return context.getString(R.string.sorted_by_rating);
        } else if (City.FIELD_AUTHOR.equals(sortBy)) {
            return context.getString(R.string.sorted_by_author);
        } else {
            return context.getString(R.string.sorted_by_date);
        }
    }
}
