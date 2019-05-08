package com.example.eventdayfinal.Utils;

import android.app.Activity;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

import com.example.eventdayfinal.R;

public class CustomSnackbar {

    private CoordinatorLayout snackbarPlacer;
    private Snackbar snackbar;
    private Activity activity;

    public CustomSnackbar(Activity activity) {
        this.activity = activity;
        snackbarPlacer = activity.findViewById(R.id.viewSnack);
    }

    public CustomSnackbar make(int textId, int duration, int colorId) {
        this.snackbar = Snackbar.make(this.snackbarPlacer, textId,  duration);
        this.snackbar.getView().setBackgroundColor(ContextCompat.getColor(this.activity, colorId));
        return this;
    }

    public CustomSnackbar makeDefault(String text) {
        this.snackbar = Snackbar.make(this.snackbarPlacer, text, Snackbar.LENGTH_LONG);
        return this;
    }

    public CustomSnackbar makeDefault(int textId) {
        this.snackbar = Snackbar.make(this.snackbarPlacer, textId, Snackbar.LENGTH_LONG);
        return this;
    }


    public void show() {
        this.snackbar.show();
    }

}
