package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        //accessing the data passed from MainActivity
        Intent i = getIntent();
        String ticker = i.getStringExtra("inputValue");
        ticker = ticker.toUpperCase();

        //changing the title in this activity
        getSupportActionBar().setTitle(ticker);
        //to see the up (back) button in action bar
        //we also set the parent activity in the manifest file for the button to work
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getCompanyData(ticker);
        getQuoteData(ticker);
    }

    private void getCompanyData(String tickerSymbol){
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

    private void getQuoteData(String tickerSymbol){
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

                            currentPrice.setText("$"+ curPriceStr);
                            change.setText("$"+ changeValStr + " (" + changePercentStr + "%)");

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
}