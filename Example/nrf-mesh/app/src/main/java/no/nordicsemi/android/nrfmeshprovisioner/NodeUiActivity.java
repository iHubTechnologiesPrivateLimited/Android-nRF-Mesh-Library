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
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.models.GenericLevelServerModel;
import no.nordicsemi.android.meshprovisioner.models.GenericOnOffServerModel;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AddedAppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ElementUiAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAppKeyAddStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentResetNode;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NodeConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA_MODEL_NAME;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;

public class NodeUiActivity extends AppCompatActivity implements Injectable,
        ElementUiAdapter.OnItemClickListener,
        DialogFragmentAppKeyAddStatus.DialogFragmentAppKeyAddStatusListener,
        DialogFragmentResetNode.DialogFragmentNodeResetListener,
        AddedAppKeyAdapter.OnItemClickListener, ItemTouchHelperAdapter {

    private final static String TAG = NodeUiActivity.class.getSimpleName();
    private static final String PROGRESS_BAR_STATE = "PROGRESS_BAR_STATE";
    private static final String DIALOG_FRAGMENT_APP_KEY_STATUS = "DIALOG_FRAGMENT_APP_KEY_STATUS";
    private static final long DELAY = 10 * 1000; //Using the incomplete timer duration

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.main_container)
    NestedScrollView mContainer;
    @BindView(R.id.action_get_compostion_data)
    Button actionGetCompositionData;
//    @BindView(R.id.action_add_app_keys)
//    Button actionAddAppkey;
//    @BindView(R.id.action_reset_node)
//    Button actionResetNode;
    @BindView(R.id.recycler_view_elements)
    RecyclerView mRecyclerViewElements;
    @BindView(R.id.composition_data_card)
    CardView mCompositionDataCard;
    @BindView(R.id.configuration_progress_bar)
    ProgressBar mProgressbar;

    private NodeConfigurationViewModel mViewModel;
    private AddedAppKeyAdapter mAdapter;
    private Handler mHandler;
    protected ModelConfigurationViewModel mModelViewModel;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_node_ui);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(NodeConfigurationViewModel.class);
        mModelViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ModelConfigurationViewModel.class);

        final Intent intent = getIntent();
        final ProvisionedMeshNode node = intent.getParcelableExtra(Utils.EXTRA_DEVICE);
        if(savedInstanceState == null) {
            if (node == null)
                finish();
            mViewModel.setMeshNode(node);
        } else {
            if(savedInstanceState.getBoolean(PROGRESS_BAR_STATE)) {
                mProgressbar.setVisibility(View.VISIBLE);
                disableClickableViews();
            } else {
                mProgressbar.setVisibility(View.INVISIBLE);
                enableClickableViews();
            }
        }

        mHandler = new Handler();
        // Set up views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_switch_board);
        getSupportActionBar().setSubtitle(node.getNodeName());

        actionGetCompositionData = findViewById(R.id.action_get_compostion_data);
        //actionAddAppkey = findViewById(R.id.action_add_app_keys);
       // actionResetNode = findViewById(R.id.action_reset_node);
        final TextView noElementsFound = findViewById(R.id.no_elements);
        final TextView noAppKeysFound = findViewById(R.id.no_app_keys);
        final View compositionActionContainer = findViewById(R.id.composition_action_container);
        mRecyclerViewElements.setLayoutManager(new LinearLayoutManager(this));
        final ElementUiAdapter adapter = new ElementUiAdapter(this, mViewModel.getExtendedMeshNode(),mModelViewModel);
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        mRecyclerViewElements.setAdapter(adapter);




        mViewModel.getExtendedMeshNode().observe(this, extendedMeshNode -> {
            if(extendedMeshNode.getMeshNode() == null) {
                finish();
                return;
            }

            if (extendedMeshNode.hasElements()) {
                compositionActionContainer.setVisibility(View.GONE);
                noElementsFound.setVisibility(View.INVISIBLE);
                mRecyclerViewElements.setVisibility(View.VISIBLE);
            } else {
                noElementsFound.setVisibility(View.VISIBLE);
                compositionActionContainer.setVisibility(View.VISIBLE);
                mRecyclerViewElements.setVisibility(View.INVISIBLE);
            }


        });

        actionGetCompositionData.setOnClickListener(v -> {
            showProgressbar();
            mViewModel.sendGetCompositionData();
        });





        mViewModel.getCompositionDataStatus().observe(this, compositionDataStatusLiveData -> {
            hideProgressBar();
            if(!compositionDataStatusLiveData.isSuccess()){
                DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance("Transaction Failed", getString(R.string.operation_timed_out));
                fragmentMessage.show(getSupportFragmentManager(), null);
            }
        });



        mViewModel.getTransactionStatus().observe(this, transactionFailedLiveData -> {
            hideProgressBar();
            final String message;
            if(transactionFailedLiveData.isIncompleteTimerExpired()){
                message = getString(R.string.segments_not_received_timed_out);
            } else {
                message = getString(R.string.operation_timed_out);
            }
            DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance(getString(R.string.title_transaction_failed), message);
            fragmentMessage.show(getSupportFragmentManager(), null);
        });

        mViewModel.isConnected().observe(this, isConnected -> {
            if(isConnected != null && !isConnected)
                finish();
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isFinishing()){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESS_BAR_STATE, mProgressbar.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onElementItemClick(final ProvisionedMeshNode meshNode, final Element element, final MeshModel model) {
        startActivity(meshNode, element, model);
    }

    @Override
    public void onAppKeyAddStatusReceived() {

    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {

    }

    @Override
    public void onItemClick(final String appKey) {

    }

    @Override
    public void onNodeReset() {

    }

    private void showProgressbar(){
        disableClickableViews();
        mProgressbar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        mHandler.removeCallbacks(mOperationTimeout);
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
    }

    private final Runnable mOperationTimeout = () -> hideProgressBar();

    private void enableClickableViews(){
        actionGetCompositionData.setEnabled(true);

    }

    private void disableClickableViews(){
        actionGetCompositionData.setEnabled(false);

    }

    /**
     * Start activity based on the type of the model
     *
     * <p> This way we can seperate the ui logic for different activities</p>
     *
     * @param meshNode mesh node
     * @param element element
     * @param model model
     */
    public void startActivity(final ProvisionedMeshNode meshNode, final Element element, final MeshModel model) {
        final Intent intent;
        if(model instanceof GenericOnOffServerModel) {
            intent = new Intent(this, GenericOnOffServerActivity.class);
        } else if (model instanceof GenericLevelServerModel) {
            intent = new Intent(this, GenericLevelServerActivity.class);
        } else if (model instanceof VendorModel) {
            intent = new Intent(this, VendorModelActivity.class);
        } else {
            intent = new Intent(this, ModelConfigurationActivity.class);
        }

        intent.putExtra(EXTRA_DEVICE, meshNode);
        intent.putExtra(EXTRA_ELEMENT_ADDRESS, AddressUtils.getUnicastAddressInt(element.getElementAddress()));
        intent.putExtra(EXTRA_MODEL_ID, model.getModelId());
        intent.putExtra(EXTRA_DATA_MODEL_NAME, model.getModelName());
        startActivity(intent);
    }
}