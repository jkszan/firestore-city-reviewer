package com.google.firebase.sample.cityreviewer.java;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.sample.cityreviewer.R;
import com.google.firebase.sample.cityreviewer.databinding.DialogRatingBinding;
import com.google.firebase.sample.cityreviewer.java.model.City;

public class CityDialogFragment extends DialogFragment implements View.OnClickListener {

        public static final String TAG = "CityReviewDialog";

        private DialogRatingBinding mBinding;

        interface ReviewListener {

            void onReview(City city);

        }

        private com.google.firebase.sample.cityreviewer.java.CityDialogFragment.ReviewListener mReviewListener;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            mBinding = DialogRatingBinding.inflate(inflater, container, false);

            mBinding.cityFormButton.setOnClickListener(this);
            mBinding.cityFormCancel.setOnClickListener(this);

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
            City city = new City(
                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                    mBinding.cityCityText.getText().toString(),
                    mBinding.cityCountryText.getText().toString(),
                    mBinding.cityRating.getRating(),
                    mBinding.cityDescriptionText.getText().toString());

            if (mReviewListener != null) {
                mReviewListener.onReview(city);
            }

            dismiss();
        }

        private void onCancelClicked(View view) {
            dismiss();
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
        }

    }
