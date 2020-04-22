package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RentalRecyclerAdapter extends RecyclerView.Adapter<RentalRecyclerAdapter.ViewHolder> {

    public List<RentalPost> rentalPostList;
    public RentalRecyclerAdapter(List<RentalPost> rentalPostList){
        this.rentalPostList = rentalPostList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rental_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String desc_data = rentalPostList.get(position).getDesc();
        holder.setDescText(desc_data);
    }

    @Override
    public int getItemCount() {
        // Here we have to mention the number of post items that we want
        return rentalPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescText(String descText){
            descView = mView.findViewById(R.id.rental_desc);
            descView.setText(descText);

        }
    }
}
