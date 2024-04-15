package com.example.myapplication;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

import java.util.Collections;
import java.util.List;

public class PortfolioStockSection extends Section {

    private List<PortfolioStock> portfolioStocks;
    private int sectionPosition;

    public PortfolioStockSection(List<PortfolioStock> portfolioStocks, int sectionPosition) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_portfolio_stock)
                .build());
        this.portfolioStocks = portfolioStocks;
        this.sectionPosition = sectionPosition;
    }

    @Override
    public int getContentItemsTotal() {
        return portfolioStocks.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new PortfolioStockViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        PortfolioStockViewHolder viewHolder = (PortfolioStockViewHolder) holder;
        PortfolioStock portfolioStock = portfolioStocks.get(position);

        // Binding portfolio stock data to the view holder
        viewHolder.bind(portfolioStock, holder.itemView.getContext());
    }

    public String getTickerAtPosition(int position) {
        if (position >= 0 && position < portfolioStocks.size()) {
            return portfolioStocks.get(position).getTickerSymbol();
        }
        return null;
    }

    public void removeFromSection(int position) {
        if (portfolioStocks != null && position < portfolioStocks.size()) {
            portfolioStocks.remove(position);
        }
    }


    public List<PortfolioStock> getData() {
        return portfolioStocks;
    }
}
