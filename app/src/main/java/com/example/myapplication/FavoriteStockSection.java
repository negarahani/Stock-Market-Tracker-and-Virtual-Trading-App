package com.example.myapplication;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

import java.util.Collections;
import java.util.List;

public class FavoriteStockSection extends Section {

    private List<FavoriteStock> favoriteStocks;
    private int sectionPosition;

    public FavoriteStockSection(List<FavoriteStock> favoriteStocks, int sectionPosition) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_favorite_stock)
                .build());
        this.favoriteStocks = favoriteStocks;
        this.sectionPosition = sectionPosition;
    }

    @Override
    public int getContentItemsTotal() {
        return favoriteStocks.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new FavoriteStockViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        FavoriteStockViewHolder viewHolder = (FavoriteStockViewHolder) holder;
        FavoriteStock favoriteStock = favoriteStocks.get(position);

        // Binding favorite stock data to the view holder
        viewHolder.bind(favoriteStock, holder.itemView.getContext());
    }

    public String getTickerAtPosition(int position) {
        if (position >= 0 && position < favoriteStocks.size()) {
            return favoriteStocks.get(position).getTickerSymbol();
        }
        return null;
    }

    public void removeFromSection(int position) {
        if (favoriteStocks != null && position < favoriteStocks.size()) {
            favoriteStocks.remove(position);
        }
    }


    public List<FavoriteStock> getData() {
        return favoriteStocks;
    }
}
