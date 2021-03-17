package com.farid.applockersample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MainOverlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_overlay);

        TextView btnClose = findViewById(R.id.btnClose);
        TextView quotes = findViewById(R.id.quotes);

        int min = 1;
        int max = 30;
        int randomNum = 0;
        Random random = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        } else {
            randomNum = random.nextInt((max - min) + 1) + min;
        }
//        Log.e("RANDOMMMM", randomNum + "");

        quotes.setText(Utils.readSharedSetting(this, String.valueOf(randomNum), "A Crocodile cannot stick its tongue out"));

        btnClose.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
