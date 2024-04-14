package com.example.myapplication;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080";
    private List<FavoriteStock> myFavoriteStocks = new ArrayList<>(); //you may change this later!
    private String curTicker;
    private String curCompanyName;
    private boolean isInFavorites;

    private static final String PREFS_NAME = "MyFavoriteStocksPrefs";
    private static final String KEY_FAVORITE_STOCKS = "favoriteStocks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        //accessing the data passed from MainActivity
        Intent i = getIntent();
        String ticker = i.getStringExtra("inputValue");
        curTicker = ticker.toUpperCase();


        Toolbar toolbar = findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);

        //changing the title in this activity
        getSupportActionBar().setTitle(curTicker);
        //to see the up (back) button in action bar
        //we also set the parent activity in the manifest file for the button to work
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        /************** other methods to get data from backend *************/
        initStarButton(ticker);
        getCompanyData(ticker);
        getQuoteData(ticker);

        /******* set up the star button onClick listener *************/
        ImageButton starButton = findViewById(R.id.star_icon);

        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInFavorites) {
                    starButton.setImageResource(R.drawable.star_border);
                    Log.d("DetailsActivity", "Star button emptied");
                    deleteFavoriteStock(curTicker);
                } else {
                    starButton.setImageResource(R.drawable.full_star);
                    Log.d("DetailsActivity", "Star button filled");
                    addFavoriteStock(curTicker);
                }
                isInFavorites = !isInFavorites;
            }
        });
    }

    private void initStarButton(String tickerSymbol){
        myFavoriteStocks = loadFavoriteStocks();
        Log.d("DetailsActivity", "FavoriteStocks list retreived from shared preference is: " + myFavoriteStocks);

        isInFavorites = false;
        for (FavoriteStock stock : myFavoriteStocks) {
            if (stock.getTickerSymbol().equals(curTicker)) {
                isInFavorites = true;
                break;
            }
        }
        if (isInFavorites) {
            ImageButton starButton = findViewById(R.id.star_icon);
            starButton.setImageResource(R.drawable.full_star);
        } else {
            ImageButton starButton = findViewById(R.id.star_icon);
            starButton.setImageResource(R.drawable.star_border);
        }
    }

    //Method to get favorite stocks from SharedPreferences
    private List<FavoriteStock> loadFavoriteStocks() {
        //Getting SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String json = sharedPreferences.getString(KEY_FAVORITE_STOCKS, null);

        // converting JSON to list of favorite stocks
        Gson gson = new Gson();
        Type type = new TypeToken<List<FavoriteStock>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void getCompanyData(String tickerSymbol) {
        String companyUrl = BASE_URL + "/search-company/" + tickerSymbol;

        TextView tickerText = findViewById(R.id.company_ticker);
        TextView companyText = findViewById(R.id.company_name);
        TextView currentPrice = findViewById(R.id.current_price);
        TextView change = findViewById(R.id.change);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, companyUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parsing the JSON response
                            JSONObject jsonObject = new JSONObject(response);
                            String ticker = jsonObject.getString("ticker");
                            String name = jsonObject.getString("name");
                            tickerText.setText(ticker);
                            companyText.setText(name);

                            curCompanyName = name; //get company name for watchlist

                        } catch (JSONException e) {
                            // JSON parsing error
                            String errorMessage = "Error parsing JSON: " + e.getMessage();
                            Log.e("DetailsActivity", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handling errors
                String errorMessage = "Error fetching companyData: " + error.getMessage();
                Log.d("DetailsActivity", errorMessage);
            }
        });
        queue.add(stringRequest);
    }

    private void getQuoteData(String tickerSymbol) {
        String quoteUrl = BASE_URL + "/search-quote/" + tickerSymbol;


        TextView currentPrice = findViewById(R.id.current_price);
        TextView change = findViewById(R.id.change);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, quoteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parsing the JSON response
                            JSONObject jsonObject = new JSONObject(response);
                            double curPrice = jsonObject.getDouble("c");
                            double changeVal = jsonObject.getDouble("d");
                            double changePercent = jsonObject.getDouble("dp");

                            // Convert double values to strings
                            String curPriceStr = String.valueOf(curPrice);
                            String changeValStr = String.valueOf(changeVal);
                            String changePercentStr = String.valueOf(changePercent);

                            currentPrice.setText("$" + curPriceStr);
                            change.setText("$" + changeValStr + " (" + changePercentStr + "%)");

                        } catch (JSONException e) {
                            // JSON parsing error
                            String errorMessage = "Error parsing JSON: " + e.getMessage();
                            Log.e("DetailsActivity", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handling errors
                String errorMessage = "Error fetching companyData: " + error.getMessage();
                Log.d("DetailsActivity", errorMessage);
            }
        });
        queue.add(stringRequest);
    }

    private void addFavoriteStock(String ticker) {
        String addFavoritesUrl = BASE_URL + "/api/favorites/addFavorites/";
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("tickerSymbol", ticker);
            requestBody.put("companyName", curCompanyName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, addFavoritesUrl, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("DetailsActivity", "Favorite stock added successfully: " + ticker);

                        // Updateing the local list
                        myFavoriteStocks.add(new FavoriteStock(ticker, curCompanyName));

                        Log.d("DetailsActivity", "Favorite Stocks array after add:" + myFavoriteStocks);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                Log.e("DetailsActivity", "Error adding favorite stock: " + error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }



    private void deleteFavoriteStock(String ticker) {
        String deleteFavoritesUrl = BASE_URL + "/api/favorites/deleteFavorites/" + ticker;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, deleteFavoritesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle successful response
                        Log.d("DetailsActivity", "Favorite stock removed successfully: " + ticker);
                        // Remove the favorite stock from the local list

                        if (myFavoriteStocks != null) {
                            for (FavoriteStock stock : myFavoriteStocks) {
                                if (stock.getTickerSymbol().equals(ticker)) {
                                    myFavoriteStocks.remove(stock);
                                    break;
                                }
                            }

                        }
                        Log.d("DetailsActivity", "Favorite Stocks array after delete:" + myFavoriteStocks);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                Log.e("DetailsActivity", "Error removing favorite stock: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }
}