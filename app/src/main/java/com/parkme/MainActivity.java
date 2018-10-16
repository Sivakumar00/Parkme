package com.parkme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkme.Model.Address;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth mAuth;
    private RecyclerView mUsersList;
    DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("PARK Me");
        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("parking_available");
        mDatabase.keepSynced(true);
        FirebaseUser currentUSer=FirebaseAuth.getInstance().getCurrentUser();
        mUsersList=(RecyclerView)findViewById(R.id.user_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        if(currentUSer==null){
            sendToStart();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Address,AddressViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Address, AddressViewHolder>(
                Address.class,
                R.layout.single_parking_layout,
                AddressViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final AddressViewHolder viewHolder, Address model, int position) {
                final String key=getRef(position).getKey();
                mDatabase.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String address=dataSnapshot.child("address").getValue().toString();
                        viewHolder.setPlace(address);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(getApplicationContext(),DetailActivity.class);
                        intent.putExtra("user_id",key);
                        startActivity(intent);

                    }
                });
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    private void sendToStart() {
        Intent intent=new Intent(getApplicationContext(),StartActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_request) {

            Intent intent=new Intent(getApplicationContext(),RequestListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_payment) {
//            Intent intent=new Intent(getApplicationContext(),PaymentBillActivity.class);
//            startActivity(intent);

        }  else if (id == R.id.logout) {

            mAuth.signOut();
            sendToStart();
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public AddressViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setPlace(String text){
            TextView mTxtPlace=(TextView)mView.findViewById(R.id.place_name);
            mTxtPlace.setText(text);
        }
    }
}
