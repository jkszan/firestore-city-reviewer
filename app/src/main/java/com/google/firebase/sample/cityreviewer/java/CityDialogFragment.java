package com.google.firebase.sample.cityreviewer.java;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.sample.cityreviewer.R;
import com.google.firebase.sample.cityreviewer.databinding.DialogCityRatingBinding;
import com.google.firebase.sample.cityreviewer.java.model.City;

import java.io.IOException;

public class CityDialogFragment extends DialogFragment implements View.OnClickListener {

        public static final String TAG = "CityReviewDialog";

        private int currentImageIndex;
        private Bitmap[] imageBitMaps;
        private ImageButton[] imageButtons;
        private ActivityResultLauncher<Intent> pickPhoto;

        private DialogCityRatingBinding mBinding;

        interface ReviewListener {

            void onReview(City city);

        }

        private com.google.firebase.sample.cityreviewer.java.CityDialogFragment.ReviewListener mReviewListener;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            mBinding = DialogCityRatingBinding.inflate(inflater, container, false);

            mBinding.cityFormButton.setOnClickListener(this);
            mBinding.cityFormCancel.setOnClickListener(this);
            mBinding.imageButton.setOnClickListener(this);
            mBinding.imageButton2.setOnClickListener(this);
            mBinding.imageButton3.setOnClickListener(this);

            this.imageBitMaps = new Bitmap[3];
            this.imageButtons = new ImageButton[3];
            imageButtons[0] = mBinding.imageButton;
            imageButtons[1] = mBinding.imageButton2;
            imageButtons[2] = mBinding.imageButton3;

            pickPhoto = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == MainActivity.RESULT_OK){
                            Intent data = result.getData();

                            if (data != null && data.getData() != null) {
                                Uri selectedImageUri = data.getData();
                                Bitmap selectedImageBitmap;
                                try {
                                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                            getContext().getContentResolver(),
                                            selectedImageUri);
                                    this.imageButtons[this.currentImageIndex].setImageBitmap(selectedImageBitmap);
                                    this.imageBitMaps[this.currentImageIndex] = selectedImageBitmap;
                                }
                                catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            return mBinding.getRoot();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mBinding = null;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            if (getParentFragment() instanceof CityDialogFragment.ReviewListener) {
                mReviewListener = (com.google.firebase.sample.cityreviewer.java.CityDialogFragment.ReviewListener) getParentFragment();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

        }

        private void onSubmitClicked(View view) {

            // Deny Review if Country or City is not input
            if (mBinding.cityCity.getText().toString().isBlank() || mBinding.cityCountryText.getText().toString().isBlank()){
                return;
            }

            String[] imageDescriptions = new String[3];
            imageDescriptions[0] = mBinding.image1Description.getText().toString();
            imageDescriptions[1] = mBinding.image2Description.getText().toString();
            imageDescriptions[2] = mBinding.image3Description.getText().toString();


            City city = new City(
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                    mBinding.cityCity.getText().toString(),
                    mBinding.cityCountryText.getText().toString(),
                    mBinding.cityRating.getRating(),
                    mBinding.cityReviewText.getText().toString(),
                    imageBitMaps,
                    imageDescriptions);

            if (mReviewListener != null) {
                mReviewListener.onReview(city);
            }

            dismiss();
        }

        private void onCancelClicked(View view) {
            dismiss();
        }

        private void onAddImageClicked(View view, int imageNumber) {
            Intent addImage = new Intent();
            addImage.setType("image/*");
            addImage.setAction(Intent.ACTION_GET_CONTENT);

            this.currentImageIndex = imageNumber;
            pickPhoto.launch(addImage);
        }

        @Override
        public void onClick(View v) {
            //Due to bump in Java version, we can not use view ids in switch
            //(see: http://tools.android.com/tips/non-constant-fields), so we
            //need to use if/else:

            int viewId = v.getId();
            if (viewId == R.id.cityFormButton) {
                onSubmitClicked(v);
            } else if (viewId == R.id.cityFormCancel) {
                onCancelClicked(v);
            }
            if (viewId == R.id.imageButton){
                onAddImageClicked(v, 0);

            }
            else if (viewId == R.id.imageButton2){
                onAddImageClicked(v, 1);
            }
            else if (viewId == R.id.imageButton3){
                onAddImageClicked(v, 2);
            }
        }

    }
