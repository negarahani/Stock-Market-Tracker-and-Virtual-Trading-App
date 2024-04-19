package com.example.myapplication;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.lifecycle.Lifecycle;


public class TabFragmentAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 2;
    private String ticker;

    public TabFragmentAdapter(FragmentManager fragmentManagert, Lifecycle lifecycle, String ticker) {
        super(fragmentManagert, lifecycle);
        this.ticker = ticker;


    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return HourlyChartFragment.newInstance(ticker);
            case 1:
                return new HistoricalChartFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
