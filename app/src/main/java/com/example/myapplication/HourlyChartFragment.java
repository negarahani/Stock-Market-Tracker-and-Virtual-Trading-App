package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HourlyChartFragment extends Fragment {

    private static final String BASE_URL = "http://10.0.2.2:8080";
    String fromDateFormatted;
    String toDateFormatted;
    private static final String ARG_TICKER = "ticker";

    private String curTicker;
    private WebView webView;

    public static HourlyChartFragment newInstance(String ticker) {
        HourlyChartFragment fragment = new HourlyChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TICKER, ticker);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            curTicker = getArguments().getString(ARG_TICKER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hourly_chart, container, false);
        webView = view.findViewById(R.id.webView_hourlyChart);
        getHourlyChartDates(curTicker);

        return view;
    }
    private void getHourlyChartDates(String tickerSymbol) {
        String quoteUrl = BASE_URL + "/search-quote/" + tickerSymbol;

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, quoteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parsing the JSON response
                            JSONObject jsonObject = new JSONObject(response);

                            //calculating fromDate and toDate for the hourly chart
                            long lastOpenTime = jsonObject.getLong("t") * 1000; // Convert to milliseconds
                            // Calculate current time
                            long currentTime = System.currentTimeMillis();

                            if ((currentTime - lastOpenTime) > 5 * 60 * 1000) { // Market is closed
                                Date toDate = new Date(lastOpenTime);
                                toDateFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(toDate);

                                Calendar fromDateCalendar = Calendar.getInstance();
                                fromDateCalendar.setTime(toDate);
                                fromDateCalendar.add(Calendar.DATE, -1);
                                Date fromDate = fromDateCalendar.getTime();
                                fromDateFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fromDate);

                            } else { // Market is open
                                Date toDate = new Date(currentTime);
                                toDateFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(toDate);

                                Calendar fromDateCalendar = Calendar.getInstance();
                                fromDateCalendar.setTime(toDate);
                                fromDateCalendar.add(Calendar.DATE, -1);
                                Date fromDate = fromDateCalendar.getTime();
                                fromDateFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fromDate);
                            }

                            Log.d("DetailsActivity", "fromDateFormatted is: " + fromDateFormatted);
                            Log.d("DetailsActivity", "tDateFormatted is: " + toDateFormatted);

                            getHourlyPriceData(tickerSymbol, fromDateFormatted, toDateFormatted);


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
                String errorMessage = "Error fetching market status Data: " + error.getMessage();
                Log.d("DetailsActivity", errorMessage);
            }
        });
        queue.add(stringRequest);
    }

    private void getHourlyPriceData(String tickerSymbol, String fromDate, String toDate) {
        String hourlyPriceUrl = BASE_URL + "/search-hourly-price/" + tickerSymbol + "/" + fromDate + "/" + toDate;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, hourlyPriceUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray resultsArray = response.getJSONArray("results");
                            createHourlyChart(resultsArray);

                        } catch (JSONException e) {
                            String errorMessage = "Error parsing JSON: " + e.getMessage();
                            Log.e("MainActivity", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                String errorMessage = "Error fetching hourly price data: " + error.getMessage();
                Log.e("DetailsActivity", errorMessage);
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void createHourlyChart(JSONArray dataArray){
        Log.d("DetailsActivity","Data for creating hourly chart is " + dataArray);

        // Enable JavaScript execution in the WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the HTML file from the assets directory
        webView.loadUrl("file:///android_asset/my_hourly_chart.html");

        // Calling the method to pass data to JavaScript when the page is finished loading
        // source: StackOverflow , Url: https://stackoverflow.com/questions/57033537/how-can-i-inject-js-code-in-the-shown-page-in-a-webview
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Calling JavaScript function to pass data
                webView.loadUrl("javascript:createHourlyChart(" + dataArray + ")");
            }
        });
    }
}

