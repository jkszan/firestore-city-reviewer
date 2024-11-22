package com.google.firebase.sample.cityreviewer.java;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.sample.cityreviewer.R;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.sample.cityreviewer.databinding.FragmentCityReviewBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class CityReviewFragment extends Fragment
        implements EventListener<DocumentSnapshot>, View.OnClickListener {

    private static final String TAG = "CityDetail";

    private FragmentCityReviewBinding mBinding;

    //private PhotoDescAdapter mAdapter;

    private FirebaseFirestore mFirestore;

    private FirebaseStorage mStorage;
    private DocumentReference mCityRef;
    private ListenerRegistration mCityRegistration;

    private ImageView[] imageCodex;
    private TextView[] imageDescriptionCodex;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCityReviewBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.cityButtonBack.setOnClickListener(this);

        String cityId = CityReviewFragmentArgs.fromBundle(getArguments()).getKeyCityId();

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        // Get reference to the city
        mCityRef = mFirestore.collection("cities").document(cityId);
        imageCodex = new ImageView[3];
        imageCodex[0] = mBinding.photoView;
        imageCodex[1] = mBinding.photoView2;
        imageCodex[2] = mBinding.photoView3;

        imageDescriptionCodex = new TextView[3];
        imageDescriptionCodex[0] = mBinding.photoDescription;
        imageDescriptionCodex[1] = mBinding.photoDescription2;
        imageDescriptionCodex[2] = mBinding.photoDescription3;

    }

    @Override
    public void onStart() {
        super.onStart();

        mCityRegistration = mCityRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mCityRegistration != null) {
            mCityRegistration.remove();
            mCityRegistration = null;
        }
    }

    /**
     * Listener for the City document ({@link #mCityRef}).
     */
    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "city:onEvent", e);
            return;
        }

        onCityLoaded(snapshot);
    }

    private void setImage(StorageReference pathReference, final int imagePosition){
        pathReference.getBytes(10*1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap photoBitMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageCodex[imagePosition].setImageBitmap(photoBitMap);
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

    private void setPhotoBindingsFromStorage(ArrayList<String> photoURIs){

        StorageReference storageRef = mStorage.getReference();
        for (int i = 0; i < photoURIs.size(); i++) {
            StorageReference pathReference = storageRef.child(photoURIs.get(i));
            setImage(pathReference, i);
        }
    }
    private void onCityLoaded(DocumentSnapshot city) {
        Map<String, Object> cityData = city.getData();
        mBinding.cityReviewName.setText(cityData.get("city").toString());
        mBinding.cityReviewRating.setRating(((Double) cityData.get("rating")).floatValue());
        mBinding.cityReviewCountry.setText(cityData.get("country").toString());
        mBinding.cityReviewAuthor.setText(cityData.get("author").toString());
        mBinding.cityReviewDescription.setText(cityData.get("description").toString());
        setPhotoBindingsFromStorage((ArrayList<String>) cityData.get("photoURLs"));

        ArrayList<String> photoDescriptions = (ArrayList<String>) cityData.get("photoDescriptions");

        for (int i = 0; i < photoDescriptions.size(); i++){
            imageDescriptionCodex[i].setText(photoDescriptions.get(i));
        }
    }


    public void onBackArrowClicked(View view) {
        NavHostFragment.findNavController(this).popBackStack();
    }


    @Override
    public void onClick(View v) {
        //Due to bump in Java version, we can not use view ids in switch
        //(see: http://tools.android.com/tips/non-constant-fields), so we
        //need to use if/else:

        int viewId = v.getId();
        if (viewId == R.id.cityButtonBack) {
            onBackArrowClicked(v);
        }
    }

}