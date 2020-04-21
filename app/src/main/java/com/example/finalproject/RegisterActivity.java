package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText regEmailField;
    private TextInputEditText regPassField;
    private TextInputEditText regConfirmPassField;
    private Button regBtn;
    private Button regLoginBtn;
    private ProgressBar regProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        regEmailField = (TextInputEditText) findViewById(R.id.reg_email);
        regPassField = (TextInputEditText) findViewById(R.id.reg_pass);
        regConfirmPassField = (TextInputEditText) findViewById(R.id.reg_confirm_pass);
        regBtn = (Button) findViewById(R.id.reg_btn);
        regLoginBtn = (Button) findViewById(R.id.reg_login_btn);
        regProgress = (ProgressBar) findViewById(R.id.reg_progress);


        regLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });


        // Setting up the onClick listener for register button to create a new user
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regEmailField.getText().toString();
                String password = regPassField.getText().toString();
                String confirmPassword = regConfirmPassField.getText().toString();

                //Only proceed if all the text fields have the data.
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)){

                    if(password.equals(confirmPassword)){
                        //Show user the progress for account creation
                        regProgress.setVisibility(View.VISIBLE);

                        // Creating a new user.
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    // We can write logic here to create a user name as well as profile pic. But if internet stops
                                    // after account creation, the account would still be created and no name would be there for user.
                                    //So app might crash. So, we gave this responsibility to SetupActivity class.
                                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();
                                }else{
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error: " + errorMessage, Toast.LENGTH_LONG);
                                }


                            }
                        });

                    }else{
                        Toast.makeText(RegisterActivity.this,"Both Password and Confirm Password Fields don't match", Toast.LENGTH_LONG);
                    }

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        //This means that the user is logged in and we dont want user to stay on this page.
        if(currentUser!=null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
