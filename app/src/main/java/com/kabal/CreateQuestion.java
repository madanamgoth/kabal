package com.kabal;


import android.location.Address;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;

import static android.R.attr.data;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class CreateQuestion extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextView locationTextView;
    Spinner levelSpinner;
    Spinner stateSpinner;
    Spinner districtSpinner;
    Button post;
    EditText question;
    EditText toPoint;
    EditText hashtags;
    EditText places;
    Address address;
    int counter;

    String levelText = null;
    String stateText = null;
    String districtText = null;
    List<String> districtList;


    FirebaseDatabase database;
    DatabaseReference dref;
    DatabaseReference districtsDataReference;
    DatabaseReference districtDataReference;
    DatabaseReference qanda;
    DatabaseReference counterRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question);
        setTitle("QUESTION");

        locationTextView = (TextView) findViewById(R.id.location_text);
        levelSpinner = (Spinner) findViewById(R.id.qanda_where_to_post);
        stateSpinner = (Spinner) findViewById(R.id.select_state);
        districtSpinner = (Spinner) findViewById(R.id.select_district);
        post = (Button) findViewById(R.id.post_qanda);
        question = (EditText) findViewById(R.id.question);
        toPoint = (EditText) findViewById(R.id.to_point);
        hashtags = (EditText) findViewById(R.id.hashtag);
        places = (EditText) findViewById(R.id.place);

        districtList = new ArrayList<String>();

        database = FirebaseDatabase.getInstance();
        dref = database.getReference();
        qanda = dref.child("q&a_source");

        districtsDataReference = dref.child("district_metadata");

        ArrayAdapter<CharSequence> levelArrayAdapter = ArrayAdapter.createFromResource(this, R.array.level, android.R.layout.simple_spinner_item);
        levelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(levelArrayAdapter);
        levelSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> districtsAdapter = ArrayAdapter.createFromResource(this, R.array.india_states, android.R.layout.simple_spinner_item);
        districtsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(districtsAdapter);
        stateSpinner.setOnItemSelectedListener(this);

        districtSpinner.setOnItemSelectedListener(this);


        address = ((Mykabal) this.getApplication()).getAddress();
        Toast.makeText(getApplicationContext(), address.getLocality(), Toast.LENGTH_SHORT).show();

        locationTextView.setText("You are now in - " + address.getLocality() + ", " + address.getAdminArea());


        post.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                postQuestion();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView.getId() == R.id.qanda_where_to_post){
            if(i == 0){
                levelText = null;
                stateSpinner.setEnabled(false);
                districtSpinner.setEnabled(false);
            }
            if(i == 1){
                levelText = adapterView.getItemAtPosition(i).toString();
                stateSpinner.setEnabled(true);
                districtSpinner.setEnabled(true);
            }
            if(i == 2){
                levelText = adapterView.getItemAtPosition(i).toString();
                stateSpinner.setEnabled(true);
                districtSpinner.setEnabled(false);
            }
            if(i == 3){
                levelText = adapterView.getItemAtPosition(i).toString();
                stateSpinner.setEnabled(false);
                districtSpinner.setEnabled(false);
            }
        }
        if(adapterView.getId() == R.id.select_state){
            if(stateSpinner.isEnabled()){
                stateText = adapterView.getItemAtPosition(i).toString();

                if(districtSpinner.isEnabled()){
                    districtDataReference = districtsDataReference.child(stateText);
                    districtDataReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            districtList.clear();
//                        Log.d("Create_Post", dataSnapshot.toString());
                            for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                Log.d("Create_Post", dataSnapshot1.getKey());
                                districtList.add(dataSnapshot1.getKey());
                            }
                            ArrayAdapter<String> districtAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, districtList);
                            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                            districtSpinner.removeAllViews();
                            districtSpinner.setAdapter(districtAdapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            else{
                stateText = null;
            }
        }
        if(adapterView.getId() == R.id.select_district){
            if(districtSpinner.isEnabled()){
                districtText = adapterView.getItemAtPosition(i).toString();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void postQuestion(){
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String datePath = "" + Calendar.getInstance().get(Calendar.YEAR);
        String questionValue = question.getText().toString();
        String hashTagsvalues = hashtags.getText().toString();
        String placeValues = places.getText().toString();
        String toPointValues = toPoint.getText().toString();
        int answerCount = 0;

        int month = Calendar.getInstance().get(Calendar.MONTH);
        month += 1;
        if(month%3 == 0){
            datePath = datePath + "-0" + (month-2) + "-0" + (month-1) + "-0" + month;
        }
        if(month%3 == 1){
            datePath = datePath + "-0" + month + "-0" + (month+1) + "-0" + (month+2);
        }
        if(month%3 == 2){
            datePath = datePath + "-0" + (month-1) + "-0" + month + "-0" + (month+1);
        }
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = sdfDate.format(cal.getTime());
        String formattedTime = sdfTime.format(cal.getTime());

        Log.d("ASDFGHJKIUYTREWQASZXCVB", deviceId);
        DatabaseReference postRef = qanda.child(address.getAdminArea() + "_" + address.getLocality() + "_q&a").child(datePath).push();
        postRef.child("question").setValue(questionValue);
        postRef.child("name").setValue(deviceId);
        postRef.child("answer_count").setValue(answerCount);
        postRef.child("date").setValue(formattedDate);
        postRef.child("time").setValue(formattedTime);
        postRef.child("subject_tags").setValue(hashTagsvalues);
        postRef.child("place").setValue(placeValues);
        postRef.child("pointing_to").setValue(toPointValues);
    }
}
