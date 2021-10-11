package com.example.cardapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {
    String ticketApiUrl = "http://192.168.43.50/ticket/ticket_api";
    RequestQueue feedbackQueue;
    StringRequest feedbackRequest;
    ProgressDialog dialog;
    EditText reason_edit;
    SharedPreferences preferences;
    String uid;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        dialog = new ProgressDialog(this);
        dialog.create();
        dialog.setTitle("Submitting Issue...");
        dialog.setCancelable(false);

        //get user id
        preferences = getSharedPreferences("loggedInPrefs", MODE_PRIVATE);
        uid = preferences.getString("uid", "");

        reason_edit = (EditText) findViewById(R.id.reason_edit);

        feedbackQueue = Volley.newRequestQueue(this);
    }

    public void backToMainFeed(View view) {
        startActivity(new Intent(FeedbackActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    public void sendFeedback(View view) {
        dialog.show();
        final String feedBackText = reason_edit.getText().toString();

        if(feedBackText.isEmpty()) {
            Toast.makeText(this, "the input field cannot be empty", Toast.LENGTH_SHORT).show();
            dialog.hide();
            return;
        }

        feedbackRequest = new StringRequest(Request.Method.POST, ticketApiUrl + "/submit_feed.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.hide();
                Toast.makeText(FeedbackActivity.this, response, Toast.LENGTH_SHORT).show();

                if(response.equals("you have reported an issue successfully")) {
                    startActivity(new Intent(FeedbackActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.hide();
                Toast.makeText(FeedbackActivity.this, "Check your connection and try again", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                //params.put("uid", uid);
                params.put("feedback", feedBackText);

                return params;
            }
        };

        feedbackQueue.add(feedbackRequest);
    }
}
