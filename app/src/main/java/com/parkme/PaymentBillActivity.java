package com.parkme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkme.Config.Config;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;



public class PaymentBillActivity extends AppCompatActivity {

    private static final int PAYPAL_REQUEST_CODE=7171;
    static int BASE_FARE=20;
    int totalFare;
    String user_id;
    String amount="";
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private static PayPalConfiguration config=new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);
    Button btnPayNow;
    TextView txtFare,txtHours;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,PayPalService.class));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PAYPAL_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                PaymentConfirmation confirmation=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirmation!=null){
                    String paymentDetails=confirmation.toJSONObject().toString();
                    startActivity(new Intent(this,PaymentDetails.class)
                            .putExtra("PaymentDetails",paymentDetails)
                            .putExtra("PaymentAmount",amount));



                }
            }else if (resultCode== Activity.RESULT_CANCELED){
                Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show();
            }
        }else if (resultCode==PaymentActivity.RESULT_EXTRAS_INVALID){
            Toast.makeText(this,"Invalid",Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Intent intent=new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);
        Intent intent1=getIntent();
        user_id=intent1.getStringExtra("user_id");

        mAuth=FirebaseAuth.getInstance();
        txtFare=(TextView)findViewById(R.id.txt_fare);
        txtHours=(TextView)findViewById(R.id.edt_hour_parked);
        mDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_client").child(mAuth.getCurrentUser().getUid()).child(user_id);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int temp=Integer.parseInt(dataSnapshot.child("time_difference").getValue().toString());
                totalFare=Integer.parseInt(dataSnapshot.child("time_difference").getValue().toString())*BASE_FARE;
                txtFare.setText("Total Fare :  "+totalFare);
                txtHours.setText("Total Hours Consumed :  "+temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnPayNow=(Button)findViewById(R.id.btnPayNow);


        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPayment();

            }
        });

    }

    private void processPayment() {
        Common.user_id=user_id;
        PayPalPayment payPalPayment= new PayPalPayment(new BigDecimal(String.valueOf(totalFare)),"USD","Pay the bill",PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent=new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(intent,PAYPAL_REQUEST_CODE);

    }
}
