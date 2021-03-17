package com.farid.applockersample;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;

public class PatternOverlayView extends RelativeLayout {

    private LayoutInflater mInflater;
    private Context context;

    public PatternOverlayView(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        this.context = context;
        init();
    }

    private void init() {
        View v = mInflater.inflate(R.layout.view_one_overlay    , this, true);
        RoundedImageView irv = v.findViewById(R.id.icon);
        TextView btnClose = v.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(view -> {

        });

        Glide.with(context).load(new ColorDrawable(ContextCompat.getColor(context, R.color.red))).apply(new RequestOptions().circleCrop()).into(irv);
    }

}
