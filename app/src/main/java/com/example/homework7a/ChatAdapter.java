package com.example.homework7a;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    public  static ArrayList<Chat> messages;

    int pos;
    private static FirebaseFirestore db;


    public ChatAdapter( ArrayList<Chat>messages){
        this.messages=messages;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatlayout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Chat message = messages.get(position);
        pos=position;
        if(ChatRoom.user.email.equals(message.creatorEmail))  {
            if(message.message.length()!=0) {
                holder.tv_sender.setGravity(Gravity.RIGHT);
                holder.tv_msg.setGravity(Gravity.RIGHT);

                holder.tv_sender.setText(message.creatorName);
                holder.tv_msg.setText(message.message);
            }
            else{
                holder.tv_sender.setGravity(Gravity.RIGHT);
                Log.d("demo", "onBindViewHolder: "+message.msgImgUrl);
                if(!message.msgImgUrl.equals("") & !message.msgImgUrl.equals(null) ){
                    Picasso.get().load(message.msgImgUrl).into(holder.iv_chatmsg);
                }

                holder.tv_sender.setText(message.creatorName);
            }
        }
        else{
            if(message.message.length()!=0) {
                holder.tv_sender.setText(message.creatorName);
                holder.tv_msg.setText(message.message);
            }
            else {
                Picasso.get().load(message.msgImgUrl).into(holder.iv_chatmsg);
                holder.tv_sender.setText(message.creatorName);
            }

        }


        holder.msgitem=message;
        
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                final int pos= position;

                if (message.creatorEmail.equals(ChatRoom.user.email)) {
                    ChatRoom.messagesChat.remove(message);
                    Log.d("clicked",holder.msgitem.creatorName+ holder.msgitem.msgImgUrl);
                    if(message.message!=null){
                        String s=ChatRoom.pathUID.get(pos);
                        ChatRoom.pathUID.remove(pos);
                        db = FirebaseFirestore.getInstance();
                        db.collection("messages").document(s)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("clicked", "DocumentSnapshot successfully deleted!");

                                        ChatRoom.mAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("clicked", "Error deleting document", e);
                                    }
                                });
                    }
                   else if(message.msgImgUrl!=null){
                        //String path= ChatRoom.hashMap.get(message.msgImgUrl);
                        Log.d("demo", "onLongClick: "+message.msgImgUrl);
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference= storage.getReferenceFromUrl(message.msgImgUrl);
                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("clicked","image delete");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }

                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (messages==null)
            return 0;
        else
            return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_sender, tv_msg;
        public ImageView iv_chatmsg;
        Chat msgitem;
        String id;
        View view;

        public ViewHolder(@NonNull final View itemView ) {
            super(itemView);
            tv_msg=itemView.findViewById(R.id.tv_smsg);
            tv_sender=itemView.findViewById(R.id.tv_sname);
            iv_chatmsg=itemView.findViewById(R.id.iv_s);
            view = itemView;
            
        }


    }
}