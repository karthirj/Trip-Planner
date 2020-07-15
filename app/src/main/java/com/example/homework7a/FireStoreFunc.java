package com.example.homework7a;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FireStoreFunc {
    private static FirebaseFirestore db;

    public static void UploadData(User user){
        db = FirebaseFirestore.getInstance();

        db.collection("User").document(user.email).set(user);

    }

    public static void UploadDataTrip(Trip trip){
        db = FirebaseFirestore.getInstance();

        db.collection("Trips").document(trip.title+"-"+trip.creatorEmail).set(trip);

    }
    public static User getUserDatabyEmail(final String email){
        final User user=new User();
        db = FirebaseFirestore.getInstance();
        db.collection("User").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document: task.getResult()){
                        if(document.getId().equals(email)){
                            user.authToken=document.getString("authToken");
                            user.firstname=document.getString("firstname");
                            user.lastname=document.getString("lastname");
                            user.email=document.getString("email");
                            user.photoUrl= (document.getString("photoUrl")==null? "":document.getString("photoUrl"));
                            Log.d("demo",user.email);

                        }
                    }
                }
            }
        });
        return user;

    }

}

