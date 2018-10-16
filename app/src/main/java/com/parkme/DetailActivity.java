package com.parkme;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetailActivity extends Activity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase,mParkDatabase;
    String lat,lng,name,address,mobile;
    TextView mName,mMobile,mAddress,mDate,mTime,mTimeOut;
    EditText mCarNumber;
    Button mSubmit;
    String timeout;
    String timeDifference;
    String user_id,car_no,date_submit,time;
    Calendar myCalendar;
    DatabaseReference mRequestAdminDatabase,mRequestClientDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("users").child("admin");
        mDatabase.keepSynced(true);
        mParkDatabase=FirebaseDatabase.getInstance().getReference().child("parking_available");
        mRequestAdminDatabase=FirebaseDatabase.getInstance().getReference().child("parking_request_admin");
        mRequestAdminDatabase.keepSynced(true);
        mRequestClientDatabase=FirebaseDatabase.getInstance().getReference().child("parking_request_client");
        mRequestClientDatabase.keepSynced(true);
        //init
        mName=(TextView)findViewById(R.id.owner_name);
        mMobile=(TextView)findViewById(R.id.owner_mobile);
        mAddress=(TextView)findViewById(R.id.parking_address);
        mDate=(TextView)findViewById(R.id.book_date);
        mTime=(TextView)findViewById(R.id.book_time);
        mTimeOut=(TextView)findViewById(R.id.book_time_out);
        mCarNumber=(EditText)findViewById(R.id.edt_car_number);
        mSubmit=(Button)findViewById(R.id.submit_request);
        myCalendar = Calendar.getInstance();
        //intent
        JodaTimeAndroid.init(this);
        final Intent intent=getIntent();
        user_id=intent.getStringExtra("user_id");
        mTimeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(DetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mTimeOut.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
        mDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name=dataSnapshot.child("name").getValue().toString();
                mobile=dataSnapshot.child("mobile").getValue().toString();
                mName.setText(name);
                mMobile.setText(mobile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mParkDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                address=dataSnapshot.child("address").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //date picker
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog mDatePicker;
                mDatePicker = new DatePickerDialog(DetailActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                        /*      Your code   to get date and time    */
                        selectedmonth = selectedmonth + 1;

                        mDate.setText("" + selectedday + "/" + selectedmonth + "/" + selectedyear);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Date");
                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                mDatePicker.show();
            }
        });




        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //time picker
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(DetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                car_no=mCarNumber.getText().toString();
                date_submit=mDate.getText().toString();
                time=mTime.getText().toString();
                timeout=mTimeOut.getText().toString();
                computeTimeDifference(date_submit,time,timeout);
                if(!TextUtils.isEmpty(car_no)&&
                        !TextUtils.isEmpty(date_submit)&&
                        !TextUtils.isEmpty(time)&&!TextUtils.isEmpty(timeout)){
                    Map <String,String>map=new HashMap<>();
                    map.put("status","Requested");
                    map.put("car_no",car_no);
                    map.put("date",date_submit);
                    map.put("time",time);
                    map.put("timeout",timeout);
                    map.put("time_difference",timeDifference);
                    mRequestClientDatabase.child(mAuth.getCurrentUser().getUid()).child(user_id).setValue(map);
                    mRequestAdminDatabase.child(user_id).child(mAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(DetailActivity.this, "Requested..!", Toast.LENGTH_SHORT).show();
                                Intent intent1=new Intent(getApplicationContext(),MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }else
                            {
                                Toast.makeText(DetailActivity.this, "Request Failed.. Retry", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

    }

    private void computeTimeDifference(String date,String time, String timeout) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Date d1 = null;
        Date d2 = null;

        try {
            String t1=date+" "+time;
            String t2=date+" "+timeout;
            d1 = format.parse(t1);
            d2 = format.parse(t2);

            DateTime dt1 = new DateTime(d1);
            DateTime dt2 = new DateTime(d2);

//            System.out.print(Days.daysBetween(dt1, dt2).getDays() + " days, ");
//            System.out.print(Hours.hoursBetween(dt1, dt2).getHours() % 24 + " hours, ");
//            System.out.print(Minutes.minutesBetween(dt1, dt2).getMinutes() % 60 + " minutes, ");
//            System.out.print(Seconds.secondsBetween(dt1, dt2).getSeconds() % 60 + " seconds.");
            String result= String.valueOf(Hours.hoursBetween(dt1,dt2));
            timeDifference = result.replaceAll("[^0-9]+", "").trim();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateLabelDate() {
        String myFormat = "dd/mm/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mDate.setText(sdf.format(myCalendar.getTime()));
    }
}
