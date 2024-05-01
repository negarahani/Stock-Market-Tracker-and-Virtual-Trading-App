package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.FavoriteStockSection;
import com.google.gson.Gson;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class FavoritesSwipeToDeleteCallback extends ItemTouchHelper.Callback {

    private final SectionedRecyclerViewAdapter mAdapter;
    private final Context mContext;
    private final List<FavoriteStock> mFavoriteStocks; // Reference to the ArrayList
    private static final String BASE_URL = "https://mywebtech4-729326.lm.r.appspot.com/";

    private Drawable deleteDrawable;
    private final int intrinsicWidth;
    private final int intrinsicHeight;

    private static final String PREFS_NAME = "MyFavoriteStocksPrefs";
    private static final String KEY_FAVORITE_STOCKS = "favoriteStocks";

    public FavoritesSwipeToDeleteCallback(Context context, SectionedRecyclerViewAdapter adapter, List<FavoriteStock> favoriteStocks) {
        mAdapter = adapter;
        mContext = context;
        mFavoriteStocks = favoriteStocks;
        deleteDrawable = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        intrinsicHeight = deleteDrawable.getIntrinsicHeight();
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Set movement flags to handle swipe only to left
        int swipeFlags = ItemTouchHelper.START;
        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // Not used
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Log.d("FavoriteStocks", "onSwiped: position swiped is:" + position );
        //Log.d("FavoriteStocks", "adapter length:" + mAdapter.getSectionCount()); always giving 1 ??

        String tickerSymbol = ((FavoriteStockSection) mAdapter.getSection(0)).getTickerAtPosition(position);
        Log.d("FavoriteStocks", "tickerSymbol:" + tickerSymbol);


        // Update section data --> added this line because the item would stay in the UI despite being deleted from the database
        //(the bug happened when I returned to the screen from outside
        ((FavoriteStockSection) mAdapter.getSection(0)).removeFromSection(position);


        // Perform swipe-to-delete action
        mAdapter.notifyItemRemoved(position);

        //call function to delete the favorite stock item
        deleteFavoriteStock(tickerSymbol);
    }

    private void deleteFavoriteStock(String ticker) {
        String deleteFavoritesUrl = BASE_URL + "/api/favorites/deleteFavorites/" + ticker;
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, deleteFavoritesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle successful response
                        Log.d("FavoriteStock", "Favorite stock removed successfully: " + ticker);
                        //no need for updating mFavorites because mFavoriteStocks is a reference to an array that is already updated in MainActivity!??
                        Log.d("FavoriteStock", "Favorite Stocks array after delete:" + mFavoriteStocks);

                        updateSharedPreferences();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                Log.e("MainActivity", "Error removing favorite stock: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue
        queue.add(stringRequest);

    }



    private void updateSharedPreferences() {

        Log.d("FavoriteStocks", "mFavoriteStocks inside sharedPreferences is: ");
        for (FavoriteStock item: mFavoriteStocks){
            Log.d("FavoriteStocks","FavoriteStockItem: " +  item.toString());
        }

        Gson gson = new Gson();
        String json = gson.toJson(mFavoriteStocks);

        //Getting SharedPreferences instance
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Saving the JSON string to shared preferences
        editor.putString(KEY_FAVORITE_STOCKS, json);
        editor.apply();
        Log.d("FavoriteStock", "SharedPreferences got updated");
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();
        boolean isCanceled = dX == 0 && !isCurrentlyActive;

        // Draw red background if swiped
        if (!isCanceled) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
        }

        // Draw delete icon
        int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
        int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
        int deleteIconRight = itemView.getRight() - deleteIconMargin;
        int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int deleteIconBottom = deleteIconTop + intrinsicHeight;

        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteDrawable.draw(c);
    }
}