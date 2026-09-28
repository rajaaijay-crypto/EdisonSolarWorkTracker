package com.edisonsolar.businesspro;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    DBHelper db;
    LinearLayout root;

    Project photoProject;
    Uri cameraUri;

    final int BLUE = Color.rgb(11, 94, 215);
    final int GREEN = Color.rgb(25, 135, 84);
    final int ORANGE = Color.rgb(240, 138, 36);
    final int PURPLE = Color.rgb(111, 66, 193);
    final int RED = Color.rgb(220, 53, 69);
    final int DARK = Color.rgb(23, 50, 77);
    final int BG = Color.rgb(245, 249, 253);

    int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        db = new DBHelper(this);

        home();
    }

    // =========================================================
    // COMMON UI
    // =========================================================

    TextView txt(String s, float size, boolean bold) {
        TextView t = new TextView(this);

        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(DARK);
        t.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);

        t.setPadding(
                dp(8),
                dp(7),
                dp(8),
                dp(7)
        );

        return t;
    }

    EditText field(String hint, String value) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setText(value);
        e.setTextSize(17);
        e.setTextColor(DARK);
        e.setHintTextColor(Color.GRAY);

        e.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
        );

        return e;
    }

    Button button(
            String text,
            int color,
            View.OnClickListener listener
    ) {

        Button b = new Button(this);

        b.setText(text);
        b.setTextSize(17);
        b.setTypeface(null, Typeface.BOLD);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(dp(60));

        b.setOnClickListener(listener);

        android.graphics.drawable.GradientDrawable g =
                new android.graphics.drawable.GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(dp(16));

        b.setBackground(g);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(62)
                );

        p.setMargins(
                dp(4),
                dp(7),
                dp(4),
                dp(7)
        );

        b.setLayoutParams(p);

        return b;
    }

    TextView card(String text) {

        TextView t = txt(
                text,
                17,
                true
        );

        t.setBackgroundColor(Color.WHITE);

        t.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        p.setMargins(
                dp(4),
                dp(6),
                dp(4),
                dp(6)
        );

        t.setLayoutParams(p);

        return t;
    }

    void page(String title) {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(12),
                dp(14),
                dp(12),
                dp(28)
        );

        root.setBackgroundColor(BG);

        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        Button back =
                new Button(this);

        back.setText("←");
        back.setTextSize(30);
        back.setTextColor(DARK);
        back.setBackgroundColor(Color.TRANSPARENT);

        back.setOnClickListener(
                v -> home()
        );

        header.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(64)
                )
        );

        TextView titleView =
                txt(
                        title,
                        24,
                        true
                );

        header.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        0,
                        dp(64),
                        1
                )
        );

        root.addView(header);

        scroll.addView(root);

        setContentView(scroll);
    }

    double num(String s) {

        try {
            return Double.parseDouble(
                    s.trim().replace(",", "")
            );
        } catch (Exception e) {
            return 0;
        }
    }

    String fmt(double x) {
        return DBHelper.fmt(x);
    }

    String safe(String s) {
        return s == null ? "" : s;
    }

    String today() {
        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());
    }

    String todayISO() {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());
    }

    void confirm(
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
                        "OK",
                        (d, w) -> yes.run()
                )
                .show();
    }

    // =========================================================
    // HOME
    // =========================================================

    void home() {

        page(
                "EDISON SOLAR MANAGER PRO"
        );

        ImageView logo =
                new ImageView(this);

        int logoId =
                getResources().getIdentifier(
                        "edison_solar_logo",
                        "drawable",
                        getPackageName()
                );

        if (logoId != 0) {
            logo.setImageResource(logoId);
        }

        logo.setAdjustViewBounds(true);

        logo.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        root.addView(
                logo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(150)
                )
        );

        root.addView(
                card(db.dashboard())
        );

        root.addView(
                button(
                        "🏢  Companies",
                        BLUE,
                        v -> companies()
                )
        );

        root.addView(
                button(
                        "☀️  Projects",
                        BLUE,
                        v -> projects()
                )
        );

        root.addView(
                button(
                        "👷  Workers / Attendance",
                        PURPLE,
                        v -> workers()
                )
        );

        root.addView(
                button(
                        "💰  Payment Collection",
                        GREEN,
                        v -> payments()
                )
        );

        root.addView(
                button(
                        "🧾  Expenses",
                        ORANGE,
                        v -> expenses()
                )
        );

        root.addView(
                button(
                        "📅  Calendar",
                        PURPLE,
                        v -> calendar()
                )
        );

        root.addView(
                button(
                        "📊  Reports / PDF",
                        PURPLE,
                        v -> reports()
                )
        );
    }

    // =========================================================
    // COMPANIES
    // =========================================================

    void companies() {

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
                                    safe(c.phone) +
                                    "\nProjects: " +
                                    countProjects(c.id) +
                                    " | kW: " +
                                    fmt(companyKw(c.id))
                    )
            );

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            row.addView(
                    button(
                            "✏️ Edit",
                            BLUE,
                            v -> companyDialog(c)
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62),
                            1
                    )
            );

            row.addView(
                    button(
                            "🗑 Delete",
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
                            dp(62),
                            1
                    )
            );

            root.addView(row);
        }
    }

    int countProjects(int companyId) {

        android.database.Cursor c =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT COUNT(*) FROM projects " +
                                        "WHERE company_id=?",
                                new String[]{
                                        String.valueOf(companyId)
                                }
                        );

        int value =
                c.moveToFirst()
                        ? c.getInt(0)
                        : 0;

        c.close();

        return value;
    }

    double companyKw(int companyId) {

        android.database.Cursor c =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT COALESCE(SUM(kw),0) " +
                                        "FROM projects " +
                                        "WHERE company_id=?",
                                new String[]{
                                        String.valueOf(companyId)
                                }
                        );

        double value =
                c.moveToFirst()
                        ? c.getDouble(0)
                        : 0;

        c.close();

        return value;
    }

    void companyDialog(Company old) {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

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

        dialog.setOnShowListener(
                x -> dialog.getButton(-1)
                        .setOnClickListener(v -> {

                            if (name.getText()
                                    .toString()
                                    .trim()
                                    .isEmpty()) {

                                name.setError(
                                        "Required"
                                );

                                return;
                            }

                            if (old == null) {

                                db.addCompany(
                                        name.getText()
                                                .toString()
                                                .trim(),
                                        phone.getText()
                                                .toString()
                                                .trim()
                                );

                            } else {

                                db.updateCompany(
                                        old.id,
                                        name.getText()
                                                .toString()
                                                .trim(),
                                        phone.getText()
                                                .toString()
                                                .trim()
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

    void projects() {

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
                    card(projectSummary(p))
            );

            root.addView(
                    button(
                            "📊 Open Full Project Report",
                            BLUE,
                            v -> projectFullReport(p)
                    )
            );

            root.addView(
                    button(
                            "✏️ Edit / Delete Project",
                            PURPLE,
                            v -> projectMenu(p)
                    )
            );
        }
    }

    String projectSummary(Project p) {

        double collection =
                db.collection(p.id);

        double pending =
                db.pending(p.id);

        double expense =
                db.expenses(p.id);

        double salary =
                projectSalary(p);

        int workDays =
                projectWorkDays(p);

        double profit =
                collection -
                        expense -
                        salary;

        return
                "☀️ PROJECT: " +
                        p.number +

                        "\nCustomer: " +
                        p.customer +

                        "\nCompany: " +
                        p.company +

                        "\nSite: " +
                        p.site +

                        "\n⚡ Solar: " +
                        fmt(p.kw) +
                        " kW" +

                        "\n💰 Collection: ₹" +
                        fmt(collection) +

                        "\n💸 Expenses: ₹" +
                        fmt(expense) +

                        "\n👷 Work Days: " +
                        workDays +

                        "\n👷 Site Salary: ₹" +
                        fmt(salary) +

                        "\n📊 Profit: ₹" +
                        fmt(profit) +

                        "\n🔴 Pending Collection: ₹" +
                        fmt(pending) +

                        "\n📅 Date: " +
                        p.date;
    }

    int projectWorkDays(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT COUNT(DISTINCT date) " +
                                        "FROM attendance " +
                                        "WHERE status='Present' " +
                                        "AND (site=? OR site LIKE ? OR site LIKE ?)",
                                new String[]{
                                        p.site,
                                        p.number + "%",
                                        "%" + p.number + "%"
                                }
                        );

        int value =
                c.moveToFirst()
                        ? c.getInt(0)
                        : 0;

        c.close();

        return value;
    }

    double projectSalary(Project p) {

        double total = 0;

        android.database.Cursor c =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT worker_id,COUNT(*) " +
                                        "FROM attendance " +
                                        "WHERE status='Present' " +
                                        "AND (site=? OR site LIKE ? OR site LIKE ?) " +
                                        "GROUP BY worker_id",
                                new String[]{
                                        p.site,
                                        p.number + "%",
                                        "%" + p.number + "%"
                                }
                        );

        while (c.moveToNext()) {

            Worker w =
                    db.worker(c.getInt(0));

            if (w != null) {

                total +=
                        c.getInt(1) *
                                w.dailySalary;
            }
        }

        c.close();

        return total;
    }

    void projectDialog(Project old) {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        EditText company =
                field(
                        "Company",
                        old == null
                                ? ""
                                : old.company
                );

        EditText customer =
                field(
                        "Customer",
                        old == null
                                ? ""
                                : old.customer
                );

        EditText phone =
                field(
                        "Phone",
                        old == null
                                ? ""
                                : old.phone
                );

        EditText site =
                field(
                        "Site",
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
                        "Date",
                        old == null
                                ? today()
                                : old.date
                );

        EditText status =
                field(
                        "Status",
                        old == null
                                ? "Pending"
                                : old.status
                );

        EditText work =
                field(
                        "Work Detail",
                        old == null
                                ? ""
                                : old.work
                );

        layout.addView(company);
        layout.addView(customer);
        layout.addView(phone);
        layout.addView(site);
        layout.addView(kw);
        layout.addView(amount);
        layout.addView(date);
        layout.addView(status);
        layout.addView(work);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                old == null
                                        ? "Add Project"
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

        dialog.setOnShowListener(
                x -> dialog.getButton(-1)
                        .setOnClickListener(v -> {

                            if (customer.getText()
                                    .toString()
                                    .trim()
                                    .isEmpty()) {

                                customer.setError(
                                        "Customer required"
                                );

                                return;
                            }

                            if (old == null) {

                                db.addProject(
                                        0,
                                        company.getText().toString(),
                                        customer.getText().toString(),
                                        phone.getText().toString(),
                                        site.getText().toString(),
                                        num(kw.getText().toString()),
                                        num(amount.getText().toString()),
                                        date.getText().toString(),
                                        status.getText().toString(),
                                        work.getText().toString()
                                );

                            } else {

                                db.updateProject(
                                        old,
                                        old.companyId,
                                        company.getText().toString(),
                                        customer.getText().toString(),
                                        phone.getText().toString(),
                                        site.getText().toString(),
                                        num(kw.getText().toString()),
                                        num(amount.getText().toString()),
                                        date.getText().toString(),
                                        status.getText().toString(),
                                        work.getText().toString()
                                );
                            }

                            dialog.dismiss();

                            projects();
                        })
        );

        dialog.show();
    }

    void projectMenu(Project p) {

        String[] items = {
                "✏️ Edit Project",
                "💰 Payments",
                "🧾 Expenses",
                "📷 Photos",
                "📍 GPS / Map",
                "📄 PDF",
                "📱 WhatsApp",
                "🗑 Delete"
        };

        new AlertDialog.Builder(this)
                .setTitle(p.customer)
                .setItems(
                        items,
                        (d, position) -> {

                            if (position == 0) {

                                projectDialog(p);

                            } else if (position == 1) {

                                projectPayments(p);

                            } else if (position == 2) {

                                projectExpenses(p);

                            } else if (position == 3) {

                                photos(p);

                            } else if (position == 4) {

                                gps(p);

                            } else if (position == 5) {

                                pdf(p);

                            } else if (position == 6) {

                                share(
                                        projectReport(p)
                                );

                            } else {

                                confirm(
                                        "Delete project?",
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

    // =========================================================
    // PROJECT REPORT
    // =========================================================

    void projectFullReport(Project p) {

        page("Project Report");

        root.addView(
                card(projectSummary(p))
        );

        root.addView(
                txt(
                        "💰 Collection History",
                        20,
                        true
                )
        );

        showPaymentHistory(
                root,
                p
        );

        root.addView(
                txt(
                        "💸 Expense History",
                        20,
                        true
                )
        );

        showProjectExpenseHistory(p);

        root.addView(
                txt(
                        "👷 Workers / Salary",
                        20,
                        true
                )
        );

        showProjectWorkers(p);

        root.addView(
                txt(
                        "📅 Work Days",
                        20,
                        true
                )
        );

        showProjectWorkDays(p);

        root.addView(
                button(
                        "📄 Generate Project PDF",
                        PURPLE,
                        v -> pdf(p)
                )
        );

        root.addView(
                button(
                        "📱 WhatsApp / Share Project Report",
                        GREEN,
                        v -> share(
                                projectReport(p)
                        )
                )
        );
    }

    String projectReport(Project p) {

        double collection =
                db.collection(p.id);

        double pending =
                db.pending(p.id);

        double expense =
                db.expenses(p.id);

        double salary =
                projectSalary(p);

        int workDays =
                projectWorkDays(p);

        double profit =
                collection -
                        expense -
                        salary;

        StringBuilder s =
                new StringBuilder();

        s.append(
                "EDISON SOLAR MANAGER PRO\n\n"
        );

        s.append(
                "PROJECT: "
        ).append(
                p.number
        ).append("\n");

        s.append(
                "Company: "
        ).append(
                safe(p.company)
        ).append("\n");

        s.append(
                "Customer: "
        ).append(
                safe(p.customer)
        ).append("\n");

        s.append(
                "Phone: "
        ).append(
                safe(p.phone)
        ).append("\n");

        s.append(
                "Site: "
        ).append(
                safe(p.site)
        ).append("\n");

        s.append(
                "Solar: "
        ).append(
                fmt(p.kw)
        ).append(
                " kW\n"
        );

        s.append(
                "Project Value: ₹"
        ).append(
                fmt(p.amount)
        ).append("\n");

        s.append(
                "Date: "
        ).append(
                safe(p.date)
        ).append("\n");

        s.append(
                "Status: "
        ).append(
                safe(p.status)
        ).append("\n\n");

        s.append(
                "TOTAL COLLECTION: ₹"
        ).append(
                fmt(collection)
        ).append("\n");

        s.append(
                "PENDING COLLECTION: ₹"
        ).append(
                fmt(pending)
        ).append("\n");

        s.append(
                "TOTAL EXPENSES: ₹"
        ).append(
                fmt(expense)
        ).append("\n");

        s.append(
                "WORK DAYS: "
        ).append(
                workDays
        ).append("\n");

        s.append(
                "SITE SALARY: ₹"
        ).append(
                fmt(salary)
        ).append("\n");

        s.append(
                "PROFIT: ₹"
        ).append(
                fmt(profit)
        ).append("\n\n");

        s.append(
                "COLLECTION HISTORY\n"
        );

        android.database.Cursor pc =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT date,amount,mode,note " +
                                        "FROM payments " +
                                        "WHERE project_id=? " +
                                        "ORDER BY date,id",
                                new String[]{
                                        String.valueOf(p.id)
                                }
                        );

        boolean paymentFound = false;

        while (pc.moveToNext()) {

            paymentFound = true;

            s.append("- ")
                    .append(
                            safe(pc.getString(0))
                    )
                    .append(" | ₹")
                    .append(
                            fmt(pc.getDouble(1))
                    )
                    .append(" | ")
                    .append(
                            safe(pc.getString(2))
                    );

            String note =
                    safe(pc.getString(3));

            if (!note.isEmpty()) {

                s.append(
                        " | "
                ).append(note);
            }

            s.append("\n");
        }

        pc.close();

        if (!paymentFound) {

            s.append(
                    "No collection entries.\n"
            );
        }

        s.append(
                "\nEXPENSE HISTORY\n"
        );

        android.database.Cursor ec =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT date,category,amount,note " +
                                        "FROM expenses " +
                                        "WHERE project_id=? " +
                                        "ORDER BY date,id",
                                new String[]{
                                        String.valueOf(p.id)
                                }
                        );

        boolean expenseFound = false;

        while (ec.moveToNext()) {

            expenseFound = true;

            s.append("- ")
                    .append(
                            safe(ec.getString(0))
                    )
                    .append(" | ")
                    .append(
                            safe(ec.getString(1))
                    )
                    .append(" | ₹")
                    .append(
                            fmt(ec.getDouble(2))
                    );

            String note =
                    safe(ec.getString(3));

            if (!note.isEmpty()) {

                s.append(
                        " | "
                ).append(note);
            }

            s.append("\n");
        }

        ec.close();

        if (!expenseFound) {

            s.append(
                    "No expense entries.\n"
            );
        }

        return s.toString();
    }

    // =========================================================
    // PAYMENTS
    // =========================================================

    void payments() {

        page("Payment Collection");

        root.addView(
                card(
                        "💰 PAYMENT COLLECTION\n" +
                                "Project wise Collection / Pending"
                )
        );

        for (Project p : db.projects()) {

            root.addView(
                    card(
                            projectSummary(p)
                    )
            );

            root.addView(
                    button(
                            "💰 Add / Edit Collection",
                            GREEN,
                            v -> projectPayments(p)
                    )
            );

            root.addView(
                    button(
                            "📊 Full Project Report",
                            BLUE,
                            v -> projectFullReport(p)
                    )
            );
        }
    }

    void projectPayments(Project p) {

        page(
                "Payments / Collection"
        );

        root.addView(
                card(
                        "☀️ " +
                                p.number +
                                "\nCustomer: " +
                                p.customer +
                                "\nProject Value: ₹" +
                                fmt(p.amount) +
                                "\nCollection: ₹" +
                                fmt(db.collection(p.id)) +
                                "\nPending: ₹" +
                                fmt(db.pending(p.id))
                )
        );

        root.addView(
                button(
                        "+ Add Collection",
                        GREEN,
                        v -> paymentDialog(
                                p,
                                null
                        )
                )
        );

        showPaymentHistory(
                root,
                p
        );
    }

    void showPaymentHistory(
            LinearLayout area,
            Project p
    ) {

        android.database.Cursor c =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT id,date,amount,mode,note " +
                                        "FROM payments " +
                                        "WHERE project_id=? " +
                                        "ORDER BY date DESC,id DESC",
                                new String[]{
                                        String.valueOf(p.id)
                                }
                        );

        if (!c.moveToFirst()) {

            area.addView(
                    card(
                            "📭 No collection entries yet."
                    )
            );

            c.close();

            return;
        }

        do {

            int id =
                    c.getInt(0);

            String date =
                    safe(c.getString(1));

            double amount =
                    c.getDouble(2);

            String mode =
                    safe(c.getString(3));

            String note =
                    safe(c.getString(4));

            area.addView(
                    card(
                            "📅 " +
                                    date +
                                    "\n💰 ₹" +
                                    fmt(amount) +
                                    "\n💳 " +
                                    mode +
                                    (
                                            note.isEmpty()
                                                    ? ""
                                                    : "\n📝 " +
                                                    note
                                    )
                    )
            );

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            row.addView(
                    button(
                            "✏️ Edit",
                            BLUE,
                            v -> paymentDialog(
                                    p,
                                    id
                            )
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62),
                            1
                    )
            );

            row.addView(
                    button(
                            "🗑 Delete",
                            RED,
                            v -> confirm(
                                    "Delete this collection?",
                                    () -> {

                                        db.getWritableDatabase()
                                                .delete(
                                                        "payments",
                                                        "id=?",
                                                        new String[]{
                                                                String.valueOf(
                                                                        id
                                                                )
                                                        }
                                                );

                                        projectPayments(p);
                                    }
                            )
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62),
                            1
                    )
            );

            area.addView(row);

        } while (c.moveToNext());

        c.close();
    }

    void paymentDialog(
            Project p,
            Integer paymentId
    ) {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        EditText amount =
                field(
                        "Collection Amount ₹",
                        ""
                );

        EditText date =
                field(
                        "Collection Date YYYY-MM-DD",
                        todayISO()
                );

        Spinner mode =
                new Spinner(this);

        String[] modes = {
                "Cash",
                "UPI",
                "Bank Transfer",
                "Cheque",
                "Other"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        modes
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        mode.setAdapter(adapter);

        EditText note =
                field(
                        "Collection Note",
                        ""
                );

        layout.addView(
                txt(
                        "☀️ " +
                                p.number +
                                " • " +
                                p.customer,
                        19,
                        true
                )
        );

        layout.addView(amount);
        layout.addView(date);

        layout.addView(
                txt(
                        "💳 Payment Mode",
                        17,
                        true
                )
        );

        layout.addView(
                mode,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        layout.addView(note);

        if (paymentId != null) {

            android.database.Cursor c =
                    db.getReadableDatabase()
                            .rawQuery(
                                    "SELECT amount,date,mode,note " +
                                            "FROM payments " +
                                            "WHERE id=?",
                                    new String[]{
                                            String.valueOf(
                                                    paymentId
                                            )
                                    }
                            );

            if (c.moveToFirst()) {

                amount.setText(
                        String.valueOf(
                                c.getDouble(0)
                        )
                );

                date.setText(
                        safe(c.getString(1))
                );

                note.setText(
                        safe(c.getString(3))
                );

                for (int i = 0;
                     i < modes.length;
                     i++) {

                    if (modes[i]
                            .equalsIgnoreCase(
                                    safe(c.getString(2))
                            )) {

                        mode.setSelection(i);

                        break;
                    }
                }
            }

            c.close();
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                paymentId == null
                                        ? "Add Collection"
                                        : "Edit Collection"
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

        dialog.setOnShowListener(
                x -> dialog.getButton(-1)
                        .setOnClickListener(v -> {

                            double value =
                                    num(
                                            amount.getText()
                                                    .toString()
                                    );

                            if (value <= 0) {

                                amount.setError(
                                        "Enter amount"
                                );

                                return;
                            }

                            ContentValues cv =
                                    new ContentValues();

                            cv.put(
                                    "project_id",
                                    p.id
                            );

                            cv.put(
                                    "amount",
                                    value
                            );

                            cv.put(
                                    "date",
                                    date.getText()
                                            .toString()
                                            .trim()
                            );

                            cv.put(
                                    "mode",
                                    mode.getSelectedItem()
                                            .toString()
                            );

                            cv.put(
                                    "note",
                                    note.getText()
                                            .toString()
                                            .trim()
                            );

                            if (paymentId == null) {

                                db.getWritableDatabase()
                                        .insert(
                                                "payments",
                                                null,
                                                cv
                                        );

                            } else {

                                db.getWritableDatabase()
                                        .update(
                                                "payments",
                                                cv,
                                                "id=?",
                                                new String[]{
                                                        String.valueOf(
                                                                paymentId
                                                        )
                                                }
                                        );
                            }

                            dialog.dismiss();

                            projectPayments(p);
                        })
        );

        dialog.show();
    }

    // =========================================================
    // EXPENSES
    // =========================================================

    void expenses() {

        page("Expenses");

        root.addView(
                button(
                        "+ Add Expense",
                        ORANGE,
                        v -> expenseDialog(
                                null,
                                null
                        )
                )
        );

        boolean found = false;

        for (Project p : db.projects()) {

            android.database.Cursor c =
                    db.getReadableDatabase()
                            .rawQuery(
                                    "SELECT COUNT(*) " +
                                            "FROM expenses " +
                                            "WHERE project_id=?",
                                    new String[]{
                                            String.valueOf(p.id)
                                    }
                            );

            int count =
                    c.moveToFirst()
                            ? c.getInt(0)
                            : 0;

            c.close();

            if (count > 0) {

                found = true;

                root.addView(
                        card(
                                "☀️ " +
                                        p.number +
                                        " • " +
                                        p.customer +
                                        "\n💸 Expenses: ₹" +
                                        fmt(
                                                db.expenses(
                                                        p.id
                                                )
                                        ) +
                                        "\n📊 Profit: ₹" +
                                        fmt(
                                                db.profit(
                                                        p.id
                                                )
                                        )
                        )
                );

                addExpenseEntries(
                        p.id,
                        p
                );
            }
        }

        if (!found) {

            root.addView(
                    card(
                            "No expenses added yet."
                    )
            );
        }
    }

    void projectExpenses(Project p) {

        page("Project Expenses");

        root.addView(
                card(
                        "☀️ " +
                                p.number +
                                "\n" +
                                p.customer +
                                "\n💸 Total Expenses: ₹" +
                                fmt(db.expenses(p.id)) +
                                "\n📊 Profit: ₹" +
                                fmt(db.profit(p.id))
                )
        );

        root.addView(
                button(
                        "+ Add Expense",
                        ORANGE,
                        v -> expenseDialog(
                                p,
                                null
                        )
                )
        );

        addExpenseEntries(
                p.id,
                p
        );
    }

    void addExpenseEntries(
            int projectId,
            Project project
    ) {

        android.database.Cursor c =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT id,amount,category,date,note " +
                                        "FROM expenses " +
                                        "WHERE project_id=? " +
                                        "ORDER BY date DESC,id DESC",
                                new String[]{
                                        String.valueOf(
                                                projectId
                                        )
                                }
                        );

        while (c.moveToNext()) {

            int id =
                    c.getInt(0);

            double amount =
                    c.getDouble(1);

            String category =
                    safe(c.getString(2));

            String date =
                    safe(c.getString(3));

            String note =
                    safe(c.getString(4));

            root.addView(
                    card(
                            "💸 ₹" +
                                    fmt(amount) +
                                    "\n📂 " +
                                    category +
                                    "\n📅 " +
                                    date +
                                    (
                                            note.isEmpty()
                                                    ? ""
                                                    : "\n📝 " +
                                                    note
                                    )
                    )
            );

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            row.addView(
                    button(
                            "✏️ Edit",
                            BLUE,
                            v -> expenseDialog(
                                    project,
                                    id
                            )
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62),
                            1
                    )
            );

            row.addView(
                    button(
                            "🗑 Delete",
                            RED,
                            v -> confirm(
                                    "Delete this expense?",
                                    () -> {

                                        deleteExpense(id);

                                        if (project != null) {
                                            projectExpenses(project);
                                        } else {
                                            expenses();
                                        }
                                    }
                            )
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62),
                            1
                    )
            );

            root.addView(row);
        }

        c.close();
    }

    void showProjectExpenseHistory(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase()
                        .rawQuery(
                                "SELECT id,date,category,amount,note " +
                                        "FROM expenses " +
                                        "WHERE project_id=? " +
                                        "ORDER BY date DESC,id DESC",
                                new String[]{
                                        String.valueOf(p.id)
                                }
                        );

        if (!c.moveToFirst()) {

            root.addView(
                    card(
                            "No expense entries."
                    )
            );

            c.close();

            return;
        }

        do {

            int id =
                    c.getInt(0);

            String date =
                    safe(c.getString(1));

            String category =
                    safe(c.getString(2));

            double amount =
                    c.getDouble(3);

            String note =
                    safe(c.getString(4));

            root.addView(
                    card(
                            "📅 " +
                                    date +
                                    "\n📂 " +
                                    category +
                                    "\n💸 ₹" +
                                    fmt(amount) +
                                    (
                                            note.isEmpty()
                                                    ? ""
                                                    : "\n📝 " +
                                                    note
                                    )
                    )
            );

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            row.addView(
                    button(
                            "✏️ Edit",
                            BLUE,
                            v -> expenseDialog(
                                    p,
                                    id
                            )
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62),
                            1
                    )
            );

            row.addView(
                    button(
                            "🗑 Delete",
                            RED,
                            v -> confirm(
                                    "Delete this expense?",
                                    () -> {

                                        deleteExpense(id);

                                        projectFullReport(p);
                                    }
                            )
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62),
                            1
                    )
            );

            root.addView(row);

        } while (c.moveToNext());

        c.close();
    }

    void expenseDialog(
            Project selectedProject,
            Integer expenseId
    ) {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        ArrayList<Project> projects =
                db.projects();

        Spinner projectSpinner =
                new Spinner(this);

        ArrayList<String> names =
                new ArrayList<>();

        names.add(
                "Select Project / Site"
        );

        for (Project p : projects) {

            names.add(
                    p.number +
                            " • " +
                            p.customer
            );
        }

        ArrayAdapter<String> projectAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        names
                );

        projectAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        projectSpinner.setAdapter(
                projectAdapter
        );

        EditText amount =
                field(
                        "Expense Amount ₹",
                        ""
                );

        Spinner type =
                new Spinner(this);

        String[] types = {
                "Other",
                "Material",
                "Labour",
                "Tea",
                "Lunch",
                "Transportation",
                "Fuel",
                "Tools",
                "Travel",
                "Site Expense"
        };

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        types
                );

        typeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        type.setAdapter(typeAdapter);

        EditText date =
                field(
                        "Date YYYY-MM-DD",
                        todayISO()
                );

        EditText note =
                field(
                        "Note",
                        ""
                );

        layout.addView(
                txt(
                        "Project / Site",
                        17,
                        true
                )
        );

        layout.addView(
                projectSpinner,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        layout.addView(amount);

        layout.addView(
                txt(
                        "Expense Type",
                        17,
                        true
                )
        );

        layout.addView(
                type,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        layout.addView(date);
        layout.addView(note);

        if (expenseId != null) {

            android.database.Cursor c =
                    db.getReadableDatabase()
                            .rawQuery(
                                    "SELECT project_id,amount,category,date,note " +
                                            "FROM expenses " +
                                            "WHERE id=?",
                                    new String[]{
                                            String.valueOf(
                                                    expenseId
                                            )
                                    }
                            );

            if (c.moveToFirst()) {

                int oldProject =
                        c.getInt(0);

                amount.setText(
                        String.valueOf(
                                c.getDouble(1)
                        )
                );

                String oldType =
                        safe(c.getString(2));

                date.setText(
                        safe(c.getString(3))
                );

                note.setText(
                        safe(c.getString(4))
                );

                for (int i = 0;
                     i < projects.size();
                     i++) {

                    if (projects.get(i).id ==
                            oldProject) {

                        projectSpinner
                                .setSelection(
                                        i + 1
                                );

                        break;
                    }
                }

                for (int i = 0;
                     i < types.length;
                     i++) {

                    if (types[i]
                            .equalsIgnoreCase(
                                    oldType
                            )) {

                        type.setSelection(i);

                        break;
                    }
                                        type.setSelection(i);

                        break;
                    }
                }

                c.close();
            }
        } else if (selectedProject != null) {

            for (int i = 0; i < projects.size(); i++) {

                if (projects.get(i).id == selectedProject.id) {

                    projectSpinner.setSelection(i + 1);

                    break;
                }
            }
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                expenseId == null
                                        ? "Add Expense"
                                        : "Edit Expense"
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

        dialog.setOnShowListener(
                x -> dialog.getButton(-1)
                        .setOnClickListener(v -> {

                            if (projectSpinner.getSelectedItemPosition() <= 0) {

                                Toast.makeText(
                                        this,
                                        "Select Project / Site",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            double value =
                                    num(
                                            amount.getText()
                                                    .toString()
                                    );

                            if (value <= 0) {

                                amount.setError(
                                        "Enter amount"
                                );

                                return;
                            }

                            int projectId =
                                    projects.get(
                                            projectSpinner
                                                    .getSelectedItemPosition() - 1
                                    ).id;

                            ContentValues cv =
                                    new ContentValues();

                            cv.put(
                                    "project_id",
                                    projectId
                            );

                            cv.put(
                                    "amount",
                                    value
                            );

                            cv.put(
                                    "category",
                                    type.getSelectedItem()
                                            .toString()
                            );

                            cv.put(
                                    "date",
                                    date.getText()
                                            .toString()
                                            .trim()
                            );

                            cv.put(
                                    "note",
                                    note.getText()
                                            .toString()
                                            .trim()
                            );

                            if (expenseId == null) {

                                db.getWritableDatabase()
                                        .insert(
                                                "expenses",
                                                null,
                                                cv
                                        );

                            } else {

                                db.getWritableDatabase()
                                        .update(
                                                "expenses",
                                                cv,
                                                "id=?",
                                                new String[]{
                                                        String.valueOf(
                                                                expenseId
                                                        )
                                                }
                                        );
                            }

                            dialog.dismiss();

                            if (selectedProject != null) {

                                projectExpenses(
                                        selectedProject
                                );

                            } else {

                                expenses();
                            }
                        })
        );

        dialog.show();
    }
}    
