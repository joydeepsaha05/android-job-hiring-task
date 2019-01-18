package com.joydeep.solar.calculator.util;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.joydeep.solar.calculator.R;
import com.joydeep.solar.calculator.adapter.SavedLocationAdapter;
import com.joydeep.solar.calculator.realm.RealmLocation;
import com.joydeep.solar.calculator.realm.RealmSingleton;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class SavedLocationsDialog {

    public void showDialog(Context context, onItemSelectedListener listener) {
        Realm realm = RealmSingleton.getInstance().getRealm();
        RealmResults<RealmLocation> realmLocations = realm
                .where(RealmLocation.class)
                .findAll();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Saved Locations");

        if (realmLocations.isEmpty()) {
            builder.setMessage("No saved locations!");
            builder.setPositiveButton("Close", null);
            builder.setCancelable(true);
            builder.show();
            return;
        }

        builder.setView(R.layout.layout_saved_location_dialog);
        builder.setNegativeButton("CANCEL", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        List<RealmLocation> locationItems = new ArrayList<>(realmLocations);

        RecyclerView recyclerView = alertDialog.findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            SavedLocationAdapter rvAdapter = new SavedLocationAdapter(locationItems, realmLocation -> {
                alertDialog.dismiss();
                listener.onItemSelected(realmLocation);
            });
            recyclerView.setAdapter(rvAdapter);
        }
    }

    public interface onItemSelectedListener {
        void onItemSelected(RealmLocation realmLocation);
    }
}
