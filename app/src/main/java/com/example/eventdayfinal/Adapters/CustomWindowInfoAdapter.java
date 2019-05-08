package com.example.eventdayfinal.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.eventdayfinal.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomWindowInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private TextView title;
    private View view;
    private Context context;

    @Override
    public View getInfoWindow(Marker marker) {
        view = LayoutInflater.from(context).inflate(R.layout.info__event_window, null);
        renderInfo(marker);
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderInfo(marker);
        return view;
    }


    public CustomWindowInfoAdapter(Context mContext) {
        context = mContext;

    }

    private void renderInfo(Marker marker) {
        title = view.findViewById(R.id.info_window_title);

        title.setText(marker.getTitle());
    }
}
