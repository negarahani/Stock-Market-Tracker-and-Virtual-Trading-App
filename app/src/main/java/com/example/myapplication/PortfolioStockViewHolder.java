package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;


public class PortfolioStockViewHolder extends RecyclerView.ViewHolder{

    private TextView stockNameTextView;
    private TextView quantityTextView;
    private TextView marketValueTextView;
    private TextView changeTextView;

    private ImageButton portfolioStockButton;

    public PortfolioStockViewHolder(View itemView) {
        super(itemView);
        stockNameTextView = itemView.findViewById(R.id.stockTickerText_portfolio);
        quantityTextView = itemView.findViewById(R.id.quantityText_portfolio);
        marketValueTextView = itemView.findViewById(R.id.marketValueText_portfolio);
        changeTextView = itemView.findViewById(R.id.changeText_portfolio);

        portfolioStockButton = itemView.findViewById(R.id.portfolioButton);

    }

    public void bind(PortfolioStock portfolioStock, Context context) {
        stockNameTextView.setText(portfolioStock.getTickerSymbol());
        quantityTextView.setText(String.valueOf(portfolioStock.getQuantity()) + " Shares");

        double marketValue = portfolioStock.getMarketValue();
        String marketValueFormatted = String.format("%.2f", marketValue);

        double change = portfolioStock.getChange();
        String changeFormatted = String.format("%.2f", change);

        double changePercent = portfolioStock.getChangePercent();
        String changePercentFormatted = String.format("%.2f", changePercent);

        marketValueTextView.setText("$" + marketValueFormatted);
        changeTextView.setText("$" + changeFormatted + "( " + changePercentFormatted + "% )");

        portfolioStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("PortfolioStockViewHolder", "the clicked item is " + portfolioStock.getTickerSymbol());
                // Launch DetailsActivity with the corresponding stock ticker
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("inputValue", portfolioStock.getTickerSymbol());
                context.startActivity(intent);
            }
        });
    }
}
