package com.edisonsolar.businesspro;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DBHelper db;
    private LinearLayout root;

    private final int blue = Color.rgb(11, 94, 215);
    private final int green = Color.rgb(25, 135, 84);
    private final int orange = Color.rgb(240, 138, 36);
    private final int purple = Color.rgb(111, 66, 193);
    private final int red = Color.rgb(220, 53, 69);
    private final int dark = Color.rgb(23, 50, 77);
    private final int bg = Color.rgb(245, 249, 253);

    private Project currentProjectForPhoto;
    private Uri cameraUri;

    private static final int LOC = 100;
    private static final int CAMERA = 101;
    private static final int GALLERY = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);

        try {

            db = new DBHelper(this);

            showHome();

        } catch (Throwable e) {

            String error =
                    e.getClass().getName()
                            + "\n\nMessage:\n"
                            + String.valueOf(e.getMessage());

            new AlertDialog.Builder(this)
                    .setTitle(
                            "EDISON SOLAR MANAGER PRO - ERROR"
                    )
                    .setMessage(error)
                    .setPositiveButton(
                            "OK",
                            null
                    )
                    .show();
        }
    }

    // =========================================================
    // BASIC UI
    // =========================================================

    private TextView tv(
            String text,
            float size
    ) {

        TextView t =
                new TextView(this);

        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(dark);
        t.setPadding(16, 12, 16, 12);

        return t;
    }

    private LinearLayout vertical() {

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.VERTICAL
        );

        l.setPadding(
                20,
                10,
                20,
                10
        );

        return l;
    }

    private void base(String title) {

        ScrollView scroll =
                new ScrollView(this);

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                16,
                16,
                16,
                30
        );

        root.setBackgroundColor(bg);

        scroll.addView(root);

        setContentView(scroll);

        LinearLayout bar =
                new LinearLayout(this);

        bar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        Button back =
                new Button(this);

        back.setText("‹");
        back.setTextSize(28);
        back.setAllCaps(false);

        back.setOnClickListener(
                v -> showHome()
        );

        bar.addView(
                back,
                new LinearLayout.LayoutParams(
                        55,
                        55
                )
        );

        TextView titleView =
                tv(title, 21);

        titleView.setTypeface(
                null,
                1
        );

        bar.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        0,
                        55,
                        1
                )
        );

        root.addView(bar);
    }

    private void logo() {

        ImageView image =
                new ImageView(this);

        int id =
                getResources().getIdentifier(
                        "edison_solar_logo",
                        "drawable",
                        getPackageName()
                );

        if (id != 0) {
            image.setImageResource(id);
        }

        image.setAdjustViewBounds(true);

        image.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        135
                );

        p.setMargins(
                0,
                5,
                0,
                10
        );

        root.addView(
                image,
                p
        );
    }

    private Button btn(
            String text,
            int color,
            View.OnClickListener listener
    ) {

        Button b =
                new Button(this);

        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);

        b.setOnClickListener(listener);

        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(18);

        b.setBackground(drawable);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        54
                );

        p.setMargins(
                0,
                6,
                0,
                6
        );

        b.setLayoutParams(p);

        return b;
    }

    private TextView card(String text) {

        TextView t =
                tv(text, 15);

        t.setBackgroundColor(
                Color.WHITE
