package com.farid.applockersample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDataQuotes();
        checkOverlayPermission();

        if (!checkPermission()) {
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            }
            startActivity(intent);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, new Intent(this, AppLockService.class));
            } else {
                this.startService(new Intent(this, AppLockService.class));
            }
        }
    }


    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this);
        }
    }

    private boolean checkPermission() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = null;
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            } else {
                return true;
            }
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void setDataQuotes() {
        Utils.saveSharedSetting(this, "1", "It is impossible for most people to lick their own elbow. (try it!)");
        Utils.saveSharedSetting(this, "2", "A crocodile cannot stick its tongue out.");
        Utils.saveSharedSetting(this, "3", "A shrimp's heart is in its head.");
        Utils.saveSharedSetting(this, "4", "It is physically impossible for pigs to look up into the sky.");
        Utils.saveSharedSetting(this, "5", "The \"sixth sick sheik's sixth sheep's sick\" is believed to be the toughest tongue twister in the English language.");
        Utils.saveSharedSetting(this, "6", "If you sneeze too hard, you could fracture a rib.");
        Utils.saveSharedSetting(this, "7", "Wearing headphones for just an hour could increase the bacteria in your ear by 700 times.");
        Utils.saveSharedSetting(this, "8", "In the course of an average lifetime, while sleeping you might eat around 70 assorted insects and 10 spiders, or more.");
        Utils.saveSharedSetting(this, "9", "Some lipsticks contain fish scales.");
        Utils.saveSharedSetting(this, "10", "Cat urine glows under a black-light.");
        Utils.saveSharedSetting(this, "11", "Like fingerprints, everyone's tongue print is different.");
        Utils.saveSharedSetting(this, "12", "Rubber bands last longer when refrigerated.");
        Utils.saveSharedSetting(this, "13", "There are 293 ways to make change for a dollar.");
        Utils.saveSharedSetting(this, "14", "The average person's left hand does 56% of the typing (when using the proper position of the hands on the keyboard; Hunting and pecking doesn't count!).");
        Utils.saveSharedSetting(this, "15", "A shark is the only known fish that can blink with both eyes.");
        Utils.saveSharedSetting(this, "16", "The longest one-syllable words in the English language are \"scraunched\" and \"strengthed.\" Some suggest that \"squirreled\" could be included, but squirrel is intended to be pronounced as two syllables (squir-rel) according to most dictionaries. \"Screeched\" and \"strengths\" are two other long one-syllable words, but they only have 9 letters.");
        Utils.saveSharedSetting(this, "17", "\"Dreamt\" is the only English word that ends in the letters \"mt\".");
        Utils.saveSharedSetting(this, "18", "Almonds are a member of the peach family.");
        Utils.saveSharedSetting(this, "19", "Maine is the only state that has a one-syllable name.");
        Utils.saveSharedSetting(this, "20", "There are only four words in the English language which end in \"dous\": tremendous, horrendous, stupendous, and hazardous.");
        Utils.saveSharedSetting(this, "21", "Los Angeles' full name is \"El Pueblo de Nuestra Senora la Reina de los Angeles de Porciuncula\"");
        Utils.saveSharedSetting(this, "22", "A cat has 32 muscles in each ear.");
        Utils.saveSharedSetting(this, "23", "An ostrich's eye is bigger than its brain.");
        Utils.saveSharedSetting(this, "24", "Tigers have striped skin, not just striped fur.");
        Utils.saveSharedSetting(this, "25", "In many advertisements, the time displayed on a watch is 10:10.");
        Utils.saveSharedSetting(this, "26", "The characters Bert and Ernie on Sesame Street were named after Bert the cop and Ernie the taxi driver in Frank Capra's \"It's a Wonderful Life.\"");
        Utils.saveSharedSetting(this, "27", "A dime has 118 ridges around the edge.");
        Utils.saveSharedSetting(this, "28", "The giant squid has the largest eyes in the world.");
        Utils.saveSharedSetting(this, "29", "Most people fall asleep in seven minutes.");
        Utils.saveSharedSetting(this, "30", "\"Stewardesses\" is the longest word that is typed with only the left hand.");
    }
}