package com.example.finalproject;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton addPostBtn;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private BottomNavigationView mainBottomNav;

    private HomeFragment homeFragment;
    private AccountFragment accountFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        if(mAuth.getCurrentUser() !=null) {
            mainBottomNav = (BottomNavigationView) findViewById(R.id.mainBottomNav);
            //Initializing fragments

            homeFragment = new HomeFragment();
            accountFragment = new AccountFragment();

            replaceFragment(homeFragment);
            addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newPostIntent = new Intent(MainActivity.this, NewRentalActivity.class);
                    startActivity(newPostIntent);
                }
            });

            //Setting up the onClick listener for bottom navigation bar. This listener is different than normal button listener
            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.bottom_action_home:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.bottom_action_account:
                            replaceFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //If the user is not logged in, then it is going directly into the login page
        if(currentUser==null) {
            sendToLogin();
        }
        //Means if the user logged in then only we can apply the check
        else{
            currentUserId = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){
                        // If the user doesnot have the name or profile picture, then it goes directly to the account settings page.
                        if(!task.getResult().exists()) {
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    }
                    else{
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error: " + errorMessage, Toast.LENGTH_LONG);
                    }

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    //Here we are going to write the logic for signout the current logged in user from main activity.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case (R.id.action_logout_btn):
                logOut();
                return true;
            case (R.id.action_settings_btn):
                Intent settingsIntent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return false;
        }

    }

    private void logOut() {
        // Removing current user session and then sending back user to the login page.
        mAuth.signOut();
        sendToLogin();

    }

    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        // With finish user wont be able to come back by pressing the back button
        finish();
    }

    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //With with thing we want to replace our fragment. In our case main_container(FrameLayout)
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }
}
