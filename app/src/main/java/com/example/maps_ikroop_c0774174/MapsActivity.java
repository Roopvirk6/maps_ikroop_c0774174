package com.example.maps_ikroop_c0774174;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.N;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Geocoder gcode;
    List<Address> addresses;
    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    private Marker destMarker;
    double  latitude,longitude,distance,distanceLines;
    Polyline line;
    Polygon shape;
    private static final int POLYGON_SIDES = 4;
    List<Marker> markers = new ArrayList();


    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(mMap != null) {
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View row = getLayoutInflater().inflate(R.layout.custom_address,null);
                    TextView t1 = (TextView)row.findViewById(R.id.latitude);
                    TextView t2 = (TextView)row.findViewById(R.id.longitude);
                    TextView t3 = (TextView)row.findViewById(R.id.txt_title);
                    TextView t4 = (TextView)row.findViewById(R.id.txt_snippet);
                    TextView t8 = (TextView)row.findViewById(R.id.txt_distancecalculated);
                    LatLng latLng = marker.getPosition();
                    t3.setText(marker.getTitle());
                    t1.setText(String.valueOf(latLng.latitude));
                    t2.setText(String.valueOf(latLng.longitude));
                    t4.setText(marker.getSnippet());
                    t8.setText("Total distance calculated : " + distance + " miles");
                    return row;
                }
            });
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setHomeMarker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();



        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View row1 = getLayoutInflater().inflate(R.layout.custom_distance,null);
                        TextView t5 = (TextView) row1.findViewById(R.id.txt_distance);
                        TextView t6 = (TextView) row1.findViewById(R.id.txt_totaldistance);
                        t5.setText(" Total Distance : " + distance +  " miles");
                        t6.setText("Distance between two lines: " + distanceLines + "miles");





                        return row1;
                    }
                });



            }
        });


        // apply long press gesture
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //  Location location = new Location("Your Destination");
                //    location.setLatitude(latLng.latitude);
                //       location.setLongitude(latLng.longitude);
                // set marker

                setMarker(latLng);



            }


            private void setMarker(LatLng latLng) {

                MarkerOptions options = new MarkerOptions().position(latLng)
                        .title("your destination").snippet("reached location");
                distance=CalculationByDistance(43.653, 79.3832, 35.0,75.0);
                distanceLines = CalculationByLines(43.563, 79.382);





                // check if there are already the same number of markers, we clear the map.
                if (markers.size() == POLYGON_SIDES)
                    clearMap();

                markers.add(mMap.addMarker(options));
                if (markers.size() == POLYGON_SIDES)
                    drawShape();
            }

            public double CalculationByLines(double initialLat, double initialLong)
            {
                double latDiff = initialLong - initialLat;
                return latDiff;
            }

               public double CalculationByDistance(double initialLat, double initialLong, double finalLat, double finalLong)
               {


               double latDiff = finalLat - initialLat;
               double longDiff = finalLong - initialLong;
               double earthRadius = 6371;

               double distance = 2*earthRadius*Math.asin(Math.sqrt(Math.pow(Math.sin(latDiff/2.0),2)+Math.cos(initialLat)*Math.cos(finalLat)*Math.pow(Math.sin(longDiff/2),2)));

                return distance;

            }


            private void drawShape() {
                PolygonOptions options = new PolygonOptions()
                        .fillColor(Color.GREEN)
                        .strokeColor(Color.RED)
                        .strokeWidth(5);

                for (int i = 0; i < POLYGON_SIDES; i++) {
                    options.add(markers.get(i).getPosition()).fillColor(Color.GREEN).clickable(true);


                }


                shape = mMap.addPolygon(options);

                shape.setClickable(true);



            }



            private void clearMap() {

                for (Marker marker : markers)
                    marker.remove();

                markers.clear();
                shape.remove();
                shape = null;
            }
        });
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

        //Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //setHomeMarker(lastKnownLocation);
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {


        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("Brampton")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your location").draggable(true);
        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

    }

    public String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses!=null && addresses.size() > 0 ) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0)).append(" ");
                result.append(address.getThoroughfare()).append(" ");
                result.append(address.getLocality()).append(" ");
                result.append(address.getPostalCode()).append(" ");
                result.append(address.getCountryName());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }


        return result.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }
}