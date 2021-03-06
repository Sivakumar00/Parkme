package com.parkme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.roger.catloadinglibrary.CatLoadingView;

import java.util.concurrent.TimeUnit;

public class StartActivity extends AppCompatActivity {

    public static int TAG = 1;
    Button mNext;
    EditText mMobile;
    String mVerificationId, mResendToken;
    FirebaseAuth mAuth;
    Button mBtnVerify;
    EditText mCode;
    String verifID;
    String mobile;
    CatLoadingView mView;
    DatabaseReference mDatabase;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //init
        mNext = (Button) findViewById(R.id.btn_next);
        mBtnVerify = (Button) findViewById(R.id.btn_verify);
        mCode = (EditText) findViewById(R.id.edt_code);
        mMobile = (EditText) findViewById(R.id.edt_mobile_num);
        mAuth = FirebaseAuth.getInstance();
        mView = new CatLoadingView();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("users").child("client");
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                // Save the verification id somewhere
                // ...
                verifID = verificationId;
                showComponents();
                // The corresponding whitelisted code above should be used to complete sign-in.

            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                // Sign in with the credential
                // ...
                Toast.makeText(getApplicationContext(), "sss", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // ...
                Toast.makeText(StartActivity.this, "Failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        };
        mBtnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mView.setText("Loading..!");
                mView.setCanceledOnTouchOutside(false);
                mView.show(getSupportFragmentManager(), "");
                verify();
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobile = mMobile.getText().toString();
                if (!TextUtils.isEmpty(mobile)) {
                    send_sms();
                }
            }
        });

    }

    private void send_sms() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile, 30L, TimeUnit.SECONDS,
                this, mCallbacks);

    }

    private void verify() {
        String input_Code = mCode.getText().toString();
        if (!input_Code.equals("")) {
            verifyNumber(verifID, input_Code);
        } else {
            Toast.makeText(this, "Enter code", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyNumber(String verifID, String input_code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifID, input_code);
        signInWithPhoneAuthCredential(credential);
    }

    private void showComponents() {
        mCode.setVisibility(View.VISIBLE);
        mBtnVerify.setVisibility(View.VISIBLE);
        mMobile.setVisibility(View.INVISIBLE);
        mNext.setVisibility(View.INVISIBLE);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(String.valueOf(TAG), "signInWithCredential:success");
                            mDatabase.child(mAuth.getCurrentUser().getUid()).child("mobile").setValue(mobile);
                            Toast.makeText(StartActivity.this, "Signed IN", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), UserDetailActivity.class);
                            intent.putExtra("user_id",mAuth.getCurrentUser().getUid());
                            startActivity(intent);
                            finish();
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            mView.dismiss();
                            Log.w(String.valueOf(TAG), "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

}
