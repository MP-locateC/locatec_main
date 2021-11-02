package com.example.locatec;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ReportFirstPage extends Fragment {
    Button goNext;
    MapView mapview;
    LatLng pickCoord;
    private GoogleMap mMap;
    private LatLng schoolCenterCoord = new LatLng(37.63232307069136, 127.07801836259382);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = (ViewGroup) inflater.inflate(
                R.layout.report_firstpage, container, false);

        mapview = (MapView) v.findViewById(R.id.mapView);
        mapview.onCreate(savedInstanceState);
        mapview.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapview.getMapAsync(new OnMapReadyCallback() {
            Bitmap markerImg;
            Marker curMarker;
            @Override
            public void onMapReady(GoogleMap mMapInput) {
                mMap = mMapInput;

                markerImg = Bitmap.createScaledBitmap(
                        ((BitmapDrawable)getResources().getDrawable(R.drawable.map_marker)).getBitmap(),
                        120, 120, false);

                GpsTracker gpsTracker = new GpsTracker(getContext());
                pickCoord = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                if (pickCoord.latitude <= schoolCenterCoord.latitude + 0.007 &&
                        pickCoord.longitude <= schoolCenterCoord.longitude + 0.007 &&
                        pickCoord.latitude >= schoolCenterCoord.latitude - 0.007 &&
                        pickCoord.longitude >= schoolCenterCoord.longitude - 0.007
                ) {
                    curMarker  = mMap.addMarker(new MarkerOptions().position(pickCoord).icon(BitmapDescriptorFactory.fromBitmap(markerImg)));
                } else {
                    Toast.makeText(getContext(), "현재 학교 밖에 계십니다.", Toast.LENGTH_SHORT).show();
                    pickCoord = new LatLng(37.63232307069136, 127.07801836259382);
                }

                CameraPosition cameraPosition = new CameraPosition.Builder().target(pickCoord).zoom(18).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        pickCoord = latLng;
                        curMarker.remove();
                        curMarker = mMap.addMarker(new MarkerOptions().position(pickCoord).icon(BitmapDescriptorFactory.fromBitmap(markerImg)));
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(pickCoord).zoom(18).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                });
            }
        });

        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        mapview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapview.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapview.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapview.onLowMemory();
    }
}
