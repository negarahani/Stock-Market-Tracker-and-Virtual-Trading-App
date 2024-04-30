package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity{

//    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static final String BASE_URL = "https://mywebtech4-729326.lm.r.appspot.com/";
    AutoCompleteTextView autoCompleteTextView;

    //related to Portfolio
    private SectionedRecyclerViewAdapter sectionAdapter_portfolio;
    RecyclerView recyclerView_portfolio;
    List<PortfolioStock> portfolioStocks;

    //related to Favorites
    private SectionedRecyclerViewAdapter sectionAdapter;
    RecyclerView recyclerView;
    List<FavoriteStock> favoriteStocks;


    //related to shared preferences of Favorites
    private static final String PREFS_NAME = "MyFavoriteStocksPrefs";
    private static final String KEY_FAVORITE_STOCKS = "favoriteStocks";
    //related to shared preferences of Portfolio
    private static final String PREFS_NAME_PORTFOLIO = "MyPortfolioStocksPrefs";
    private static final String KEY_PORTFOLIO_STOCKS = "portfolioStocks";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView and SectionedRecyclerViewAdapter for Portfolio Section
        recyclerView_portfolio = findViewById(R.id.portfolioRecyclerView);
        sectionAdapter_portfolio = new SectionedRecyclerViewAdapter();
        recyclerView_portfolio.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_portfolio.setAdapter(sectionAdapter_portfolio);

        // Initialize RecyclerView and SectionedRecyclerViewAdapter for Favorites Section
        recyclerView = findViewById(R.id.favoritesRecyclerView);
        sectionAdapter = new SectionedRecyclerViewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionAdapter);

        //set the finhub footer
        TextView finhubFooterText = findViewById(R.id.finhubFooter);
        finhubFooterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "https://finnhub.io/";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                startActivity(intent);
            }
        });


    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        getFavoriteStocks();
        getAPIBalanceAndPortfolioStocks();
        getCurrentTime();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getCurrentTime(){

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedTime = now.format(formatter);

        TextView curDateText = findViewById(R.id.dateText);
        curDateText.setText(formattedTime);
    }
    private void getAPIBalanceAndPortfolioStocks() {
        getAPIBalance(new BalanceCallback() {
            @Override
            public void onBalanceReceived(double cashBalance) {
                getPortfolioStocks(new PortfolioStocksCallback() {
                    @Override
                    public void onPortfolioStocksReceived(List<PortfolioStock> portfolioStocks) {
                        updateNetWorth(cashBalance, portfolioStocks);
                    }
                });
            }
        });
    }

    private void updateNetWorth(double cashBalance, List<PortfolioStock> portfolioStocks) {

        double netWorth = 0;
        double sumOfAllMarketValues = 0;

        for(PortfolioStock portfolioItem : portfolioStocks){
            sumOfAllMarketValues += portfolioItem.getMarketValue();
        }
        netWorth = cashBalance + sumOfAllMarketValues;
        String netWorthFormatted = String.format("%.2f", netWorth);

        TextView netWorthText = findViewById(R.id.networthText);
        netWorthText.setText(String.format("$" + netWorthFormatted));

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
        Log.d("Details Activity", "new DetailsActivity launched!");
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

    private void getAPIBalance(BalanceCallback callback) {
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
                            String formattedBalance = "$" + String.format("%.2f", cashBalance);
                            balanceText.setText(formattedBalance);

                            callback.onBalanceReceived(cashBalance);


                        } catch (JSONException e) {
                            String errorMessage = "Error parsing JSON: " + e.getMessage();
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

    /********************* Getting Portfolio Data Section ****************************/
    private void getPortfolioStocks(PortfolioStocksCallback callback){
        String portfolioUrl = BASE_URL + "/api/portfolio/getPortfolio";
        RequestQueue queue = Volley.newRequestQueue(this);

        portfolioStocks = new ArrayList<>(); // Clear the previous results
        // Clear the adapter before fetching new data
        sectionAdapter_portfolio.removeAllSections();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, portfolioUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response){
                        Log.d("PortfolioStocks", "response is: " + response);

                        parsePortfolioData(response, new ParsePortfolioDataCallback() {
                            @Override
                            public void onParsePortfolioDataCompleted(List<PortfolioStock> portfolioStocks) {

                                for (PortfolioStock stock : portfolioStocks) {
                                    Log.d("PortfolioStocks", "Portfolio Stock: " + stock.toString());
                                }

                                callback.onPortfolioStocksReceived(portfolioStocks);

                                // //call the method to save the portfolio stocks array to shared preferences
                                savePortfolioStocks(portfolioStocks);

                                PortfolioStockSection portfolioStockSection = new PortfolioStockSection(portfolioStocks, sectionAdapter_portfolio.getSectionCount());
                                // Add the section to the SectionedRecyclerViewAdapter
                                sectionAdapter_portfolio.addSection(portfolioStockSection);
                                // Notify adapter about the data change
                                sectionAdapter_portfolio.notifyDataSetChanged();



                            }
                        });

                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error fetching portfolio stocks" + error.getMessage();
                        Log.d("PortfolioStocks", errorMessage);

                    }
                });

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

    private void parsePortfolioData(String response, ParsePortfolioDataCallback callback) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            portfolioStocks = new ArrayList<>();

            // Keep track of the number of responses received
            AtomicInteger responsesReceived = new AtomicInteger(0);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String tickerSymbol = jsonObject.getString("ticker");
                Integer quantity = jsonObject.getInt("quantity");
                Double totalCost = jsonObject.getDouble("total_cost");

                getQuoteData_Portfolio(tickerSymbol, new QuoteDataCallback_Portfolio() {
                    @Override
                    public void onQuoteDataCompleted_Portfolio(Map<String, Double> quoteData_Portfolio) {
                        if (quoteData_Portfolio != null) {
                            double currentPrice = quoteData_Portfolio.get("current_price");

                            //calculating other values and adding to portfolio
                            if (quantity != 0 && totalCost != 0) {
                                double marketValue = quantity * currentPrice;
                                double avgPriceOfStock = totalCost / quantity;
                                double changeInPriceFromTotalCost = (currentPrice - avgPriceOfStock) * quantity;
                                double changeInPriceFromTotalCostPercent = (changeInPriceFromTotalCost / totalCost) * 100;


                                PortfolioStock portfolioStock = new PortfolioStock(tickerSymbol, quantity, totalCost, marketValue, changeInPriceFromTotalCost, changeInPriceFromTotalCostPercent);
                                portfolioStocks.add(portfolioStock);
                            }

                            // Check if all responses have been received
                            if (responsesReceived.incrementAndGet() == jsonArray.length()) {
                                // All portfolio stocks have been processed
                                callback.onParsePortfolioDataCompleted(portfolioStocks);
                            }
                        } else {
                            Log.e("PortfolioStocks", "Quote data is null");

                            // Still increment the counter even if there's an error to ensure completion
                            if (responsesReceived.incrementAndGet() == jsonArray.length()) {
                                // All portfolio stocks have been processed
                                callback.onParsePortfolioDataCompleted(portfolioStocks);
                            }
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getQuoteData_Portfolio(String ticker, QuoteDataCallback_Portfolio callback) {
        Log.d("PortfolioStocks", "getQuoteData_Portfolio executed");
        Log.d("PortfolioStocks", "current ticker is " + ticker);

        String quoteUrl = BASE_URL + "/search-quote/" + ticker;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, quoteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            double curPrice = jsonObject.getDouble("c");

                            Map<String, Double> quoteData_Portfolio = new HashMap<>();
                            quoteData_Portfolio.put("current_price", curPrice);

                            callback.onQuoteDataCompleted_Portfolio(quoteData_Portfolio);

                        } catch (JSONException e) {
                            String errorMessage = "Error parsing quote data: " + e.getMessage();
                            Log.e("PortfolioStocks", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching quoteData: " + error.getMessage();
                Log.d("PortfolioStocks", errorMessage);
            }
        });
        queue.add(stringRequest);
    }



    public interface QuoteDataCallback_Portfolio {
        void onQuoteDataCompleted_Portfolio(Map<String, Double> quoteData_Portfolio);
    }

    public interface ParsePortfolioDataCallback {
        void onParsePortfolioDataCompleted(List<PortfolioStock> portfolioStocks);
    }

    // Method to save portfolio stocks to SharedPreferences
    private void savePortfolioStocks(List<PortfolioStock> portfolioStocks) {
        //Getting SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME_PORTFOLIO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Converting portfolioStocks list to JSON String
        Gson gson = new Gson();
        String json = gson.toJson(portfolioStocks);

        //Saving JSON String to SharedPreferences
        editor.putString(KEY_PORTFOLIO_STOCKS, json);
        editor.apply();
    }

    /********************* Getting Favorites Data Section ****************************/
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

                                for (FavoriteStock stock : favoriteStocks) {
                                    Log.d("FavoriteStocks", "Favorite Stock: " + stock.toString());
                                }

                                //call the method to save the favorite stocks array to shared preferences
                                saveFavoriteStocks(favoriteStocks);

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
                            Log.e("FavoriteStocks", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching quoteData: " + error.getMessage();
                Log.d("FavoriteStocks", errorMessage);
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

    public interface BalanceCallback{
        void onBalanceReceived(double balance);
    }

    private interface PortfolioStocksCallback {
        void onPortfolioStocksReceived(List<PortfolioStock> portfolioStocks);
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


    // Method to save favorite stocks to SharedPreferences
    private void saveFavoriteStocks(List<FavoriteStock> favoriteStocks) {
        //Getting SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Converting favoriteStocks list to JSON String
        Gson gson = new Gson();
        String json = gson.toJson(favoriteStocks);

        //Saving JSON String to SharedPreferences
        editor.putString(KEY_FAVORITE_STOCKS, json);
        editor.apply();
    }
    /********************* End of Getting Favorites Data Section ****************************/
}
