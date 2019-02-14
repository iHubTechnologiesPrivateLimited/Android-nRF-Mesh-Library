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
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.models.GenericOnOffServerModel;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffSet;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.GenericOnOffServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.NodeConfigurationActivity;
import no.nordicsemi.android.nrfmeshprovisioner.NodeUiActivity;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NodeConfigurationViewModel;

// ihub class
// this class is eqvivalent to ElementAdapter
// we set all the elements in here to display in single screen, instead of multiple pages
public class ElementUiAdapter extends RecyclerView.Adapter<ElementUiAdapter.ViewHolder> {


    private final Context mContext;
    private final List<Element> mElements = new ArrayList<>();
    private final String TAG = ElementUiAdapter.class.getSimpleName();
    private ElementUiAdapter.OnItemClickListener mOnItemClickListener;
    private ProvisionedMeshNode mProvisionedMeshNode;
    private NodeUiActivity nodeUiActivity;

    public NodeConfigurationViewModel mViewModel;
    public ModelConfigurationViewModel mModelViewModel;
    public List<MeshModel> models;
    RecyclerView mRecyclerViewElements;

    public ElementUiAdapter(RecyclerView mRecyclerViewElements, final NodeUiActivity NodeUiActivity, final NodeConfigurationViewModel mViewModel, final ModelConfigurationViewModel mModelViewModel) {
        this.mContext = NodeUiActivity.getApplicationContext();
        this.mRecyclerViewElements = mRecyclerViewElements;
        this.nodeUiActivity = NodeUiActivity;
        this.mViewModel = mViewModel;
        this.mModelViewModel = mModelViewModel;
        ExtendedMeshNode extendedMeshnode = mViewModel.getSelectedMeshNode();

        final ProvisionedMeshNode node = mModelViewModel.getSelectedMeshNode().getMeshNode();
        //Log.d(TAG, "# of elements in node are: "+node.getNumberOfElements());


        extendedMeshnode.observe(NodeUiActivity, meshNode -> {
            if (meshNode != null) {
                mProvisionedMeshNode = meshNode;
                mElements.clear();
                mElements.addAll(mProvisionedMeshNode.getElements().values());
                notifyDataSetChanged();
            }
        });

    }
    public int getCpos() {
        return cpos;
    }

    public void setCpos(int cpos) {
        this.cpos = cpos;
    }

    protected int cpos;

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
        holder.mElementContainer.setTag(element);
        final int modelCount = element.getSigModelCount() + element.getVendorModelCount();
        holder.mElementTitle.setText(mContext.getString(R.string.element_address, MeshParserUtils.bytesToHex(element.getElementAddress(), false)));
        holder.mElementSubtitle.setText(mContext.getString(R.string.model_count, modelCount));
        models = new ArrayList<>(element.getMeshModels().values());
        inflateModelViews(holder, models);
    }


    private void inflateModelViews(final ViewHolder holder, final List<MeshModel> models){

       // holder.mModelContainer.removeAllViews();

        //for (MeshModel model : models) {
           // final View modelView = LayoutInflater.from(mContext).inflate(R.layout.model_item, holder.mElementContainer, false);
            //modelView.setTag(model.getModelId());
            //final TextView modelNameView = modelView.findViewById(R.id.address);
            //final TextView modelIdView = modelView.findViewById(R.id.model_id);
            //modelNameView.setText(model.getModelName());
//            if (model instanceof VendorModel) {
//                modelIdView.setText(mContext.getString(R.string.format_vendor_model_id, CompositionDataParser.formatModelIdentifier(model.getModelId(), true)));
//            } else {
//                modelIdView.setText(mContext.getString(R.string.format_sig_model_id, CompositionDataParser.formatModelIdentifier((short) model.getModelId(), true)));
//            }

//            modelView.setOnClickListener(v -> {
//                final int position = holder.getAdapterPosition();
//                final Element element = mElements.get(position);
//                final MeshModel model1 = element.getMeshModels().get(v.getTag());
//                mOnItemClickListener.onElementItemClick(mProvisionedMeshNode, element, model1);
//            });
//            holder.mModelContainer.addView(modelView);
        //}
//
//        mViewModel.getMeshMessageLiveData().observe(nodeUiActivity, meshMessage -> {
//            Log.d(TAG, "In inflateModelViews: ");
//            Log.d(TAG, "getCpos: before"+getCpos());
//            Log.d(TAG, "getMeshMessageLiveData: " + meshMessage.getMessage());
//            if(meshMessage instanceof GenericOnOffStatus) {
//                final GenericOnOffStatus status = (GenericOnOffStatus) meshMessage;
//                final boolean presentState = status.getPresentState();
//                final Boolean targetOnOff = status.getTargetState();
//                View view = mRecyclerViewElements.findViewHolderForAdapterPosition(getCpos()).itemView;
//                Button mActionOnOff = (Button) view.findViewById(R.id.action_on_off);
//
//                Log.d(TAG, "getCpos: after" + getCpos());
//                if (targetOnOff == null) {
//                    if (presentState) {
//                        //onOffState.setText(R.string.generic_state_on);
//                        mActionOnOff.setText(R.string.action_generic_off);
//                    } else {
//                        //onOffState.setText(R.string.generic_state_off);
//                        mActionOnOff.setText(R.string.action_generic_on);
//                    }
//                    //remainingTime.setVisibility(View.GONE);
//                } else {
//                    if (!targetOnOff) {
//                        //  onOffState.setText(R.string.generic_state_on);
//                        mActionOnOff.setText(R.string.action_generic_off);
//                    } else {
//                        //onOffState.setText(R.string.generic_state_off);
//                        mActionOnOff.setText(R.string.action_generic_on);
//                    }
//                    //remainingTime.setText(getString(R.string.remaining_time, MeshParserUtils.getRemainingTransitionTime(resolution, steps)));
//                    //remainingTime.setVisibility(View.VISIBLE);
//                }
//            }
//        });





    }

//
//
//    protected void updateMeshMessage(final MeshMessage meshMessage) {
////            super.updateMeshMessage(meshMessage);
//      //  if (meshMessage instanceof GenericOnOffStatus) {
//            //hideProgressBar();
//        Log.d(TAG, "getCpos: "+getCpos());
//        Log.d(TAG, "getMeshMessageLiveData: " + meshMessage.getMessage());
//            final GenericOnOffStatus status = (GenericOnOffStatus) meshMessage;
//            final boolean presentState = status.getPresentState();
//            final Boolean targetOnOff = status.getTargetState();
//            View view = mRecyclerViewElements.findViewHolderForAdapterPosition(getCpos()).itemView;
//            Button mActionOnOff = (Button) view.findViewById(R.id.action_on_off);
//
//
//            if (targetOnOff == null) {
//                if (presentState) {
//                    //onOffState.setText(R.string.generic_state_on);
//                    mActionOnOff.setText(R.string.action_generic_off);
//                } else {
//                    //onOffState.setText(R.string.generic_state_off);
//                    mActionOnOff.setText(R.string.action_generic_on);
//                }
//                //remainingTime.setVisibility(View.GONE);
//            } else {
//                if (!targetOnOff) {
//                    //  onOffState.setText(R.string.generic_state_on);
//                    mActionOnOff.setText(R.string.action_generic_off);
//                } else {
//                    //onOffState.setText(R.string.generic_state_off);
//                    mActionOnOff.setText(R.string.action_generic_on);
//                }
//                //remainingTime.setText(getString(R.string.remaining_time, MeshParserUtils.getRemainingTransitionTime(resolution, steps)));
//                //remainingTime.setVisibility(View.VISIBLE);
//            }
//        //}
//
//    }

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


        //public void setModel(MeshModel model) {  this.model = model; }





        //protected  MeshModel model = mModelViewModel.getSelectedModel().getMeshModel();

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

                        for(MeshModel model :  models) {

                            //this will give clicked adapter position
                            final int position = this.getAdapterPosition();
                            final Element element = mElements.get(position);
                            final MeshModel model1 = element.getMeshModels().get(v.getTag());
                        //    mOnItemClickListener.onElementItemClick(mProvisionedMeshNode, element, model1);
                        if(model instanceof GenericOnOffServerModel){
                                Log.d("inflate", "getElementAddress: " + AddressUtils.getUnicastAddressInt(mElements.get(this.getAdapterPosition()).getElementAddress())+" holder position: "+this.getAdapterPosition()+ "size: "+model.getModelName());
                         //mViewModel.setModel(mProvisionedMeshNode, AddressUtils.getUnicastAddressInt(mElements.get(this.getAdapterPosition()).getElementAddress()),model.getModelId());
                         mViewModel.setSelectedModel(model1);
                         mViewModel.setSelectedElement(element);

                        if (mActionOnOff.getText().toString().equals("ON")) {
                            Log.d(TAG, "getAdapterPosition: "+ this.getAdapterPosition() +" from click" );
                            setCpos(getAdapterPosition());
                           // nodeUiActivity.sendGenericOnOff(true,0);
                            Log.d(TAG, "sendGenericOnOff: "+model.getModelName()+"ELement: " + element.getElementAddress());
                            if (!model.getBoundAppKeyIndexes().isEmpty()) {
                                final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                                final byte[] appKey = model.getBoundAppKey(appKeyIndex).getKey();
                                if (!model.getSubscriptionAddresses().isEmpty()) {
                                    for (byte[] address : model.getSubscriptionAddresses()) {
                                        final MeshMessage message;
                                        Log.d(TAG, "Subscription addresses found for model: " + CompositionDataParser.formatModelIdentifier(model.getModelId(), true)
                                                + ". Sending acknowledged message to subscription address: " + MeshParserUtils.bytesToHex(address, true));
                                        message = new GenericOnOffSet(appKey, true, 0,mModelViewModel.getSelectedMeshNode().getMeshNode().getReceivedSequenceNumber(), 0, 0);
                                        mModelViewModel.getMeshManagerApi().sendMeshMessage(address, message);
                                                        //showProgressbar();
                                    }
                                } else {
                                    final byte[] address = element.getElementAddress();
                                    Log.d(TAG, "No subscription addresses found for model: " + CompositionDataParser.formatModelIdentifier(model.getModelId(), true)
                                            + ". Sending message to element's unicast address: " + MeshParserUtils.bytesToHex(address, true));
                                    final GenericOnOffSet genericOnOffSet = new GenericOnOffSet(appKey, true, mModelViewModel.getSelectedMeshNode().getMeshNode().getReceivedSequenceNumber(), 0, 0, 0);
                                    mModelViewModel.getMeshManagerApi().sendMeshMessage(address, genericOnOffSet);
                                }
                            } else {
                                //Toast.makeText(this, R.string.error_no_app_keys_bound, Toast.LENGTH_SHORT).show();
                            }
                            mActionOnOff.setText(R.string.action_generic_off);
                        } else {
                            setCpos(getAdapterPosition());

                            Log.d(TAG, "sendGenericOnOff: "+model.getModelName()+"ELement: " + element.getElementAddress());
                            if (!model.getBoundAppKeyIndexes().isEmpty()) {
                                final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                                final byte[] appKey = model.getBoundAppKey(appKeyIndex).getKey();
                                if (!model.getSubscriptionAddresses().isEmpty()) {
                                    for (byte[] address : model.getSubscriptionAddresses()) {
                                        final MeshMessage message;
                                        Log.d(TAG, "Subscription addresses found for model: " + CompositionDataParser.formatModelIdentifier(model.getModelId(), true)
                                                + ". Sending acknowledged message to subscription address: " + MeshParserUtils.bytesToHex(address, true));
                                        message = new GenericOnOffSet(appKey, false, 0,mModelViewModel.getSelectedMeshNode().getMeshNode().getReceivedSequenceNumber(), 0, 0);
                                        mModelViewModel.getMeshManagerApi().sendMeshMessage(address, message);
                                        //showProgressbar();
                                    }
                                } else {
                                    final byte[] address = element.getElementAddress();
                                    Log.d(TAG, "No subscription addresses found for model: " + CompositionDataParser.formatModelIdentifier(model.getModelId(), true)
                                            + ". Sending message to element's unicast address: " + MeshParserUtils.bytesToHex(address, true));
                                    final GenericOnOffSet genericOnOffSet = new GenericOnOffSet(appKey, false, mModelViewModel.getSelectedMeshNode().getMeshNode().getReceivedSequenceNumber(), 0, 0, 0);
                                    mModelViewModel.getMeshManagerApi().sendMeshMessage(address, genericOnOffSet);
                                }
                            } else {
                                //Toast.makeText(this, R.string.error_no_app_keys_bound, Toast.LENGTH_SHORT).show();
                            }   //mActionOnOff.setText(R.string.action_generic_off);

                            mActionOnOff.setText(R.string.action_generic_on);

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
                   // final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getMeshNode();
                    //gof.sendGenericOnOffGet(node);
                    try {

                        for(MeshModel model :  models) {

                            //this will give clicked adapter position
                            final int position = this.getAdapterPosition();
                            final Element element = mElements.get(position);
                            final MeshModel model1 = element.getMeshModels().get(v.getTag());
                            //    mOnItemClickListener.onElementItemClick(mProvisionedMeshNode, element, model1);
                            if(model instanceof GenericOnOffServerModel){
                                Log.d("inflate", "getElementAddress: " + AddressUtils.getUnicastAddressInt(mElements.get(this.getAdapterPosition()).getElementAddress())+" holder position: "+this.getAdapterPosition()+ "size: "+model.getModelName());
                                //mViewModel.setModel(mProvisionedMeshNode, AddressUtils.getUnicastAddressInt(mElements.get(this.getAdapterPosition()).getElementAddress()),model.getModelId());
                                mViewModel.setSelectedElement(element);
                                mViewModel.setSelectedModel(model1);
                                //gof.sendGenericOnOffGet();
                            }
                        }
                        //uiof.progressBar();
                    } catch (IllegalArgumentException ex) {
                        Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }finally {

                    }




                default:
                    break;
            }

        }
    }

}
