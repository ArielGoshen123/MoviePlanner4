package com.example.mpv4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.ViewHolder> {

    public interface OnMovieInteractionListener {
        void onMovieShortClick(DiscoverItem item);
        void onMovieLongClick(DiscoverItem item);
    }

    private final List<DiscoverItem> items;
    private final OnMovieInteractionListener listener;

    public DiscoverAdapter(List<DiscoverItem> items, OnMovieInteractionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<DiscoverItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_in_discover, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiscoverItem item = items.get(position);
        holder.tvMovieTitle.setText(item.getName());

        String formattedRating = String.format(Locale.US, "%.1f/10", item.getRating());
        holder.tvMovieYear.setText(item.getYear() + " • " + formattedRating);

        if (item.getPosterUrl() != null && !item.getPosterUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getPosterUrl())
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.ivMoviePoster);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieShortClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onMovieLongClick(item);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMoviePoster;
        TextView tvMovieTitle;
        TextView tvMovieYear;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoviePoster = itemView.findViewById(R.id.ivMoviePoster);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvMovieYear = itemView.findViewById(R.id.tvMovieYear);
        }
    }
}