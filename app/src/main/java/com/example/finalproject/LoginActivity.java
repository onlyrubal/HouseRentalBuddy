package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText loginEmailText;
    private TextInputEditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;
    private FirebaseAuth mAuth;
    ProgressBar loginProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        loginEmailText = findViewById(R.id.login_email);
        loginPassText = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_btn);
        loginRegBtn = findViewById(R.id.login_reg_btn);
        loginProgress = findViewById(R.id.login_progress);

        //Created on click listener for the register button
        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        //To Authenticate the user after entering email and password text from the fields.
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginEmail = loginEmailText.getText().toString();
                String loginPassword = loginPassText.getText().toString();

                //Once the user enters both email and password
                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)){
                    loginProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendToMain();
                            }
                            // Here user is not logged in. So we will give error message to the user in the invisible text view.
                            else{
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                loginProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }

            }
        });

    }

    // If on starting the app, if user has active login session then it goes directly to the main screen.
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //This is going to happen if user is logged in i.e we got the user, that is why we are allowing user to go to main screen of the app.
        if(currentUser!=null) {
            sendToMain();
        }

    }

    private void sendToMain() {
            //This intent is going to the main activity.
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
}

