package com.example.homework7a;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class ChatRoom extends AppCompatActivity {

    private static FirebaseFirestore db;

    public static HashMap<String, String> hashMap = new HashMap<>();
    String URL;
    Bitmap bitmapUpload = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    EditText et_chatmsg;
    Button bt_msg;
    ImageView iv_chat_msg;
    Chat chatModel;
    public static User user;
    public static Trip trip;
    RecyclerView mRecyclerView;
    public static RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    public static ArrayList<String> pathUID = new ArrayList<>();
    public static ArrayList<Chat> messagesChat = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        db = FirebaseFirestore.getInstance();
        mRecyclerView = findViewById(R.id.rv);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(ChatRoom.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ChatAdapter(null);
        mRecyclerView.setAdapter(mAdapter);


        if (getIntent() != null && getIntent().getExtras() != null) {
            user = (User) getIntent().getExtras().getSerializable("chat");
            trip = (Trip) getIntent().getExtras().getSerializable("trip");
            messagesChat = new ArrayList<>();
            pathUID = new ArrayList<>();

            iv_chat_msg = findViewById(R.id.iv_chat_msg);
            et_chatmsg = findViewById(R.id.et_chatmsg);
            bt_msg = findViewById(R.id.bt_msg);
            iv_chat_msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dispatchTakePictureIntent();
                }
            });
            bt_msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String msg = et_chatmsg.getText().toString();
                    et_chatmsg.setText("");
                    chatModel = new Chat();
                    chatModel.trip_name = trip.title;
                    chatModel.creatorEmail = user.email;
                    chatModel.message = msg;
                    chatModel.creatorName = user.firstname + user.lastname;
                    chatModel.msgImgUrl = URL;
                    URL = "";

                    //if(iv_chat_msg.getDrawable()==getResources().getDrawable(android.R.drawable.ic_dialog_email)){
                    if (!msg.equals("")) {
//                    chatModel.creatorEmail = user.email;
//                    chatModel.message = msg;
//                    chatModel.creatorName = user.firstname + user.lastname;
                        chatModel.msgImgUrl = "";
                        URL = "";
                        Log.d("demo", "onClick: " + et_chatmsg.getText().toString());

                        db.collection("messages").add(chatModel)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("demo", documentReference.getId());

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("demo", "Error adding document", e);
                            }
                        });
                    } else {

                        uploadImageAndSaveData(getBitmapCamera(), user);
                    }

                }
            });


            db.collection("messages")
                    .whereEqualTo("trip_name", trip.title)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("demo", "listen:error", e);
                                return;
                            }

                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    Log.d("demo", "message: " + dc.getDocument().getData());
                                    Chat cm = new Chat();
                                    cm.message = dc.getDocument().getString("message");
                                    cm.creatorName = dc.getDocument().getString("creatorName");
                                    cm.creatorEmail = dc.getDocument().getString("creatorEmail");
                                    cm.msgImgUrl = dc.getDocument().getString("msgImgUrl");
                                    messagesChat.add(cm);
                                    pathUID.add(dc.getDocument().getId());

                                }
                                Log.d("demo", "outside: " + dc.getDocument().getData());
//                            mAdapter = new ChatAdapter(messagesChat);
//                            mRecyclerView.setAdapter(mAdapter);
//                            mAdapter.notifyDataSetChanged();
                                if (dc.getType() == DocumentChange.Type.REMOVED) {
                                    messagesChat.remove(dc.getDocument().getData());
                                }
                                mAdapter = new ChatAdapter(messagesChat);
                                mRecyclerView.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();

                            }

                        }
                    });

        } else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
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
            iv_chat_msg.setImageBitmap(imageBitmap);
            bitmapUpload = imageBitmap;
        }
    }

    private Bitmap getBitmapCamera() {
        if (bitmapUpload == null) {
            try {
                return ((BitmapDrawable) iv_chat_msg.getDrawable()).getBitmap();
            } catch (Exception e) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_email);
                return bitmap;
            }
        }
        return bitmapUpload;
    }

    private void uploadImageAndSaveData(Bitmap photoBitmap, final User userModel) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        final String path = "chatimages/" + UUID.randomUUID() + ".png";
        final StorageReference imageRepo = storageReference.child(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imageRepo.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.d("Tag", "Image Download URL" + task.getResult());
                    URL = task.getResult().toString();

                    hashMap.put(URL, path);
                    String msg = et_chatmsg.getText().toString();
                    et_chatmsg.setText("");
                    chatModel.creatorEmail = user.email;
                    chatModel.message = msg;
                    chatModel.creatorName = user.firstname + user.lastname;
                    chatModel.msgImgUrl = URL;
                    URL = "";

                    db.collection("messages").add(chatModel)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("demo", documentReference.getId());

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("demo", "Error adding document", e);
                        }
                    });
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
