package com.parkme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PaymentListActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Toolbar mToolbar;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mToolbar=(Toolbar)findViewById(R.id.payment_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Community");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView=(RecyclerView)findViewById(R.id.paylist);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        
    }
}
