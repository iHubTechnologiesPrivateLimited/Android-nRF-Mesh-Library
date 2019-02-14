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

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;

import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigError;

import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

public class MainActivity extends AppCompatActivity implements Injectable, HasSupportFragmentInjector, BottomNavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemReselectedListener,
        ScannerFragment.ScannerFragmentListener, FragmentManager.OnBackStackChangedListener {

    private static final String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";

    @Inject
    DispatchingAndroidInjector<Fragment> mDispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.state_scanning)
    View mScanningView;

    private BottomNavigationView mBottomNavigationView;

    private NetworkFragment mNetworkFragment;
    private ScannerFragment mScannerFragment;
    private Fragment mSettingsFragment;
    private UiFragment mUiFragment;
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);


        mUiFragment = (UiFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_ui);

        mNetworkFragment = (NetworkFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_network);
        mScannerFragment = (ScannerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_scanner);
        mSettingsFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_settings);


        mBottomNavigationView = findViewById(R.id.bottom_navigation_view);

        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        mBottomNavigationView.setOnNavigationItemReselectedListener(this);

        if (savedInstanceState == null) {
            onNavigationItemSelected(mBottomNavigationView.getMenu().findItem(R.id.action_ui));
        } else {
            mBottomNavigationView.setSelectedItemId(savedInstanceState.getInt(CURRENT_FRAGMENT));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.PROVISIONING_SUCCESS) {
            if (resultCode == RESULT_OK) {
                final boolean provisioningSuccess = data.getBooleanExtra(Utils.PROVISIONING_COMPLETED, false);
                if (provisioningSuccess) {
                    mBottomNavigationView.setSelectedItemId(R.id.action_network);
                    final boolean compositionDataReceived = data.getBooleanExtra(Utils.COMPOSITION_DATA_COMPLETED, false);
                    final boolean appKeyAddCompleted = data.getBooleanExtra(Utils.APP_KEY_ADD_COMPLETED, false);
                    final DialogFragmentConfigError fragmentConfigError;
                    if(compositionDataReceived){
                        if(!appKeyAddCompleted){
                            fragmentConfigError =
                                    DialogFragmentConfigError.newInstance(getString(R.string.title_init_config_error)
                                            , getString(R.string.init_config_error_app_key_msg));
                            fragmentConfigError.show(getSupportFragmentManager(), null);
                        }
                    } else {
                        fragmentConfigError =
                                DialogFragmentConfigError.newInstance(getString(R.string.title_init_config_error)
                                        , getString(R.string.init_config_error_all));
                        fragmentConfigError.show(getSupportFragmentManager(), null);
                    }
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (id) {
            case R.id.action_network:
                ft.show(mNetworkFragment).hide(mScannerFragment).hide(mSettingsFragment).hide(mUiFragment);
                break;
            case R.id.action_scanner:
                ft.hide(mNetworkFragment).show(mScannerFragment).hide(mSettingsFragment).hide(mUiFragment);
                break;
            case R.id.action_settings:
                ft.hide(mNetworkFragment).hide(mScannerFragment).show(mSettingsFragment).hide(mUiFragment);
                break;
            case R.id.action_ui:
                ft.hide(mNetworkFragment).hide(mScannerFragment).hide(mSettingsFragment).show(mUiFragment);
                break;
        }
        ft.commit();
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
    }

    @Override
    public void showProgressBar() {
        mScanningView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mScanningView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackStackChanged() {

    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return mDispatchingAndroidInjector;
    }
}
