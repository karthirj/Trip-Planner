package com.example.homework7a;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class OtherTripDetailsActivity extends AppCompatActivity {
    User loggedInUser;
    Trip trip;
    TextView tv_name;
    TextView tv_email;
    TextView tv_latitude;
    TextView tv_longitude;
    ImageView iv_Photo;
    ImageButton bt_join;
    ImageButton bt_remove;
    ImageButton bt_chat;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_trip_details);
        tv_email=findViewById(R.id.tv_emailu);
        tv_name=findViewById(R.id.tv_onamae);
        tv_latitude=findViewById(R.id.tv_latu);
        tv_longitude=findViewById(R.id.tv_longu);
        iv_Photo=findViewById(R.id.iv_outrip);

        bt_chat=findViewById(R.id.ib_chatu);
        bt_join=findViewById(R.id.ib_joinu);
        bt_remove=findViewById(R.id.ib_removeu);

        bt_chat.setVisibility(View.INVISIBLE);
        bt_remove.setVisibility(View.INVISIBLE);
        db=FirebaseFirestore.getInstance();

        if(getIntent()!=null && getIntent().getSerializableExtra(DashboardActivity.currentUserModelKey)!=null) {
            loggedInUser = (User) getIntent().getSerializableExtra(DashboardActivity.currentUserModelKey);
            trip = (Trip) getIntent().getSerializableExtra(DashboardActivity.tripKey);
            Log.d("demo", "onCreate: " + trip);


            tv_longitude.setText(String.valueOf(trip.longi));
            tv_latitude.setText(String.valueOf(trip.lat));
            tv_name.setText(trip.creatorName);
            tv_email.setText(trip.creatorEmail);
            Picasso.get().load(trip.tripPhotoUrl).into(iv_Photo);

            if (trip.userList.contains(loggedInUser.email)) {
                bt_chat.setVisibility(View.VISIBLE);
                bt_remove.setVisibility(View.VISIBLE);
                bt_join.setVisibility(View.INVISIBLE);
            }

            bt_join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Map<String, Object> addUserToArrayMap = new HashMap<>();
                    addUserToArrayMap.put("userList", FieldValue.arrayUnion(loggedInUser.email));

                    db.collection("Trips").document(trip.title + "-" + trip.creatorEmail).update(addUserToArrayMap);
                    bt_chat.setVisibility(View.VISIBLE);
                    bt_remove.setVisibility(View.VISIBLE);
                    bt_join.setVisibility(View.INVISIBLE);
                    Toast.makeText(OtherTripDetailsActivity.this, "Trip joint successfully", Toast.LENGTH_SHORT).show();
                }
            });

            bt_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (loggedInUser.email.equals(trip.creatorEmail)) {

                        db.collection("Trips").document(trip.title + "-" + trip.creatorEmail)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(OtherTripDetailsActivity.this, "Trip removed successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(OtherTripDetailsActivity.this, "Oops something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        final Map<String, Object> addUserToArrayMap = new HashMap<>();
                        addUserToArrayMap.put("userList", FieldValue.arrayRemove(loggedInUser.email));
                        db.collection("Trips").document(trip.title + "-" + trip.creatorEmail).update(addUserToArrayMap);
                        bt_chat.setVisibility(View.INVISIBLE);
                        bt_remove.setVisibility(View.INVISIBLE);
                        bt_join.setVisibility(View.VISIBLE);
                        Toast.makeText(OtherTripDetailsActivity.this, "Trip removed successfully", Toast.LENGTH_SHORT).show();

                    }
                }
            });

            bt_chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(OtherTripDetailsActivity.this, ChatRoom.class);
                    intent.putExtra("chat", loggedInUser);
                    intent.putExtra("trip", trip);
                    startActivity(intent);
                    finish();
                }
            });
        }else{
            Log.d("demo", "onCreate: Error");
        }


    }


}
