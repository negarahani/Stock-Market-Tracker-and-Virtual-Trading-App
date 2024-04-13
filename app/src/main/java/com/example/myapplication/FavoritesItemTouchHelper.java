package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class FavoritesItemTouchHelper extends ItemTouchHelper.Callback {

    private final MainActivity activity;
    private final SectionedRecyclerViewAdapter adapter;

    public FavoritesItemTouchHelper(MainActivity activity, SectionedRecyclerViewAdapter adapter) {
        this.activity = activity;
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.DOWN | ItemTouchHelper.UP;
        final int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder fromViewHolder, @NonNull RecyclerView.ViewHolder toViewHolder) {

        int fromPosition = fromViewHolder.getAdapterPosition();
        int toPosition = toViewHolder.getAdapterPosition();
        //get the range of portfolio : use getBindingAdapterPosition
        activity.onItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // not needed
    }
}
