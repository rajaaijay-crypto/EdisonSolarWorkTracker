package com.edisonsolar.businesspro;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
    static final int GALLERY = 102;
    static final int CAPTURE = 103;

    @Override
    public void onCreate(Bundle b) {
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

    void base(String title) {

        ScrollView sv = new ScrollView(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        root.setPadding(16, 16, 16, 30);
        root.setBackgroundColor(bg);

        sv.addView(root);

        setContentView(sv);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        Button back = new Button(this);
        back.setText("‹");
        back.setTextSize(28);

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

        root.addView(
                im,
                new LinearLayout.LayoutParams(-1, 135)
        );
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

        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(18);

        b.setBackground(g);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, 54);

        p.setMargins(0, 6, 0, 6);

        b.setLayoutParams(p);

        return b;
    }

    TextView card(String s) {

        TextView t = tv(s, 15);

        t.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);

        p.setMargins(0, 5, 0, 5);

        t.setLayoutParams(p);

        return t;
    }

    EditText field(String hint, String value) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setText(value);

        e.setPadding(12, 8, 12, 8);

        return e;
    }

    String today() {

        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());
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

        for (Company c : db.companies()) {

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

            LinearLayout row =
                    new LinearLayout(this);

            row.addView(
                    btn(
                            "Edit",
                            blue,
                            v -> companyDialog(c)
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            52,
                            1
                    )
            );

            row.addView(
                    btn(
                            "Delete",
                            red,
                            v -> confirm(
                                    "Delete company?",
                                    () -> {
                                        db.deleteCompany(c.id);
                                        companies();
                                    }
                            )
                    ),
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
                            v -> shareText(
                                    companyReport(c)
                            )
                    )
            );
        }
    }

    String companyReport(Company c) {

        StringBuilder s =
                new StringBuilder(
                        c.name +
                        "\nPhone: " +
                        c.phone +
                        "\n\n"
                );

        for (Project p : db.projects()) {

            if (p.companyId == c.id) {

                s.append(
                        p.number +
                        " | " +
                        p.customer +
                        " | " +
                        p.kw +
                        " kW\n"
                );

                s.append(
                        "₹" +
                        p.amount +
                        " | Collection ₹" +
                        db.collection(p.id) +
                        " | Pending ₹" +
                        db.pending(p.id) +
                        " | Expenses ₹" +
                        db.expenses(p.id) +
                        " | Profit ₹" +
                        db.profit(p.id) +
                        "\n"
                );

                s.append(
                        p.site +
                        "\n\n"
                );
            }
        }

        return s.toString();
    }

    void companyDialog(Company old) {

        LinearLayout l = vertical();

        EditText n =
                field(
                        "Company Name",
                        old == null ? "" : old.name
                );

        EditText p =
                field(
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
                d.getButton(-1).setOnClickListener(v -> {

                    if (
                            n.getText()
                                    .toString()
                                    .trim()
                                    .isEmpty()
                    ) {

                        n.setError("Required");
                        return;
                    }

                    if (old == null) {

                        db.addCompany(
                                n.getText().toString(),
                                p.getText().toString()
                        );

                    } else {

                        db.updateCompany(
                                old.id,
                                n.getText().toString(),
                                p.getText().toString()
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

                            "\nSite: " +
                            p.site +

                            "\nAmount: ₹" +
                            DBHelper.fmt(p.amount) +

                            " | Collection: ₹" +
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

                            " | Site Profit: ₹" +
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

                            if (w == 0) {

                                projectDialog(p);

                            } else if (w == 1) {

                                projectPayments(p);

                            } else if (w == 2) {

                                projectExpenses(p);

                            } else if (w == 3) {

                                photos(p);

                            } else if (w == 4) {

                                gpsMap(p);

                            } else if (w == 5) {

                                createPdf(p);

                            } else if (w == 6) {

                                shareText(
                                        projectReport(p)
                                );

                            } else {

                                confirm(
                                        "Delete this project and its records?",
                                        () -> {

                                            db.deleteProject(p.id);

                                            projects();
                                        }
                                );
                            }
                        }
                )
                .show();
    }

    String projectReport(Project p) {

        return
                "EDISON SOLAR MANAGER PRO\n" +

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
                DBHelper.fmt(
                        db.collection(p.id)
                ) +

                "\nPending: ₹" +
                DBHelper.fmt(
                        db.pending(p.id)
                ) +

                "\nExpenses: ₹" +
                DBHelper.fmt(
                        db.expenses(p.id)
                ) +

                "\nSite Profit: ₹" +
                DBHelper.fmt(
                        db.profit(p.id)
                ) +

                "\nDate: " +
                p.date +

                "\nStatus: " +
                p.status +

                "\nWork: " +
                p.work;
    }

    void projectDialog(Project old) {

        ArrayList<Company> cs =
                db.companies();

        if (cs.isEmpty()) {

            Toast.makeText(
                    this,
                    "Add company first",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        LinearLayout l = vertical();

        Spinner sp = new Spinner(this);

        ArrayList<String> names =
                new ArrayList<>();

        for (Company c : cs) {
            names.add(c.name);
        }

        sp.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                )
        );

        if (old != null) {

            for (int i = 0; i < cs.size(); i++) {

                if (cs.get(i).id == old.companyId) {

                    sp.setSelection(i);
                }
            }
        }

        EditText cu =
                field(
                        "Customer Name",
                        old == null
                                ? ""
                                : old.customer
                );

        EditText ph =
                field(
                        "Customer Phone",
                        old == null
                                ? ""
                                : old.phone
                );

        EditText site =
                field(
                        "Site Address",
                        old == null
                                ? ""
                                : old.site
                );

        EditText kw =
                field(
                        "Solar kW",
                        old == null
                                ? ""
                                : "" + old.kw
                );

        EditText amt =
                field(
                        "Project Amount",
                        old == null
                                ? ""
                                : "" + old.amount
                );

        EditText date =
                field(
                        "Installation Date",
                        old == null
                                ? today()
                                : old.date
                );

        EditText work =
                field(
                        "Site Work Detail",
                        old == null
                                ? ""
                                : old.work
                );

        kw.setInputType(2);
        amt.setInputType(2);

        RadioGroup rg =
                new RadioGroup(this);

        RadioButton a =
                new RadioButton(this);

        a.setText("Pending");

        RadioButton b =
                new RadioButton(this);

        b.setText("Work Going On");

        rg.addView(a);
        rg.addView(b);

        if (
                old != null &&
                "Work Going On".equals(old.status)
        ) {

            b.setChecked(true);

        } else {

            a.setChecked(true);
        }

        l.addView(sp);
        l.addView(cu);
        l.addView(ph);
        l.addView(site);
        l.addView(kw);
        l.addView(amt);
        l.addView(date);
        l.addView(rg);
        l.addView(work);

        Button dateBtn =
                new Button(this);

        dateBtn.setText(
                "📅 Choose Installation Date"
        );

        dateBtn.setOnClickListener(
                v -> datePicker(date)
        );

        l.addView(dateBtn);

        AlertDialog d =
                new AlertDialog.Builder(this)

                        .setTitle(
                                old == null
                                        ? "New Project"
                                        : "Edit Project"
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
                d.getButton(-1).setOnClickListener(v -> {

                    Company c =
                            cs.get(
                                    sp.getSelectedItemPosition()
                            );

                    double k = num(kw);

                    double money = num(amt);

                    if (
                            cu.getText()
                                    .toString()
                                    .trim()
                                    .isEmpty()
                    ) {

                        cu.setError("Required");
                        return;
                    }

                    String status =
                            b.isChecked()
                                    ? "Work Going On"
                                    : "Pending";

                    if (old == null) {

                        db.addProject(
                                c.id,
                                c.name,
                                cu.getText().toString(),
                                ph.getText().toString(),
                                site.getText().toString(),
                                k,
                                money,
                                date.getText().toString(),
                                status,
                                work.getText().toString()
                        );

                    } else {

                        db.updateProject(
                                old,
                                c.id,
                                c.name,
                                cu.getText().toString(),
                                ph.getText().toString(),
                               
