package com.example.citibiketracker.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citibiketracker.R;

public class MyViewHolder extends RecyclerView.ViewHolder {
    TextView stationName,stationId;
    ImageView ic_favorite;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        stationName=itemView.findViewById(R.id.station_name);
        stationId=itemView.findViewById(R.id.station_id);
        ic_favorite=itemView.findViewById(R.id.ic_favorite);
    }
}
