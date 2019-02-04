/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmeshprovisioner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.BindAppKeyAdapter;

public class BindAppKeysActivity extends AppCompatActivity implements BindAppKeyAdapter.OnItemClickListener {

    public static final String RESULT_APP_KEY = "RESULT_APP_KEY";
    public static final String RESULT_APP_KEY_INDEX = "RESULT_APP_KEY_INDEX";
    public static final String APP_KEYS = "APP_KEYS";
    private final static String TAG = BindAppKeysActivity.class.getSimpleName();
    //UI Bindings
    @BindView(android.R.id.empty)
    View mEmptyView;
    @BindView(R.id.container)
    View container;

    private List<ApplicationKey> mAppKeysMap = new ArrayList<>();
    private BindAppKeyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_app_keys);
        final HashMap<Integer, ApplicationKey> tempAppKeys = (HashMap<Integer, ApplicationKey>) getIntent().getSerializableExtra(APP_KEYS);

        //Bind Ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_select_app_key);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final RecyclerView appKeysRecyclerView = findViewById(R.id.recycler_view_app_keys);
        appKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(appKeysRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        appKeysRecyclerView.addItemDecoration(dividerItemDecoration);
        appKeysRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new BindAppKeyAdapter(this, populateAppKeys(tempAppKeys));
        mAdapter.setOnItemClickListener(this);
        appKeysRecyclerView.setAdapter(mAdapter);

        final boolean empty = mAdapter.getItemCount() == 0;
        mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);

//        ihub edits start
        Intent returnIntent = new Intent();
        Log.d(TAG, "onCreate: "+tempAppKeys.get(0));
        returnIntent.putExtra(RESULT_APP_KEY, tempAppKeys.get(0));
        setResult(Activity.RESULT_OK, returnIntent);
       // Toast.makeText(getApplicationContext(),"In app keys",Toast.LENGTH_SHORT).show();
        finish();
//        ihub edits end
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onItemClick(final int keyIndex, final ApplicationKey appKey) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(RESULT_APP_KEY, appKey);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private List<ApplicationKey> populateAppKeys(final HashMap<Integer, ApplicationKey> tempAppKeys){
        final List<ApplicationKey> applicationKeys = new ArrayList<>();
        for (Map.Entry<Integer, ApplicationKey> appKeyEntry : tempAppKeys.entrySet()) {
            final ApplicationKey applicationKey = appKeyEntry.getValue();
            applicationKeys.add(applicationKey);
           // mAppKeysMap.add(applicationKey);
        }
        return applicationKeys;
    }
}
