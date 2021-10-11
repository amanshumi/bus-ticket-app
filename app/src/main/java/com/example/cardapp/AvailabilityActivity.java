package com.example.cardapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimationUtilsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static android.view.View.GONE;

public class AvailabilityActivity extends AppCompatActivity {

    ListView availableBusList;
    TicketAdapter ticketAdapter;
    ArrayList<Ticket> ticketArrayList;
    RelativeLayout top_list_relative;
    SharedPreferences loginPrefs;
    String uid, origin, destination, travelDate;
    RequestQueue availabilityQueue;
    CustomJsonObjectRequest availabilityRequest;
    String ticketApiUrl = "http://192.168.43.50/ticket/ticket_api";
    ProgressBar availabilityProgressBar;
    JSONObject objecto;
    TextView destinationTv, originTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability);

        availabilityProgressBar = (ProgressBar) findViewById(R.id.availabilityProgressBar);
        uid = getIntent().getStringExtra("uid");
        origin = getIntent().getStringExtra("origin");
        destination = getIntent().getStringExtra("destination");
        travelDate = getIntent().getStringExtra("travel_date");
        destinationTv = (TextView) findViewById(R.id.text_from_route);
        originTv = (TextView) findViewById(R.id.startText);

        destinationTv.setText(destination);
        originTv.setText(origin);

        availabilityProgressBar.setVisibility(View.VISIBLE);

        //setup a requaest queue
        availabilityQueue = Volley.newRequestQueue(this);

        //send the availability request
        sendAvailabilityRequest();

        availableBusList = (ListView) findViewById(R.id.availableBusLis);
        top_list_relative =  (RelativeLayout) findViewById(R.id.top_list_relative);
        ticketArrayList = new ArrayList<Ticket>();
//
//        ticketArrayList.add(new Ticket(7, 291801, "Mercedes 1890", 1, 11, 60, 140, "11:30", "Adama", "Addis Ababa", "Jan 10, 2021", "Jan 11, 2021", true));
//
//        ticketAdapter = new TicketAdapter(this, ticketArrayList);
//
//        availableBusList.setAdapter(ticketAdapter);
//
//        availableBusList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Ticket singleTicket = (Ticket) parent.getItemAtPosition(position);
//
//                Toast.makeText(AvailabilityActivity.this, singleTicket.getBusName(), Toast.LENGTH_SHORT).show();
//                Intent singleTicketIntent = new Intent(AvailabilityActivity.this, BusDetailActivity.class);
//
//                singleTicketIntent.putExtra("ticketId", singleTicket.getTicketId());
//                singleTicketIntent.putExtra("busName", singleTicket.getBusName());
//                singleTicketIntent.putExtra("departingTime", singleTicket.getDepartingTime());
//                singleTicketIntent.putExtra("price", Integer.toString(singleTicket.getTicketPrice()));
//                singleTicketIntent.putExtra("origin", singleTicket.getOrigin());
//                singleTicketIntent.putExtra("destination", singleTicket.getDestination());
//                startActivity(singleTicketIntent);
//            }
//        });
    }

    public void sendAvailabilityRequest () {
        objecto = new JSONObject();
        try{
           // objecto.put("uid", uid);
            objecto.put("origin", origin);
            objecto.put("destination", destination);
            objecto.put("travel_date", travelDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //send the request
        availabilityRequest = new CustomJsonObjectRequest(Request.Method.POST, ticketApiUrl + "/find_availability.php", objecto, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               // Toast.makeText(AvailabilityActivity.this, response.toString(), Toast.LENGTH_LONG).show();

                try {
                    String status = response.getString("status");

                    if(!status.equals("success")) {
                        Toast.makeText(AvailabilityActivity.this, status, Toast.LENGTH_SHORT).show();
                    } else {
                        //parse the json object into a java arrays and objects
                        try{
                            JSONArray allBus = response.getJSONArray("data");

                            for(int i = 0; i < allBus.length(); i++) {
                                JSONObject details = allBus.getJSONObject(i);
                                int ticket_id = details.getInt("ticket_id");
                                int bus_id = details.getInt("bus_id");
                                String bus_name = details.getString("bus_model");
                                String bus_side_no = details.getString("bus_side_no");
                                String bus_license_plate = details.getString("bus_license_plate");
                                String origin = details.getString("starting_location");
                                String destiny = details.getString("destination");
                                int seats_available = details.getInt("seats_available");
                                double price = details.getDouble("ticket_price");
                                String bus_img = "http://192.168.43.56/transport/images/vehicle/" + details.getString("bus_img");

                                ticketArrayList.add(new Ticket(ticket_id, bus_name, bus_id, seats_available, price, travelDate, origin, destiny, bus_img));
                            }

                            ticketAdapter = new TicketAdapter(AvailabilityActivity.this, ticketArrayList);

                            availableBusList.setAdapter(ticketAdapter);

                            //add event listener for each bus list items

                            availableBusList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Ticket singleTicket = (Ticket) parent.getItemAtPosition(position);

                                    //Toast.makeText(AvailabilityActivity.this, Integer.toString(singleTicket.getTicketPrice()), Toast.LENGTH_SHORT).show();
                                    Intent singleTicketIntent = new Intent(AvailabilityActivity.this, BusDetailActivity.class);

                                    singleTicketIntent.putExtra("ticketId", singleTicket.getTicketId());
                                    singleTicketIntent.putExtra("busName", singleTicket.getBusName());
                                    singleTicketIntent.putExtra("busId", singleTicket.getBusId());
                                    singleTicketIntent.putExtra("seatsAvailable", singleTicket.getSeatAvailable());
                                    singleTicketIntent.putExtra("travelDate", singleTicket.getTravelDate());
                                    singleTicketIntent.putExtra("price", singleTicket.getTicketPrice());
                                    singleTicketIntent.putExtra("origin", singleTicket.getOrigin());
                                    singleTicketIntent.putExtra("destination", singleTicket.getDestination());
                                    startActivity(singleTicketIntent);
                                }
                            });
                        } catch(JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(AvailabilityActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                availabilityProgressBar.setVisibility(GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AvailabilityActivity.this, "Check your connection", Toast.LENGTH_SHORT).show();
                availabilityProgressBar.setVisibility(GONE);
                startActivity(new Intent(AvailabilityActivity.this, BookActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        availabilityQueue.add(availabilityRequest);
    }

    public void startDetail(View view) {
        startActivity(new Intent(AvailabilityActivity.this, BusDetailActivity.class));
    }

    public void backToBook(View view) {
        startActivity(new Intent(AvailabilityActivity.this, BookActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}

class CustomJsonObjectRequest extends JsonRequest<JSONObject> {
    /*
     * Copyright (C) 2011 The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */


    /**
     * Creates a new request.
     *
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param jsonRequest A {@link JSONObject} to post with the request. Null indicates no
     *     parameters will be posted along with request.
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public CustomJsonObjectRequest(
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(
                method,
                url,
                (jsonRequest == null) ? null : jsonRequest.toString(),
                listener,
                errorListener);
    }

    /**
     * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is <code>null
     * </code> , <code>POST</code> otherwise.
     *
     * @see #CustomJsonObjectRequest(String, JSONObject, Response.Listener, Response.ErrorListener) (int, String, JSONObject, Response.Listener, Response.ErrorListener)
     */
    public CustomJsonObjectRequest(
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        this(
                jsonRequest == null ? Request.Method.GET : Request.Method.POST,
                url,
                jsonRequest,
                listener,
                errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(
                            response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(
                    new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

}

