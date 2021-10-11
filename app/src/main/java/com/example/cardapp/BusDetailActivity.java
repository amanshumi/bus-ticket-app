package com.example.cardapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Array;

public class BusDetailActivity extends AppCompatActivity {

//    int[] seats;
//    RadioButton seat_1, seat_2, seat_3, seat_4, seat_5, seat_6, seat_7, seat_8, seat_9, seat_10, seat_11, seat_12, seat_13, seat_14, seat_15, seat_16, seat_17, seat_18, seat_19, seat_20, seat_21, seat_22, seat_23, seat_24, seat_25, seat_26, seat_27, seat_28, seat_29, seat_30, seat_31, seat_32, seat_33;
//    RadioButton[] seat_img_all;
//    RadioGroup seat_list_radio_group;
    Spinner seat_spinner, payment_option_spinner;
    EditText fnameEdit, lnameEdit, addressEdit, phoneEdit, childrenEditText, adultEditText;
    TextView busNameText, departingTimeText, ticketPriceText, originText, destinationText, seatsAvailableText;
    int ticketId, busId, seatsAvailable, children, adults;
    double ticketPrice;
    String busName, departingTime, origin, destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_detail);

        busNameText = (TextView) findViewById(R.id.busNameText);
        departingTimeText = (TextView) findViewById(R.id.departingTimeText);
        ticketPriceText = (TextView) findViewById(R.id.ticketPriceText);
        originText = (TextView) findViewById(R.id.text_from_route);
        destinationText = (TextView) findViewById(R.id.text_to_route);
        seatsAvailableText = (TextView) findViewById(R.id.text_seats_available);
       // seat_spinner = (Spinner) findViewById(R.id.spinner_seat_list);
        payment_option_spinner = (Spinner) findViewById(R.id.spinner_payment_option);

        fnameEdit = (EditText) findViewById(R.id.fnameEdit);
        lnameEdit= (EditText) findViewById(R.id.lnameEdit);
        phoneEdit = (EditText) findViewById(R.id.phoneEdit);
        addressEdit = (EditText) findViewById(R.id.addressEdit);
        childrenEditText = (EditText) findViewById(R.id.childrenEditText);
        adultEditText = (EditText) findViewById(R.id.adultsEditText);

        //retrieve availability details from the availability activity
        ticketId = getIntent().getIntExtra("ticketId", 0);
        busId = getIntent().getIntExtra("busId", 0);
        busName = getIntent().getStringExtra("busName");
        departingTime = getIntent().getStringExtra("travelDate");
        ticketPrice = getIntent().getDoubleExtra("price", 0);
        seatsAvailable = getIntent().getIntExtra("seatsAvailable", 0);
        origin = getIntent().getStringExtra("origin");
        destination = getIntent().getStringExtra("destination");

        busNameText.setText(busName);
        departingTimeText.setText("Departing time : " + departingTime);
        ticketPriceText.setText("Price : " + ticketPrice + " ETB");
        originText.setText("Origin : " + origin);
        destinationText.setText("Destination : " + destination);
        seatsAvailableText.setText("Seats Available : " + String.valueOf(seatsAvailable));

        String[] payment_methods = {"YenePay"};

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        ArrayAdapter<String> payment_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, payment_methods);

        payment_option_spinner.setAdapter(payment_adapter);
      //  seat_spinner.setAdapter(adapter);
    }

    public void startPaymentProcess(View view) {
        //get the values of the number of children and adults for which tickets will be reserved
        String fname = fnameEdit.getText().toString();
        String lname = lnameEdit.getText().toString();
        String phone = phoneEdit.getText().toString();
        String address = addressEdit.getText().toString();
        String childrenStr = childrenEditText.getText().toString();
        String adultStr = adultEditText.getText().toString();

        //set the percentage for children
        double childrenPercentage =  ticketPrice * 0.25;
        double childrenOverallPrice = 0;
        double adultOverallPrice = 0;
        int numberOfSeatsToBook = 0;

        if(!childrenStr.isEmpty()) {
            children = Integer.parseInt(childrenEditText.getText().toString());

            if(children > 0) {
                childrenOverallPrice = (children * childrenPercentage);
            } else {
                childrenOverallPrice = 0;
            }
        } else {
            children = 0;
        }

        if(!adultStr.isEmpty()) {
            adults = Integer.parseInt(adultEditText.getText().toString());

            if(adults > 0) {
                adultOverallPrice = (ticketPrice * adults);
            } else {
                adultOverallPrice = 0;
            }
        } else {
            adults = 0;
        }

        //calculate overall price if children or other adults are getting tickets
        double newTicketPrice = (adultOverallPrice + childrenOverallPrice) + ticketPrice;

        //set seat number automatically
        if(seatsAvailable > 0) {
            numberOfSeatsToBook = children + adults + 1;
        }

        if(numberOfSeatsToBook > seatsAvailable) {
            Toast.makeText(this, "invalid number of seats", Toast.LENGTH_SHORT).show();
            return;
        }

        //Toast.makeText(this, Integer.toString(numberOfSeatsToBook), Toast.LENGTH_SHORT).show();
        //seat number will be chosen on backend rather than on front end

        SharedPreferences preferences = getSharedPreferences("loggedInPrefs", MODE_PRIVATE);
        //String uid = preferences.getString("uid", "");

        //put all availability values in the shared preferences so that it will be accessed later after payment has been completed
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("uid", phone);
        editor.putInt("ticketId", ticketId);
        editor.putInt("busId", busId);
        editor.putString("fname", fname);
        editor.putString("lname", lname);
        editor.putString("address", address);
        editor.putString("busName", busName);
        editor.putString("travelDate", departingTime);
        editor.putFloat("ticketPrice", (float) newTicketPrice);
        editor.putString("origin", origin);
        editor.putString("destination", destination);
        editor.putInt("seatsAvailable", seatsAvailable);
        editor.putInt("numberOfSeatsToBook", numberOfSeatsToBook);
        editor.apply();

        startActivity(new Intent(BusDetailActivity.this, PaymentProcessActivity.class));
    }

    public void backToAvail(View view) {
        startActivity(new Intent(BusDetailActivity.this, AvailabilityActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
