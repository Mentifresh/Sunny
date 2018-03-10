package com.danielkilders.sunny;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

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
