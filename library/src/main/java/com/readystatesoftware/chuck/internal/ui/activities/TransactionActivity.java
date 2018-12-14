/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readystatesoftware.chuck.internal.ui.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.support.FormatUtils;
import com.readystatesoftware.chuck.internal.support.SimpleOnPageChangedListener;
import com.readystatesoftware.chuck.internal.ui.adapters.FragmentsAdapter;
import com.readystatesoftware.chuck.internal.ui.viewmodels.TransactionViewModel;

import static com.readystatesoftware.chuck.internal.data.ChuckContentProvider.TRANSACTION_URI;

public class TransactionActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_TRANSACTION_ID = "transaction_id";

    public static final int TYPE_OVERVIEW = 0;
    public static final int TYPE_REQUEST = 1;
    public static final int TYPE_RESPONSE = 2;

    private static int selectedTabPosition = 0;

    public static void start(Context context, long transactionId) {
        Intent intent = new Intent(context, TransactionActivity.class);
        intent.putExtra(ARG_TRANSACTION_ID, transactionId);
        context.startActivity(intent);
    }

    private TextView title;

    private long transactionId;

    private TransactionViewModel model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chuck_activity_transaction);

        model = ViewModelProviders.of(this).get(TransactionViewModel.class);
        model.transaction.observe(this, new Observer<HttpTransaction>() {

            @Override
            public void onChanged(@Nullable HttpTransaction transaction) {
                populateUI();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.toolbar_title);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = findViewById(R.id.viewpager);
        if (viewPager != null) setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        transactionId = getIntent().getLongExtra(ARG_TRANSACTION_ID, 0);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chuck_transaction, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        HttpTransaction transaction = model.transaction.getValue();
        if (transaction != null) {
            if (item.getItemId() == R.id.share_text) {
                share(FormatUtils.getShareText(this, transaction));
                return true;
            } else if (item.getItemId() == R.id.share_curl) {
                share(FormatUtils.getShareCurlCommand(transaction));
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
            this,
            ContentUris.withAppendedId(TRANSACTION_URI, transactionId),
            null,
            null,
            null,
            null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        model.transaction.setValue(LocalCupboard.getInstance().withCursor(data).get(HttpTransaction.class));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) { }

    private void populateUI() {
        HttpTransaction transaction = model.transaction.getValue();
        if (transaction != null) {
            title.setText(String.format("%s %s", transaction.getMethod(), transaction.getPath()));
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setAdapter(new FragmentsAdapter(this, getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new SimpleOnPageChangedListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTabPosition = position;
            }
        });
        viewPager.setCurrentItem(selectedTabPosition);
    }

    private void share(String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }
}
