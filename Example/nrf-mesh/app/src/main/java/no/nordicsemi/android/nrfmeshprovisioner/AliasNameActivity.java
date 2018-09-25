package no.nordicsemi.android.nrfmeshprovisioner;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ManageAppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ManageAppKeysViewModel;

public class AliasNameActivity extends AppCompatActivity  {

    public static final String ALIAS_KEYS = "ALIAS_KEYS";
    public static final int MANAGE_ALIAS_NAMES = 2014; //Random number

    private static final String CALLING_ACTIVITY = ".MainActivity";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    //UI Bindings
    @BindView(android.R.id.empty)
    View mEmptyView;
    @BindView(R.id.container)
    View container;

    private ManageAppKeysViewModel mViewModel;
    private ManageAppKeyAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alias_name);
//        Intent intent = getIntent();
//        HashMap<String, String> nodes = (HashMap<String, String>)intent.getSerializableExtra(AliasNameActivity.ALIAS_KEYS);
//        nodes.get("nodelist");
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("result",nodes);
//        setResult(Activity.RESULT_OK, returnIntent);
//        finish();


    }
}