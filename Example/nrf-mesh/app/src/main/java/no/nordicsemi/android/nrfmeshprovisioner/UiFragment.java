package no.nordicsemi.android.nrfmeshprovisioner;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.UiNodeAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

public class UiFragment extends Fragment implements  Injectable, UiNodeAdapter.OnItemClickListener {


   SharedViewModel mViewModel;

   @Inject
   ViewModelProvider.Factory mViewModelFactory;

    private UiNodeAdapter mAdapter;
    public interface UiFragmentListener {
        void onProvisionedMeshNodeSelected();
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_ui, null);
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_provisioned_nodes);
        final View noNetworksConfiguredView = rootView.findViewById(R.id.no_networks_configured);
       mViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(SharedViewModel.class);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if(isTablet){
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); //If its a tablet we use a grid layout with 2 columns
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        Log.d("UI", "UiFragment: "+getActivity());
        mAdapter = new UiNodeAdapter(getActivity(), mViewModel.getProvisionedNodes());
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        // Create view model containing utility methods for scanning
        mViewModel.getProvisionedNodes().observe(this, nodes -> {
            if(!nodes.isEmpty()) {
                noNetworksConfiguredView.setVisibility(View.GONE);
            } else {
                noNetworksConfiguredView.setVisibility(View.VISIBLE);
            }
            mAdapter.notifyDataSetChanged();
        });

        mViewModel.getProvisionedNodes().observe(this, provisionedNodes -> requireActivity().invalidateOptionsMenu());

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if(isConnected != null) {
                requireActivity().invalidateOptionsMenu();
            }
        });

        mViewModel.getConnectedMeshNodeAddress().observe(this, unicastAddress -> mAdapter.selectConnectedMeshNode(unicastAddress));


        return  rootView;
    }

    @Override
    public void onConfigureClicked(final ProvisionedMeshNode node) {
        final Boolean isConnectedToProxy = mViewModel.isConnectedToProxy().getValue();
        if(isConnectedToProxy != null && isConnectedToProxy) {
            mViewModel.setSelectedMeshNode(node);
            final Intent meshConfigurationIntent = new Intent(getActivity(), NodeUiActivity.class);
            requireActivity().startActivity(meshConfigurationIntent);
        } else {
            Toast.makeText(getActivity(), R.string.disconnected_network_rationale, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetailsClicked(final ProvisionedMeshNode node) {

    }
}
