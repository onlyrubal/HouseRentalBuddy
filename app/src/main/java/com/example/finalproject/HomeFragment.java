package com.example.finalproject;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView rentalListView;
    private List<RentalPost> rentalPostList;

    private FirebaseFirestore firebaseFirestore;
    private RentalRecyclerAdapter rentalRecyclerAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        rentalPostList = new ArrayList<>();
        rentalListView = view.findViewById(R.id.rental_list_view);

        rentalListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rentalRecyclerAdapter = new RentalRecyclerAdapter(rentalPostList);
        rentalListView.setAdapter(rentalRecyclerAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Rentals").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                    if(documentChange.getType() == DocumentChange.Type.ADDED) {
                        RentalPost rentalPost = documentChange.getDocument().toObject(RentalPost.class);
                        rentalPostList.add(rentalPost);
                        rentalRecyclerAdapter.notifyDataSetChanged();
                    }
                }

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}
