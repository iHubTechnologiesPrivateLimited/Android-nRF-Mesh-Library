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
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ReconnectViewModel;

public class ReconnectActivity extends AppCompatActivity implements Injectable {
	public static final int REQUEST_DEVICE_READY = 1122; //Random number
	private ReconnectViewModel mReconnectViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

	@BindView(R.id.connectivity_progress_container) View mConnectivityProgress;
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reconnect);
		ButterKnife.bind(this);

		final Intent intent = getIntent();
		final ExtendedBluetoothDevice device = intent.getParcelableExtra(Utils.EXTRA_DEVICE);
		final String deviceName = device.getName();
		final String deviceAddress = device.getAddress();

		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(deviceName);
		getSupportActionBar().setSubtitle(deviceAddress);
        final TextView connectionState = findViewById(R.id.connection_state);
		// Create view model containing utility methods for scanning
		mReconnectViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ReconnectViewModel.class);
		//Toast.makeText(this, " Not yet Connected ", Toast.LENGTH_SHORT).show();
		Log.d("connectAuto", ":: not yet connected");
		mReconnectViewModel.connect(device);
		mReconnectViewModel.isConnected().observe(this, isConnected -> {

			if(!isConnected){
				Log.d("connectAuto", ":: "+isConnected);
				finish();
			}else{
				Log.d("connectAuto", ":: connected");

			}
		});

		mReconnectViewModel.getConnectionState().observe(this, connectionState::setText);

		mReconnectViewModel.isDeviceReady().observe(this, deviceReady -> {
			if(deviceReady) {
				Intent returnIntent = new Intent();
                returnIntent.putExtra(Utils.ACTIVITY_RESULT, true);
                setResult(Activity.RESULT_OK, returnIntent);
				Log.d("connectAuto", " :: returnIntent "+true);
				finish();

            }
		});

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
		mReconnectViewModel.disconnect();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
