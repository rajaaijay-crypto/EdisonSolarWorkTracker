package com.edisonsolar.businesspro;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends Activity {

    private DBHelper db;
    private LinearLayout root;
    private Project photoProject;
    private Uri cameraUri;

    private final int BLUE = Color.rgb(11,94,215);
    private final int GREEN = Color.rgb(25,135,84);
    private final int ORANGE = Color.rgb(240,138,36);
    private final int PURPLE = Color.rgb(111,66,193);
    private final int RED = Color.rgb(220,53,69);
    private final int DARK = Color.rgb(23,50,77);
    private final int BG = Color.rgb(245,249,253);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.WHITE);

        try {
            db = new DBHelper(this);
            home();
        } catch (Throwable e) {
            showCrash("ON CREATE / HOME", e);
        }
    }

    // =========================================================
    // CRASH SCREEN
    // =========================================================

    private void showCrash(String place, Throwable e) {

        StringBuilder error = new StringBuilder();

        error.append("EDISON SOLAR MANAGER PRO\n\n");
        error.append("CRASH LOCATION:\n");
        error.append(place);
        error.append("\n\nERROR:\n");
        error.append(e.toString());

        Throwable cause = e.getCause();

        if (cause != null) {
            error.append("\n\nCAUSE:\n");
            error.append(cause.toString());
        }

        error.append("\n\nSTACK TRACE:\n");

        for (StackTraceElement item : e.getStackTrace()) {
            error.append(item.toString());
            error.append("\n");
        }

        TextView tv = new TextView(this);
        tv.setText(error.toString());
        tv.setTextSize(14);
        tv.setTextColor(Color.RED);
        tv.setPadding(24,24,24,24);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(tv);

        setContentView(scroll);
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private TextView text(String s, float size) {

        TextView t = new TextView(this);

        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(DARK);
        t.setPadding(16,12,16,12);

        return t;
    }

    private EditText field(String hint, String value) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setText(value);
        e.setTextSize(15);
        e.setPadding(12,8,12,8);

        return e;
    }

    private LinearLayout box() {

        LinearLayout l = new LinearLayout(this);

        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(20,10,20,10);

        return l;
    }

    private Button button(
            String s,
            int color,
            View.OnClickListener listener) {

        Button b = new Button(this);

        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setOnClickListener(listener);

        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();

        bg.setColor(color);
        bg.setCornerRadius(18);

        b.setBackground(bg);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1,54);

        params.setMargins(0,6,0,6);

        b.setLayoutParams(params);

        return b;
    }

    private TextView card(String s) {

        TextView t = text(s,15);

        t.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1,-2);

        params.setMargins(0,5,0,5);

        t.setLayoutParams(params);

        return t;
    }

    private void page(String title) {

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16,16,16,30);
        root.setBackgroundColor(BG);

        scroll.addView(root);

        setContentView(scroll);

        LinearLayout bar = new LinearLayout(this);

        bar.setGravity(Gravity.CENTER_VERTICAL);

        Button back = new Button(this);

        back.setText("‹");
        back.setTextSize(28);
        back.setAllCaps(false);

        back.setOnClickListener(v -> home());

        bar.addView(
                back,
                new LinearLayout.LayoutParams(55,55)
        );

        TextView titleView = text(title,21);

        titleView.setTypeface(null,1);

        bar.addView(
                titleView,
                new LinearLayout.LayoutParams(0,55,1)
        );

        root.addView(bar);
    }

    private void addLogo() {

        ImageView image = new ImageView(this);

        int id = getResources().getIdentifier(
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

        root.addView(
                image,
                new LinearLayout.LayoutParams(-1,135)
        );
    }

    private String today() {

        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());
    }

    private double number(EditText e) {

        try {
            return Double.parseDouble(
                    e.getText().toString().trim()
            );
        } catch (Exception ex) {
            return 0;
        }
    }

    // =========================================================
    // HOME
    // =========================================================

    private void home() {

        try {

            page("EDISON SOLAR MANAGER PRO");

            addLogo();

            root.addView(
                    card(db.dashboard())
            );

            root.addView(
                    button(
                            "🏢 Companies",
                            BLUE,
                            v -> companies()
                    )
            );

            root.addView(
                    button(
                            "☀️ Projects",
                            BLUE,
                            v -> projects()
                    )
            );

            root.addView(
                    button(
                            "💰 Payment Collection",
                            GREEN,
                            v -> payments()
                    )
            );

            root.addView(
                    button(
                            "🧾 Expenses",
                            ORANGE,
                            v -> expenses()
                    )
            );

            root.addView(
                    button(
                            "📅 Calendar",
                            PURPLE,
                            v -> calendar()
                    )
            );

            root.addView(
                    button(
                            "📊 Reports / PDF",
                            PURPLE,
                            v -> reports()
                    )
            );

            root.addView(
                    button(
                            "📱 WhatsApp Report",
                            GREEN,
                            v -> share(db.dashboard())
                    )
            );

        } catch (Throwable e) {

            showCrash("HOME", e);
        }
    }

    // =========================================================
    // COMPANIES
    // =========================================================

    private void companies() {

        try {

            page("Companies");

            root.addView(
                    button(
                            "+ Add Company",
                            BLUE,
                            v -> companyDialog(null)
                    )
            );

            for (Company c : db.companies()) {

                root.addView(
                        card(
                                c.name +
                                "\nPhone: " +
                                c.phone +
                                "\nProjects: " +
                                db.countProjects(c.id) +
                                " | kW: " +
                                DBHelper.fmt(
                                        db.companyKw(c.id)
                                ) +
                                "\nCollection: ₹" +
                                DBHelper.fmt(
                                        db.companyCollection(c.id)
                                ) +
                                "\nExpenses: ₹" +
                                DBHelper.fmt(
                                        db.companyExpenses(c.id)
                                ) +
                                "\nPending: ₹" +
                                DBHelper.fmt(
                                        db.companyValue(c.id) -
                                        db.companyCollection(c.id)
                                ) +
                                "\nProfit: ₹" +
                                DBHelper.fmt(
                                        db.companyValue(c.id) -
                                        db.companyExpenses(c.id)
                                )
                        )
                );

                LinearLayout row =
                        new LinearLayout(this);

                row.addView(
                        button(
                                "Edit",
                                BLUE,
                                v -> companyDialog(c)
                        ),
                        new LinearLayout.LayoutParams(
                                0,52,1
                        )
                );

                row.addView(
                        button(
                                "Delete",
                                RED,
                                v -> confirm(
                                        "Delete company?",
                                        () -> {
                                            db.deleteCompany(c.id);
                                            companies();
                                        }
                                )
                        ),
                        new LinearLayout.LayoutParams(
                                0,52,1
                        )
                );

                root.addView(row);
            }

        } catch (Throwable e) {

            showCrash("COMPANIES", e);
        }
    }

    private void companyDialog(Company old) {

        LinearLayout layout = box();

        EditText name = field(
                "Company Name",
                old == null ? "" : old.name
        );

        EditText phone = field(
                "Phone",
                old == null ? "" : old.phone
        );

        layout.addView(name);
        layout.addView(phone);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                old == null
                                        ? "Add Company"
                                        : "Edit Company"
                        )
                        .setView(layout)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save",
                                null
                        )
                        .create();

        dialog.setOnShowListener(v -> {

            dialog.getButton(-1)
                    .setOnClickListener(view -> {

                        String companyName =
                                name
