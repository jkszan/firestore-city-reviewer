package com.google.firebase.sample.cityreviewer.java;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.sample.cityreviewer.R;
import com.google.firebase.sample.cityreviewer.databinding.DialogFiltersBinding;
import com.google.firebase.sample.cityreviewer.java.model.City;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Dialog Fragment containing filter form.
 */
public class FilterDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "FilterDialog";
    public ArrayList<String> countryList;

    interface FilterListener {

        void onFilter(Filters filters);

    }

    private DialogFiltersBinding mBinding;

    private ArrayAdapter<String> countryAdapter;
    private FilterListener mFilterListener;

    public FilterDialogFragment(ArrayList<String> countryList){
        this.countryList = countryList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DialogFiltersBinding.inflate(inflater, container, false);


        mBinding.buttonSearch.setOnClickListener(this);
        mBinding.buttonCancel.setOnClickListener(this);
        updateCountrySpinner();
        return mBinding.getRoot();
    }

    // Sets contents of country dropdown dynamically
    public void updateCountrySpinner(){
        Spinner countryOptions = (Spinner) this.mBinding.getRoot().findViewById(R.id.spinnerCountry);
        countryAdapter = new ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item, this.countryList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryOptions.setAdapter(countryAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getParentFragment() instanceof FilterListener) {
            mFilterListener = (FilterListener) getParentFragment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }

        dismiss();
    }

    public void onCancelClicked() {
        dismiss();
    }

    @Nullable
    private String getSelectedCountry() {
        String selected = (String) mBinding.spinnerCountry.getSelectedItem();
        if (getString(R.string.value_any_country).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedCity() {
        String selected = mBinding.CityFilterText.getText().toString();
        if (selected.isEmpty()) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedSortBy() {
        String selected = (String) mBinding.spinnerSort.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected)) {
            return City.FIELD_RATING;
        } if (getString(R.string.sort_by_author).equals(selected)) {
            return City.FIELD_AUTHOR;
        } if (getString(R.string.sort_by_date).equals(selected)){
            return City.FIELD_TIME;
        }

        return null;
    }

    @Nullable
    private Query.Direction getSortDirection() {
        String selected = (String) mBinding.spinnerSort.getSelectedItem();

        if (getString(R.string.sort_by_rating).equals(selected)) {
            return Query.Direction.DESCENDING;
        }
        if (getString(R.string.sort_by_author).equals(selected)) {
            return Query.Direction.ASCENDING;
        }

        if (getString(R.string.sort_by_date).equals(selected)){
            return Query.Direction.DESCENDING;
        }

        return null;
    }

    public void resetFilters() {
        if (mBinding != null) {
            mBinding.spinnerCountry.setSelection(0);
            mBinding.CityFilterText.setText("");
            mBinding.spinnerSort.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        if (mBinding != null) {
            filters.setCountry(getSelectedCountry());
            filters.setCity(getSelectedCity());
            filters.setSortBy(getSelectedSortBy());
            filters.setSortDirection(getSortDirection());
        }

        return filters;
    }

    @Override
    public void onClick(View v) {
        //Due to bump in Java version, we can not use view ids in switch
        //(see: http://tools.android.com/tips/non-constant-fields), so we
        //need to use if/else:

        int viewId = v.getId();
        if (viewId == R.id.buttonSearch) {
            onSearchClicked();
        } else if (viewId == R.id.buttonCancel) {
            onCancelClicked();
        }
    }
}
