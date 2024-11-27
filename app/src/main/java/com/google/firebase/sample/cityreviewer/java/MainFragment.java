package com.google.firebase.sample.cityreviewer.java;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.sample.cityreviewer.R;
import com.google.firebase.sample.cityreviewer.databinding.FragmentMainBinding;
import com.google.firebase.sample.cityreviewer.databinding.ItemCityBinding;
import com.google.firebase.sample.cityreviewer.java.adapter.CityAdapter;
import com.google.firebase.sample.cityreviewer.java.model.City;
import com.google.firebase.sample.cityreviewer.java.util.CityUtil;
import com.google.firebase.sample.cityreviewer.java.viewmodel.MainActivityViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class MainFragment extends Fragment implements
        FilterDialogFragment.FilterListener,
        CityDialogFragment.ReviewListener,
        CityAdapter.OnCitySelectedListener, View.OnClickListener,
        MenuProvider {

    private static final String TAG = "MainActivity";

    private static final int LIMIT = 50;

    private FragmentMainBinding mBinding;

    private FirebaseFirestore mFirestore;

    private FirebaseStorage imageStorage;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;

    private CityDialogFragment mReviewDialog;

    private CityAdapter mAdapter;

    private ActivityResultLauncher<Intent> signinLauncher;
    private MainActivityViewModel mViewModel;

    private ArrayList<String> uniqueCountries;
    private MenuHost menuHost;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        signinLauncher = requireActivity()
                .registerForActivityResult(new FirebaseAuthUIActivityResultContract(),
                        this::onSignInResult
                );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMainBinding.inflate(inflater, container, false);

        // MenuProvider
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.filterBar.setOnClickListener(this);
        mBinding.buttonClearFilter.setOnClickListener(this);
        if (menuHost == null) {
            menuHost = requireActivity();
            menuHost.addMenuProvider(this);
        }


        // View model
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();
        imageStorage = FirebaseStorage.getInstance();

        // TODO: Use basic strategy with a where clause for user posts/community posts
        // Get ${LIMIT} cities
        mQuery = mFirestore.collection("cities")
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new CityAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mBinding.recyclerCities.setVisibility(View.GONE);
                    mBinding.viewEmpty.setVisibility(View.VISIBLE);
                } else {
                    mBinding.recyclerCities.setVisibility(View.VISIBLE);
                    mBinding.viewEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(mBinding.getRoot(),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mBinding.recyclerCities.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.recyclerCities.setAdapter(mAdapter);
        uniqueCountries = new ArrayList<String>();
        uniqueCountries.add("All countries");
        populateCountrySet();

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment(uniqueCountries);
        mReviewDialog = new CityDialogFragment();
    }

    private void populateCountrySet() {

        mFirestore.collection("countries")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String newCountry = document.getId();
                                if (!uniqueCountries.contains(newCountry)){
                                    uniqueCountries.add(newCountry);
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        // Apply filters
        onFilter(mViewModel.getFilters());

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        //Due to bump in Java version, we can not use view ids in switch
        //(see: http://tools.android.com/tips/non-constant-fields), so we
        //need to use if/else:

        int itemId = item.getItemId();
        if (itemId == R.id.menu_add_items) {
            //onAddItemsClicked(); //TODO: Either re-enable or remove from UI
            return true;
        } else if (itemId == R.id.menu_sign_out) {
            //FirebaseAuth.getInstance().signOut();
            AuthUI.getInstance().signOut(requireContext());
            startSignIn();
            return true;
        } else if (itemId == R.id.menu_write_review) {
            onWriteReviewClicked();
        }
        return false;
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        mViewModel.setIsSigningIn(false);

        if (result.getResultCode() != Activity.RESULT_OK) {
            if (response == null) {
                // User pressed the back button.
                requireActivity().finish();
            } else if (response.getError() != null
                    && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSignInErrorDialog(R.string.message_no_network);
            } else {
                showSignInErrorDialog(R.string.message_unknown);
            }
        }
    }

    public void onFilterClicked() {
        // Show the dialog containing filter options
        populateCountrySet();
        mFilterDialog.countryList = uniqueCountries;
        mFilterDialog.show(getChildFragmentManager(), FilterDialogFragment.TAG);
    }

    public void onClearFilterClicked() {
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }

    @Override
    public void onCitySelected(DocumentSnapshot city) {

        // Go to the details page for the selected restaurant
        MainFragmentDirections.ActionMainFragmentToCityReviewFragment action = MainFragmentDirections
                .actionMainFragmentToCityReviewFragment(city.getId());

        NavHostFragment.findNavController(this)
                .navigate(action);
    }

    @Override
    public void onDeleteCityClicked(DocumentSnapshot city) {
        String documentPath = city.getReference().getPath();
        mFirestore.document(documentPath)
            .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Document deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to delete document", e);
                    }
                });
    }

    private Query startsWith(Query query, String field, String pattern){
        char lastChar = pattern.charAt(pattern.length()-1);
        lastChar++;
        String patternBound = pattern.substring(0, pattern.length()-1) + lastChar;
        query = query.whereGreaterThanOrEqualTo(field, pattern);
        query = query.whereLessThan(field, patternBound);
        return query;

    }

    @Override
    public void onFilter(Filters filters) {
        // Construct query basic query
        Query query = mFirestore.collection("cities");

        // Country (equality filter)
        if (filters.hasCountry()) {
            query = query.whereEqualTo(City.FIELD_COUNTRY, filters.getCountry());
        }

        // City (starts with filter)
        if (filters.hasCity()) {
            query = startsWith(query, City.FIELD_CITY, filters.getCity());
        }

        // Author (starts with filter)
        if (filters.hasAuthor()) {
            query = startsWith(query, City.FIELD_AUTHOR, filters.getAuthor());
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mAdapter.setQuery(query);

        // Set header
        mBinding.textCurrentSearch.setText(HtmlCompat.fromHtml(filters.getSearchDescription(requireContext()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        mBinding.textCurrentSortBy.setText(filters.getOrderDescription(requireContext()));

        // Save filters
        mViewModel.setFilters(filters);
    }

    private boolean shouldStartSignIn() {
        FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();
        System.err.println("USER ID:" + curUser);
        if (curUser != null){
            System.err.println("Email:" + curUser.getEmail());
        }
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void startSignIn() {
        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        )).setIsSmartLockEnabled(false)
        .setTheme(R.style.AppTheme) //TODO: Fix this to include Sign-in banner
        .build();

        signinLauncher.launch(intent);
        mViewModel.setIsSigningIn(true);
    }

    private void addNewCountry(String country){
        WriteBatch newCountry = mFirestore.batch();
        DocumentReference countryRef = mFirestore.collection("countries").document(country);
        newCountry.set(countryRef, new HashMap<>());
        newCountry.commit();
        uniqueCountries.add(country);
    }

    private void onAddItemsClicked(){
        WriteBatch batch = mFirestore.batch();
        for (int i = 0; i < 10; i++) {
            DocumentReference cityRef = mFirestore.collection("cities").document();
            City cityReview = CityUtil.getRandom(requireContext());
            System.err.println("CITY DOCS:" + cityReview.getCity() + cityReview.getCountry());
            cityReview.setTimeCreated(new Date());
            batch.set(cityRef, cityReview);
            if (!uniqueCountries.contains(cityReview.getCountry())){
                addNewCountry(cityReview.getCountry());
            }
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch succeeded.");
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });
    }

    private void showSignInErrorDialog(@StringRes int message) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_sign_in_error)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.option_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                      startSignIn();
                    }
                })
                .setNegativeButton(R.string.option_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requireActivity().finish();
                    }
                }).create();

        dialog.show();
    }

    public void onWriteReviewClicked() {
        mReviewDialog.show(getChildFragmentManager(), CityDialogFragment.TAG);
    }

    @Override
    public void onClick(View v) {
        //Due to bump in Java version, we can not use view ids in switch
        //(see: http://tools.android.com/tips/non-constant-fields), so we
        //need to use if/else:

        int viewId = v.getId();
        if (viewId == R.id.filterBar) {
            onFilterClicked();
        } else if (viewId == R.id.buttonClearFilter) {
            onClearFilterClicked();
        }
    }

    public void retrieveIcon(String iconPath, ItemCityBinding binding){
        StorageReference storageRef = imageStorage.getReference();

        StorageReference pathReference = storageRef.child(iconPath);
        pathReference.getBytes(10*1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap photoBitMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                Glide.with(binding.cityItemImage.getContext())
                        .load(photoBitMap) // TODO: Fix to grab an icon photo
                        .into(binding.cityItemImage);
                // Data for "images/island.jpg" is returns, use this as needed
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.err.println("Failed to injest photos");
                // Handle any errors
            }
        });
    }
    private ArrayList<String> storeImages(City city){

        Bitmap[] photos = city.getPhotos();
        ArrayList<String> photoUrls = new ArrayList<>();

        StorageReference imageRef = imageStorage.getReference();

        if (photos.length > 0){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            photos[0].compress(Bitmap.CompressFormat.PNG, 25, outputStream);
            StorageReference fullImageRef = imageRef.child("cityPhotos/"+city.getUID()+"/"+city.getCity()+"/icon");
            fullImageRef.putBytes(outputStream.toByteArray());
            city.setIconPath(fullImageRef.getPath());
        }

        for (Bitmap photo: photos){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            StorageReference fullImageRef = imageRef.child("cityPhotos/" + city.getUID()+"/"+city.getCity()+"/"+photoUrls.size());
            fullImageRef.putBytes(outputStream.toByteArray());
            photoUrls.add(fullImageRef.getPath());
        }
        return photoUrls;

    }
    public void onReview(City city) {
        // Create reference for new rating, for use inside the transaction
        final DocumentReference cityRef = mFirestore.collection("cities").document();
        ArrayList<String> imageUrls = storeImages(city);
        HashMap<String, Object> cityData = new HashMap<String, Object>();
        cityData.put("city", city.getCity());
        cityData.put("country", city.getCountry());
        cityData.put("photoURLs", imageUrls);
        cityData.put("iconPath", city.getIconPath());
        cityData.put("rating", city.getRating());
        cityData.put("photoDescriptions", city.getPhotoDescriptions());
        cityData.put("description", city.getDescription());
        cityData.put("UID", city.getUID());
        cityData.put("author", city.getAuthor());
        cityData.put("timeCreated", new Date());

        WriteBatch batch = mFirestore.batch();
        batch.set(cityRef, cityData);
        batch.commit();
        if (!uniqueCountries.contains(city.getCountry())){
            addNewCountry(city.getCountry());
        }
    }

}
