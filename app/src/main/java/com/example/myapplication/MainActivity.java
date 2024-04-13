package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity{

    private static final String BASE_URL = "http://10.0.2.2:8080";
    AutoCompleteTextView autoCompleteTextView;
    private SectionedRecyclerViewAdapter sectionAdapter;

    RecyclerView recyclerView;
    List<FavoriteStock> favoriteStocks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView and SectionedRecyclerViewAdapter
        recyclerView = findViewById(R.id.favoritesRecyclerView);
        sectionAdapter = new SectionedRecyclerViewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionAdapter);

    }


    @Override
    protected void onResume() {
        super.onResume();
        getAPIBalance();
        getFavoriteStocks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search...");

        autoCompleteTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setAdapter(adapter);

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        String query = newText.trim();
                        if (!query.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line);
                            autoCompleteTextView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            getAutocompleteData(query);
                        } else {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        launchDetailsActivity(query);
                        return false;
                    }
                });

        return true;
    }

    public void launchDetailsActivity(String queryTicker) {
        Intent i = new Intent(this, DetailsActivity.class);
        i.putExtra("inputValue", queryTicker);
        startActivity(i);
    }

    private void getAutocompleteData(String tickerSymbol) {
        String autoCompleteUrl = BASE_URL + "/search-autocomplete/" + tickerSymbol;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, autoCompleteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray resultArray = jsonObject.getJSONArray("result");
                            List<String> filteredResults = new ArrayList<>();
                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject item = resultArray.getJSONObject(i);
                                String type = item.getString("type");
                                String symbol = item.getString("symbol");
                                if ("Common Stock".equals(type) && !symbol.contains(".")) {
                                    filteredResults.add(symbol + " | " + item.getString("description"));
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line);
                            adapter.addAll(filteredResults);
                            autoCompleteTextView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                                    String selectedItem = (String) adapterView.getItemAtPosition(itemIndex);
                                    String[] parts = selectedItem.split(" \\| ");
                                    if (parts.length >= 2) {
                                        String symbol = parts[0];
                                        launchDetailsActivity(symbol);
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            String errorMessage = "Error parsing JSON in AC: " + e.getMessage();
                            Log.e("MainActivity", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching autocomplete data: " + error.getMessage();
                Log.d("MainActivity", errorMessage);
            }
        });
        queue.add(stringRequest);
    }

    private void getAPIBalance() {
        String balanceUrl = BASE_URL + "/api/wallet/getBalance";
        final TextView balanceText = findViewById(R.id.balanceText);
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, balanceUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            double cashBalance = jsonObject.getDouble("cash_balance");
                            balanceText.setText("Balance: " + cashBalance);
                        } catch (JSONException e) {
                            String errorMessage = "Error parsing JSON: " + e.getMessage();
                            balanceText.setText(errorMessage);
                            Log.e("MainActivity", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching balance: " + error.getMessage();
                balanceText.setText(errorMessage);
                Log.d("MainActivity", errorMessage);
            }
        });
        queue.add(stringRequest);
    }

    private void getFavoriteStocks() {
        String favoritesUrl = BASE_URL + "/api/favorites/getFavorites";
        RequestQueue queue = Volley.newRequestQueue(this);

        favoriteStocks = new ArrayList<>(); // Clear the previous results
        // Clear the adapter before fetching new data
        sectionAdapter.removeAllSections();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, favoritesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("FavoriteStocks", "response is:" + response);
                        parseFavoritesData(response, new ParseFavoritesDataCallback() {
                            @Override
                            public void onParseFavoritesDataCompleted(List<FavoriteStock> favoriteStocks) {
                                // Handle the completion of parsing favorite stocks here
                                for (FavoriteStock stock : favoriteStocks) {
                                    Log.d("FavoriteStocks", "Favorite Stock: " + stock.toString());
                                }

                                FavoriteStockSection favoriteStockSection = new FavoriteStockSection(favoriteStocks, sectionAdapter.getSectionCount());
                                // Add the section to the SectionedRecyclerViewAdapter
                                sectionAdapter.addSection(favoriteStockSection);
                                // Notify adapter about the data change
                                sectionAdapter.notifyDataSetChanged();


                                //swipe to delete functionality
                                ItemTouchHelper.Callback callback = new FavoritesSwipeToDeleteCallback(MainActivity.this, sectionAdapter, favoriteStocks);
                                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);

                                //drag and reorder functionality
                                ItemTouchHelper.Callback callback_2 = new FavoritesItemTouchHelper(MainActivity.this, sectionAdapter);
                                ItemTouchHelper itemTouchHelper_2 = new ItemTouchHelper(callback_2);
                                itemTouchHelper_2.attachToRecyclerView(recyclerView);

                            }
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching favorite stocks" + error.getMessage();
                Log.d("FavoriteStocks", errorMessage);
            }
        });

        queue.add(stringRequest);
    }

    public void onItemMoved(int fromPosition, int toPosition) {
        Log.d("FavoriteStocks", "onItemMoved triggered");

        // Get the favorite stock list from the adapter
        FavoriteStockSection section = (FavoriteStockSection) sectionAdapter.getSection(0);
        List<FavoriteStock> favoriteStocks = section.getData();

        Log.d("FavoriteStocks", "Before swap: " + favoriteStocks);
        // Swap the positions of the items in the list
        Collections.swap(favoriteStocks, fromPosition, toPosition);
        Log.d("FavoriteStocks", "After swap: " + favoriteStocks);

        // Notify the adapter about the data change
        sectionAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    private void parseFavoritesData(String response, ParseFavoritesDataCallback callback) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            favoriteStocks = new ArrayList<>();

            // Keep track of the number of responses received
            AtomicInteger responsesReceived = new AtomicInteger(0);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String tickerSymbol = jsonObject.getString("tickerSymbol");
                String companyName = jsonObject.getString("companyName");

                getQuoteData(tickerSymbol, new QuoteDataCallback() {
                    @Override
                    public void onQuoteDataCompleted(Map<String, Double> quoteData) {
                        if (quoteData != null) {
                            double currentPrice = quoteData.get("current_price");
                            double change = quoteData.get("change");
                            double changePercent = quoteData.get("change_percent");

                            FavoriteStock favoriteStock = new FavoriteStock(tickerSymbol, companyName, currentPrice, change, changePercent);
                            favoriteStocks.add(favoriteStock);

                            // Check if all responses have been received
                            if (responsesReceived.incrementAndGet() == jsonArray.length()) {
                                // All favorite stocks have been processed
                                callback.onParseFavoritesDataCompleted(favoriteStocks);
                            }
                        } else {
                            Log.e("FavoriteStocks", "Quote data is null");

                            // Still increment the counter even if there's an error to ensure completion
                            if (responsesReceived.incrementAndGet() == jsonArray.length()) {
                                // All favorite stocks have been processed
                                callback.onParseFavoritesDataCompleted(favoriteStocks);
                            }
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getQuoteData(String ticker, QuoteDataCallback callback) {
        Log.d("FavoriteStocks", "getQuoteData executed");
        Log.d("FavoriteStocks", "current ticker is " + ticker);

        String quoteUrl = BASE_URL + "/search-quote/" + ticker;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, quoteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            double curPrice = jsonObject.getDouble("c");
                            double changeVal = jsonObject.getDouble("d");
                            double changePercent = jsonObject.getDouble("dp");

                            Map<String, Double> quoteData = new HashMap<>();
                            quoteData.put("current_price", curPrice);
                            quoteData.put("change", changeVal);
                            quoteData.put("change_percent", changePercent);

                            callback.onQuoteDataCompleted(quoteData);

                        } catch (JSONException e) {
                            String errorMessage = "Error parsing quote data: " + e.getMessage();
                            Log.e("DetailsActivity", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching companyData: " + error.getMessage();
                Log.d("DetailsActivity", errorMessage);
            }
        });
        queue.add(stringRequest);
    }

    public interface QuoteDataCallback {
        void onQuoteDataCompleted(Map<String, Double> quoteData);
    }

    public interface ParseFavoritesDataCallback {
        void onParseFavoritesDataCompleted(List<FavoriteStock> favoriteStocks);
    }



}
