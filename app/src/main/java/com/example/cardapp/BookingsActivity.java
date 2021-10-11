package com.example.cardapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

//import google map library classes
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookingsActivity extends FragmentActivity {

    ImageView qrCodeImage;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    private GoogleMap mMap;
    boolean locationPermissionGranted;
    TextView fullName, ticketNumber, busSideNo, busLicensePlate, origin, destination, seatsBooked, seatNumbers, distanceCov, travelDate, ticketPrice;
    int user_id, ticket_number, bus_id, bus_side_no, seats_taken, cancelled_status;
    String full_name, bus_license_plate, orig, destin, distance, price, travel_date;
    RequestQueue bookingsQueue;
    CustomJsonObjectRequest bookingsRequest;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String uid;
    String ticketApiUrl = "http://192.168.43.50/ticket/ticket_api";
    JSONObject jsonObject;
    ProgressDialog dialog;
    int ticketNumberText;
    boolean currentlyBooked;
    RelativeLayout currentBookingStatus, bookingExpiredRel, bookingCancelledRel;
    LinearLayout bookingDetailsLinear;
    String allSeats = "";
    Calendar calendar;
    SimpleDateFormat sdformat;
    StringRequest cancelBookingRequest;
    Button btnCancelBooking;
    String mainStatus = "";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        //initiate the progres dialog
        dialog = new ProgressDialog(this);
        dialog.create();
        dialog.setCancelable(false);

        bookingsQueue = Volley.newRequestQueue(this);

        calendar = Calendar.getInstance();
        sdformat = new SimpleDateFormat("YYYY-MM-dd");

        //get user id
        preferences = getSharedPreferences("loggedInPrefs", MODE_PRIVATE);
        editor = preferences.edit();
        uid = preferences.getString("uid", "");
       // Toast.makeText(this, uid, Toast.LENGTH_SHORT).show();
        currentlyBooked = preferences.getBoolean("currentlyBooked", false);
        btnCancelBooking = (Button) findViewById(R.id.cancelBookingBtn);

        currentBookingStatus = (RelativeLayout) findViewById(R.id.currentBookingStatus);
        bookingExpiredRel = (RelativeLayout) findViewById(R.id.bookingExpiredRel);
        bookingCancelledRel = (RelativeLayout) findViewById(R.id.bookingCancelledRel);
        bookingDetailsLinear = (LinearLayout) findViewById(R.id.bookedDetailsLinear);

        //get the text views
        qrCodeImage = (ImageView) findViewById(R.id.qrCodeImage);
        ticketNumber = (TextView) findViewById(R.id.ticketNumberText);
        fullName = (TextView) findViewById(R.id.detailsFullName);
        busSideNo = (TextView) findViewById(R.id.detailsSideNo);
        busLicensePlate = (TextView) findViewById(R.id.detailsLicensePlate);
        origin = (TextView) findViewById(R.id.detailsOrigin);
        destination = (TextView) findViewById(R.id.detailsDestination);
        seatsBooked = (TextView) findViewById(R.id.detailsSeatsBooked);
        seatNumbers = (TextView) findViewById(R.id.detailsSeatNos);
        distanceCov = (TextView) findViewById(R.id.detailsDistance);
        ticketPrice = (TextView) findViewById(R.id.detailsTicketPrice);
        travelDate = (TextView) findViewById(R.id.detailsTravelDate);

        bookingExpiredRel.setVisibility(View.GONE);

        bookingDetailsLinear.setVisibility(View.GONE);
        currentBookingStatus.setVisibility(View.VISIBLE);
        btnCancelBooking.setVisibility(View.GONE);
        bookingCancelledRel.setVisibility(View.GONE);

//            SupportMapFragment mapFragment =(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//            mapFragment.getMapAsync(this);

        sendBookingRequest();
    }

    public void sendBookingRequest() {
        dialog.show();
        jsonObject = new JSONObject();

        try{
            jsonObject.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //send the request to retrieve current booking information
        bookingsRequest = new CustomJsonObjectRequest(Request.Method.POST, ticketApiUrl + "/get_bookings.php", jsonObject, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) {
               // Toast.makeText(BookingsActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                try {
                    mainStatus = response.getString("status");
                    JSONObject resObject = response.getJSONObject("all");

                        String status = resObject.getString("status");
                            if(status.equals("success")) {

                                JSONObject dataObject = resObject.getJSONObject("data");

                                // Toast.makeText(BookingsActivity.this, "found", Toast.LENGTH_SHORT).show();
                                //
                                dialog.hide();
                                bookingDetailsLinear.setVisibility(View.VISIBLE);
                                currentBookingStatus.setVisibility(View.GONE);
                                btnCancelBooking.setVisibility(View.VISIBLE);

                                full_name = dataObject.getString("first_name") + " " + dataObject.getString("last_name");
                                ticket_number = dataObject.getInt("ticket_number");
                                bus_id = dataObject.getInt("bus_id");
                                bus_side_no = dataObject.getInt("bus_side_no");
                                bus_license_plate = dataObject.getString("bus_license_plate");
                                orig = dataObject.getString("starting_location");
                                destin = dataObject.getString("destination");
                                seats_taken = dataObject.getInt("seats_taken");
                                distance = String.valueOf(dataObject.getDouble("distance")) + " KM";
                                price = String.valueOf(dataObject.getInt("ticket_price"));
                                travel_date = dataObject.getString("travel_date");
                                cancelled_status = dataObject.getInt("cancelled");

                                //compare the current date with the travel date and find out if the booking has expired or not
                                String currentDate = sdformat.format(calendar.getTime());

                                if(travel_date.compareTo(currentDate) <= 0) {
                                    bookingExpiredRel.setVisibility(View.VISIBLE);
                                } else {
                                    bookingExpiredRel.setVisibility(View.GONE);
                                }

                                //Toast.makeText(BookingsActivity.this, currentDate, Toast.LENGTH_SHORT).show();

                                fullName.setText(full_name);
                                ticketNumber.setText(String.valueOf(ticket_number));
                                busSideNo.setText(String.valueOf(bus_side_no));
                                busLicensePlate.setText(bus_license_plate);
                                origin.setText(orig);
                                destination.setText(destin);
                                seatsBooked.setText(String.valueOf(seats_taken));
                                distanceCov.setText(distance);
                                ticketPrice.setText(price);
                                travelDate.setText(travel_date);

                                //get the number of seats as an array
                                JSONArray seatsArray = resObject.getJSONArray("seats");
                                for(int i = 0; i < seatsArray.length(); i++) {
                                    JSONObject singleSeatObj = seatsArray.getJSONObject(i);
                                    String singleSeatt = String.valueOf(singleSeatObj.getInt("seat_number"));
                                    allSeats += singleSeatt + ", ";
                                }

                                seatNumbers.setText(allSeats);

                                if(cancelled_status == 1) {
                                    bookingCancelledRel.setVisibility(View.VISIBLE);
                                    btnCancelBooking.setVisibility(View.GONE);
                                    seatNumbers.setText("cancelled");
                                }

                                //QR code part
                                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                                // initializing a variable for default display.
                                Display display = manager.getDefaultDisplay();

                                // creating a variable for point which
                                // is to be displayed in QR Code.
                                Point point = new Point();
                                display.getSize(point);

                                // getting width and
                                // height of a point
                                int width = point.x;
                                int height = point.y;

                                // generating dimension from width and height.
                                int dimen = width < height ? width : height;
                                dimen = dimen * 3 / 4;

                                QRGEncoder qrgEncoder = new QRGEncoder(String.valueOf(ticket_number), null, QRGContents.Type.TEXT, dimen);
                                qrgEncoder.setColorBlack(Color.BLUE);
                                qrgEncoder.setColorWhite(Color.WHITE);

                                try {
                                    // Getting QR-Code as Bitmap
                                    bitmap = qrgEncoder.getBitmap();
                                    // Setting Bitmap to ImageView
                                    qrCodeImage.setImageBitmap(bitmap);
                                } catch (Exception e) {
                                    Toast.makeText(BookingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                QRGSaver qrgSaver = new QRGSaver();
                                qrgSaver.save(Environment.getExternalStorageState(), "shut up", bitmap, QRGContents.ImageType.IMAGE_JPEG);


                            } else {
                                bookingDetailsLinear.setVisibility(View.GONE);
                                currentBookingStatus.setVisibility(View.VISIBLE);
                                btnCancelBooking.setVisibility(View.GONE);
                                dialog.hide();
                                editor.remove("currentlyBooked");
                                editor.apply();
                            }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.hide();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BookingsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.hide();
                startActivity(new Intent(BookingsActivity.this, MainActivity.class));
            }
        });

        //getLocationPermission();

        bookingsQueue.add(bookingsRequest);
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        LatLng sydney = new LatLng(-122, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Somewhere"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//    }
//
//    private void getLocationPermission() {
//
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            locationPermissionGranted = true;
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//        }
//    }

    //method to find the current location
//    private void getDeviceLocation() {
//        /*
//         * Get the best and most recent location of the device, which may be null in rare
//         * cases when a location is not available.
//         */
//        try {
//            if (locationPermissionGranted) {
//                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
//                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        if (task.isSuccessful()) {
//                            // Set the map's camera position to the current location of the device.
//                            lastKnownLocation = task.getResult();
//                            if (lastKnownLocation != null) {
//                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                        new LatLng(lastKnownLocation.getLatitude(),
//                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//                            }
//                        } else {
//                            Log.d(TAG, "Current location is null. Using defaults.");
//                            Log.e(TAG, "Exception: %s", task.getException());
//                            map.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
//                            map.getUiSettings().setMyLocationButtonEnabled(false);
//                        }
//                    }
//                });
//            }
//        } catch (SecurityException e)  {
//            Log.e("Exception: %s", e.getMessage(), e);
//        }
//    }

    public void cancelBookings(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to cancel this booking?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogg, int which) {
                dialog.show();

                cancelBookingRequest = new StringRequest(Request.Method.POST, ticketApiUrl + "/request_cancel.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("cancelled_success")) {
                            editor.remove("currentlyBooked");
                            editor.apply();
                            bookingDetailsLinear.setVisibility(View.GONE);
                            currentBookingStatus.setVisibility(View.VISIBLE);
                            btnCancelBooking.setVisibility(View.GONE);
                            qrCodeImage.setVisibility(View.GONE);
                            //qrCodeImage.setImageBitmap(null);
                            Toast.makeText(BookingsActivity.this, "Booking has been cancelled successfully", Toast.LENGTH_SHORT).show();
                            sendBookingRequest();
                        } else {
                            Toast.makeText(BookingsActivity.this, response, Toast.LENGTH_SHORT).show();
                        }

                        dialog.hide();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.hide();
                        Toast.makeText(BookingsActivity.this, "something went wrong. try again", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("uid", uid);

                        return params;
                    }
                };

                bookingsQueue.add(cancelBookingRequest);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(BookingsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    public void backFromBookings(View view) {
        startActivity(new Intent(BookingsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}