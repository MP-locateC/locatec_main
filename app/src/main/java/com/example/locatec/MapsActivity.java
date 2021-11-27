package com.example.locatec;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.locatec.databinding.ActivityMapsBinding;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // 데이터들
    List<MarkerData> markers = new ArrayList<MarkerData>();
    int curMarkerType = 0;
    Bitmap smokingMarkerImage, userMarkerImage, trashMarkerImage;

    // 위치 관련
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private LatLng schoolCenterCoord = new LatLng(37.63232307069136, 127.07801836259382);
    private LatLng userCoord= new LatLng(37.63232307069136, 127.07801836259382);
    private boolean isInside = false;

    // 위젯들
    Button gotoReport, gotoClosest;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    List<Marker> curMarker = new ArrayList<Marker>();
    SpeedDialView speedDialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        // 서버에서 받은 데이터를 파싱하여 마커로 등록.
        try {
            String response = getIntent().getStringExtra("markerDatas");
            JSONObject jsonObj = new JSONObject(response);
            JSONArray datas = jsonObj.getJSONArray("response");

            for (int i = 0; i < datas.length(); i++) {
                JSONObject obj = datas.getJSONObject(i);
                Double longitude = obj.getDouble("longitude");
                Double latitude = obj.getDouble("latitude");
                String type = obj.getString("type");
                String image = obj.getString("imageUrl");

                markers.add(new MarkerData(obj.getInt("id"), type, image, latitude, longitude));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        loadMap();
        buttonsConnection();
        speedDialConnection();
    }

    private void loadMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private void buttonsConnection() {
        // connect buttons
        gotoReport = (Button) findViewById(R.id.gotoReport);
        gotoClosest = (Button) findViewById(R.id.gotoClosest);

        // 추가 요청 페이지로
        gotoReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScreenSlidePagerActivity.class);
                startActivity(intent);
            }
        });

        // 가장 가까운 곳.
        gotoClosest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int closest = -1;
                BigDecimal dst = new BigDecimal("2000000");
                double d = 2000000;

                for(int i = 0; i<curMarker.size(); i++) {
                    BigDecimal curLat = new BigDecimal(userCoord.latitude - curMarker.get(i).getPosition().latitude);
                    BigDecimal curLog = new BigDecimal(userCoord.longitude - curMarker.get(i).getPosition().longitude);
                    BigDecimal latDiff = curLat.multiply(curLat);
                    BigDecimal logDiff = curLog.multiply(curLog);

                    if(dst.compareTo(latDiff.add(logDiff)) > 0 ) {
                        dst = latDiff.add(logDiff);
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
    private void speedDialConnection() {
        speedDialView = (SpeedDialView) findViewById(R.id.changeMarker);

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.action_smoke, R.drawable.ic_baseline_smoking_rooms_24)
                        .setFabBackgroundColor(0xffb8b8b8).setFabImageTintColor(0xffffffff)
                        .setLabel(R.string.menu_item_smoking)
                        .create());
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.action_trash, R.drawable.ic_baseline_delete_24)
                        .setFabBackgroundColor(0xffb8b8b8).setFabImageTintColor(0xffffffff)
                        .setLabel(R.string.menu_item_trashcan)
                        .create());

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch((actionItem.getId())) {
                    case R.id.action_smoke:
                        speedDialView.setMainFabClosedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_smoking_rooms_24));
                        markerTypeChange(0);
                        break;
                    case R.id.action_trash:
                        speedDialView.setMainFabClosedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_delete_24));
                        markerTypeChange(1);
                        break;
                }
                return false;
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
        markerRender();
        if(isInside) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(userCoord));
            mMap.addMarker(new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(userMarkerImage)).
                    position(userCoord).title("현재 위치"));
        }
    }

    private void markerRender() {
        Bitmap markerImg = curMarkerType == 0 ? smokingMarkerImage : trashMarkerImage;
        for(int i =0; i<curMarker.size(); i++) {
            curMarker.get(i).remove();
        }
        curMarker.clear();
        for(int i = 0; i<markers.size(); i++) {
            if(markers.get(i).type == curMarkerType) {
                curMarker.add(mMap.addMarker(
                        new MarkerOptions().
                                icon(BitmapDescriptorFactory.fromBitmap(markerImg)).
                                position(markers.get(i).coord).title("Marker_" + i).
                                snippet(markers.get(i).image)));
            }
        }
    }
    private void markerTypeChange(int to) {
        if(curMarkerType == to)
            return;
        curMarkerType = to;
        markerRender();
    }

    // 유저의 위치를 받아서 학교 안 또는 근처면 유저의 위치를, 아니면 학교 중심으로 이동
    private void getFirstUserLocation() {
        gpsTracker = new GpsTracker(MapsActivity.this);
        userCoord = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        if (userCoord.latitude <= schoolCenterCoord.latitude + 0.005 &&
                userCoord.longitude <= schoolCenterCoord.longitude + 0.005 &&
                userCoord.latitude >= schoolCenterCoord.latitude - 0.005 &&
                userCoord.longitude >= schoolCenterCoord.longitude - 0.005
        ) {
            isInside = true;
        } else {
            Toast.makeText(MapsActivity.this, "현재 학교 밖에 계십니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                // 사용자가 허가 했을때 실행
                getFirstUserLocation();
                if(isInside) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(userCoord));
                    mMap.addMarker(new MarkerOptions().
                            icon(BitmapDescriptorFactory.fromBitmap(userMarkerImage)).
                            position(userCoord).title("Marker_user"));
                }
            } else {
                // 거부한 퍼미션이 있다면 설명해줍니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MapsActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MapsActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면 실행
            getFirstUserLocation();
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MapsActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MapsActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MapsActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplication());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}