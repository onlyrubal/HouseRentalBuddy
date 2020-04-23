package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RentalRecyclerAdapter extends RecyclerView.Adapter<RentalRecyclerAdapter.ViewHolder> {

    public List<RentalPost> rentalPostList;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    public RentalRecyclerAdapter(List<RentalPost> rentalPostList){
        this.rentalPostList = rentalPostList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rental_list_item, parent, false);
        context = parent.getContext();

        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        // Getting the rental post description
        String desc_data = rentalPostList.get(position).getDesc();
        holder.setDescText(desc_data);

        //Getting the rental post image.
        String image_url = rentalPostList.get(position).getImage_url();
        holder.setRentalImage(image_url);

        //Getting the user related data like name and image of the user who posted the actual rental post.

        String user_id = rentalPostList.get(position).getUser_id();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    holder.setUserData(userName,userImage);
                }else{
                    //Firebase Exception
                }
            }
        });

        //Getting the rental post date on which it was posted.
        long milliseconds = rentalPostList.get(position).getTimestamp().getTime();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String dateString = dateFormat.format(milliseconds);
        holder.setDate(dateString);

    }

    @Override
    public int getItemCount() {
        // Here we have to mention the number of post items that we want
        return rentalPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView descView;
        private ImageView rentalImageView;
        private TextView rentalPostDate;
        private TextView userName;
        private CircleImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        // Loading the description of the rental post.
        public void setDescText(String descText){
            descView = mView.findViewById(R.id.rental_desc);
            descView.setText(descText);

        }

        //Loading the image of the rental post.

        public void setRentalImage(String downloadUri){
            rentalImageView = mView.findViewById(R.id.rental_image);
            RequestOptions placeholderoption = new RequestOptions();
            placeholderoption.placeholder(R.drawable.random_image_1);
            Glide.with(context).applyDefaultRequestOptions(placeholderoption).load(downloadUri).into(rentalImageView);
        }

        public void setDate(String date){

            rentalPostDate = mView.findViewById(R.id.rental_date);
            rentalPostDate.setText(date);

        }

        public void setUserData(String name, String image){
            userImage = mView.findViewById(R.id.user_image);
            userName = mView.findViewById(R.id.rental_user_name);

            RequestOptions placeholderoption  = new RequestOptions();
            placeholderoption.placeholder(R.drawable.default_profile_image);
            userName.setText(name);
            Glide.with(context).applyDefaultRequestOptions(placeholderoption).load(image).into(userImage);
        }
    }
}
