package com.example.homework7a;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class UserProfileActivity extends AppCompatActivity {

    EditText fname;
    EditText lname;
    TextView tv_email;
    Button bt_save;
    RadioGroup rg_gender;
    RadioButton rb_male;
    RadioButton rb_female;
    ImageView iv;
    String URL;
    Bitmap bitmapUpload = null;

    private static FirebaseFirestore db;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tv_email=findViewById(R.id.tv_email_conf);
        rg_gender=findViewById(R.id.rg);
        rb_male=findViewById(R.id.rb_male);
        rb_female=findViewById(R.id.rb_female);
        fname=findViewById(R.id.et_fname);
        lname=findViewById(R.id.et_lname);
        bt_save= findViewById(R.id.bt_save);
        iv=findViewById(R.id.iv_photo);

        if (getIntent()!=null && getIntent().getExtras()!=null) {
            String email = getIntent().getExtras().getString(MainActivity.key);
            if(email!=null){
                populateUserData(email);
            }

            String emailOfUserToEdit=getIntent().getExtras().getString(DashboardActivity.editKey);
            if(emailOfUserToEdit!=null){
                populateUserData(emailOfUserToEdit);
            }

        }

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user=new User();
                if(fname.getText()==null||fname.getText().toString().equals("")){
                    Toast.makeText(UserProfileActivity.this, "First name is required", Toast.LENGTH_LONG).show();
                }
                else if(lname.getText()==null||lname.getText().toString().equals("")){
                    Toast.makeText(UserProfileActivity.this, "Last name is required", Toast.LENGTH_LONG).show();
                }
                user.firstname=fname.getText().toString();
                user.lastname=lname.getText().toString();
                if(rg_gender.getCheckedRadioButtonId()==R.id.rb_male)
                    user.gender="male";
                if(rg_gender.getCheckedRadioButtonId()==R.id.rb_female)
                    user.gender="female";
                user.email=tv_email.getText().toString();

                if(user!=null){

                    uploadImageAndSaveData(getBitmapCamera(),user);
                }
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
        if (bitmapUpload == null) {
            try {
                return ((BitmapDrawable) iv.getDrawable()).getBitmap();
            } catch (Exception e) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_email);
                return bitmap;
            }
        }
        return bitmapUpload;
    }
    private void uploadImageAndSaveData(Bitmap photoBitmap, final User userModel){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        final StorageReference imageRepo = storageReference.child("images/"+userModel.email+".png");

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
                    userModel.photoUrl=URL;
                    FireStoreFunc.UploadData(userModel);
                    Intent i= new Intent(UserProfileActivity.this,DashboardActivity.class);
                    i.putExtra("user",userModel);
                    startActivity(i);
                    Toast.makeText(UserProfileActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                /*progressDialog.setProgress((int) progress);*/
                System.out.println("Loading" + progress + "% done");
            }
        });

    }

    public void populateUserData(final String email){
        db = FirebaseFirestore.getInstance();
        db.collection("User").whereEqualTo("email",email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    //populate data to edit the user;
                    if(task.getResult().size()>0){
                        for (QueryDocumentSnapshot document: task.getResult()){
                            if(document.getId().equals(email)){
                                User user=new User();
                                switch (document.getString("gender")){
                                    case "male": rb_male.setChecked(true);
                                        break;
                                    case "female": rb_female.setChecked(true);
                                }
                                tv_email.setText(document.getString("email"));
                                fname.setText(user.firstname=document.getString("firstname"));
                                lname.setText(user.lastname=document.getString("lastname"));
                                URL=document.getString("photoUrl");
                                Picasso.get().load(URL).into(iv);
                            }
                        }
                    }
                    else
                    {
                        rb_male.setChecked(true);
                        tv_email.setText(email);
                    }
                }
            }
        });
    }


}

