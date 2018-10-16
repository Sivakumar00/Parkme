package com.parkme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentDetails extends AppCompatActivity {

    TextView txtAmount,txtId,txtStatus;
    Button mGoToHome;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    String user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_details);
        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("payment").child(mAuth.getCurrentUser().getUid());
        txtId=(TextView)findViewById(R.id.txt_id);
        txtAmount=(TextView)findViewById(R.id.txt_fare);
        txtStatus=(TextView)findViewById(R.id.txt_status);
        mGoToHome=(Button)findViewById(R.id.btnHome);
        user_id=Common.user_id;
        mGoToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        Intent intent=getIntent();
        try {
            JSONObject jsonObject=  new JSONObject(intent.getStringExtra("PaymentDetails"));
            showDetails(jsonObject.getJSONObject("response"),intent.getStringExtra("PaymentAmount"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showDetails(JSONObject response, String paymentAmount) {
        try {
            txtAmount.setText(paymentAmount);
            txtStatus.setText(response.getString("state"));

            txtId.setText(response.getString("id"));
            Map<String,String> map=new HashMap<>();
            map.put("payment_id",response.getString("id"));
            map.put("payment_amount",paymentAmount);
            map.put("status",response.getString("state"));
            mDatabase.child(user_id).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(PaymentDetails.this, "Payment successful..", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
