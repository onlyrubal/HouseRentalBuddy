package com.example.finalproject;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private Button accountSettingsBtn;
    private Button logoutBtn;
    private FirebaseAuth mAuth;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
         View view =  inflater.inflate(R.layout.fragment_account, container, false);

         accountSettingsBtn = (Button) view.findViewById(R.id.fragment_account_settings);
         logoutBtn = (Button) view.findViewById(R.id.fragment_logout_btn);
         mAuth = FirebaseAuth.getInstance();

         accountSettingsBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent settingsIntent = new Intent(getActivity(),SetupActivity.class);
                 startActivity(settingsIntent);
             }
         });

         logoutBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Removing current user session and then sending back user to the login page.
                 mAuth.signOut();
                 sendToLogin();
             }
         });

        return view;
    }

    private void sendToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        // With finish user wont be able to come back by pressing the back button
         getActivity().finish();
    }
}
