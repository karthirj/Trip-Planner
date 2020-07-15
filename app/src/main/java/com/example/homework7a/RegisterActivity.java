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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText email;
    EditText password;
    EditText password2;
    Button bt_register;
    Button bt_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email=findViewById(R.id.et_email);
        password=findViewById(R.id.et_password);
        password2=findViewById(R.id.et_password2);

        mAuth=FirebaseAuth.getInstance();
        bt_register=findViewById(R.id.bt_register);
        bt_cancel=findViewById(R.id.bt_cancel);

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText()!=null && password.getText()!=null
                        && password2.getText()!=null
                        && !email.getText().toString().equals("") && !password.getText().toString().equals("")
                        && !password2.getText().toString().equals("") && password.getText().toString().equals(password2.getText().toString())
                        && password.getText().toString().length()>=6){
                    String emailString=email.getText().toString();
                    String passwordString=password.getText().toString();
                    mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                                        intent.putExtra(MainActivity.key,user.getEmail());
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Sign Up failed.",Toast.LENGTH_SHORT).show();
                                        Log.d("demo", "createUserWithEmail:failure", task.getException());
                                    }
                                }
                            });
                }
                else if(email.getText()==null || email.getText().toString().equals(""))
                        Toast.makeText(RegisterActivity.this, "Email is required", Toast.LENGTH_LONG).show();

                else if(password.getText()==null || password.getText().toString().equals(""))
                        Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_LONG).show();

                else if(password2.getText()==null || password2.getText().toString().equals(""))
                        Toast.makeText(RegisterActivity.this, "Re-enter Password is required", Toast.LENGTH_LONG).show();

                else if(!password.getText().toString().equals("") && !password2.getText().toString().equals("") && password.getText().toString().length()<6)
                        Toast.makeText(RegisterActivity.this, "Password must at least be 6 characters", Toast.LENGTH_LONG).show();

                else if(!password.getText().toString().equals("") && !password2.getText().toString().equals("") && !password.getText().toString().equals(password2.getText().toString()))
                        Toast.makeText(RegisterActivity.this, "Re-enter password does not match", Toast.LENGTH_LONG).show();
                }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(RegisterActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });

    }
}
