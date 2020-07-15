package com.example.homework7a;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    GoogleSignInClient GoogleClient;
    SignInButton bt_Google;
    Button bt_login;
    Button bt_register;
    EditText et_email;
    EditText et_password;

    static String key = "Email";
    private static final int GOOGLE_LOGIN = 201;

    private static FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_Google= findViewById(R.id.bt_Google);
        bt_login=findViewById(R.id.bt_login);
        bt_register=findViewById(R.id.bt_register);
        et_email=findViewById(R.id.et_email);
        et_password=findViewById(R.id.et_password);

        mAuth = FirebaseAuth.getInstance();

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((et_email.getText()!= null && et_password.getText()!= null)
                        && (!et_email.getText().toString().equals("") && !et_password.getText().toString().equals(""))){
                    String email = et_email.getText().toString();
                    String password = et_password.getText().toString();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        String email=user.getEmail();
                                        getData(email);
                                    } else {
                                        Log.d("demo", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(MainActivity.this, "Wrong email/password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else if(et_email.getText() == null || et_email.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Email is required", Toast.LENGTH_LONG).show();
                }
                else if(et_password.getText() == null || et_password.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Password is required", Toast.LENGTH_LONG).show();
                }
            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleClient= GoogleSignIn.getClient(this, gso );

        bt_Google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_LOGIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                getData(account.getEmail());
            } catch (ApiException e) {
                Log.d("demo", "Google sign in failed", e);
            }
        }
    }

    public void signIn(){

        Intent signInIntent = GoogleClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_LOGIN);
    }

    public void getData(final String email){
        db = FirebaseFirestore.getInstance();
        db.collection("User").whereEqualTo("email",email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if(task.getResult().size()>0){
                        for (QueryDocumentSnapshot document: task.getResult()){
                            if(document.getId().equals(email)){
                                User user=new User();
                                user.firstname=document.getString("firstname");
                                user.lastname = document.getString("lastname");
                                user.email=document.getString("email");
                                user.photoUrl= (document.getString("photoUrl")==null? "":document.getString("photoUrl"));
                                user.gender= (document.getString("gender")==null? "":document.getString("gender"));
                                Log.d("demo",user.email);
                                Intent i= new Intent(MainActivity.this,DashboardActivity.class);
                                i.putExtra("user",user);
                                startActivity(i);
                                finish();
                            }
                        }
                    }
                    else{
                        Intent i= new Intent(MainActivity.this,UserProfileActivity.class);
                        i.putExtra(key,email);
                        startActivity(i);
                        finish();
                    }
                }
            }
        });
    }
}

