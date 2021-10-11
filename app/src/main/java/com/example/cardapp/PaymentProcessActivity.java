package com.example.cardapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yenepaySDK.PaymentOrderManager;
import com.yenepaySDK.PaymentResponse;
import com.yenepaySDK.YenePayPaymentActivity;
import com.yenepaySDK.errors.InvalidPaymentException;
import com.yenepaySDK.model.OrderedItem;
import com.yenepaySDK.model.Payment;
import com.yenepaySDK.model.YenePayConfiguration;

public class PaymentProcessActivity extends YenePayPaymentActivity {

    //get booking details from the shared preference
    SharedPreferences preferences;
    double ticketPrice;
    int ticketId, seatsToBook;
    String busName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_process);

        //retrieve all booking details
        preferences = getSharedPreferences("loggedInPrefs", MODE_PRIVATE);

        ticketId = preferences.getInt("ticketId", 0);
        busName = preferences.getString("busName", "");
        ticketPrice = preferences.getFloat("ticketPrice", 0);
        seatsToBook = preferences.getInt("numberOfSeatsToBook", 0);

        checkout();

        PendingIntent completionIntent = PendingIntent.getActivity(getApplicationContext(),
                PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE,
                new Intent(getApplicationContext(), PaymentResponseActivity.class), 0);
        PendingIntent cancellationIntent = PendingIntent.getActivity(getApplicationContext(),
                PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE,
                new Intent(getApplicationContext(), BusDetailActivity.class), 0);
        YenePayConfiguration.setDefaultInstance(new YenePayConfiguration.Builder(getApplicationContext())
                .setGlobalCompletionIntent(completionIntent)
                .setGlobalCancelIntent(cancellationIntent)
                .build());
    }

    private void checkout(){
        PaymentOrderManager paymentMgr = new PaymentOrderManager(
                "SB01090", "SB01090");
        paymentMgr.setPaymentProcess(PaymentOrderManager.PROCESS_EXPRESS);
        paymentMgr.setReturnUrl("com.example.cardapp:/payment2redirect");
        //If you want to move to production just omit this line
        paymentMgr.setUseSandboxEnabled(true);
        //This will disable shopping cart mode to enable set it true.
        paymentMgr.setShoppingCartMode(false);
        try {
            paymentMgr.addItem(new OrderedItem(Integer.toString(ticketId), busName, 1, ticketPrice));
            paymentMgr.startCheckout(this);
        } catch (InvalidPaymentException e) {
            Toast.makeText(this, "payment error", Toast.LENGTH_SHORT).show();
            Intent failedIntent = new Intent(PaymentProcessActivity.this, BusDetailActivity.class);
            failedIntent.putExtra("paymentFailure", "yes");
            startActivity(failedIntent);
        }
    }

    @Override
    public void onPaymentResponseArrived(PaymentResponse response) {
        //Handle Payment response
        if(response.isPaymentCompleted()){
            Toast.makeText(this, "payment completed", Toast.LENGTH_LONG).show();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("currentlyBooked", true);
            editor.apply();
            Intent intent = new Intent(PaymentProcessActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "something went wrong. please try again.", Toast.LENGTH_SHORT).show();
            Intent failedIntent = new Intent(PaymentProcessActivity.this, BusDetailActivity.class);
            failedIntent.putExtra("paymentFailure", "yes");
            startActivity(failedIntent);
        }
    }

    @Override
    public void onPaymentResponseError(String error) {
        //Handle payment request error.
        Toast.makeText(this, "payment error", Toast.LENGTH_SHORT).show();
        Intent failedIntent = new Intent(PaymentProcessActivity.this, BusDetailActivity.class);
        failedIntent.putExtra("paymentFailure", "yes");
        startActivity(failedIntent);
    }

    public void retryPayment(View view) {
        checkout();
    }

    public void startHome(View view) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("ticketId");
        editor.remove("busId");
        editor.remove("busName");
        editor.remove("travelDate");
        editor.remove("ticketPrice");
        editor.remove("origin");
        editor.remove("destination");
        editor.remove("seatsAvailable");
        editor.remove("numberOfSeatsToBook");
        editor.apply();

        startActivity(new Intent(PaymentProcessActivity.this, MainActivity.class));
    }
}
