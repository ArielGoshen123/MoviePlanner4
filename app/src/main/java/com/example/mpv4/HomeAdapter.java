package com.example.mpv4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private List<Movie> movies;

    public HomeAdapter(List<Movie> movies) {
        this.movies = movies;
    }

    public void updateList(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_in_home, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie m = movies.get(position);
        holder.tvTitle.setText(m.getTitle());

        String status = (m.getStatus() != null) ? m.getStatus() : "Watchlist";
        String genres = (m.getGenres() != null && !m.getGenres().isEmpty())
                ? String.join(", ", m.getGenres())
                : "General";

        holder.tvInfo.setText((m.getDurationInSeconds() / 60) + " min • " + genres + " (" + status + ")");
        holder.ivPoster.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvInfo;

        ViewHolder(View v) {
            super(v);
            ivPoster = v.findViewById(R.id.ivMoviePoster);
            tvTitle = v.findViewById(R.id.tvMovieTitle);
            tvInfo = v.findViewById(R.id.tvWatchDate);
        }
    }
}