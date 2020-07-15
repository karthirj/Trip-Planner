package com.example.homework7a;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class CreateTrip extends AppCompatActivity {

    ImageView iv;
    EditText et_tname;
    EditText et_latitude;
    EditText et_longitude;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Button bt_create;
    Bitmap bitmapUpload = null;
    Trip trip=new Trip();
    User creator;

    String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        iv=findViewById(R.id.iv);
        et_latitude=findViewById(R.id.et_latitude);
        et_longitude=findViewById(R.id.et_longitude);
        et_tname=findViewById(R.id.et_trip_name);
        bt_create=findViewById(R.id.bt_create_trip);

        if(getIntent()!=null && getIntent().getExtras()!=null){
            creator=(User) getIntent().getSerializableExtra(DashboardActivity.tripCreatorModelKey);
        }
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });


        bt_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(et_tname.getText()==null||et_tname.getText().toString().equals("")){
                    Toast.makeText(CreateTrip.this, "Trip title is required", Toast.LENGTH_LONG).show();
                }
                else
                    trip.title=et_tname.getText().toString();
                if(et_latitude.getText()==null||et_latitude.getText().toString().equals("")){
                    Toast.makeText(CreateTrip.this, "Trip latitude is required", Toast.LENGTH_LONG).show();
                }
                else
                    trip.lat=Double.parseDouble(et_latitude.getText().toString());
                if(et_longitude.getText()==null||et_longitude.getText().toString().equals("")){
                    Toast.makeText(CreateTrip.this, "Trip longitude is required", Toast.LENGTH_LONG).show();
                }
                else
                    trip.longi=Double.parseDouble(et_longitude.getText().toString());

                trip.creatorEmail=creator.email;
                trip.creatorName = creator.firstname + creator.lastname;
                trip.userList=new ArrayList<>();
                trip.userList.add(creator.email);

                Trip trip1=trip;

                if (trip1!=null){
                    if(bitmapUpload==null)
                        Toast.makeText(CreateTrip.this, "Please upload an image", Toast.LENGTH_SHORT).show();
                    else {
                        trip=trip1;
                        uploadImage(getBitmapCamera(),trip1.title);
                    }}
            }
        });

    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iv.setImageBitmap(imageBitmap);
            bitmapUpload = imageBitmap;
        }
    }
    private Bitmap getBitmapCamera() {
        if (bitmapUpload == null){
            return ((BitmapDrawable) iv.getDrawable()).getBitmap();
        }
        return bitmapUpload;
    }
    private void uploadImage(Bitmap photoBitmap,String title){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        final StorageReference imageRepo = storageReference.child("tripimages/"+title+".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRepo.putBytes(data);


        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }

                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Log.d("Tag", "Image Download URL"+ task.getResult());
                    URL = task.getResult().toString();
                    trip.tripPhotoUrl=URL;
                    FireStoreFunc.UploadDataTrip(trip);
                    finish();

                    Toast.makeText(CreateTrip.this, "uploaded image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Loading" + progress + "% done");
            }
        });

    }

}
