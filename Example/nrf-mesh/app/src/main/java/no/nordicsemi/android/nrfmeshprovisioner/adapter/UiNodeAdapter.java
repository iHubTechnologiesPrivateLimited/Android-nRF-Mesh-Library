package no.nordicsemi.android.nrfmeshprovisioner.adapter;

import android.support.v4.app.FragmentActivity;
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

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.CompanyIdentifiers;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisionedNodesLiveData;

public class UiNodeAdapter  extends RecyclerView.Adapter<UiNodeAdapter.ViewHolder>{

        private final FragmentActivity mContext;
        private final List<ProvisionedMeshNode> mNodes = new ArrayList<>();
        private UiNodeAdapter.OnItemClickListener mOnItemClickListener;

    public UiNodeAdapter(final FragmentActivity fragmentActivity, final ProvisionedNodesLiveData provisionedNodesLiveData) {
            this.mContext = fragmentActivity;
            Log.d("UI", "UiNodeAdapter: "+fragmentActivity);
            provisionedNodesLiveData.observe(fragmentActivity, provisionedNodesLiveData1 -> {
                final Map<Integer, ProvisionedMeshNode> nodes = provisionedNodesLiveData1.getProvisionedNodes();
                if(nodes != null){
                    mNodes.clear();
                    mNodes.addAll(nodes.values());
                }
            });

        }

        public void setOnItemClickListener(final UiNodeAdapter.OnItemClickListener listener) {
            mOnItemClickListener = listener;
        }

        @Override
        public UiNodeAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            Log.d("UI:", "mContext: " +mContext);
            final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.ui_network_item, parent, false);
            return new UiNodeAdapter.ViewHolder(layoutView);
        }

        @Override
        public void onBindViewHolder(final UiNodeAdapter.ViewHolder holder, final int position) {
            final ProvisionedMeshNode node = mNodes.get(position);
            holder.configure.setText(node.getNodeName());
            //holder.unicastAddress.setText(MeshParserUtils.bytesToHex(node.getUnicastAddress(), false));
            final Map<Integer, Element> elements = node.getElements();
//            if(elements != null && !elements.isEmpty()) {
//                holder.notConfiguredView.setVisibility(View.GONE);
//                holder.nodeInfoContainer.setVisibility(View.VISIBLE);
//                holder.companyIdentifier.setText(CompanyIdentifiers.getCompanyName(node.getCompanyIdentifier().shortValue()));
//                holder.elements.setText(String.valueOf(elements.size()));
//                holder.models.setText(String.valueOf(getModels(elements)));
//            } else {
//                holder.companyIdentifier.setText(R.string.unknown);
//                holder.elements.setText(String.valueOf(node.getNumberOfElements()));
//                holder.models.setText(R.string.unknown);
//            }
        }

        @Override
        public int getItemCount() {
            return mNodes.size();
        }

        public boolean isEmpty() {
            return getItemCount() == 0;
        }

        private int getModels(final Map<Integer, Element> elements){
            int models = 0;
            for(Element element : elements.values()) {
                models += element.getMeshModels().size();
            }
            return models;
        }

        public interface OnItemClickListener {
            void onConfigureClicked(final ProvisionedMeshNode node);
            void onDetailsClicked(final ProvisionedMeshNode node);
        }

        final class ViewHolder extends RecyclerView.ViewHolder{

//            @BindView(R.id.node_name)
//            TextView name;
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

//                provisionedView.findViewById(R.id.action_details).setOnClickListener(v -> {
//
//                    if (mOnItemClickListener != null) {
//                        mOnItemClickListener.onDetailsClicked(mNodes.get(getAdapterPosition()));
//                    }
//                });
            }
        }
}
