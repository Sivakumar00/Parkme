package com.parkme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserDetailActivity extends AppCompatActivity {

    String user_id;
    EditText mEdtName;
    Button mNextBtn;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        final Intent intent=getIntent();
         user_id=intent.getStringExtra("user_id");

         //init
        mDatabase= FirebaseDatabase.getInstance().getReference().child("users").child("client");
        mEdtName=(EditText)findViewById(R.id.edt_name);
        mNextBtn=(Button)findViewById(R.id.btn_next);

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(mEdtName.getText().toString())){
                    mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue(mEdtName.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(UserDetailActivity.this, "Let's Go..", Toast.LENGTH_SHORT).show();
                                Intent intent1=new Intent(getApplicationContext(),MainActivity.class);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent1);
                            }else{
                                Toast.makeText(UserDetailActivity.this, "Failed to connect server", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Enter the field..!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
