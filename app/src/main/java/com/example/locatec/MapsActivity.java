package com.example.locatec;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.locatec.databinding.ActivityMapsBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // 데이터들
    MarkerData temp[] = {
            new MarkerData(0,"smoke", "https://newsimg.hankookilbo.com/cms/articlerelease/2015/10/18/201510182224437401_1.jpg", 37.629635550859, 127.08086267102873),
            new MarkerData(1,"trash", "http://img.danawa.com/prod_img/500000/736/826/img/11826736_1.jpg?shrink=330:330&_v=20210728170639",37.630682295065505, 127.0804025572257),
            new MarkerData(2,"smoke", "https://newsimg.hankookilbo.com/cms/articlerelease/2015/10/18/201510182224437401_1.jpg",37.63133962861005, 127.07673969062887),
            new MarkerData(3,"trash", "http://img.danawa.com/prod_img/500000/736/826/img/11826736_1.jpg?shrink=330:330&_v=20210728170639",37.63311154223848, 127.07690659454285),
            new MarkerData(4,"smoke", "https://newsimg.hankookilbo.com/cms/articlerelease/2015/10/18/201510182224437401_1.jpg",37.633976049288925, 127.08052886291345),
            new MarkerData(5,"trash", "http://img.danawa.com/prod_img/500000/736/826/img/11826736_1.jpg?shrink=330:330&_v=20210728170639",37.634836974008856, 127.07739828481888),
            new MarkerData(6,"smoke", "https://newsimg.hankookilbo.com/cms/articlerelease/2015/10/18/201510182224437401_1.jpg",37.6349341635446, 127.07542743118924)};
    private LatLng schoolCenterCoord = new LatLng(37.63232307069136, 127.07801836259382);
    private LatLng userCoord= new LatLng(37.63232307069136, 127.07801836259382);
    int curMarkerType = 0;
    Bitmap smokingMarkerImage, userMarkerImage, trashMarkerImage;

    // 위젯들
    Button gotoReport, gotoClosest;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    List<Marker> curMarker = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        // connect buttons
        gotoReport = (Button) findViewById(R.id.gotoReport);
        gotoClosest = (Button) findViewById(R.id.gotoClosest);

        // 추가 요청 페이지로
        gotoReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ReportPage.class);
                startActivity(intent);
            }
        });

        // 가장 가까운 곳.
        gotoClosest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int closest = -1;
                double d = 2000000;

                for(int i = 0; i<curMarker.size(); i++) {
                    double distance = ((userCoord.latitude - curMarker.get(i).getPosition().latitude)* (userCoord.latitude - curMarker.get(i).getPosition().latitude)) +
                            ((userCoord.latitude - curMarker.get(i).getPosition().latitude)* (userCoord.latitude - curMarker.get(i).getPosition().latitude));
                    if(d > distance) {
                        d = distance;
                        closest = i;
                    }
                }

                int finalClosest = closest;
                mMap.animateCamera(CameraUpdateFactory.newLatLng(curMarker.get(closest).getPosition()), 500, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        curMarker.get(finalClosest).showInfoWindow();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
        });
    }

    /**
     * 지도 로드 시 실행
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getApplicationContext()));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(schoolCenterCoord, 18));

        // 마커 이미지 불러오기
        smokingMarkerImage = Bitmap.createScaledBitmap(((BitmapDrawable)getResources().getDrawable(R.drawable.map_marker_smoking)).getBitmap(), 120, 120, false);
        trashMarkerImage = Bitmap.createScaledBitmap(((BitmapDrawable)getResources().getDrawable(R.drawable.map_marker_trash)).getBitmap(), 120, 120, false);
        userMarkerImage = Bitmap.createScaledBitmap(((BitmapDrawable)getResources().getDrawable(R.drawable.map_marker_user)).getBitmap(), 120, 120, false);

        // 나중에 서버에서 가져와서 넣기
        for(int i = 0; i<temp.length; i++) {
            if(temp[i].type == curMarkerType) {
                curMarker.add(mMap.addMarker(
                        new MarkerOptions().
                        icon(BitmapDescriptorFactory.fromBitmap(smokingMarkerImage)).
                        position(temp[i].coord).title("Marker_" + i).
                        snippet(temp[i].image)));
            }
        }
    }
}