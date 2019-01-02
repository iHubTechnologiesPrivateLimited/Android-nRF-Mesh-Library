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

package no.nordicsemi.android.nrfmeshprovisioner.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;


public class DialogFragmentSourceAddress extends DialogFragment {

    private static final String UNICAST_ADDRESS = "UNICAST_ADDRESS";
    //UI Bindings
    @BindView(R.id.text_input_layout)
    TextInputLayout unicastAddressInputLayout;
    @BindView(R.id.text_input)
    TextInputEditText unicastAddressInput;

    private int mUnicastAddress;

    public static DialogFragmentSourceAddress newInstance(final int unicastAddress) {
        DialogFragmentSourceAddress fragmentIvIndex = new DialogFragmentSourceAddress();
        final Bundle args = new Bundle();
        args.putInt(UNICAST_ADDRESS, unicastAddress);
        fragmentIvIndex.setArguments(args);
        return fragmentIvIndex;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUnicastAddress = getArguments().getInt(UNICAST_ADDRESS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_address_input, null);

        //Bind ui
        ButterKnife.bind(this, rootView);

        final KeyListener hexKeyListener = new HexKeyListener();
        final String unicastAddress = String.format(Locale.US, "%04X", mUnicastAddress);
        unicastAddressInputLayout.setHint(getString((R.string.hint_src_address)));
        unicastAddressInput.setText(unicastAddress);
        unicastAddressInput.setSelection(unicastAddress.length());
        unicastAddressInput.setKeyListener(hexKeyListener);
        unicastAddressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    unicastAddressInputLayout.setError(getString(R.string.error_empty_unicast_address));
                } else {
                    unicastAddressInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext()).setView(rootView)
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);

        alertDialogBuilder.setIcon(R.drawable.ic_lan_black_alpha_24dp);
        alertDialogBuilder.setTitle(R.string.title_src_address);
        alertDialogBuilder.setMessage(R.string.dialog_summary_src);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String unicast = unicastAddressInput.getText().toString();
            if (validateInput(unicast)) {
                try {
                    if (getParentFragment() == null) {
                        if(((DialogFragmentSourceAddressListener) getActivity()).setSourceAddress(Integer.parseInt(unicast, 16))){
                            dismiss();
                        }
                    } else {
                        if(((DialogFragmentSourceAddressListener) getParentFragment()).setSourceAddress(Integer.parseInt(unicast, 16))){
                            dismiss();
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    unicastAddressInputLayout.setError(ex.getMessage());
                }
            }
        });

        return alertDialog;
    }

    private boolean validateInput(final String input) {

        try {

            if(input.length() % 4 != 0 || !input.matches(Utils.HEX_PATTERN)) {
                unicastAddressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            if(MeshParserUtils.validateUnicastAddressInput(getContext(), input)) {
                return true;
            }
        } catch (IllegalArgumentException ex) {
            unicastAddressInputLayout.setError(ex.getMessage());
        }

        return false;
    }


    public interface DialogFragmentSourceAddressListener {

        boolean setSourceAddress(final int sourceAddress);

    }
}
