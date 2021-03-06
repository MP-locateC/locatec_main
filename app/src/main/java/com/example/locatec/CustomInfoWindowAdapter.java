package com.example.locatec;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private LayoutInflater inflater;
    private Context context;

    public CustomInfoWindowAdapter(Context context){
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // Getting view from the layout file
        View v = inflater.inflate(R.layout.info_window_layout, null);

        String url = marker.getSnippet();
        if(url == null || url.isEmpty()) {
            return null;
        }

        ImageView iv = (ImageView) v.findViewById(R.id.infoWindowImage);

        Picasso.with(context)
                .load(marker.getSnippet()).resize(160, 120).centerCrop()
                .into(iv, new MarkerCallback(marker));

        return v;
    }

    public class MarkerCallback implements Callback {
        Marker marker=null;

        MarkerCallback(Marker marker) {
            this.marker=marker;
        }

        @Override
        public void onError() {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
        }

        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }
    }
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

}
