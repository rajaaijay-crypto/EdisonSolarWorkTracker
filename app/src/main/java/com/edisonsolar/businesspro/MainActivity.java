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

        db = new DBHelper(this);

        getWindow().setStatusBarColor(Color.WHITE);

        showHome();
    }

    // =========================================================
    // BASIC UI
    // =========================================================

    private TextView tv(String text, float size) {

        TextView t = new TextView(this);

        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(dark);
        t.setPadding(16, 12, 16, 12);

        return t;
    }

    private LinearLayout vertical() {

        LinearLayout l = new LinearLayout(this);

        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(20, 10, 20, 10);

        return l;
    }

    private void base(String title) {

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 30);
        root.setBackgroundColor(bg);

        scroll.addView(root);

        setContentView(scroll);

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

        TextView titleView = tv(title, 21);

        titleView.setTypeface(null, 1);

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

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        135
                );

        p.setMargins(0, 5, 0, 10);

        root.addView(image, p);
    }

    private Button btn(
            String text,
            int color,
            View.OnClickListener listener
    ) {

        Button b = new Button(this);

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

        p.setMargins(0, 6, 0, 6);

        b.setLayoutParams(p);

        return b;
    }

    private TextView card(String text) {

        TextView t = tv(text, 15);

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

    private EditText field(
            String hint,
            String value
    ) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setText(value);
        e.setTextSize(15);
        e.setPadding(12, 8, 12, 8);

        return e;
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

    private void showHome() {

        base("EDISON SOLAR MANAGER PRO");

        logo();

        root.addView(
                card(db.dashboard())
        );

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

    // =========================================================
    // COMPANIES
    // =========================================================

    private void companies() {

        base("Companies");

        root.addView(
                btn(
                        "+ Add Company",
                        blue,
                        v -> companyDialog(null)
                )
        );

        ArrayList<Company> list =
                db.companies();

        for (Company c : list) {

            String text =
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

            root.addView(card(text));

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            Button edit =
                    btn(
                            "Edit",
                            blue,
                            v -> companyDialog(c)
                    );

            Button delete =
                    btn(
                            "Delete",
                            red,
                            v -> confirm(
                                    "Delete company?",
                                    () -> {

                                        db.deleteCompany(
                                                c.id
                                        );

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
                    delete,
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

    private String companyReport(Company c) {

        StringBuilder s =
                new StringBuilder();

        s.append(
                "EDISON SOLAR MANAGER PRO\n\n"
        );

        s.append("COMPANY: ")
                .append(c.name)
                .append("\n");

        s.append("PHONE: ")
                .append(c.phone)
                .append("\n\n");

        for (Project p : db.projects()) {

            if (p.companyId == c.id) {

                s.append(
                        "------------------------\n"
                );

                s.append(p.number)
                        .append(" | ")
                        .append(p.customer)
                        .append("\n");

                s.append("Solar: ")
                        .append(p.kw)
                        .append(" kW\n");

                s.append("Amount: ₹")
                        .append(
                                DBHelper.fmt(
                                        p.amount
                                )
                        )
                        .append("\n");

                s.append("Collection: ₹")
                        .append(
                                DBHelper.fmt(
                                        db.collection(p.id)
                                )
                        )
                        .append("\n");

                s.append("Pending: ₹")
                        .append(
                                DBHelper.fmt(
                                        db.pending(p.id)
                                )
                        )
                        .append("\n");

                s.append("Expenses: ₹")
                        .append(
                                DBHelper.fmt(
                                        db.expenses(p.id)
                                )
                        )
                        .append("\n");

                s.append("Profit: ₹")
                        .append(
                                DBHelper.fmt(
                                        db.profit(p.id)
                                )
                        )
                        .append("\n");

                s.append("Site: ")
                        .append(p.site)
                        .append("\n\n");
            }
        }

        return s.toString();
    }

    private void companyDialog(Company old) {

        LinearLayout l = vertical();

        EditText name =
                field(
                        "Company Name",
                        old == null
                                ? ""
                                : old.name
                );

        EditText phone =
                field(
                        "Phone",
                        old == null
                                ? ""
                                : old.phone
                );

        l.addView(name);
        l.addView(phone);

        AlertDialog dialog =
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

        dialog.setOnShowListener(
                x ->
                        dialog.getButton(-1)
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

                                    dialog.dismiss();

                                    companies();
                                })
        );

        dialog.show();
    }

    // =========================================================
    // PROJECTS
    // =========================================================

    private void projects() {

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
                            DBHelper.fmt(
                                    p.amount
                            ) +
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

                        (dialog, which) -> {

                            switch (which) {

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

                                                db.deleteProject(
                                                        p.id
                                                );

                                                projects();
                                            }
                                    );
                                    break;

                                default:
                                    break;
                            }
                        }
                )
                .show();
    }

    private String projectReport(Project p) {

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
                DBHelper.fmt(
                        p.amount
                ) +

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

                "\nInstallation Date: " +
                p.date +

                "\nStatus: " +
                p.status +

                "\nWork: " +
                p.work;
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

        LinearLayout l = vertical();

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
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                )
        );

        if (old != null) {

            for (int i = 0;
                 i < companies.size();
                 i++) {

                if (
                        companies.get(i).id
                                == old.companyId
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
                                : String.valueOf(
                                        old.kw
                                )
                );

        EditText amount =
                field(
                        "Project Amount",
                        old == null
                                ? ""
                                : String.valueOf(
                                        old.amount
                                )
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
        amount.setInputType(2);

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
                "Work Going On".equals(
                        old.status
                )
        ) {

            going.setChecked(true);

        } else {

            pending.setChecked(true);
        }

        l.addView(
                tv(
                        "Company",
                        14
                )
        );

        l.addView(spinner);
        l.addView(customer);
        l.addView(phone);
        l.addView(site);
        l.addView(kw);
        l.addView(amount);
        l.addView(date);
        l.addView(group);
        l.addView(work);

        Button dateButton =
                new Button(this);

        dateButton.setText(
                "📅 Choose Installation Date"
        );

        dateButton.setOnClickListener(
                v -> datePicker(date)
        );

        l.addView(dateButton);

        AlertDialog dialog =
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

        dialog.setOnShowListener(
                x ->
                        dialog.getButton(-1)
                                .setOnClickListener(v -> {

                                    Company company =
                                            companies.get(
                                                    spinner.getSelectedItemPosition()
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

                                    double solarKw =
                                            num(kw);

                                    double projectAmount =
                                            num(amount);

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
                                                solarKw,
                                                projectAmount,
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
                                                solarKw,
                                                projectAmount,
                                                date.getText()
                                                        .toString(),
                                                status,
                                                work.getText()
                                                        .toString()
                                        );
                                    }

                                    dialog.dismiss();

                                    projects();
                                })
        );

        dialog.show();
    }

    // =========================================================
    // PAYMENTS
    // =========================================================

    private void projectPayments(Project p) {

        base(
                "Payments • " +
                p.customer
        );

        root.addView(
                card(
                        "Project Amount: ₹" +
                        DBHelper.fmt(
                                p.amount
                        ) +
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
                        "+ Add Payment",
                        green,
                        v -> paymentDialog(p)
                )
        );

        root.addView(
                btn(
                        "WhatsApp Payment Report",
                        purple,
                        v -> shareText(
                                projectReport(p)
                        )
                )
        );
    }

    private void paymentDialog(Project p) {

        LinearLayout l = vertical();

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

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Add Payment"
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

        dialog.setOnShowListener(
                x ->
                        dialog.getButton(-1)
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

                                    dialog.dismiss();

                                    projectPayments(p);
                                })
        );

        dialog.show();
    }

    private void payments() {

        base(
                "Payment Collection"
        );

        double total = 0;

        for (Project p : db.projects()) {

            total +=
                    db.collection(p.id);
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
                            "\nAmount: ₹" +
                            DBHelper.fmt(
                                    p.amount
                            ) +
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
                            green,
                            v -> paymentDialog(p)
                    )
            );
        }
    }

    // =========================================================
    // EXPENSES
    // =========================================================

    private void projectExpenses(Project p) {

        base(
                "Expenses • " +
                p.customer
        );

        root.addView(
                card(
                        "Project Expenses: ₹" +
                        DBHelper.fmt(
                                db.expenses(p.id)
                        ) +
                        "\nSite Profit: ₹" +
                        DBHelper.fmt(
                                db.profit(p.id)
                        )
                )
        );

        root.addView(
                btn(
                        "+ Add Expense",
                        orange,
                        v -> expenseDialog(p)
                )
        );
    }

    private void expenseDialog(Project p) {

        LinearLayout l = vertical();

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

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Add Expense"
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

        dialog.setOnShowListener(
                x ->
                        dialog.getButton(-1)
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

                                    dialog.dismiss();

                                    projectExpenses(p);
                                })
        );

        dialog.show();
    }

    private void expenses() {

        base("Expenses");

        double total = 0;

        for (Project p : db.projects()) {

            total +=
                    db.expenses(p.id);
        }

        root.addView(
                card(
                        "TOTAL PROJECT EXPENSES\n₹" +
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

            root.addView(
                    btn(
                            "Add Expense",
                            orange,
                            v -> expenseDialog(p)
                    )
            );
        }
    }

    // =========================================================
    // CALENDAR
    // =========================================================

    private void calendar() {

        base("Calendar");

        Calendar cal =
                Calendar.getInstance();

        DatePickerDialog picker =
                new DatePickerDialog(
                        this,
                        (view, year, month, day) -> {

                            String selected =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d-%02d-%04d",
                                            day,
                                            month + 1,
                                            year
                                    );

                            calendarDay(selected);
                        },
                        cal.get(
                                Calendar.YEAR
                        ),
                        cal.get(
                                Calendar.MONTH
                        ),
                        cal.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        root.addView(
                btn(
                        "📅 Select Date",
                        purple,
                        v -> picker.show()
                )
        );

        root.addView(
                card(
                        "Select a date to see company / site work details."
                )
        );
    }

    private void calendarDay(
            String date
    ) {

        base(
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
                                "\n☀️ " +
                                p.kw +
                                " kW" +
                                "\n👤 " +
                                p.customer +
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

        base("Reports / PDF");

        root.addView(
                card(
                        db.dashboard()
                )
        );

        root.addView(
                btn(
                        "📊 Overall WhatsApp Report",
                        green,
                        v -> shareText(
                                db.dashboard()
                        )
                )
        );

        for (Project p : db.projects()) {

            root.addView(
                    card(
                            p.number +
                            " • " +
                            p.customer +
                            "\n" +
                            p.kw +
                            " kW | ₹" +
                            DBHelper.fmt(
                                    p.amount
                            )
                    )
            );

            root.addView(
                    btn(
                            "PDF • " +
                            p.number,
                            purple,
                            v -> createPdf(p)
                    )
            );
        }
    }

    // =========================================================
    // PHOTOS
    // =========================================================

    private void photos(Project p) {

        currentProjectForPhoto = p;

        base("Photos / Gallery");

        root.addView(
                card(
                        p.customer +
                        "\n" +
                        p.site
                )
        );

        root.addView(
                btn(
                        "📷 Take Site Photo",
                        blue,
                        v -> takePhoto(p)
                )
        );

        root.addView(
                btn(
                        "🖼️ Open Gallery",
                        purple,
                        v -> openGallery()
                )
        );

        root.addView(
                card(
                        "Site photos can be captured and attached to this project."
                )
        );
    }

    private void takePhoto(Project p) {

        currentProjectForPhoto = p;

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
                    CAMERA
            );

            return;
        }

        try {

            File directory =
                    getExternalFilesDir(
                            Environment.DIRECTORY_PICTURES
                    );

            if (directory == null) {

                Toast.makeText(
                        this,
                        "Storage unavailable",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file =
                    new File(
                            directory,
                            "EDISON_" +
                            p.id +
                            "_" +
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
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivityForResult(
                    intent,
                    CAMERA
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Camera error: " +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openGallery() {

        Intent intent =
                new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );

        startActivityForResult(
                intent,
                GALLERY
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == CAMERA &&
                resultCode == RESULT_OK
        ) {

            if (
                    currentProjectForPhoto != null &&
                    cameraUri != null
            ) {

                db.addPhoto(
                        currentProjectForPhoto.id,
                        cameraUri.toString()
                );
            }

            Toast.makeText(
                    this,
                    "Photo captured successfully",
                    Toast.LENGTH_SHORT
            ).show();

            if (
                    currentProjectForPhoto != null
            ) {

                photos(
                        currentProjectForPhoto
                );
            }
        }

        if (
                requestCode == GALLERY &&
                resultCode == RESULT_OK &&
                data != null
        ) {

            Uri uri = data.getData();

            if (uri != null) {

                if (
                        currentProjectForPhoto != null
                ) {

                    db.addPhoto(
                            currentProjectForPhoto.id,
                            uri.toString()
                    );
                }

                Toast.makeText(
                        this,
                        "Photo selected",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    // =========================================================
    // GPS / MAP
    // =========================================================

    private void gpsMap(Project p) {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                        != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOC
            );

            return;
        }

        try {

            LocationManager manager =
                    (LocationManager)
                            getSystemService(
                                    Context.LOCATION_SERVICE
                            );

            Location location =
                    manager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER
                    );

            if (location == null) {

                location =
                        manager.getLastKnownLocation(
                                LocationManager.NETWORK_PROVIDER
                        );
            }

            if (location != null) {

                String url =
                        "geo:" +
                        location.getLatitude() +
                        "," +
                        location.getLongitude() +
                        "?q=" +
                        location.getLatitude() +
                        "," +
                        location.getLongitude();

                Intent intent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url)
                        );

                startActivity(intent);

            } else {

                Toast.makeText(
                        this,
                        "Current location not available. Turn on GPS.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Google Maps not available",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // PDF
    // =========================================================

    private void createPdf(Project p) {

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

            String[] lines =
                    projectReport(p)
                            .split("\n");

            for (String line : lines) {

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

            pdf.finishPage(page);

            File directory =
                    new File(
                            getExternalFilesDir(
                                    Environment.DIRECTORY_DOCUMENTS
                            ),
                            "EDISON_SOLAR"
                    );

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file =
                    new File(
                            directory,
                            p.number +
                                    "_Report.pdf"
                    );

            FileOutputStream output =
                    new FileOutputStream(file);

            pdf.writeTo(output);

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

            try {
                pdf.close();
            } catch (Exception ignored) {
            }
        }
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private void datePicker(
            EditText target
    ) {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, day) -> {

                            target.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d-%02d-%04d",
                                            day,
                                            month + 1,
                                            year
                                    )
                            );
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }

    // =========================================================
    // CONFIRM
    // =========================================================

    private void confirm(
            String message,
            Runnable yes
    ) {

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                yes.run()
                )
                .show();
    }

    // =========================================================
    // WHATSAPP / SHARE
    // =========================================================

    private void shareText(
            String text
    ) {

        Intent intent =
                new Intent(
                        Intent.ACTION_SEND
                );

        intent.setType(
                "text/plain"
        );

        intent.putExtra(
                Intent.EXTRA_TEXT,
                text
        );

        startActivity(
                Intent.createChooser(
                        intent,
                        "Share Report"
                )
        );
    }

    // =========================================================
    // END OF MAIN ACTIVITY
    // =========================================================
}
