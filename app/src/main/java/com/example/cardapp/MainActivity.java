package com.example.cardapp;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

//import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView top_welcome_text_dash;
    ActionMenuView main_menu_top;
    SharedPreferences loggedInPrefs;
    SharedPreferences.Editor editor;
    boolean currentlyBooked;
    private NotificationManager mNotificationManager;
    private int notificationID = 100;
    RequestQueue checkQueue;
    StringRequest checkRequest;
    ProgressDialog dialog;
    String uid = "";
    String ticketApiUrl = "http://192.168.43.50/ticket/ticket_api";

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(this);
        dialog.create();
        dialog.setCancelable(false);

        //setup request queue
        checkQueue = Volley.newRequestQueue(this);

        loggedInPrefs = getSharedPreferences("loggedInPrefs", MODE_PRIVATE);
        editor = loggedInPrefs.edit();
        uid = loggedInPrefs.getString("uid", "");
        currentlyBooked = loggedInPrefs.getBoolean("currentlyBooked", false);
        editor.remove("currentlyBooked");
        editor.apply();

        top_welcome_text_dash = (TextView) findViewById(R.id.top_welcome_text_dash);
        main_menu_top = (ActionMenuView) findViewById(R.id.main_menu_top);

//        main_menu_top.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//            @Override
//            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                getMenuInflater().inflate(R.menu.menu_main, menu);
//            }
//        });
//
//        main_menu_top.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                int id = item.getItemId();
//
//                if(id == R.id.logout_menu) {
//                    //remove user session from local storage
//                    SharedPreferences.Editor editor = loggedInPrefs.edit();
//                    editor.putString("uid", null);
//                    editor.putBoolean("remember_me", false);
//                    editor.apply();
//                    startActivity(new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
//                }
//
//                return false;
//            }
//        });

       // startService(new Intent(MainActivity.this, ContractNotificationService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public void startBook(View view) {
        //startActivity(new Intent(MainActivity.this, BookActivity.class));
        dialog.show();
        checkRequest = new StringRequest(Request.Method.POST, ticketApiUrl + "/check_user_booked.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("exists")) {
                    dialog.hide();
                    Toast.makeText(MainActivity.this, "You have already reserved a seat", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    startActivity(new Intent(MainActivity.this, BookActivity.class));
                    dialog.hide();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Check your connection", Toast.LENGTH_SHORT).show();
                dialog.hide();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uid", uid);

                return params;
            }
        };

        checkQueue.add(checkRequest);
    }

    public void startBookings(View view) {
        startActivity(new Intent(MainActivity.this, BookingsActivity.class));
    }

//    public void startContract(View view) {
//        startActivity(new Intent(MainActivity.this, ContractActivity.class));
//    }

    public void startFeedbackActivity(View view) {
        startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
    }

    public void finishApp(View view) {
        finish();
    }

    public void startAbout(View view) {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }

    public void startTutorial(View view) {
        startActivity(new Intent(MainActivity.this, TutorialActivity.class));
    }
}
