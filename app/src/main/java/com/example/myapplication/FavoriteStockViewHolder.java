package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.FavoriteStock;

public class FavoriteStockViewHolder extends RecyclerView.ViewHolder{

    private TextView stockNameTextView;
    private TextView companyNameTextView;
    private TextView currentPriceTextView;
    private TextView changeTextView;
    private Button favStockButton;


    public FavoriteStockViewHolder(View itemView) {
        super(itemView);
        stockNameTextView = itemView.findViewById(R.id.stockTickerText_fav);
        companyNameTextView = itemView.findViewById(R.id.companyNameText_fav);
        currentPriceTextView = itemView.findViewById(R.id.currentPriceText_fav);
        changeTextView = itemView.findViewById(R.id.changeText_fav);

        favStockButton = itemView.findViewById(R.id.favButton);

    }

    public void bind(FavoriteStock favoriteStock, Context context) {
        stockNameTextView.setText(favoriteStock.getTickerSymbol());
        companyNameTextView.setText(favoriteStock.getCompanyName());
        currentPriceTextView.setText(String.valueOf(favoriteStock.getCurrentPrice()));
        changeTextView.setText(String.valueOf(favoriteStock.getChange()) + "(" + String.valueOf(favoriteStock.getChangePercent()) + "%)");

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
