package com.example.homework7a;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DashboardActivity extends AppCompatActivity {

    TextView tv_name;
    ImageButton bt_profile;
    ImageButton bt_logout;
    ImageButton bt_chatroom;
    ImageButton bt_addtrip;

    ArrayList<User> arrayList;
    ArrayList<Trip> tripArrayList;
    static String editKey = "editUser";
    static String currentUserEmailKey = "currentUserEmail";
    static String currentUserModelKey = "currentUserModel";
    static String tripCreatorModelKey = "tripCreator";
    static String tripKey = "tripData";

    FirebaseFirestore db;
    User userModel, loggedInUserModel;
    String userEmail;
    ArrayList<Trip> tripsArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tv_name = findViewById(R.id.tv_welcome);

        bt_profile = findViewById(R.id.bt_edit);
        bt_logout = findViewById(R.id.bt_logout);

        if (getIntent() != null && getIntent().getExtras() != null) {
            userModel = (User) getIntent().getExtras().getSerializable("user");
            String str = "Welcome " + userModel.firstname + " " + userModel.lastname;
            tv_name.setText(str);
        }

        final ListView listView= findViewById(R.id.lv_users);

        db = FirebaseFirestore.getInstance();

        db.collection("User").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.w("tripIt","ListenFailed",e);
                    return;
                }
                arrayList=new ArrayList<>();
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    arrayList.add(doc.toObject(User.class));
                }

                Iterator<User> iter = arrayList.iterator();
                while (iter.hasNext()) {
                    User user = iter.next();

                    if (user.email.equals(userModel.email))
                        iter.remove();
                }

                final UserAdapter userAdapter= new UserAdapter(DashboardActivity.this, R.layout.otherusers,arrayList,userModel);
                listView.setAdapter(userAdapter);
                userAdapter.notifyDataSetChanged();

            }
        });

        final ListView listView1= findViewById(R.id.lv_trip);

        if(loggedInUserModel!=null)
            userEmail=loggedInUserModel.email;

        if(userModel!=null)
            userEmail=userModel.email;

        db=FirebaseFirestore.getInstance();

        if(userModel==null && loggedInUserModel!=null){
            db.collection("Trips")
                    .whereArrayContains("userList", loggedInUserModel.email)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("tripIt", "Listen failed.", e);
                                return;
                            }
                            Log.d("tripit", "onEvent: +trips");
                            tripsArrayList= new ArrayList<Trip>();
                            for (QueryDocumentSnapshot doc : value) {
                                tripsArrayList.add(doc.toObject(Trip.class));
                            }
                            final TripAdapter tripAdapter= new TripAdapter(DashboardActivity.this, R.layout.triplayout,tripsArrayList,loggedInUserModel);
                            listView1.setAdapter(tripAdapter);
                            tripAdapter.notifyDataSetChanged();
                        }
                    });
        }
        else{
            tripsArrayList= new ArrayList<Trip>();
            db.collection("Trips")
                    .whereArrayContains("userList",userModel.email)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("tripIt", "Listen failed.", e);
                                return;
                            }
                            Log.d("tripit", "onEvent: +trips");

                            for (QueryDocumentSnapshot doc : value) {
                                Log.d("tripit", "onEvent: "+doc.toObject(Trip.class));
                                tripsArrayList.add(doc.toObject(Trip.class));
                            }
                        }
                    });
            final TripAdapter tripAdapter= new TripAdapter(DashboardActivity.this, R.layout.triplayout,tripsArrayList,userModel);

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


                            for (QueryDocumentSnapshot doc : value) {
                                if(!tripsArrayList.contains(doc.toObject(Trip.class))){
                                    tripsArrayList.add(doc.toObject(Trip.class));
                                }
                            }

                            //TripAdapter tripAdapter= new TripAdapter(DashboardActivity.this, R.layout.triplayout,tripsArrayList,userModel);
                            //listView1.setAdapter(tripAdapter);
                            listView1.setAdapter(tripAdapter);
                            tripAdapter.notifyDataSetChanged();
                        }
                    });
        }

        bt_addtrip = findViewById(R.id.bt_addtrip);
        bt_chatroom = findViewById(R.id.bt_chatroom);

        bt_addtrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("demo","baro nin ayyan");
                Intent i = new Intent(DashboardActivity.this, CreateTrip.class);
                i.putExtra(tripCreatorModelKey, userModel);
                startActivity(i);
            }
        });

        bt_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, UserProfileActivity.class);
                intent.putExtra(editKey, userModel.email);
                startActivity(intent);
            }
        });
        bt_chatroom = findViewById(R.id.bt_chatroom);
        bt_chatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, ChatRoom.class);
                if(userModel!=null){
                intent.putExtra("chat", userModel);}
                else{
                    intent.putExtra("chat", loggedInUserModel);
                }
                startActivity((intent));
            }
        });
        bt_logout = findViewById(R.id.bt_logout);
        bt_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });


    }
    public class UserAdapter extends ArrayAdapter<User> {
        User loggedInUser;
        public UserAdapter(@NonNull Context context, int resource, @NonNull List<User> objects, User loggedInUser) {
            super(context, resource, objects);
            this.loggedInUser=loggedInUser;
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final User userModel = getItem(position);
            ViewHolder viewHolder;
            try{
                if(convertView==null){
                    convertView= LayoutInflater.from(getContext()).inflate(R.layout.otherusers,parent,false);
                    viewHolder = new ViewHolder();
                    viewHolder.tv_name= convertView.findViewById(R.id.tv_uname);
                    viewHolder.bt_showtrip=convertView.findViewById(R.id.bt_showtrip);
                    convertView.setTag(viewHolder);
                }else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                String Uname = "Welcome "+userModel.firstname+" "+userModel.lastname;
                viewHolder.tv_name.setText(Uname);
                viewHolder.bt_showtrip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i= new Intent(getContext(),OtherUserTripsActivity.class);
                        i.putExtra("currentUserModel",loggedInUser);
                        i.putExtra("userWhoCreatedTheTrips",userModel);
                        getContext().startActivity(i);
                    }
                });
            }
            catch(Exception ex){
                Toast.makeText(getContext(),"Something went wrong!",Toast.LENGTH_SHORT).show();
            }
            return convertView;
        }

        private class ViewHolder{
            TextView tv_name;
            Button bt_showtrip;
        }
    }

    public static class TripAdapter extends ArrayAdapter<Trip> {
        ArrayList<Trip> trips;
        User loggedInUserModel;
        public TripAdapter(@NonNull Context context, int resource, @NonNull List<Trip> objects, @NonNull User loggedInUserModel) {
            super(context, resource, objects);
            if (objects.size() == 0)
                Toast.makeText(getContext(), "No trips to display", Toast.LENGTH_SHORT).show();
            this.trips = (ArrayList<Trip>) objects;
            this.loggedInUserModel = loggedInUserModel;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Trip trip = trips.get(position);
            ViewHolder viewHolder;
            try {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.triplayout, parent, false);
                    viewHolder = new ViewHolder();
                    viewHolder.view = convertView;
                    viewHolder.tv_tripTitle = convertView.findViewById(R.id.tv_tripno);
                    viewHolder.creatorEmail = convertView.findViewById(R.id.tv_cemail);
                    viewHolder.creatorName = convertView.findViewById(R.id.tv_cname);
                    viewHolder.button_viewDetails = convertView.findViewById(R.id.bt_view);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.tv_tripTitle.setText(trip.title);
                viewHolder.creatorEmail.setText(trip.creatorEmail);
                viewHolder.creatorName.setText(trip.creatorName);
                viewHolder.button_viewDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getContext(), OtherTripDetailsActivity.class);
                        Log.d("demo", "onClick: "+loggedInUserModel);
                        i.putExtra(DashboardActivity.currentUserModelKey, loggedInUserModel);
                        i.putExtra(DashboardActivity.tripKey, trip);
                        getContext().startActivity(i);
                    }
                });
                viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        FirebaseFirestore db;
                        db = FirebaseFirestore.getInstance();
                        db.collection("Trips").document(trip.title + "-" + trip.creatorEmail).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Trip removed successfully", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Oops something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return true;
                    }
                });

            } catch (Exception ex) {
                Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return trips.size();
        }

        private class ViewHolder {
            TextView tv_tripTitle, creatorName, creatorEmail;
            Button button_viewDetails;
            View view;
        }
    }

}