package com.google.firebase.sample.cityreviewer.java.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.sample.cityreviewer.databinding.ItemCityBinding;
import com.google.firebase.sample.cityreviewer.java.model.City;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

/**
 * RecyclerView adapter for a list of Cities.
 */
public class CityAdapter extends FirestoreAdapter<CityAdapter.ViewHolder> {

    public interface OnCitySelectedListener {

        void onCitySelected(DocumentSnapshot city);

    }

    private OnCitySelectedListener mListener;

    public CityAdapter(Query query, OnCitySelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCityBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemCityBinding binding;

        public ViewHolder(ItemCityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnCitySelectedListener listener) {

            City city = snapshot.toObject(City.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(binding.cityItemImage.getContext())
                    .load(city.getPhoto())
                    .into(binding.cityItemImage);

            binding.cityItemCity.setText(city.getCity());
            binding.cityItemRating.setRating((float) city.getRating());
            binding.cityItemAuthor.setText(city.getAuthor());
            binding.cityItemCountry.setText(city.getCountry());
            binding.cityItemDescription.setText(city.getDescription());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onCitySelected(snapshot);
                    }
                }
            });
        }

    }
}
