package com.anchal.canteentest;

import static com.anchal.canteentest.PaymentGateway.setTotal_amount;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anchal.canteentest.Common.Common;
import com.anchal.canteentest.Database.Database;
import com.anchal.canteentest.Model.Order;
import com.anchal.canteentest.Model.Request;
import com.anchal.canteentest.Remote.IGoogleService;
import com.anchal.canteentest.ViewHolder.CartAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    double total_price;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnPlaceOrder;
    Button btnClearCart;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    // Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private static int LOCATION_REQUEST_CODE = 9999;
    private static int PLAY_SERVICES_REQUEST = 9997;

    private int check = 0;
    IGoogleService mGoogleMapServices;
    private static double distancek = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mGoogleMapServices = Common.getGoogleMapsAPI();


        // Taking Runtime permission for location during runtime, since precision location is classified as dangerous permission.
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("1");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        }
        else {
            if(checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        // Getting reference of database .
        // This database is associated with firebase account of email account LIT2019007@iiitl.ac.in
        database = FirebaseDatabase.getInstance("https://fir-app-36b01-default-rtdb.asia-southeast1.firebasedatabase.app");
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = findViewById(R.id.total);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnClearCart = findViewById(R.id.btnClearCart);

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        btnClearCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCart();
            }
        });
        loadListFood();

    }

    // Clearing the cart.
    private void clearCart() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this, R.style.AlertDialogTheme);
        alertDialog.setTitle("Clear Cart");
        alertDialog.setMessage("Are you sure you want to clear cart?");

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Cart Cleared", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();

//        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
//        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);

    }

    private void createLocationRequest()
    {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    // Checking for permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 9999) {
            System.out.println(grantResults[0]);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("2");
                if (checkPlayServices()) {
                    buildGoogleApiClient();
                    createLocationRequest();
                }
            }
        }
    }


    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this, R.style.AlertDialogTheme);
        alertDialog.setTitle("One More Step!");
        alertDialog.setMessage("Enter your Address: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address = inflater.inflate(R.layout.order_address, null);
        final MaterialEditText edtAddress = order_address.findViewById(R.id.edtaddress);
        alertDialog.setView(order_address);
        alertDialog.setIcon(R.drawable.shopping_cart);


        // This is our default longitude and latitude.
        // i.e. this is IIIT Lucknow Canteen's location which is currently inside the admin building.

        final double localLat = 26.801425;
        final double localLng = 81.024457;

        final boolean[] isAddressManual = {true};

        RadioButton rdiCurrentAddress = order_address.findViewById(R.id.rdiCurrentAddress);
        RadioButton rdiHomeAddress = order_address.findViewById(R.id.rdiHomeAddress);
        RadioButton rdiCashOnDelivery = order_address.findViewById(R.id.rdiCashOnDelivery);
        RadioButton rdiPayNow = order_address.findViewById(R.id.rdiPayNow);

        final Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());
        rdiCurrentAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    try {
                        List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                        String address = addresses.get(0).getAddressLine(0);
                        edtAddress.setText(address);
                        distancek = distance(localLat, localLng, mLastLocation.getLatitude(), mLastLocation.getLongitude(), "K");
                        System.out.println(distancek);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    isAddressManual[0] = false;
                }
            }
        });

        // This is the default address, with distance = 0.01 km.
        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edtAddress.setText("IIIT Lucknow Hostel - 01");
                distancek = 0.01;

                isAddressManual[0] = false;
            }
        });

        rdiPayNow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    check = 1;
                }
            }
        });

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {



                if(isAddressManual[0])
                {
                    String myLocation = edtAddress.getText().toString();
                    Geocoder geocoder = new Geocoder(Cart.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocationName(myLocation, 1);
                    } catch (IOException e) {
                        Toast.makeText(Cart.this, "Can't find this address. Please try different address.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    Address address = addresses.get(0);

                    if(addresses.size() > 0) {
                        double latitude = addresses.get(0).getLatitude();
                        double longitude = addresses.get(0).getLongitude();

                        distancek = distance(localLat, localLng, latitude, longitude, "K");
                    }
                }

                if(distancek > 2.0)
                {
                    Toast.makeText(Cart.this, "Please enter address which is closer to the canteen.", Toast.LENGTH_SHORT).show();
                    return;
                }


                Request request = new Request(
                        Common.currentUser.getPhoneNo(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        cart,
                        String.valueOf(distancek)
                );

                requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);

                new Database(getBaseContext()).cleanCart();

                if(check == 1) {
                    Intent payment = new Intent(Cart.this, PaymentGateway.class);
                    startActivity(payment);
                }

                Toast.makeText(Cart.this, "Order Placed!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Cart.this, "Order is not placed.", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        recyclerView.setAdapter(adapter);

        double total = 0;
        for(Order order:cart)
            total += ((Double.parseDouble(order.getPrice())) * (Double.parseDouble(order.getQuantity())));

        Locale locale = new Locale("en", "IN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        setTotal_amount(total);

        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null) {
            Log.d("LOCATION", "Your Location : "+ mLastLocation.getLatitude()+ " ," + mLastLocation.getLongitude());
        }
        else {
            Log.d("LOCATION", "Couldn't find Location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    // Calculating distance from our canteen's longitude and latitude to the entered longitude and latitude.
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));

            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;

            // K represents Kilometers.
            // M represents Miles.
            // N represents Nautical Miles.

            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

}
