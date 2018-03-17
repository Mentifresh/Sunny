package com.danielkilders.sunny.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.danielkilders.sunny.R;

public class AlertDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.Sorry)
                .setMessage(R.string.error)
                .setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
