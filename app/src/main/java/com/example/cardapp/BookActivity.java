package com.example.cardapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookActivity extends AppCompatActivity {

    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;
    Spinner originEditText, destinationEditText;
    EditText datePickerTravelDate;
    SharedPreferences loginPrefs;
    String uid;
    String[] origin;
    String[] destination;
    RequestQueue routeQueue;
    CustomJsonObjectRequest routeRequest;
    String ticketApiUrl = "http://192.168.43.50/ticket/ticket_api";
    String originText, destinationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        myCalendar = Calendar.getInstance();

        //get the views
        datePickerTravelDate = (EditText) findViewById(R.id.datePickerTravelDate);
        originEditText = (Spinner) findViewById(R.id.originEditText);
        destinationEditText = (Spinner) findViewById(R.id.destinationEditText);

        //initialize the request queue
        routeQueue = Volley.newRequestQueue(this);
        routeRequest = new CustomJsonObjectRequest(Request.Method.GET, ticketApiUrl + "/route_list.php", null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) {
               // Toast.makeText(BookActivity.this, response.toString(), Toast.LENGTH_LONG).show();

                try {
                    JSONArray originArray = response.getJSONArray("origin");
                    JSONArray destArray = response.getJSONArray("destination");

                    origin = new String[originArray.length()];
                    destination = new String[destArray.length()];

                    for(int i = 0; i < originArray.length(); i++) {
                        JSONObject singleOrigin = originArray.getJSONObject(i);
                        origin[i] = singleOrigin.getString("starting_location");
                    }

                    for(int i = 0; i < destArray.length(); i++) {
                        JSONObject singleDest = destArray.getJSONObject(i);
                        destination[i] = singleDest.getString("destination");
                    }

                    //initialize the adapters
                    ArrayAdapter<String> originAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, origin);
                    ArrayAdapter<String> destAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, destination);

                    //assign the adapter values to the spinner
                    originEditText.setAdapter(originAdapter);
                    destinationEditText.setAdapter(destAdapter);
                } catch (JSONException e) {
                    Toast.makeText(BookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BookActivity.this, "Check your connection and try again", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(BookActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        routeQueue.add(routeRequest);

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        //get user id

        loginPrefs = getSharedPreferences("loggedInPrefs", MODE_PRIVATE);
        uid = loginPrefs.getString("uid", "");

        originEditText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                originText = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        destinationEditText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                destinationText = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateLabel() {
        String myFormat = "YYYY-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        datePickerTravelDate.setText(sdf.format(myCalendar.getTime()));
    }

    public void startAvailability(View view) {
        String travelDate = datePickerTravelDate.getText().toString();

        if(originText.isEmpty() || destinationText.isEmpty() || travelDate.isEmpty()) {
            Toast.makeText(this, "please fill in all fields", Toast.LENGTH_SHORT).show();
        } else if(originText.equals(destinationText)) {
            Toast.makeText(this, "origin and destination cannot bethe same", Toast.LENGTH_SHORT).show();
        } else {
            Intent bookIntent = new Intent(BookActivity.this, AvailabilityActivity.class);

            bookIntent.setType("text/plain");
            bookIntent.putExtra("uid", uid);
            bookIntent.putExtra("origin", originText);
            bookIntent.putExtra("destination", destinationText);
            bookIntent.putExtra("travel_date", travelDate);

            startActivity(bookIntent);
        }
    }

    public void showDatePicker(View view) {
        new DatePickerDialog(BookActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void backToMain(View view) {
        startActivity(new Intent(BookActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(BookActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
