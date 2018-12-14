package com.readystatesoftware.chuck.internal.ui.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.ui.fragments.TransactionOverviewFragment;
import com.readystatesoftware.chuck.internal.ui.fragments.TransactionPayloadFragment;

import java.util.ArrayList;
import java.util.List;

import static com.readystatesoftware.chuck.internal.ui.activities.TransactionActivity.TYPE_OVERVIEW;

public class FragmentsAdapter extends FragmentStatePagerAdapter {

    private static final int[] titleIds = new int[]{R.string.chuck_overview, R.string.chuck_request, R.string.chuck_response};

    private final List<String> fragmentTitles = new ArrayList<>();

    public FragmentsAdapter(Context context, FragmentManager fm) {
        super(fm);
        for (int id : titleIds) fragmentTitles.add(context.getString(id));
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TYPE_OVERVIEW:
                return new TransactionOverviewFragment();
            default:
                return TransactionPayloadFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return titleIds.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }
}