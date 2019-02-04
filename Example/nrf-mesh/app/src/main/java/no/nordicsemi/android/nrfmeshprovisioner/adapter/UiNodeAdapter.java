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

package no.nordicsemi.android.nrfmeshprovisioner.adapter;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.CompanyIdentifiers;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class UiNodeAdapter extends RecyclerView.Adapter<UiNodeAdapter.ViewHolder> {
    private Integer mUnicastAddress;
    private int mNodeIndex = -1;
    private final FragmentActivity mContext;
    private final List<ProvisionedMeshNode> mNodes = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public UiNodeAdapter(final FragmentActivity fragmentActivity, LiveData<List<ProvisionedMeshNode>> provisionedNodesLiveData) {
        this.mContext = fragmentActivity;

        provisionedNodesLiveData.observe(fragmentActivity, provisionedNodes -> {
            if (provisionedNodes != null) {
                mNodes.clear();
                mNodes.addAll(provisionedNodes);
            }
        });
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.ui_network_item, parent, false);
        return new UiNodeAdapter.ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ProvisionedMeshNode node = mNodes.get(position);
        //here we are showing name of the node in the configure button
        holder.configure.setText(node.getNodeName());

        final Map<Integer, Element> elements = node.getElements();

    }

    @Override
    public int getItemCount() {
        return mNodes.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    private int getModels(final Map<Integer, Element> elements) {
        int models = 0;
        for (Element element : elements.values()) {
            models += element.getMeshModels().size();
        }
        return models;
    }

    public void selectConnectedMeshNode(final Integer unicastAddress) {
        if (unicastAddress != null) {
            final int index = mNodeIndex = getMeshNodeIndex(unicastAddress);
            if (index > -1) {
                notifyItemChanged(mNodeIndex);
            }
        } else {
            notifyItemChanged(mNodeIndex);
        }
        mUnicastAddress = unicastAddress;
    }

    private int getMeshNodeIndex(final int unicastAddress) {
        for (int i = 0; i < mNodes.size(); i++) {
            if (unicastAddress == mNodes.get(i).getUnicastAddressInt()) {
                return i;
            }
        }
        return -1;
    }

    public interface OnItemClickListener {
        void onConfigureClicked(final ProvisionedMeshNode node);

        void onDetailsClicked(final ProvisionedMeshNode node);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
//        @BindView(R.id.node_name)
//        TextView name;
        //            @Nullable
//            @BindView(R.id.configured_node_info_container)
//            View nodeInfoContainer;
//            @Nullable
//            @BindView(R.id.unicast)
//            TextView unicastAddress;
//            @Nullable
//            @BindView(R.id.company_identifier)
//            TextView companyIdentifier;
//            @Nullable
//            @BindView(R.id.elements)
//            TextView elements;
//            @Nullable
//            @BindView(R.id.models)
//            TextView models;
        @BindView(R.id.not_configured_view)
        View notConfiguredView;
        @BindView(R.id.action_configure)
        Button configure;
        //            @BindView(R.id.action_details)
//            Button details;
        private ViewHolder(final View provisionedView) {
            super(provisionedView);
            ButterKnife.bind(this, provisionedView);

            provisionedView.findViewById(R.id.action_configure).setOnClickListener(v -> {

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onConfigureClicked(mNodes.get(getAdapterPosition()));
                }

            });

//            provisionedView.findViewById(R.id.action_details).setOnClickListener(v -> {
//
//                if (mOnItemClickListener != null) {
//                    mOnItemClickListener.onDetailsClicked(mNodes.get(getAdapterPosition()));
//                }
//            });
        }
    }
}
