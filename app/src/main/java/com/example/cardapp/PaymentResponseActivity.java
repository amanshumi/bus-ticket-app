package com.example.cardapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yenepaySDK.PaymentOrderManager;
import com.yenepaySDK.PaymentResponse;
import com.yenepaySDK.YenePayPaymentActivity;
import com.yenepaySDK.model.YenePayConfiguration;

import java.util.HashMap;
import java.util.Map;

import static android.icu.text.UnicodeSet.from;

public class PaymentResponseActivity extends YenePayPaymentActivity {
    TextView paymentResponseTitle;
    RequestQueue reserveQueue;
    StringRequest reserveRequest;
    ProgressBar paymentResponseProgressBar;
    SharedPreferences preferences;
    String ticketApiUrl = "http://192.168.43.50/ticket/ticket_api";
    ImageView successResponseImg;
    Button retryBookingBtn, goHomeBtn;
    private NotificationManager mNotificationManager;
    private int notificationID = 100;
    boolean preventGoBack = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_response);

        paymentResponseTitle = (TextView) findViewById(R.id.paymentResponseTitle);
        paymentResponseProgressBar = (ProgressBar) findViewById(R.id.paymentResponseProgressBar);
        successResponseImg = (ImageView) findViewById(R.id.succeed_responseImg);
        retryBookingBtn = (Button) findViewById(R.id.retryBookingBtn);
        goHomeBtn = (Button) findViewById(R.id.homeBtnRes);
        reserveQueue = Volley.newRequestQueue(this);
        preferences = getSharedPreferences("loggedInPrefs", MODE_PRIVATE);
        retryBookingBtn.setVisibility(View.GONE);
        goHomeBtn.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(preventGoBack) {
            startActivity(new Intent(PaymentResponseActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            Toast.makeText(this, "please finish booking process before leaving", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentResponseArrived(PaymentResponse response) {
        //Handle Payment response

       // Toast.makeText(this, response.toString(), Toast.LENGTH_SHORT).show();
        if(response.isPaymentCompleted()){
            bookTicket();
            Toast.makeText(this, "payment has been completed successfully", Toast.LENGTH_SHORT).show();
        } else {

         //   Toast.makeText(this, "payment completed", Toast.LENGTH_SHORT).show();
            paymentResponseTitle.setText("Something Went Wrong");
            paymentResponseProgressBar.setVisibility(View.GONE);
            startActivity(new Intent(PaymentResponseActivity.this, BusDetailActivity.class));
        }
    }

    public void retryBooking(View view) {
        retryBookingBtn.setEnabled(false);
        bookTicket();
    }

    public void bookTicket() {
        paymentResponseProgressBar.setVisibility(View.VISIBLE);

        //reserve seats and store in database
        //get all data from sharedprefs first
        final String uid = preferences.getString("uid", "");
        final int ticketId = preferences.getInt("ticketId", 0);
        final int busId = preferences.getInt("busId", 0);
        final String fname = preferences.getString("fname", "");
        final String lname = preferences.getString("lname", "");
        final String address = preferences.getString("address", "");
        final String busName = preferences.getString("busName", "");
        final String travelDate = preferences.getString("travelDate", "");
        final float ticketPrice = preferences.getFloat("ticketPrice", 0);
        final String origin = preferences.getString("origin", "");
        final String destination = preferences.getString("destination", "");
        final int seatsAvailable = preferences.getInt("seatsAvailable", 0);
        final int seatsTaken = preferences.getInt("numberOfSeatsToBook", 0);

        reserveRequest = new StringRequest(Request.Method.POST, ticketApiUrl + "/reserve_seat.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                retryBookingBtn.setEnabled(false);
                retryBookingBtn.setVisibility(View.GONE);
                goHomeBtn.setVisibility(View.VISIBLE);
                successResponseImg.setImageResource(R.drawable.images);
                paymentResponseTitle.setText("Booking Completed");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("currentlyBooked", true);
                editor.apply();
                preventGoBack = true;
                paymentResponseProgressBar.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    displayNotification();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                retryBookingBtn.setEnabled(true);
                preventGoBack = false;
                paymentResponseTitle.setText("Something happened. please click on retry again button");
                successResponseImg.setImageResource(R.drawable.error_status_booking);
                retryBookingBtn.setVisibility(View.VISIBLE);
                goHomeBtn.setVisibility(View.GONE);
                paymentResponseProgressBar.setVisibility(View.GONE);
                Toast.makeText(PaymentResponseActivity.this, "something happened. please try again", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uid", uid);
                params.put("ticket_id", String.valueOf(ticketId));
                params.put("bus_id", String.valueOf(busId));
                params.put("fname", fname);
                params.put("lname", lname);
                params.put("address", address);
                params.put("bus_name", busName);
                params.put("travel_date", travelDate);
                params.put("ticket_price", String.valueOf(ticketPrice));
                params.put("origin", origin);
                params.put("destination", destination);
                params.put("seats_available", String.valueOf(seatsAvailable));
                params.put("seats_taken", String.valueOf(seatsTaken));

                return params;
            }
        };

        reserveQueue.add(reserveRequest);
    }

    @Override
    public void onPaymentResponseError(String error) {
        goHomeBtn.setVisibility(View.GONE);
        retryBookingBtn.setVisibility(View.VISIBLE);
        //Handle payment request error.
        Toast.makeText(this, "payment error", Toast.LENGTH_SHORT).show();
        paymentResponseProgressBar.setVisibility(View.GONE);
        startActivity(new Intent(PaymentResponseActivity.this, BusDetailActivity.class));
    }

    public void goHome(View view) {
        startActivity(new Intent(PaymentResponseActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void displayNotification() {
        String channelId = "my_channel";
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel;

        notificationChannel = new NotificationChannel(channelId, "Ticket booking status", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[] {100, 200, 300, 400, 500, 500, 300, 200, 400});
        mNotificationManager.createNotificationChannel(notificationChannel);

        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this, channelId);
        mBuilder.setContentTitle("New Message");
        mBuilder.setContentText("You have booked a ticket successfully. Navigate to bookings section to see your booking details");
        mBuilder.setTicker("Booking Alert!!");
        mBuilder.setSmallIcon(R.drawable.img_succeed);
        mBuilder.setColor(R.color.colorPrimary);

        Intent resultIntent = new Intent(this, BookingsActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        /* Adds the Intent that starts the Activity to the top of the stack */
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        /* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(notificationID, mBuilder.build());
    }


}
