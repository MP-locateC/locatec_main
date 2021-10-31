package com.example.locatec;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private LayoutInflater inflater;
    private Context context;

    public CustomInfoWindowAdapter(Context context){
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.context = context;
    }

    private class DownloadFilesTask extends AsyncTask<String,Void, Bitmap> {
        ImageView iv_image;
        private Marker marker;

        public DownloadFilesTask(ImageView iv, Marker marker) {
            this.iv_image = iv;
            this.marker = marker;
        }

        @Override
        protected Bitmap doInBackground(String... img_urls) {
            Bitmap bmp = null;
            try {
                URL url = new URL(img_urls[0]);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // doInBackground 에서 받아온 total 값 사용 장소
            iv_image.setImageBitmap(result);
            marker.showInfoWindow();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // Getting view from the layout file
        View v = inflater.inflate(R.layout.info_window_layout, null);

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
