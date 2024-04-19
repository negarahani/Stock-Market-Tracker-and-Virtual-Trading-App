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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoricalChartFragment extends Fragment {
    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static final String ARG_TICKER = "ticker";

    private String curTicker;
    private WebView webView;
    JSONArray historicalDataArray;

    public static HistoricalChartFragment newInstance(String ticker) {
        HistoricalChartFragment fragment = new HistoricalChartFragment();
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
        View view = inflater.inflate(R.layout.chart_layout, container, false);
        webView = view.findViewById(R.id.webView_1);

        getHistoricalChartData(curTicker);

        return view;
    }
    private void getHistoricalChartData(String tickerSymbol){
        String historicalChartUrl = BASE_URL + "/search-chart/" + tickerSymbol;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, historicalChartUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray resultsArray = response.getJSONArray("results");
                            historicalDataArray = resultsArray; //I did not really need this extra variable
                            createHistoricalChart(historicalDataArray);

                        } catch (JSONException e) {
                            String errorMessage = "Error parsing JSON: " + e.getMessage();
                            Log.e("MainActivity", errorMessage);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching chart data: " + error.getMessage();
                Log.e("DetailsActivity", errorMessage);
            }
        });
        queue.add(jsonObjectRequest);

    }

    private void createHistoricalChart(JSONArray dataArray){
        Log.d("DetailsActivity","Data for creating historical chart is " + dataArray);

        // Enable JavaScript execution in the WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the HTML file from the assets directory
        webView.loadUrl("file:///android_asset/sample.html");

        // Calling the method to pass data to JavaScript when the page is finished loading
        // source: StackOverflow , Url: https://stackoverflow.com/questions/57033537/how-can-i-inject-js-code-in-the-shown-page-in-a-webview
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Calling JavaScript function to pass data
                webView.loadUrl("javascript:createHistoricalChart(" + dataArray + ")");
            }
        });
    }
}
