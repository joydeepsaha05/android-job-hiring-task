package com.joydeep.solar.calculator.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joydeep.solar.calculator.R;
import com.joydeep.solar.calculator.realm.RealmLocation;
import com.joydeep.solar.calculator.realm.RealmSingleton;
import com.joydeep.solar.calculator.util.SavedLocationsDialog;

import java.util.List;

import io.realm.Realm;

public class SavedLocationAdapter extends RecyclerView.Adapter<SavedLocationAdapter.LocationViewHolder> {

    private List<RealmLocation> realmLocationList;
    private SavedLocationsDialog.onItemSelectedListener listener;
    private Realm realm;

    public SavedLocationAdapter(List<RealmLocation> realmLocationList,
                                SavedLocationsDialog.onItemSelectedListener listener) {
        this.realmLocationList = realmLocationList;
        this.listener = listener;
        realm = RealmSingleton.getInstance().getRealm();
    }

    @NonNull
    @Override
    public SavedLocationAdapter.LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                      int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_saved_location, parent, false);
        return new LocationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        holder.locationTV.setText(realmLocationList.get(position).toString());

        holder.deleteIV.setOnClickListener(v -> {
            RealmLocation location = realmLocationList.get(holder.getAdapterPosition());
            realm.executeTransaction(realm -> {
                RealmLocation realmLocation = realm.where(RealmLocation.class)
                        .equalTo("latitude", location.latitude)
                        .equalTo("longitude", location.longitude)
                        .findFirst();
                if (realmLocation != null) {
                    realmLocation.deleteFromRealm();
                    realmLocationList.remove(holder.getAdapterPosition());
                }
                notifyDataSetChanged();
            });
        });
    }

    @Override
    public int getItemCount() {
        return realmLocationList.size();
    }


    class LocationViewHolder extends RecyclerView.ViewHolder {

        TextView locationTV;
        ImageView deleteIV;

        LocationViewHolder(View v) {
            super(v);
            locationTV = v.findViewById(R.id.tv_location);
            deleteIV = v.findViewById(R.id.image_delete);

            v.setOnClickListener(v1 -> listener.onItemSelected(realmLocationList.get(getAdapterPosition())));
        }
    }
}