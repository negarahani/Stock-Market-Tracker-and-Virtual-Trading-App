package com.example.myapplication;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.util.DisplayMetrics;

public class DetailsActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080";
    private Double curBalance;
    private List<FavoriteStock> myFavoriteStocks = new ArrayList<>(); //you may change this later!
    private List<PortfolioStock> myPortfolioStocks = new ArrayList<>(); //you may change this later!
    private String curTicker;
    private String curCompanyName;
    private Double curPrice;
    private boolean isInFavorites;

    private static final String PREFS_NAME = "MyFavoriteStocksPrefs";
    private static final String KEY_FAVORITE_STOCKS = "favoriteStocks";
    private static final String PREFS_NAME_PORTFOLIO = "MyPortfolioStocksPrefs";
    private static final String KEY_PORTFOLIO_STOCKS = "portfolioStocks";

    private Button tradeButton;

    //related to portfolio
    Integer quantityToBuy = 0;
    Double totalCostToBuy = 0.00;

    Integer curQuantity = 0;
    Double curAvgCostShare = 0.00;
    Double curTotalCost = 0.00;
    Double curChange_Trade = 0.00;
    Double curMarketValue = 0.00;

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
        getBalanceData();
        getCompanyData(ticker);
        getQuoteData(ticker);

        initStarButton(ticker);
        initTradeSection(ticker);

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

        /*********** set up the dialogue button event listener *****************/

            tradeButton = (Button) findViewById(R.id.tradeButton);

            // add button listener
            tradeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    // custom dialog
                    final Dialog dialog = new Dialog(DetailsActivity.this);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    dialog.setContentView(R.layout.trade_dialogue);

                    //setting the dialogue width to 90% of the screen
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int dialogWidth = (int) (displayMetrics.widthPixels * 0.90);
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.copyFrom(dialog.getWindow().getAttributes());
                    layoutParams.width = dialogWidth;
                    dialog.getWindow().setAttributes(layoutParams);

                    // set the custom dialog components - text, image and button
                    TextView dialogueHeader = dialog.findViewById(R.id.dialogueHeader);
                    dialogueHeader.setText("Trade " + curCompanyName + " Shares");

                    EditText editText = dialog.findViewById(R.id.editText);
                    TextView tradeInputGuideText = dialog.findViewById(R.id.tradeInputGuideText);
                    tradeInputGuideText.setText(quantityToBuy + "*$" + curPrice + "/share = " + totalCostToBuy);

                    TextView tradeBalanceText = dialog.findViewById(R.id.tradeBalanceText);
                    tradeBalanceText.setText("$"+ curBalance +" to buy " + curTicker);

                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence seq, int start, int count, int after) {
                            // not needed
                        }

                        @Override
                        public void onTextChanged(CharSequence seq, int start, int before, int count) {
                            //Updating tradeInputListener TextView with the text entered by the user

                            if (!seq.toString().isEmpty()) {
                                quantityToBuy = Integer.parseInt(seq.toString());
                                totalCostToBuy = quantityToBuy * curPrice;
                                String formattedTotalCostToBuy = String.format("%.2f", totalCostToBuy);
                                tradeInputGuideText.setText(quantityToBuy + "*$" + curPrice + "/share = $" + formattedTotalCostToBuy);
                            } else {
                                // If the input is empty, set quantityToBuy and totalCostToBuy to 0
                                quantityToBuy = 0;
                                totalCostToBuy = 0.0;
                                tradeInputGuideText.setText(quantityToBuy + "*$" + curPrice + "/share = $" + totalCostToBuy);
                            }

                        }

                        @Override
                        public void afterTextChanged(Editable e) {
                            // not needed
                        }
                    });


                    Button buyButton = (Button) dialog.findViewById(R.id.buyButton);
                    // if button is clicked, close the custom dialog
                    buyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            });
    }



    private void initTradeSection(String tickerSymbol){
        myPortfolioStocks = loadPortfolioStocks();
        Log.d("DetailsActivity", "list of portfolio stocks retrieved from shared preferences is: " + myPortfolioStocks);

        for (PortfolioStock stock : myPortfolioStocks) {
            if (stock.getTickerSymbol().equals(curTicker)) {

                curQuantity = stock.getQuantity();
                curTotalCost = stock.getTotalCost();
                curAvgCostShare = curTotalCost / curQuantity;
                curChange_Trade = stock.getChange();
                curMarketValue = stock.getMarketValue();

            }


        }

        TextView sharesOwnedText = findViewById(R.id.sharesOwnedText);
        TextView avgCostShareText = findViewById(R.id.avgCostShareText);
        TextView totalCostText = findViewById(R.id.totalCostText);
        TextView changeTextDetails = findViewById(R.id.changeTextTrade);
        TextView marketValueText = findViewById(R.id.marketValueText);

        sharesOwnedText.setText(String.valueOf(curQuantity));
        avgCostShareText.setText(String.format("%.2f",curAvgCostShare));
        totalCostText.setText(String.format("%.2f",curTotalCost));
        changeTextDetails.setText(String.format("%.2f", curChange_Trade));
        marketValueText.setText(String.format("%.2f", curMarketValue));

    }

    //Method to get portfolio stocks from SharedPreferences
    private List<PortfolioStock> loadPortfolioStocks() {
        //Getting SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME_PORTFOLIO, Context.MODE_PRIVATE);

        String json = sharedPreferences.getString(KEY_PORTFOLIO_STOCKS, null);

        // converting JSON to list of favorite stocks
        Gson gson = new Gson();
        Type type = new TypeToken<List<PortfolioStock>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void getBalanceData() {
        String balanceUrl = BASE_URL + "/api/wallet/getBalance";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, balanceUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            curBalance = jsonObject.getDouble("cash_balance");

                        } catch (JSONException e) {
                            String errorMessage = "Error parsing JSON: " + e.getMessage();

                            Log.e("MainActivity", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching balance: " + error.getMessage();
                Log.d("MainActivity", errorMessage);
            }
        });
        queue.add(stringRequest);
    }

    private void updateBalanceData(Double newBalance) {
        String updateBalanceUrl = BASE_URL + "/api/wallet/updateBalance/";

        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("cash_balance", newBalance);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, updateBalanceUrl, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DetailsActivity", "Balance updated successfully");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                Log.e("DetailsActivity", "Error updating balance: " + error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void updatePortfolioItem(String tickerSymbol , String companyName, Integer stockQuantity, Double totalCost){
        String updatePortfolioUrl = BASE_URL + "api/portfolio/updatePortfolio";
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("ticker", tickerSymbol);
            requestBody.put("company_name", companyName);
            requestBody.put("quantity", stockQuantity);
            requestBody.put("total_cost", totalCost);

        } catch (JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, updatePortfolioUrl, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.d("DetailsActivity", "Portfolio Item for " + tickerSymbol + " updated successfully");
                        //updating the local list? maybe not needed!!
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("DetailsActivity", "Error updating portfolio item: " + volleyError.getMessage());
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void deletePortfolioItem(String tickerSymbol){
        String deletePortfolioUrl = BASE_URL + "api/portfolio/deletePortfolio/" + tickerSymbol;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, deletePortfolioUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("DetailsActivity", "Portfolio item " + tickerSymbol + " removed successfully");
                        // removing item from the local list? probably not needed
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("DetailsActivity", "Error deleting portfolio item: " + volleyError.getMessage());
            }
        });
        queue.add(stringRequest);
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


        TextView currentPriceText = findViewById(R.id.current_price);
        TextView changeText = findViewById(R.id.change);

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
                            double currentPrice = jsonObject.getDouble("c");
                            double changeVal = jsonObject.getDouble("d");
                            double changePercent = jsonObject.getDouble("dp");

                            curPrice = currentPrice; //set the class attribute to the obtained current price

                            // Convert double values to strings
                            String curPriceStr = String.valueOf(currentPrice);
                            String changeValStr = String.valueOf(changeVal);
                            String changePercentStr = String.valueOf(changePercent);

                            currentPriceText.setText("$" + curPriceStr);
                            changeText.setText("$" + changeValStr + " (" + changePercentStr + "%)");

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
    /*********************** related to favorite stocks *************************/
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
    /********************* End of favorite stocks methods *******************************/
}