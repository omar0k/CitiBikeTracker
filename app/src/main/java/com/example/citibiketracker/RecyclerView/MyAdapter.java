package com.example.citibiketracker.RecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citibiketracker.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    Context context;
    List<Station> items;

    public MyAdapter(Context context, List<Station> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.stationId.setText(items.get(position).getID());
        holder.stationName.setText(items.get(position).getName());
        holder.ic_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Station currentStation = items.get(position);
                currentStation.setFavorite(!currentStation.isFavorite);
                ImageView icon = (ImageView) view;
                if (icon.getDrawable().getConstantState() == icon.getResources().getDrawable(android.R.drawable.btn_star_big_off).getConstantState()) {
                    icon.setImageResource(android.R.drawable.btn_star_big_on);
                } else {
                    icon.setImageResource(android.R.drawable.btn_star_big_off);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
