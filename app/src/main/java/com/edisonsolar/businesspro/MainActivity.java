package com.edisonsolar.businesspro;

import android.Manifest;
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

import androidx.appcompat.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

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
    // CRASH DIAGNOSTIC
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

        for (StackTraceElement element : e.getStackTrace()) {

            error.append(element.toString());
            error.append("\n");
        }

        TextView errorView = new TextView(this);

        errorView.setText(error.toString());
        errorView.setTextSize(14);
        errorView.setTextColor(Color.RED);
        errorView.setPadding(24,24,24,24);

        ScrollView scroll = new ScrollView(this);

        scroll.addView(errorView);

        setContentView(scroll);
    }

    // =========================================================
    // BASIC UI
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
            View.OnClickListener l) {

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
                new LinearLayout.LayoutParams(-1,54);

        p.setMargins(0,6,0,6);

        b.setLayoutParams(p);

        return b;
    }

    private TextView card(String s) {

        TextView t = text(s,15);

        t.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1,-2);

        p.setMargins(0,5,0,5);

        t.setLayoutParams(p);

        return t;
    }

    private void page(String title) {

        ScrollView sv = new ScrollView(this);

        root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16,16,16,30);
        root.setBackgroundColor(BG);

        sv.addView(root);

        setContentView(sv);

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

        TextView tv = text(title,21);

        tv.setTypeface(null,1);

        bar.addView(
                tv,
                new LinearLayout.LayoutParams(0,55,1)
        );

        root.addView(bar);
    }

    private void addLogo() {

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

        im.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        root.addView(
                im,
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

        } catch (Exception x) {

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
                                "\nPhone: " + c.phone +
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

        LinearLayout l = box();

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

        l.addView(name);
        l.addView(phone);

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

                            String n =
                                    name.getText()
                                            .toString()
                                            .trim();

                            if (n.isEmpty()) {

                                name.setError("Required");

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
                    button(
                            "+ Add Project",
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
                                p.kw +
                                " kW" +
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
                        button(
                                "Open Project",
                                BLUE,
                                v -> projectMenu(p)
                        )
                );
            }

        } catch (Throwable e) {

            showCrash("PROJECTS", e);
        }
    }

    private void projectDialog(Project old) {

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

        LinearLayout l = box();

        Spinner sp = new Spinner(this);

        ArrayList<String> names =
                new ArrayList<>();

        for (Company c : cs) {

            names.add(c.name);
        }

        sp.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        names
                )
        );

        if (old != null) {

            for (int i = 0; i < cs.size(); i++) {

                if (cs.get(i).id == old.companyId) {

                    sp.setSelection(i);

                    break;
                }
            }
        }

        EditText customer =
                field(
                        "Customer Name",
                        old == null ? "" : old.customer
                );

        EditText phone =
                field(
                        "Customer Phone",
                        old == null ? "" : old.phone
                );

        EditText site =
                field(
                        "Site Address",
                        old == null ? "" : old.site
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
                        old == null ? "" : old.work
                );

        kw.setInputType(2);
        amount.setInputType(2);

        RadioGroup rg =
                new RadioGroup(this);

        RadioButton pending =
                new RadioButton(this);

        pending.setText("Pending");

        RadioButton going =
                new RadioButton(this);

        going.setText("Work Going On");

        rg.addView(pending);
        rg.addView(going);

        if (
                old != null &&
                "Work Going On".equals(old.status)
        ) {

            going.setChecked(true);

        } else {

            pending.setChecked(true);
        }

        l.addView(text("Company",14));
        l.addView(sp);
        l.addView(customer);
        l.addView(phone);
        l.addView(site);
        l.addView(kw);
        l.addView(amount);
        l.addView(date);
        l.addView(rg);
        l.addView(work);

        Button dateBtn = new Button(this);

        dateBtn.setText(
                "📅 Choose Installation Date"
        );

        dateBtn.setOnClickListener(
                v -> chooseDate(date)
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

                d.getButton(-1)
                        .setOnClickListener(v -> {

                            Company c =
                                    cs.get(
                                            sp.getSelectedItemPosition()
                                    );

                            String cust =
                                    customer.getText()
                                            .toString()
                                            .trim();

                            if (cust.isEmpty()) {

                                customer.setError("Required");

                                return;
                            }

                            String status =
                                    going.isChecked()
                                            ? "Work Going On"
                                            : "Pending";

                            if (old == null) {

                                db.addProject(
                                        c.id,
                                        c.name,
                                        cust,
                                        phone.getText().toString(),
                                        site.getText().toString(),
                                        number(kw),
                                        number(amount),
                                        date.getText().toString(),
                                        status,
                                        work.getText().toString()
                                );

                            } else {

                                db.updateProject(
                                        old,
                                        c.id,
                                        c.name,
                                        cust,
                                        phone.getText().toString(),
                                        site.getText().toString(),
                                        number(kw),
                                        number(amount),
                                        date.getText().toString(),
                                        status,
                                        work.getText().toString()
                                );
                            }

                            d.dismiss();

                            projects();
                        })
        );

        d.show();
    }

    private void projectMenu(Project p) {

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
                        (d,w) -> {

                            if (w == 0)
                                projectDialog(p);

                            else if (w == 1)
                                projectPayments(p);

                            else if (w == 2)
                                projectExpenses(p);

                            else if (w == 3)
                                photos(p);

                            else if (w == 4)
                                gps(p);

                            else if (w == 5)
                                pdf(p);

                            else if (w == 6)
                                share(projectReport(p));

                            else
                                confirm(
                                        "Delete this project and its records?",
                                        () -> {

                                            db.deleteProject(p.id);

                                            projects();
                                        }
                                );
                        }
                )
                .show();
    }

    private String projectReport(Project p) {

        return
                "EDISON SOLAR MANAGER PRO\n\n" +
                "Project: " + p.number + "\n" +
                "Company: " + p.company + "\n" +
                "Customer: " + p.customer + "\n" +
                "Phone: " + p.phone + "\n" +
                "Site: " + p.site + "\n" +
                "Solar: " + p.kw + " kW\n" +
                "Amount: ₹" +
                DBHelper.fmt(p.amount) + "\n" +
                "Collection: ₹" +
                DBHelper.fmt(
                        db.collection(p.id)
                ) + "\n" +
                "Pending: ₹" +
                DBHelper.fmt(
                        db.pending(p.id)
                ) + "\n" +
                "Expenses: ₹" +
                DBHelper.fmt(
                        db.expenses(p.id)
                ) + "\n" +
                "Profit: ₹" +
                DBHelper.fmt(
                        db.profit(p.id)
                ) + "\n" +
                "Date: " + p.date + "\n" +
                "Status: " + p.status + "\n" +
                "Work: " + p.work;
    }

    // =========================================================
    // PAYMENTS
    // =========================================================

    private void projectPayments(Project p) {

        page("Payments • " + p.customer);

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
                button(
                        "+ Add Payment",
                        GREEN,
                        v -> paymentDialog(p)
                )
        );
    }

    private void paymentDialog(Project p) {

        LinearLayout l = box();

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

        l.addView(amount);
        l.addView(note);

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle("Add Payment")
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

                            double a =
                                    number(amount);

                            if (a <= 0) {

                                amount.setError(
                                        "Enter amount"
                                );

                                return;
                            }

                            db.addPayment(
                                    p.id,
                                    a,
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
                    button(
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

        page("Expenses • " + p.customer);

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
                button(
                        "+ Add Expense",
                        ORANGE,
                        v -> expenseDialog(p)
                )
        );
    }

    private void expenseDialog(Project p) {

        LinearLayout l = box();

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

        l.addView(amount);
        l.addView(type);
        l.addView(note);

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle("Add Expense")
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

                            double a =
                                    number(amount);

                            if (a <= 0) {

                                amount.setError(
                                        "Enter amount"
                                );

                                return;
                            }

                            db.addExpense(
                                    p.id,
                                    a,
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
                button(
                        "📅 Select Date",
                        PURPLE,
                        v -> {

                            Calendar c =
                                    Calendar.getInstance();

                            DatePickerDialog d =
                                    new DatePickerDialog(
                                            this,
                                            (view,year,month,day) -> {

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
                                    );

                            d.show();
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

        page("Work • " + date);

        boolean found = false;

        for (Project p : db.projects()) {

            if (date.equals(p.date)) {

                found = true;

                root.addView(
                        card(
                                "🏢 " + p.company +
                                "\n👤 " + p.customer +
                                "\n☀️ " + p.kw + " kW" +
                                "\n📍 " + p.site +
                                "\nStatus: " + p.status +
                                "\nWork: " + p.work
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
                card(db.dashboard())
        );

        root.addView(
                button(
                        "📱 Overall WhatsApp Report",
                        GREEN,
                        v -> share(db.dashboard())
                )
        );

        for (Project p : db.projects()) {

            root.addView(
                    button(
                            "PDF • " + p.number,
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
                button(
                        "📷 Take Site Photo",
                        BLUE,
                        v -> takePhoto()
                )
        );

        root.addView(
                button(
                        "🖼️ Open Gallery",
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
                            Environment.DIRECTORY_PICTURES
                    );

            if (dir == null)
                return;

            if (!dir.exists())
                dir.mkdirs();

            File f =
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
                            f
                    );

            Intent i =
                    new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE
                    );

            i.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    cameraUri
            );

            i.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );

            startActivityForResult(
                    i,
                    101
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openGallery() {

        Intent i =
                new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media
                                .EXTERNAL_CONTENT_URI
                );

        startActivityForResult(
                i,
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

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == 101) {

            if (
                    photoProject != null &&
                    cameraUri != null
            ) {

                db.addPhoto(
                        photoProject.id,
                        cameraUri.toString()
                );
            }

            Toast.makeText(
                    this,
                    "Photo saved",
                    Toast.LENGTH_SHORT
            ).show();

            if (photoProject != null)
                photos(photoProject);
        }

        if (
                requestCode == 102 &&
                data != null
        ) {

            Uri u = data.getData();

            if (
                    photoProject != null &&
                    u != null
            ) {

                db.addPhoto(
                        photoProject.id,
                        u.toString()
                );

                Toast.makeText(
                        this,
                        "Photo selected",
                        Toast.LENGTH_SHORT
                ).show();
            }
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
                            LocationManager.GPS_PROVIDER
                    );

            if (loc == null) {

                loc =
                        lm.getLastKnownLocation(
                                LocationManager
                                        .NETWORK_PROVIDER
                        );
            }

            if (loc != null) {

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

            } else {

                Toast.makeText(
                        this,
                        "Turn on GPS",
                        Toast.LENGTH_LONG
                ).show();
            }

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

        PdfDocument pdf =
                new PdfDocument();

        try {

            PdfDocument.PageInfo info =
                    new PdfDocument.PageInfo.Builder(
                            595,
                            842,
                            1
                    ).create();

            PdfDocument.Page page =
                    pdf.startPage(info);

            Paint paint = new Paint();

            paint.setColor(Color.BLACK);
            paint.setTextSize(18);

            paint.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            float y = 45;

            page.getCanvas().drawText(
                    "EDISON SOLAR",
                    40,
                    y,
                    paint
            );

            y += 35;

            paint.setTextSize(13);

            paint.setTypeface(
                    android.graphics.Typeface.DEFAULT
            );

            for (
                    String line :
                    projectReport(p).split("\n")
            ) {

                if (y > 800)
                    break;

                page.getCanvas().drawText(
                        line,
                        40,
                        y,
                        paint
                );

                y += 22;
            }

            pdf.finishPage(page);

            File dir =
                    new File(
                            getExternalFilesDir(
                                    Environment
                                            .DIRECTORY_DOCUMENTS
                            ),
                            "EDISON_SOLAR"
                    );

            if (!dir.exists())
                dir.mkdirs();

            File f =
                    new File(
                            dir,
                            p.number +
                            "_Report.pdf"
                    );

            FileOutputStream out =
                    new FileOutputStream(f);

            pdf.writeTo(out);

            out.close();

            Uri uri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() +
                                    ".fileprovider",
                            f
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

            pdf.close();
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
                (view,year,month,day) ->
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
    // HELPERS
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
                        (d,w) -> action.run()
                )
                .show();
    }

    private void share(String s) {

        Intent i =
                new Intent(
                        Intent.ACTION_SEND
                );

        i.setType("text/plain");

        i.putExtra(
                Intent.EXTRA_TEXT,
                s
        );

        startActivity(
                Intent.createChooser(
                        i,
                        "Share Report"
                )
        );
    }
}
