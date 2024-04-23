package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;


public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.NewsViewHolder> {

    private final NewsRecyclerViewInterface newsRecyclerViewInterface;

    Context context;
    ArrayList<NewsItem> newsItems;
    public NewsRecyclerViewAdapter(Context context, ArrayList<NewsItem> newsItems, NewsRecyclerViewInterface newsRecyclerViewInterface){
        this.context = context;
        this.newsItems = newsItems;
        this.newsRecyclerViewInterface = newsRecyclerViewInterface;
    }
//    Source of the following method (making first item layout different): StackOverflow , Link: https://stackoverflow.com/questions/40240405/how-to-change-the-recyclerview-first-row-layout
    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }
    @NonNull
    @Override
    public NewsRecyclerViewAdapter.NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == 1) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.first_news_row, parent, false);
            return new NewsRecyclerViewAdapter.NewsViewHolder(view, newsRecyclerViewInterface);
        } else {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.news_row, parent, false);
            return new NewsRecyclerViewAdapter.NewsViewHolder(view, newsRecyclerViewInterface);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull NewsRecyclerViewAdapter.NewsViewHolder holder, int position) {
        int squareSize = 50;
        //using picasso to load and crop the image
        Picasso.get().load(newsItems.get(position).getImage())
                .fit()
                .centerCrop()
                .into(holder.newsImage);

        holder.newsSource.setText(newsItems.get(position).getSource());
        holder.newsDatetime.setText(newsItems.get(position).getHoursPassed());
        holder.newsTitle.setText(newsItems.get(position).getHeadline());
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder{

        ImageView newsImage;
        TextView newsSource;
        TextView newsDatetime;
        TextView newsTitle;

        public NewsViewHolder(@NonNull View itemView, NewsRecyclerViewInterface newsRecyclerViewInterface) {
            super(itemView);

            newsImage = itemView.findViewById(R.id.news_image);
            newsSource = itemView.findViewById(R.id.news_source);
            newsDatetime = itemView.findViewById(R.id.news_datetime);
            newsTitle = itemView.findViewById(R.id.news_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (newsRecyclerViewInterface != null){
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION){
                            newsRecyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }


}
