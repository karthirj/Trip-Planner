
package com.example.homework7a;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OtherUserTripsActivity extends AppCompatActivity {
    ArrayList<Trip> tripsArrayList=new ArrayList<>();
    User userModel;
    User loggedInUserModel;
    FirebaseFirestore db;
    String userEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_trips);
        final ListView listView= findViewById(R.id.lv_trip);

        if (getIntent().getExtras()!=null){
            loggedInUserModel= (User) getIntent().getExtras().getSerializable("currentUserModel");
            userModel= (User) getIntent().getExtras().getSerializable("userWhoCreatedTheTrips");
        }

        if(loggedInUserModel!=null)
            userEmail=loggedInUserModel.email;

        if(userModel!=null)
            userEmail=userModel.email;


        db=FirebaseFirestore.getInstance();

        if(userModel==null && loggedInUserModel!=null){
            db.collection("Trips")
                    .whereArrayContains("userList", userEmail)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("tripIt", "Listen failed.", e);
                                return;
                            }

                            tripsArrayList= new ArrayList<Trip>();
                            for (QueryDocumentSnapshot doc : value) {
                                tripsArrayList.add(doc.toObject(Trip.class));
                            }
                            final DashboardActivity.TripAdapter tripAdapter= new DashboardActivity.TripAdapter(OtherUserTripsActivity.this, R.layout.triplayout,tripsArrayList,loggedInUserModel);
                            listView.setAdapter(tripAdapter);
                            tripAdapter.notifyDataSetChanged();
                        }
                    });
        }
        else{
            db.collection("Trips")
                    .whereEqualTo("creatorEmail",userModel.email)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("tripIt", "Listen failed.", e);
                                return;
                            }
                            tripsArrayList= new ArrayList<Trip>();
                            for (QueryDocumentSnapshot doc : value) {
                                tripsArrayList.add(doc.toObject(Trip.class));
                            }
                            final DashboardActivity.TripAdapter tripAdapter= new DashboardActivity.TripAdapter(OtherUserTripsActivity.this, R.layout.triplayout,tripsArrayList,loggedInUserModel);
                            listView.setAdapter(tripAdapter);
                            tripAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }
}

