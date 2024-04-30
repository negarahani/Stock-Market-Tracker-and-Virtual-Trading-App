package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.FavoriteStock;

public class FavoriteStockViewHolder extends RecyclerView.ViewHolder{

    private TextView stockNameTextView;
    private TextView companyNameTextView;
    private TextView currentPriceTextView;
    private TextView changeTextView;
    private ImageButton favStockButton;
    private ImageView changeImageView;


    public FavoriteStockViewHolder(View itemView) {
        super(itemView);
        stockNameTextView = itemView.findViewById(R.id.stockTickerText_fav);
        companyNameTextView = itemView.findViewById(R.id.companyNameText_fav);
        currentPriceTextView = itemView.findViewById(R.id.currentPriceText_fav);
        changeTextView = itemView.findViewById(R.id.changeText_fav);

        favStockButton = itemView.findViewById(R.id.favButton);

        changeImageView = itemView.findViewById(R.id.favorites_change_image);


    }

    public void bind(FavoriteStock favoriteStock, Context context) {
        stockNameTextView.setText(favoriteStock.getTickerSymbol());
        companyNameTextView.setText(favoriteStock.getCompanyName());
        currentPriceTextView.setText("$" + String.format("%.2f",favoriteStock.getCurrentPrice()));
        changeTextView.setText("$" + String.format("%.2f",favoriteStock.getChange()) + "(" + String.format("%.2f",favoriteStock.getChangePercent()) + "%)");

        //set the color fo changeTextView and the corresponding images

        if (favoriteStock.getChange() > 0 ){
            changeTextView.setTextColor(ContextCompat.getColor(context, R.color.positive_color));
            changeImageView.setImageResource(R.drawable.trending_up);
            changeImageView.setVisibility(View.VISIBLE);
        } else if (favoriteStock.getChange() < 0) {
            changeTextView.setTextColor(ContextCompat.getColor(context, R.color.negative_color));
            changeImageView.setImageResource(R.drawable.trending_down);
            changeImageView.setVisibility(View.VISIBLE);
        }  else {
            //If change is zero, don't show any image
            changeImageView.setVisibility(View.GONE);
        }

        favStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("FavoriteStockViewHolder", "the clicked item is " + favoriteStock.getTickerSymbol());
                // Launch DetailsActivity with the corresponding stock ticker
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("inputValue", favoriteStock.getTickerSymbol());
                context.startActivity(intent);
            }
        });
    }
}
