package com.example.locatec;

import com.google.android.gms.maps.model.LatLng;

public class MarkerData {
    public int id;
    public int type;
    public String image;
    public LatLng coord;

    MarkerData(int id, String type, String image, double lat, double lng) {
        this.id = id;
        switch(type) {
            case "smoke":
                this.type = 0;
                break;
            case "trash":
                this.type = 1;
                break;
            default:
                this.type = 2;
        }
        this.image = image;
        this.coord = new LatLng(lat, lng);
    }
}
