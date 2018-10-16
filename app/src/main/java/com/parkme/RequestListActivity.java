package com.parkme;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkme.Model.Request;

public class RequestListActivity extends AppCompatActivity {
    DatabaseReference mRequestClientDatabase,mParkingDatabase;
    FirebaseAuth mAuth;
    RecyclerView mRecyclerView;
    Toolbar mToolbar;
    String address,status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);

        //init
        mAuth=FirebaseAuth.getInstance();
        mRequestClientDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_client").child(mAuth.getCurrentUser().getUid());
        mRequestClientDatabase.keepSynced(true);
        mParkingDatabase=FirebaseDatabase.getInstance().getReference().child("parking_available");
        mParkingDatabase.keepSynced(true);
        mToolbar=(Toolbar)findViewById(R.id.request_list_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Your Requests");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recyclerview
        mRecyclerView=(RecyclerView)findViewById(R.id.request_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Request,RequestViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                Request.class,
                R.layout.single_request_layout,
                RequestViewHolder.class,
                mRequestClientDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Request model, int position) {
              final String key=getRef(position).getKey();
                Log.w("User_id",key);
                mParkingDatabase.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        address=dataSnapshot.child("address").getValue().toString();
                        viewHolder.setAddress(address);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                mRequestClientDatabase.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        status=dataSnapshot.child("status").getValue().toString();
                        viewHolder.setAddress(address);
                        viewHolder.setStatus(status);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(status.equals("Accepted")) {
                            Intent intent = new Intent(getApplicationContext(), TicketActivity.class);
                            intent.putExtra("user_id", key);
                            startActivity(intent);
                        }
                    }
                });
            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }
    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public RequestViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setAddress(String text){
            TextView txtAddress=(TextView)mView.findViewById(R.id.request_address);
            txtAddress.setText(text);
        }
        public void setStatus(String text){
            TextView txtAddress=(TextView)mView.findViewById(R.id.request_status);
            if(text.equals("Requested")){
                txtAddress.setTextColor(mView.getResources().getColor(R.color.red));
            }else if(text.equals("Accepted")){
                txtAddress.setTextColor(mView.getResources().getColor(R.color.green));
            }else if(text.equals("Rejected")){
                txtAddress.setTextColor(mView.getResources().getColor(R.color.red));
            }
            txtAddress.setText(text);

        }
    }
}
