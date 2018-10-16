package com.parkme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TicketActivity extends AppCompatActivity {

    TextView mToken,mAddress,mDate,mTime;
    Button mParked,mCancel;
    String user_id;
    DatabaseReference mDatabase,mParkDatabase;
    DatabaseReference mRequestClientDatabase,mRequestAdminDatabase;
    FirebaseAuth mAuth;
    ImageView mPayment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        final Intent intent=getIntent();
        user_id=intent.getStringExtra("user_id");
        mTime=(TextView)findViewById(R.id.book_time);
        mAuth=FirebaseAuth.getInstance();
        mPayment=(ImageView)findViewById(R.id.pay_btn);
        mPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(getApplicationContext(),PaymentBillActivity.class);
                intent1.putExtra("user_id",user_id);
                startActivity(intent1);
            }
        });
        mRequestAdminDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_admin").child(user_id).child(mAuth.getCurrentUser().getUid());
        mRequestAdminDatabase.keepSynced(true);
        mRequestClientDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_client").child(mAuth.getCurrentUser().getUid()).child(user_id);
        mRequestClientDatabase.keepSynced(true);
        mAddress=(TextView)findViewById(R.id.ticket_Address);
        mParkDatabase=FirebaseDatabase.getInstance().getReference().child("parking_available");
        mParkDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String address=dataSnapshot.child("address").getValue().toString();
                mAddress.setText(address);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDate=(TextView)findViewById(R.id.book_date);
        mToken=(TextView)findViewById(R.id.token_no);

        mCancel=(Button)findViewById(R.id.cancel_booking);
        mParked=(Button)findViewById(R.id.btn_parked);

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_client").child(mAuth.getCurrentUser().getUid());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String key=dataSnapshot1.getRef().getKey();
                    Log.w("keu",key);
                    String status=dataSnapshot.child(key).child("status").getValue().toString();
                    if(!status.equals("Parked")){
                        mParked.setVisibility(View.INVISIBLE);
                    }
                    mTime.setText(dataSnapshot.child(key).child("time").getValue().toString());
                    mToken.setText(dataSnapshot.child(key).child("token").getValue().toString());
                    mDate.setText(dataSnapshot.child(key).child("date").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

        mParked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child(user_id).child("status").setValue("Parked").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Status Changed..!",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(TicketActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRequestClientDatabase.removeValue();
                mRequestAdminDatabase.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Cancelled",Toast.LENGTH_SHORT).show();
                            Intent intent1=new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent1);
                        }
                    }
                });
            }
        });
    }
}
