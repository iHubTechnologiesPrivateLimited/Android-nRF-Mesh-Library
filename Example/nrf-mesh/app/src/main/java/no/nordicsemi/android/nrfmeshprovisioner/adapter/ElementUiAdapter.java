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

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.models.GenericOnOffServerModel;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.BaseModelConfigurationActivity;
import no.nordicsemi.android.nrfmeshprovisioner.GenericOnOffServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.NodeConfigurationActivity;
import no.nordicsemi.android.nrfmeshprovisioner.NodeUiActivity;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.UiOnOffServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA_MODEL_NAME;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;
import no.nordicsemi.android.nrfmeshprovisioner.NodeUiActivity;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ModelConfigurationViewModel;

public class ElementUiAdapter extends RecyclerView.Adapter<ElementUiAdapter.ViewHolder> {



    private final Context mContext;
    private final List<Element> mElements = new ArrayList<>();
    private final String TAG = ElementUiAdapter.class.getSimpleName();
    private OnItemClickListener mOnItemClickListener;
    private ProvisionedMeshNode mProvisionedMeshNode;
    private NodeUiActivity nodeUiActivity;
    public ModelConfigurationViewModel mViewModel;
    public List<MeshModel> models;
    RecyclerView mRecyclerViewElements;

    public int getCpos() {
        return cpos;
    }

    public void setCpos(int cpos) {
        this.cpos = cpos;
    }

    protected int cpos;
    public ElementUiAdapter(RecyclerView mRecyclerViewElements,final NodeUiActivity NodeUiActivity, final ExtendedMeshNode extendedMeshnode,final ModelConfigurationViewModel mViewModel ) {
        this.mRecyclerViewElements = mRecyclerViewElements;
        this.mContext = NodeUiActivity.getApplicationContext();
        nodeUiActivity = NodeUiActivity;
        this.mViewModel = mViewModel;
        extendedMeshnode.observe(NodeUiActivity, extendedMeshNode -> {
            if(extendedMeshNode.getMeshNode() != null) {
                mProvisionedMeshNode = (ProvisionedMeshNode) extendedMeshnode.getMeshNode();
                mElements.clear();
                mElements.addAll(mProvisionedMeshNode.getElements().values());
                notifyDataSetChanged();
            }
        });
    }
    public void setOnItemClickListener(final ElementUiAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.layout_ui_on_off, parent, false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Element element = mElements.get(position);
        holder.mElementContainer.setTag(element.getElementAddressInt());
        final int modelCount = element.getMeshModels().size();
        holder.mElementTitle.setText(mContext.getString(R.string.element_address, MeshParserUtils.bytesToHex(element.getElementAddress(), false)));
        holder.mElementSubtitle.setText(mContext.getString(R.string.model_count, modelCount));

        models = new ArrayList<>(element.getMeshModels().values());
        inflateModelViews(holder, models);

    }


    private void inflateModelViews(final ViewHolder holder, final List<MeshModel> models){




        mViewModel.getGenericOnOffState().observe(nodeUiActivity, genericOnOffStatusUpdate -> {
            //hideProgressBar();
            final boolean presentState = genericOnOffStatusUpdate.isPresentOnOff();
            final Boolean targetOnOff = genericOnOffStatusUpdate.getTargetOnOff();
            final int steps = genericOnOffStatusUpdate.getSteps();
            final int resolution = genericOnOffStatusUpdate.getResolution();
            View view = mRecyclerViewElements.findViewHolderForAdapterPosition(getCpos()).itemView;
            Button mActionOnOff = (Button) view.findViewById(R.id.action_on_off);
            if (targetOnOff == null) {
                if (presentState) {

                    //onOffState.setText(R.string.generic_state_on);
                    //holder.mActionOnOff.getTag();
                    Log.d(TAG, "getAdapterPosition: "+getCpos());

                    mActionOnOff.setText(R.string.action_generic_off);
                } else {
                    //onOffState.setText(R.string.generic_state_off);
                    mActionOnOff.setText(R.string.action_generic_on);
                }
               // remainingTime.setVisibility(View.GONE);
            } else {
                if (!targetOnOff) {
                    //onOffState.setText(R.string.generic_state_on);
                    mActionOnOff.setText(R.string.action_generic_off);
                } else {
                    //onOffState.setText(R.string.generic_state_off);
                    mActionOnOff.setText(R.string.action_generic_on);
                }
                //remainingTime.setText(getString(R.string.remaining_time, MeshParserUtils.getRemainingTransitionTime(resolution, steps)));
                //remainingTime.setVisibility(View.VISIBLE);
            }
        });



    }

    @Override
    public int getItemCount() {
        if(mElements == null)
            return 0;
        return mElements.size();
    }

    @Override
    public long getItemId(final int position) {
        if(mElements != null)
            mElements.get(position).getElementAddressInt();
        return super.getItemId(position);
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onElementItemClick(final ProvisionedMeshNode meshNode, final Element element, final MeshModel model);
    }

    final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.element_item_container)
        ConstraintLayout mElementContainer;
        @BindView(R.id.element_title)
        TextView mElementTitle;
        @BindView(R.id.element_subtitle)
        TextView mElementSubtitle;

        @BindView(R.id.action_read)
        Button mActionRead;


        @BindView(R.id.action_on_off)
        Button mActionOnOff;


        public void setModel(MeshModel model) {  this.model = model; }





        protected  MeshModel model;
        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            mElementContainer.setOnClickListener(this);
            mActionOnOff.setOnClickListener(this);
            mActionRead.setOnClickListener(this);

        }

        @Override
        public void onClick(final View v) {
            switch (v.getId()){
                case R.id.action_on_off:
                    try {
                        for(MeshModel model : models) {

                            if(model instanceof GenericOnOffServerModel){
                                Log.d("inflate", "getElementAddress: " + AddressUtils.getUnicastAddressInt(mElements.get(this.getAdapterPosition()).getElementAddress())+" holder position: "+this.getAdapterPosition()+ "size: "+model.getModelName());
                         mViewModel.setModel(mProvisionedMeshNode, AddressUtils.getUnicastAddressInt(mElements.get(this.getAdapterPosition()).getElementAddress()),model.getModelId());


                        if (mActionOnOff.getText().toString().equals("ON")) {
                            Log.d(TAG, "getAdapterPosition: "+ this.getAdapterPosition() +" from click" );
                            setCpos(getAdapterPosition());
                            mViewModel.sendGenericOnOff(mProvisionedMeshNode, 0, 0, 0, true);
                          //  mActionOnOff.setText(R.string.action_generic_off);
                        } else {
                            setCpos(getAdapterPosition());

                            mViewModel.sendGenericOnOff(mProvisionedMeshNode, 0, 0, 0, false);
                          //  mActionOnOff.setText(R.string.action_generic_on);
                        }
                            }
                        }
                        //uiof.progressBar();
                    } catch (IllegalArgumentException ex) {
                        Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }finally {

                    }
                    break;
                case R.id.action_read:
                    final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                    mViewModel.sendGenericOnOffGet(node);
                default:
                    break;
            }

        }
    }
}
