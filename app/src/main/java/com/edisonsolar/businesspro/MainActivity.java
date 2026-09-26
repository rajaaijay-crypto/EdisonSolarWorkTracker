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

    DBHelper db;
    LinearLayout root;

    int blue = Color.rgb(11, 94, 215);
    int green = Color.rgb(25, 135, 84);
    int orange = Color.rgb(240, 138, 36);
    int purple = Color.rgb(111, 66, 193);
    int red = Color.rgb(220, 53, 69);
    int dark = Color.rgb(23, 50, 77);
    int bg = Color.rgb(245, 249, 253);

    Project currentProjectForPhoto;
    Uri cameraUri;

    static final int LOC = 100;
    static final int CAMERA = 101;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        db = new DBHelper(this);

        getWindow().setStatusBarColor(Color.WHITE);

        showHome();
    }

    TextView tv(String s, float size) {
        TextView t = new TextView(this);

        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(dark);
        t.setPadding(16, 12, 16, 12);

        return t;
    }

    LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(20, 10, 20, 10);
        return l;
    }

    void base(String title) {

        ScrollView sv = new ScrollView(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 30);
        root.setBackgroundColor(bg);

        sv.addView(root);

        setContentView(sv);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        Button back = new Button(this);
        back.setText("‹");
        back.setTextSize(28);
        back.setAllCaps(false);

        back.setOnClickListener(v -> showHome());

        bar.addView(
                back,
                new LinearLayout.LayoutParams(55, 55)
        );

        TextView tt = tv(title, 21);
        tt.setTypeface(null, 1);

        bar.addView(
                tt,
                new LinearLayout.LayoutParams(0, 55, 1)
        );

        root.addView(bar);
    }

    void logo() {

        ImageView im = new ImageView(this);

        int id = getResources().getIdentifier(
                "edison_solar_logo",
                "drawable",
                getPackageName()
        );

        if (id != 0) {
            im.setImageResource(id);
        }

        im.setAdjustViewBounds(true);
        im.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        135
                );

        p.setMargins(0, 5, 0, 10);

        root.addView(im, p);
    }

    Button btn(
            String s,
            int color,
            View.OnClickListener l
    ) {

        Button b = new Button(this);

        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);

        b.setOnClickListener(l);

        android.graphics.drawable.GradientDrawable g =
                new android.graphics.drawable.GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(18);

        b.setBackground(g);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        54
                );

        p.setMargins(0, 6, 0, 6);

        b.setLayoutParams(p);

        return b;
    }

    TextView card(String s) {

        TextView t = tv(s, 15);

        t.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        p.setMargins(0, 5, 0, 5);

        t.setLayoutParams(p);

        return t;
    }

    EditText field(String hint, String value) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setText(value);
        e.setTextSize(15);
        e.setPadding(12, 8, 12, 8);

        return e;
    }

    String today() {

        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());
    }

    double num(EditText e) {

        try {
            return Double.parseDouble(
                    e.getText()
                            .toString()
                            .trim()
            );
        } catch (Exception ex) {
            return 0;
        }
    }

    void showHome() {

        base("EDISON SOLAR MANAGER PRO");

        logo();

        root.addView(card(db.dashboard()));

        root.addView(
                btn(
                        "🏢 Companies",
                        blue,
                        v -> companies()
                )
        );

        root.addView(
                btn(
                        "☀️ Projects",
                        blue,
                        v -> projects()
                )
        );

        root.addView(
                btn(
                        "💰 Payment Collection",
                        green,
                        v -> payments()
                )
        );

        root.addView(
                btn(
                        "🧾 Expenses",
                        orange,
                        v -> expenses()
                )
        );

        root.addView(
                btn(
                        "📅 Calendar",
                        purple,
                        v -> calendar()
                )
        );

        root.addView(
                btn(
                        "📊 Reports / PDF",
                        purple,
                        v -> reports()
                )
        );

        root.addView(
                btn(
                        "📱 WhatsApp Report",
                        green,
                        v -> shareText(db.dashboard())
                )
        );
    }

    void companies() {

        base("Companies");

        root.addView(
                btn(
                        "+ Add Company",
                        blue,
                        v -> companyDialog(null)
                )
        );

        ArrayList<Company> list = db.companies();

        for (Company c : list) {

            String s =
                    c.name +
                    "\nPhone: " +
                    c.phone +
                    "\nProjects: " +
                    db.countProjects(c.id) +
                    " | kW: " +
                    DBHelper.fmt(db.companyKw(c.id)) +
                    "\nCollection: ₹" +
                    DBHelper.fmt(
                            db.companyCollection(c.id)
                    ) +
                    " | Expenses: ₹" +
                    DBHelper.fmt(
                            db.companyExpenses(c.id)
                    ) +
                    "\nPending: ₹" +
                    DBHelper.fmt(
                            db.companyValue(c.id)
                                    - db.companyCollection(c.id)
                    ) +
                    " | Profit: ₹" +
                    DBHelper.fmt(
                            db.companyValue(c.id)
                                    - db.companyExpenses(c.id)
                    );

            root.addView(card(s));

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            Button edit = btn(
                    "Edit",
                    blue,
                    v -> companyDialog(c)
            );

            Button del = btn(
                    "Delete",
                    red,
                    v -> confirm(
                            "Delete company?",
                            () -> {
                                db.deleteCompany(c.id);
                                companies();
                            }
                    )
            );

            row.addView(
                    edit,
                    new LinearLayout.LayoutParams(
                            0,
                            52,
                            1
                    )
            );

            row.addView(
                    del,
                    new LinearLayout.LayoutParams(
                            0,
                            52,
                            1
                    )
            );

            root.addView(row);

            root.addView(
                    btn(
                            "Company Report / WhatsApp",
                            purple,
                            v -> shareText(companyReport(c))
                    )
            );
        }
    }

    String companyReport(Company c) {

        StringBuilder s =
                new StringBuilder();

        s.append("EDISON SOLAR MANAGER PRO\n\n");

        s.append("COMPANY: ")
                .append(c.name)
                .append("\n");

        s.append("PHONE: ")
                .append(c.phone)
                .append("\n\n");

        for (Project p : db.projects()) {

            if (p.companyId == c.id) {

                s.append("------------------------\n");

                s.append(p.number)
                        .append(" | ")
                        .append(p.customer)
                        .append("\n");

                s.append("Solar: ")
                        .append(p.kw)
                        .append(" kW\n");

                s.append("Amount: ₹")
                        .append(DBHelper.fmt(p.amount))
                        .append("\n");

                s.append("Collection: ₹")
                        .append(DBHelper.fmt(
                                db.collection(p.id)
                        ))
                        .append("\n");

                s.append("Pending: ₹")
                        .append(DBHelper.fmt(
                                db.pending(p.id)
                        ))
                        .append("\n");

                s.append("Expenses: ₹")
                        .append(DBHelper.fmt(
                                db.expenses(p.id)
                        ))
                        .append("\n");

                s.append("Profit: ₹")
                        .append(DBHelper.fmt(
                                db.profit(p.id)
                        ))
                        .append("\n");

                s.append("Site: ")
                        .append(p.site)
                        .append("\n\n");
            }
        }

        return s.toString();
    }

    void companyDialog(Company old) {

        LinearLayout l = vertical();

        EditText n = field(
                "Company Name",
                old == null ? "" : old.name
        );

        EditText p = field(
                "Phone",
                old == null ? "" : old.phone
        );

        l.addView(n);
        l.addView(p);

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle(
                                old == null
                                        ? "Add Company"
                                        : "Edit Company"
                        )
                        .setView(l)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save",
                                null
                        )
                        .create();

        d.setOnShowListener(x ->
                d.getButton(-1)
                        .setOnClickListener(v -> {

                            String name =
                                    n.getText()
                                            .toString()
                                            .trim();

                            if (name.isEmpty()) {
                                n.setError("Required");
                                return;
                            }

                            if (old == null) {

                                db.addCompany(
                                        name,
                                        p.getText()
                                                .toString()
                                );

                            } else {

                                db.updateCompany(
                                        old.id,
                                        name,
                                        p.getText()
                                                .toString()
                                );
                            }

                            d.dismiss();

                            companies();
                        })
        );

        d.show();
    }

    void projects() {

        base("Projects");

        root.addView(
                btn(
                        "+ Add Project",
                        blue,
                        v -> projectDialog(null)
                )
        );

        for (Project p : db.projects()) {

            root.addView(
                    card(
                            p.number +
                            " • " +
                            p.company +
                            "\nCustomer: " +
                            p.customer +
                            " | " +
                            p.kw +
                            " kW" +
                            "\nPhone: " +
                            p.phone +
                            "\nSite: " +
                            p.site +
                            "\nAmount: ₹" +
                            DBHelper.fmt(p.amount) +
                            "\nCollection: ₹" +
                            DBHelper.fmt(
                                    db.collection(p.id)
                            ) +
                            " | Pending: ₹" +
                            DBHelper.fmt(
                                    db.pending(p.id)
                            ) +
                            "\nExpenses: ₹" +
                            DBHelper.fmt(
                                    db.expenses(p.id)
                            ) +
                            " | Profit: ₹" +
                            DBHelper.fmt(
                                    db.profit(p.id)
                            ) +
                            "\n" +
                            p.date +
                            " • " +
                            p.status
                    )
            );

            root.addView(
                    btn(
                            "Open Project",
                            blue,
                            v -> projectMenu(p)
                    )
            );
        }
    }

    void projectMenu(Project p) {

        new AlertDialog.Builder(this)
                .setTitle(
                        p.number +
                        " • " +
                        p.customer
                )
                .setItems(
                        new String[]{
                                "Edit Project",
                                "Payments",
                                "Expenses",
                                "Photos / Gallery",
                                "GPS / Google Map",
                                "PDF Report",
                                "WhatsApp Report",
                                "Delete Project"
                        },
                        (d, w) -> {

                            switch (w) {

                                case 0:
                                    projectDialog(p);
                                    break;

                                case 1:
                                    projectPayments(p);
                                    break;

                                case 2:
                                    projectExpenses(p);
                                    break;

                                case 3:
                                    photos(p);
                                    break;

                                case 4:
                                    gpsMap(p);
                                    break;

                                case 5:
                                    createPdf(p);
                                    break;

                                case 6:
                                    shareText(
                                            projectReport(p)
                                    );
                                    break;

                                case 7:
                                    confirm(
                                            "Delete this project and its records?",
                                            () -> {
                                                db.deleteProject(p.id);
                                                projects();
                                            }
                                    );
                                    break;
                            }
                        }
                )
                .show();
    }

    String projectReport(Project p) {

        return
                "EDISON SOLAR MANAGER PRO\n\n" +

                "PROJECT: " +
                p.number +

                "\nCompany: " +
                p.company +

                "\nCustomer: " +
                p.customer +

                "\nPhone: " +
                p.phone +

                "\nSite: " +
                p.site +

                "\nSolar: " +
                p.kw +
                " kW" +

                "\nProject Amount: ₹" +
                DBHelper.fmt(p.amount) +

                "\nCollection: ₹" +
                DB
