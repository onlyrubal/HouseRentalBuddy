package com.example.finalproject;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
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
    private FirebaseAuth firebaseAuth;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        rentalPostList = new ArrayList<>();
        rentalListView = view.findViewById(R.id.rental_list_view);

        rentalListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rentalRecyclerAdapter = new RentalRecyclerAdapter(rentalPostList);
        rentalListView.setAdapter(rentalRecyclerAdapter);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() !=null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            rentalListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    // To check if the rental posts have reached the bottom of the recycler view
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){
                        Toast.makeText(container.getContext(), "Loading more posts", Toast.LENGTH_LONG ).show();
                        loadMorePost();
                    }
                }
            });

            //This will help to retrieve the rental posts in the descending order of the time at which rental post was posted.
            Query firstQuery = firebaseFirestore.collection("Rentals").orderBy("timestamp", Query.Direction.DESCENDING);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if(isFirstPageFirstLoad) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    }
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            RentalPost rentalPost = documentChange.getDocument().toObject(RentalPost.class);

                            if(isFirstPageFirstLoad) {
                                rentalPostList.add(rentalPost);
                            }else{
                                rentalPostList.add(0,rentalPost);
                            }
                            rentalRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                    isFirstPageFirstLoad = false;

                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }


    public void loadMorePost(){

        Query nextQuery = firebaseFirestore.collection("Rentals")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()) {
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            RentalPost rentalPost = documentChange.getDocument().toObject(RentalPost.class);
                            rentalPostList.add(rentalPost);
                            rentalRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}
