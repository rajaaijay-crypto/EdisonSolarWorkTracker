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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private DBHelper db;
    private LinearLayout root;
    private Project photoProject;
    private Uri cameraUri;

    private static final int BLUE = Color.rgb(11, 94, 215);
    private static final int GREEN = Color.rgb(25, 135, 84);
    private static final int ORANGE = Color.rgb(240, 138, 36);
    private static final int PURPLE = Color.rgb(111, 66, 193);
    private static final int RED = Color.rgb(220, 53, 69);
    private static final int DARK = Color.rgb(23, 50, 77);
    private static final int BG = Color.rgb(245, 249, 253);

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        try {
            db = new DBHelper(this);
            home();
        } catch (Throwable e) {
            crash("STARTUP", e);
        }
    }

    // =========================================================
    // CRASH
    // =========================================================

    private void crash(String where, Throwable e) {

        TextView t = new TextView(this);

        StringBuilder s = new StringBuilder();

        s.append("EDISON SOLAR MANAGER PRO\n\n");
        s.append("ERROR AT: ").append(where).append("\n\n");
        s.append(e.toString()).append("\n\n");

        if (e.getCause() != null) {
            s.append("CAUSE:\n");
            s.append(e.getCause().toString()).append("\n\n");
        }

        s.append("STACK TRACE:\n");

        for (StackTraceElement x : e.getStackTrace()) {
            s.append(x.toString()).append("\n");
        }

        t.setText(s.toString());
        t.setTextSize(14);
        t.setTextColor(Color.RED);
        t.setPadding(24, 24, 24, 24);

        ScrollView sv = new ScrollView(this);
        sv.addView(t);

        setContentView(sv);
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private TextView text(String value, float size) {

        TextView t = new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(DARK);
        t.setPadding(16, 10, 16, 10);

        return t;
    }

    private EditText field(String hint, String value) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setText(value);
        e.setTextSize(15);
        e.setSingleLine(false);
        e.setPadding(14, 8, 14, 8);

        return e;
    }

    private LinearLayout box() {

        LinearLayout l = new LinearLayout(this);

        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(18, 8, 18, 8);

        return l;
    }

    private Button btn(
            String title,
            int color,
            View.OnClickListener listener) {

        Button b = new Button(this);

        b.setText(title);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setIncludeFontPadding(true);
        b.setMinHeight(64);
        b.setMinimumHeight(64);
        b.setPadding(12, 4, 12, 4);
        b.setOnClickListener(listener);

        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();

        bg.setColor(color);
        bg.setCornerRadius(22);

        b.setBackground(bg);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        64
                );

        p.setMargins(0, 7, 0, 7);

        b.setLayoutParams(p);

        return b;
    }

    private TextView card(String value) {

        TextView t = text(value, 15);

        t.setTextColor(DARK);
        t.setBackgroundColor(Color.WHITE);
        t.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, 6, 0, 6);

        t.setLayoutParams(p);

        return t;
    }

    private void page(String title) {

        ScrollView scroll = new ScrollView(this);

        scroll.setFillViewport(true);

        root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 18, 16, 32);
        root.setBackgroundColor(BG);

        // TOP SAFE SPACE
        TextView topSpace = new TextView(this);
        topSpace.setHeight(8);
        root.addView(topSpace);

        // HEADER
        LinearLayout header = new LinearLayout(this);

        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        Button back = new Button(this);

        back.setText("‹");
        back.setTextSize(30);
        back.setTextColor(DARK);
        back.setAllCaps(false);
        back.setGravity(Gravity.CENTER);

        back.setBackgroundColor(Color.TRANSPARENT);

        back.setOnClickListener(v -> home());

        header.addView(
                back,
                new LinearLayout.LayoutParams(56, 60)
        );

        TextView titleView = new TextView(this);

        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTextColor(DARK);
        titleView.setTypeface(null, 1);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setPadding(8, 0, 8, 0);

        header.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                )
        );

        root.addView(header);

        scroll.addView(root);

        setContentView(scroll);
    }

    private void logo() {

        LinearLayout logoBox = new LinearLayout(this);

        logoBox.setOrientation(LinearLayout.VERTICAL);
        logoBox.setGravity(Gravity.CENTER);
        logoBox.setPadding(10, 8, 10, 12);

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
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        logoBox.addView(
                image,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        135
                )
        );

        root.addView(logoBox);
    }

    private String today() {

        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());
    }

    private double num(EditText e) {

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

    // =========================================================
    // HOME
    // =========================================================

    private void home() {

        try {

            page("EDISON SOLAR MANAGER PRO");

            logo();

            TextView dashboard =
                    card(db.dashboard());

            dashboard.setTextSize(16);
            dashboard.setPadding(18, 18, 18, 18);

            root.addView(dashboard);

            root.addView(
                    btn(
                            "🏢  Companies",
                            BLUE,
                            v -> companies()
                    )
            );

            root.addView(
                    btn(
                            "☀️  Projects",
                            BLUE,
                            v -> projects()
                    )
            );

            root.addView(
                    btn(
                            "💰  Payment Collection",
                            GREEN,
                            v -> payments()
                    )
            );

            root.addView(
                    btn(
                            "🧾  Expenses",
                            ORANGE,
                            v -> expenses()
                    )
            );

            root.addView(
                    btn(
                            "📅  Calendar",
                            PURPLE,
                            v -> calendar()
                    )
            );

            root.addView(
                    btn(
                            "📊  Reports / PDF",
                            PURPLE,
                            v -> reports()
                    )
            );

            root.addView(
                    btn(
                            "📱  WhatsApp Report",
                            GREEN,
                            v -> share(db.dashboard())
                    )
            );

        } catch (Throwable e) {

            crash("HOME", e);
        }
    }

    // =========================================================
    // COMPANIES
    // =========================================================

    private void companies() {

        try {

            page("Companies");

            root.addView(
                    btn(
                            "+  Add Company",
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

                row.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                row.addView(
                        btn(
                                "Edit",
                                BLUE,
                                v -> companyDialog(c)
                        ),
                        new LinearLayout.LayoutParams(
                                0,
                                60,
                                1
                        )
                );

                row.addView(
                        btn(
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
                                0,
                                60,
                                1
                        )
                );

                root.addView(row);
            }

        } catch (Throwable e) {

            crash("COMPANIES", e);
        }
    }

    private void companyDialog(Company old) {

        LinearLayout layout = box();

        EditText name =
                field(
                        "Company Name",
                        old == null ? "" : old.name
                );

        EditText phone =
                field(
                        "Phone",
                        old == null ? "" : old.phone
                );

        layout.addView(name);
        layout.addView(phone);

        AlertDialog d =
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

        d.setOnShowListener(x ->
                d.getButton(-1)
                        .setOnClickListener(v -> {

                            String n =
                                    name.getText()
                                            .toString()
                                            .trim();

                            if (n.isEmpty()) {

                                name.setError(
                                        "Required"
                                );

                                return;
                            }

                            if (old == null) {

                                db.addCompany(
                                        n,
                                        phone.getText()
                                                .toString()
                                );

                            } else {

                                db.updateCompany(
                                        old.id,
                                        n,
                                        phone.getText()
                                                .toString()
                                );
                            }

                            d.dismiss();

                            companies();
                        })
        );

        d.show();
    }

    // =========================================================
    // PROJECTS
    // =========================================================

    private void projects() {

        try {

            page("Projects");

            root.addView(
                    btn(
                            "+  Add Project",
                            BLUE,
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
                                "\nPhone: " +
                                p.phone +
                                "\nSolar: " +
                                DBHelper.fmt(p.kw) +
                                " kW" +
                                "\nSite: " +
                                p.site +
                                "\nAmount: ₹" +
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
                                "\nProfit: ₹" +
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
                                BLUE,
                                v -> projectMenu(p)
                        )
                );
            }

        } catch (Throwable e) {

            crash("PROJECTS", e);
        }
    }

    private void projectDialog(Project old) {

        ArrayList<Company> companies =
                db.companies();

        if (companies.isEmpty()) {

            Toast.makeText(
                    this,
                    "Add company first",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        LinearLayout layout = box();

        Spinner spinner =
                new Spinner(this);

        ArrayList<String> names =
                new ArrayList<>();

        for (Company c : companies) {
            names.add(c.name);
        }

        spinner.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        names
                )
        );

        if (old != null) {

            for (
                    int i = 0;
                    i < companies.size();
                    i++
            ) {

                if (
                        companies.get(i).id ==
                        old.companyId
                ) {

                    spinner.setSelection(i);
                    break;
                }
            }
        }

        EditText customer =
                field(
                        "Customer Name",
                        old == null
                                ? ""
                                : old.customer
                );

        EditText phone =
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
                                : String.valueOf(old.kw)
                );

        EditText amount =
                field(
                        "Project Amount",
                        old == null
                                ? ""
                                : String.valueOf(old.amount)
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

        RadioGroup group =
                new RadioGroup(this);

        RadioButton pending =
                new RadioButton(this);

        pending.setText("Pending");

        RadioButton going =
                new RadioButton(this);

        going.setText("Work Going On");

        group.addView(pending);
        group.addView(going);

        if (
                old != null &&
                "Work Going On"
                        .equals(old.status)
        ) {

            going.setChecked(true);

        } else {

            pending.setChecked(true);
        }

        layout.addView(
                text("Company", 14)
        );

        layout.addView(spinner);
        layout.addView(customer);
        layout.addView(phone);
        layout.addView(site);
        layout.addView(kw);
        layout.addView(amount);
        layout.addView(date);
        layout.addView(group);
        layout.addView(work);

        layout.addView(
                btn(
                        "📅  Choose Installation Date",
                        PURPLE,
                        v -> chooseDate(date)
                )
        );

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle(
                                old == null
                                        ? "New Project"
                                        : "Edit Project"
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

        d.setOnShowListener(x ->
                d.getButton(-1)
                        .setOnClickListener(v -> {

                            Company company =
                                    companies.get(
                                            spinner
                                                    .getSelectedItemPosition()
                                    );

                            String customerName =
                                    customer.getText()
                                            .toString()
                                            .trim();

                            if (
                                    customerName
                                            .isEmpty()
                            ) {

                                customer.setError(
                                        "Required"
                                );

                                return;
                            }

                            String status =
                                    going.isChecked()
                                            ? "Work Going On"
                                            : "Pending";

                            if (old == null) {

                                db.addProject(
                                        company.id,
                                        company.name,
                                        customerName,
                                        phone.getText()
                                                .toString(),
                                        site.getText()
                                                .toString(),
                                        num(kw),
                                        num(amount),
                                        date.getText()
                                                .toString(),
                                        status,
                                        work.getText()
                                                .toString()
                                );

                            } else {

                                db.updateProject(
                                        old,
                                        company.id,
                                        company.name,
                                        customerName,
                                        phone.getText()
                                                .toString(),
                                        site.getText()
                                                .toString(),
                                        num(kw),
                                        num(amount),
                                        date.getText()
                                                .toString(),
                                        status,
                                        work.getText()
                                                .toString()
                                );
                            }

                            d.dismiss();

                            projects();
                        })
        );

        d.show();
    }

    private void projectMenu(Project p) {

        String[] items = {
                "Edit Project",
                "Payments",
                "Expenses",
                "Photos / Gallery",
                "GPS / Google Map",
                "PDF Report",
                "WhatsApp Report",
                "Delete Project"
        };

        new AlertDialog.Builder(this)
                .setTitle(
                        p.number +
                        " • " +
                        p.customer
                )
                .setItems(
                        items,
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

                                gps(p);

                            } else if (w == 5) {

                                pdf(p);

                            } else if (w == 6) {

                                share(
                                        projectReport(p)
                                );

                            } else {

                                confirm(
                                        "Delete this project and its records?",
                                        () -> {

                                            db.deleteProject(
                                                    p.id
                                            );

                                            projects();
                                        }
                                );
                            }
                        }
                )
                .show();
    }

    private String projectReport(Project p) {

        return
                "EDISON SOLAR MANAGER PRO\n\n" +
                "Project: " +
                p.number + "\n" +
                "Company: " +
                p.company + "\n" +
                "Customer: " +
                p.customer + "\n" +
                "Phone: " +
                p.phone + "\n" +
                "Site: " +
                p.site + "\n" +
                "Solar: " +
                DBHelper.fmt(p.kw) +
                " kW\n" +
                "Amount: ₹" +
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
                "\nProfit: ₹" +
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

    // =========================================================
    // PAYMENTS
    // =========================================================

    private void projectPayments(Project p) {

        page(
                "Payments • " +
                p.customer
        );

        root.addView(
                card(
                        "Amount: ₹" +
                        DBHelper.fmt(p.amount) +
                        "\nCollection: ₹" +
                        DBHelper.fmt(
                                db.collection(p.id)
                        ) +
                        "\nPending: ₹" +
                        DBHelper.fmt(
                                db.pending(p.id)
                        )
                )
        );

        root.addView(
                btn(
                        "+  Add Payment",
                        GREEN,
                        v -> paymentDialog(p)
                )
        );
    }

    private void paymentDialog(Project p) {

        LinearLayout layout = box();

        EditText amount =
                field(
                        "Payment Amount",
                        ""
                );

        EditText note =
                field(
                        "Payment Note",
                        ""
                );

        layout.addView(amount);
        layout.addView(note);

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Add Payment"
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

        d.setOnShowListener(x ->
                d.getButton(-1)
                        .setOnClickListener(v -> {

                            double value =
                                    num(amount);

                            if (value <= 0) {

                                amount.setError(
                                        "Enter amount"
                                );

                                return;
                            }

                            db.addPayment(
                                    p.id,
                                    value,
                                    today(),
                                    note.getText()
                                            .toString()
                            );

                            d.dismiss();

                            projectPayments(p);
                        })
        );

        d.show();
    }

    private void payments() {

        page("Payment Collection");

        double total = 0;

        for (Project p : db.projects()) {

            total += db.collection(p.id);
        }

        root.addView(
                card(
                        "TOTAL COLLECTION\n₹" +
                        DBHelper.fmt(total)
                )
        );

        for (Project p : db.projects()) {

            root.addView(
                    card(
                            p.number +
                            " • " +
                            p.customer +
                            "\nCollected: ₹" +
                            DBHelper.fmt(
                                    db.collection(p.id)
                            ) +
                            "\nPending: ₹" +
                            DBHelper.fmt(
                                    db.pending(p.id)
                            )
                    )
            );

            root.addView(
                    btn(
                            "Add Payment",
                            GREEN,
                            v -> paymentDialog(p)
                    )
            );
        }
    }

    // =========================================================
    // EXPENSES
    // =========================================================

    private void projectExpenses(Project p) {

        page(
                "Expenses • " +
                p.customer
        );

        root.addView(
                card(
                        "Expenses: ₹" +
                        DBHelper.fmt(
                                db.expenses(p.id)
                        ) +
                        "\nProfit: ₹" +
                        DBHelper.fmt(
                                db.profit(p.id)
                        )
                )
        );

        root.addView(
                btn(
                        "+  Add Expense",
                        ORANGE,
                        v -> expenseDialog(p)
                )
        );
    }

    private void expenseDialog(Project p) {

        LinearLayout layout = box();

        EditText amount =
                field(
                        "Expense Amount",
                        ""
                );

        EditText type =
                field(
                        "Expense Type",
                        "Other"
                );

        EditText note =
                field(
                        "Expense Note",
                        ""
                );

        layout.addView(amount);
        layout.addView(type);
        layout.addView(note);

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Add Expense"
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

        d.setOnShowListener(x ->
                d.getButton(-1)
                        .setOnClickListener(v -> {

                            double value =
                                    num(amount);

                            if (value <= 0) {

                                amount.setError(
                                        "Enter amount"
                                );

                                return;
                            }

                            db.addExpense(
                                    p.id,
                                    value,
                                    type.getText()
                                            .toString(),
                                    today(),
                                    note.getText()
                                            .toString()
                            );

                            d.dismiss();

                            projectExpenses(p);
                        })
        );

        d.show();
    }

    private void expenses() {

        page("Expenses");

        double total = 0;

        for (Project p : db.projects()) {

            total += db.expenses(p.id);
        }

        root.addView(
                card(
                        "TOTAL EXPENSES\n₹" +
                        DBHelper.fmt(total)
                )
        );

        for (Project p : db.projects()) {

            root.addView(
                    card(
                            p.number +
                            " • " +
                            p.customer +
                            "\nExpenses: ₹" +
                            DBHelper.fmt(
                                    db.expenses(p.id)
                            ) +
                            "\nProfit: ₹" +
                            DBHelper.fmt(
                                    db.profit(p.id)
                            )
                    )
            );
        }
    }

    // =========================================================
    // CALENDAR
    // =========================================================

    private void calendar() {

        page("Calendar");

        root.addView(
                btn(
                        "📅  Select Date",
                        PURPLE,
                        v -> {

                            Calendar c =
                                    Calendar.getInstance();

                            new DatePickerDialog(
                                    this,
                                    (view,
                                     year,
                                     month,
                                     day) -> {

                                        String date =
                                                String.format(
                                                        Locale.getDefault(),
                                                        "%02d-%02d-%04d",
                                                        day,
                                                        month + 1,
                                                        year
                                                );

                                        calendarDay(date);
                                    },
                                    c.get(Calendar.YEAR),
                                    c.get(Calendar.MONTH),
                                    c.get(Calendar.DAY_OF_MONTH)
                            ).show();
                        }
                )
        );

        root.addView(
                card(
                        "Select a date to see company and site work."
                )
        );
    }

    private void calendarDay(String date) {

        page(
                "Work • " +
                date
        );

        boolean found = false;

        for (Project p : db.projects()) {

            if (date.equals(p.date)) {

                found = true;

                root.addView(
                        card(
                                "🏢 " +
                                p.company +
                                "\n👤 " +
                                p.customer +
                                "\n☀️ " +
                                DBHelper.fmt(p.kw) +
                                " kW" +
                                "\n📍 " +
                                p.site +
                                "\nStatus: " +
                                p.status +
                                "\nWork: " +
                                p.work
                        )
                );
            }
        }

        if (!found) {

            root.addView(
                    card(
                            "No project work on this date."
                    )
            );
        }
    }

    // =========================================================
    // REPORTS
    // =========================================================

    private void reports() {

        page("Reports / PDF");

        root.addView(
                card(
                        db.dashboard()
                )
        );

        root.addView(
                btn(
                        "📱  Overall WhatsApp Report",
                        GREEN,
                        v -> share(
                                db.dashboard()
                        )
                )
        );

        for (Project p : db.projects()) {

            root.addView(
                    btn(
                            "PDF • " +
                            p.number,
                            PURPLE,
                            v -> pdf(p)
                    )
            );
        }
    }

    // =========================================================
    // PHOTOS
    // =========================================================

    private void photos(Project p) {

        photoProject = p;

        page("Photos / Gallery");

        root.addView(
                card(
                        p.customer +
                        "\n" +
                        p.site +
                        "\nSaved Photos: " +
                        db.photos(p.id).size()
                )
        );

        root.addView(
                btn(
                        "📷  Take Site Photo",
                        BLUE,
                        v -> takePhoto()
                )
        );

        root.addView(
                btn(
                        "🖼️  Open Gallery",
                        PURPLE,
                        v -> openGallery()
                )
        );
    }

    private void takePhoto() {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                )
                != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    101
            );

            return;
        }

        try {

            File dir =
                    getExternalFilesDir(
                            Environment
                                    .DIRECTORY_PICTURES
                    );

            if (dir == null) return;

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file =
                    new File(
                            dir,
                            "EDISON_" +
                            System.currentTimeMillis() +
                            ".jpg"
                    );

            cameraUri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() +
                                    ".fileprovider",
                            file
                    );

            Intent intent =
                    new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE
                    );

            intent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    cameraUri
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );

            startActivityForResult(
                    intent,
                    101
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Camera Error: " +
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openGallery() {

        Intent intent =
                new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media
                                .EXTERNAL_CONTENT_URI
                );

        startActivityForResult(
                intent,
                102
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (resultCode != RESULT_OK) {
            return;
        }

        if (
                requestCode == 101 &&
                photoProject != null &&
                cameraUri != null
        ) {

            db.addPhoto(
                    photoProject.id,
                    cameraUri.toString()
            );

            photos(photoProject);
        }

        if (
                requestCode == 102 &&
                data != null &&
                data.getData() != null &&
                photoProject != null
        ) {

            db.addPhoto(
                    photoProject.id,
                    data.getData().toString()
            );

            photos(photoProject);
        }
    }

    // =========================================================
    // GPS
    // =========================================================

    private void gps(Project p) {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission
                                    .ACCESS_FINE_LOCATION,
                            Manifest.permission
                                    .ACCESS_COARSE_LOCATION
                    },
                    100
            );

            return;
        }

        try {

            LocationManager lm =
                    (LocationManager)
                            getSystemService(
                                    LOCATION_SERVICE
                            );

            Location loc =
                    lm.getLastKnownLocation(
                            LocationManager
                                    .GPS_PROVIDER
                    );

            if (loc == null) {

                loc =
                        lm.getLastKnownLocation(
                                LocationManager
                                        .NETWORK_PROVIDER
                        );
            }

            if (loc == null) {

                Toast.makeText(
                        this,
                        "Turn on GPS",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            Uri uri =
                    Uri.parse(
                            "geo:" +
                            loc.getLatitude() +
                            "," +
                            loc.getLongitude() +
                            "?q=" +
                            loc.getLatitude() +
                            "," +
                            loc.getLongitude()
                    );

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            uri
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Map unavailable",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // PDF
    // =========================================================

    private void pdf(Project p) {

        PdfDocument document =
                new PdfDocument();

        try {

            PdfDocument.PageInfo info =
                    new PdfDocument.PageInfo.Builder(
                            595,
                            842,
                            1
                    ).create();

            PdfDocument.Page page =
                    document.startPage(info);

            Paint paint = new Paint();

            paint.setColor(Color.BLACK);
            paint.setTextSize(13);

            float y = 45;

            for (
                    String line :
                    projectReport(p).split("\n")
            ) {

                if (y > 800) {
                    break;
                }

                page.getCanvas().drawText(
                        line,
                        40,
                        y,
                        paint
                );

                y += 22;
            }

            document.finishPage(page);

            File dir =
                    new File(
                            getExternalFilesDir(
                                    Environment
                                            .DIRECTORY_DOCUMENTS
                            ),
                            "EDISON_SOLAR"
                    );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file =
                    new File(
                            dir,
                            p.number +
                            "_Report.pdf"
                    );

            FileOutputStream output =
                    new FileOutputStream(file);

            document.writeTo(output);
            output.close();

            Uri uri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() +
                                    ".fileprovider",
                            file
                    );

            Intent share =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            share.setType(
                    "application/pdf"
            );

            share.putExtra(
                    Intent.EXTRA_STREAM,
                    uri
            );

            share.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(
                    Intent.createChooser(
                            share,
                            "Share PDF"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "PDF Error: " +
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        } finally {

            document.close();
        }
    }

    // =========================================================
    // DATE
    // =========================================================

    private void chooseDate(EditText target) {

        Calendar c =
                Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        target.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%02d-%02d-%04d",
                                        day,
                                        month + 1,
                                        year
                                )
                        ),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // =========================================================
    // CONFIRM
    // =========================================================

    private void confirm(
            String message,
            Runnable action) {

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (d, w) -> action.run()
                )
                .show();
    }

    // =========================================================
    // SHARE
    // =========================================================

    private void share(String message) {

        Intent intent =
                new Intent(
                        Intent.ACTION_SEND
                );

        intent.setType(
                "text/plain"
        );

        intent.putExtra(
                Intent.EXTRA_TEXT,
                message
        );

        startActivity(
                Intent.createChooser(
                        intent,
                        "Share Report"
                )
        );
    }
}
