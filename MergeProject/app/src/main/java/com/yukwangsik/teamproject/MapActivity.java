package com.yukwangsik.teamproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // 현재 주소 가져오기 =========================================================
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    // ======================================================================
    private TextView MyLoc;

    private GoogleMap googleMap;
    Spinner spinner;
    String comboText;
    String[] SiGun;

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mD = mDatabase.getReference("main_info");

    public static ArrayList address = new ArrayList(); //주소
    public static ArrayList name = new ArrayList(); //업소명
    public static ArrayList rep = new ArrayList(); //대표품목
    public static ArrayList price = new ArrayList(); //가격
    public static String Si; //시도
    public static String Gun; //시군

    public static int totalElements = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // ======================================================================
        gpsTracker = new GpsTracker(MapActivity.this);
        spinner = findViewById(R.id.spinner);


        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String add = getCurrentAddress(latitude, longitude);
        System.out.println(add);
        SiGun = add.split("\n");
        MyLoc = findViewById(R.id.MyLoc);
        MyLoc.setText(SiGun[0]);

        System.out.println("현재위치 \n위도 " + latitude + "\n경도 " + longitude);
        ArrayAdapter monthAdapter = ArrayAdapter.createFromResource(this, R.array.question, android.R.layout.simple_spinner_dropdown_item);

        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(monthAdapter); //어댑터에 연결해줍니다.


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                address.clear();
                name.clear();
                rep.clear();
                price.clear();

                comboText = spinner.getItemAtPosition(position).toString();
                //System.out.println(comboText);


                mD.orderByChild("업종").equalTo(comboText).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        System.out.println(SiGun[1]+SiGun[2]+"야옹");
                        //System.out.println(snapshot.getValue());
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            Si = ds.child("시도").getValue(String.class);
                            Gun = ds.child("시군").getValue(String.class);
                            if((Si.equals(SiGun[1])) && (Gun.equals(SiGun[2]))){
                                address.add(ds.child("주소").getValue(String.class));
                                name.add(ds.child("업소명").getValue(String.class));
                                rep.add(ds.child("대표품목").getValue(String.class));
                                price.add(ds.child("가격").getValue(String.class));
                            }
                        }
                        totalElements = address.size();// arrayList의 요소의 갯수를 구한다.
                        for (int index = 0; index < totalElements; index++) {
                            System.out.println(address.get(index));
                            Log.d("TAG", address.get(index).toString() );
                        }
                        System.out.println("array List 검색 결과 : " + totalElements);
                    }


                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //딜레이 후 시작할 코드 작성
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapActivity.this);

                    }
                }, 5000);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });




        // ======================================================================



    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            System.out.println("지오코더 서비스 사용불가");
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            System.out.println("잘못된 GPS 좌표");
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            System.out.println("주소 미발견");
            return "주소 미발견";

        }

        Address address = addresses.get(0);

        String Si = address.getAdminArea();
        String Gun = address.getSubLocality();
        return address.getAddressLine(0).toString()+"\n"+Si+"\n"+Gun;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        double myLat = gpsTracker.getLatitude();
        double Mylon = gpsTracker.getLongitude();

        this.googleMap = googleMap;
        googleMap.clear();

        LatLng latLng = new LatLng(myLat, Mylon);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(13));


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            checkLocationPermissionWithRationale();
        }

        ////// 주소 -> 위도, 경도
        totalElements = address.size();// arrayList의 요소의 갯수를 구한다.

        Geocoder geocoder = new Geocoder(this);
//        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
//            @Override
//            public void onMapClick(LatLng point) {
//                MarkerOptions mOptions = new MarkerOptions();
//                // 마커 타이틀
//                mOptions.title("마커 좌표");
//                Double latitude = point.latitude; // 위도
//                Double longitude = point.longitude; // 경도
//                // 마커의 스니펫(간단한 텍스트) 설정
//                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
//                // LatLng: 위도 경도 쌍을 나타냄
//                mOptions.position(new LatLng(latitude, longitude));
//                // 마커(핀) 추가
//                googleMap.addMarker(mOptions);
//            }
//        });
        for (int index = 0; index < totalElements; index++) {
            //System.out.println(address.get(index));


            String str= address.get(index).toString();
            List<Address> addressList = null;
            try {
                // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                addressList = geocoder.getFromLocationName(str, 10); // 최대 검색 결과 개수
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(addressList.get(0).toString());
            // 콤마를 기준으로 split
            String []splitStr = addressList.get(0).toString().split(",");
            String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
            System.out.println(address);

            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
            System.out.println("\n lat"+latitude+"\n");
            System.out.println("\n long"+longitude+"\n");

            // 좌표(위도, 경도) 생성
            LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            // 마커 생성
            MarkerOptions mOptions2 = new MarkerOptions();
            mOptions2.title(name.get(index) + "\n");
            mOptions2.snippet(rep.get(index)+": "+price.get(index)); //대표품목 + 가격
            mOptions2.position(point);
            // 마커 추가
            googleMap.addMarker(mOptions2);
            // 해당 좌표로 화면 줌
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermissionWithRationale() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("위치정보")
                        .setMessage("이 앱을 사용하기 위해서는 위치정보에 접근이 필요합니다. 위치정보 접근을 허용하여 주세요.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}