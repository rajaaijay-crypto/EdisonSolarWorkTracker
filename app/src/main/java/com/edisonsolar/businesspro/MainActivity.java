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
        return (int) (n * getResources().getDisplayMetrics().density + .5f);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        db = new DBHelper(this);
        home();
    }

    TextView txt(String s, float z, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(z);
        t.setTextColor(DARK);
        t.setTypeface(null, bold ? 1 : 0);
        t.setPadding(dp(8), dp(7), dp(8), dp(7));
        return t;
    }

    EditText field(String hint, String val) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(val);
        e.setTextSize(17);
        e.setTextColor(DARK);
        e.setHintTextColor(Color.GRAY);
        e.setPadding(dp(12), dp(5), dp(12), dp(5));
        e.setSingleLine(false);
        return e;
    }

    Button button(String s, int color, View.OnClickListener l) {
        Button b = new Button(this);

        b.setText(s);
        b.setTextSize(17);
        b.setTypeface(null, 1);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(dp(60));
        b.setOnClickListener(l);

        GradientDrawableCompat(b, color);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(62));

        p.setMargins(dp(4), dp(7), dp(4), dp(7));
        b.setLayoutParams(p);

        return b;
    }

    void GradientDrawableCompat(Button b, int c) {
        android.graphics.drawable.GradientDrawable g =
                new android.graphics.drawable.GradientDrawable();

        g.setColor(c);
        g.setCornerRadius(dp(16));

        b.setBackground(g);
    }

    TextView card(String s) {

        TextView t = txt(s, 17, true);

        t.setBackgroundColor(Color.WHITE);
        t.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);

        p.setMargins(dp(4), dp(6), dp(4), dp(6));

        t.setLayoutParams(p);

        return t;
    }

    void page(String title) {

        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        root.setPadding(
                dp(12),
                dp(14),
                dp(12),
                dp(28)
        );

        root.setBackgroundColor(BG);

        LinearLayout h = new LinearLayout(this);
        h.setGravity(Gravity.CENTER_VERTICAL);

        Button back = new Button(this);
        back.setText("←");
        back.setTextSize(30);
        back.setTextColor(DARK);
        back.setBackgroundColor(Color.TRANSPARENT);
        back.setOnClickListener(v -> home());

        h.addView(
                back,
                new LinearLayout.LayoutParams(dp(58), dp(64))
        );

        TextView t = txt(title, 24, true);

        h.addView(
                t,
                new LinearLayout.LayoutParams(0, dp(64), 1)
        );

        root.addView(h);

        sc.addView(root);

        setContentView(sc);
    }

    void home() {

        page("EDISON SOLAR MANAGER PRO");

        ImageView im = new ImageView(this);

        int id = getResources().getIdentifier(
                "edison_solar_logo",
                "drawable",
                getPackageName()
        );

        if (id != 0)
            im.setImageResource(id);

        im.setAdjustViewBounds(true);
        im.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        root.addView(
                im,
                new LinearLayout.LayoutParams(-1, dp(150))
        );

        root.addView(card(db.dashboard()));

        root.addView(
                button("🏢  Companies", BLUE, v -> companies())
        );

        root.addView(
                button("☀️  Projects", BLUE, v -> projects())
        );

        root.addView(
                button("👷  Workers / Attendance", PURPLE, v -> workers())
        );

        root.addView(
                button("💰  Payment Collection", GREEN, v -> payments())
        );

        root.addView(
                button("🧾  Expenses", ORANGE, v -> expenses())
        );

        root.addView(
                button("📅  Calendar", PURPLE, v -> calendar())
        );

        root.addView(
                button("📊  Reports / PDF", PURPLE, v -> reports())
        );
    }

    // ---------------------------------------------------------
    // COMPANIES
    // ---------------------------------------------------------

    void companies() {

        page("Companies");

        root.addView(
                button("+  Add Company", BLUE,
                        v -> companyDialog(null))
        );

        for (Company c : db.companies()) {

            root.addView(
                    card(
                            c.name +
                                    "\nPhone: " + c.phone +
                                    "\nProjects: " + countProjects(c.id) +
                                    " | kW: " + fmt(companyKw(c.id))
                    )
            );

            LinearLayout r = new LinearLayout(this);

            r.addView(
                    button("Edit", BLUE,
                            v -> companyDialog(c)),
                    new LinearLayout.LayoutParams(
                            0, dp(58), 1
                    )
            );

            r.addView(
                    button("Delete", RED,
                            v -> confirm(
                                    "Delete company?",
                                    () -> {
                                        db.deleteCompany(c.id);
                                        companies();
                                    }
                            )),
                    new LinearLayout.LayoutParams(
                            0, dp(58), 1
                    )
            );

            root.addView(r);
        }
    }

    int countProjects(int id) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT COUNT(*) FROM projects WHERE company_id=?",
                        new String[]{"" + id}
                );

        int x = c.moveToFirst() ? c.getInt(0) : 0;

        c.close();

        return x;
    }

    double companyKw(int id) {

        return q(
                "SELECT COALESCE(SUM(kw),0) FROM projects WHERE company_id=?",
                id
        );
    }

    double q(String sql, int id) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        sql,
                        new String[]{"" + id}
                );

        double x = c.moveToFirst() ? c.getDouble(0) : 0;

        c.close();

        return x;
    }

    double fmtN(String s) {

        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    String fmt(double x) {
        return DBHelper.fmt(x);
    }

    void companyDialog(Company old) {

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);

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
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Save", null)
                        .create();

        d.setOnShowListener(x ->
                d.getButton(-1).setOnClickListener(v -> {

                    if (n.getText().toString().trim().isEmpty()) {
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

    // ---------------------------------------------------------
    // PROJECTS
    // ---------------------------------------------------------

    void projects() {

        page("Projects");

        root.addView(
                button("+  Add Project", BLUE,
                        v -> projectDialog(null))
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

        double collection = db.collection(p.id);
        double pending = db.pending(p.id);
        double expense = db.expenses(p.id);

        double salary = projectSalary(p);

        int workDays = projectWorkDays(p);

        double profit =
                collection -
                        expense -
                        salary;

        return
                "Project: " + p.number +
                        " • " + p.customer +

                        "\nCompany: " + p.company +

                        "\nSite: " + p.site +

                        "\n⚡ Solar: " +
                        fmt(p.kw) + " kW" +

                        "\n💰 Collection: ₹" +
                        fmt(collection) +

                        "\n💸 Expenses: ₹" +
                        fmt(expense) +

                        "\n👷 Work: " +
                        workDays + " Days" +

                        "\n👷 Site Salary: ₹" +
                        fmt(salary) +

                        "\n📊 Profit: ₹" +
                        fmt(profit) +

                        "\n💵 Pending Collection: ₹" +
                        fmt(pending) +

                        "\n📅 Project Date: " +
                        p.date;
    }

    // =========================================================
    // UPDATED: PRESENT + HALF DAY WORK DAYS
    // =========================================================

    int projectWorkDays(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT COUNT(DISTINCT date) " +
                                "FROM attendance " +
                                "WHERE status IN ('Present','Half Day') " +
                                "AND (site=? OR site LIKE ? OR site LIKE ?)",
                        new String[]{
                                p.site,
                                p.number + "%",
                                "%" + p.number + "%"
                        }
                );

        int n =
                c.moveToFirst()
                        ? c.getInt(0)
                        : 0;

        c.close();

        return n;
    }

    // =========================================================
    // UPDATED: HALF DAY = 50% SALARY
    // =========================================================

    double projectSalary(Project p) {

        double total = 0;

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT a.worker_id,a.status " +
                                "FROM attendance a " +
                                "WHERE a.status IN ('Present','Half Day') " +
                                "AND (a.site=? OR a.site LIKE ? OR a.site LIKE ?)",
                        new String[]{
                                p.site,
                                p.number + "%",
                                "%" + p.number + "%"
                        }
                );

        while (c.moveToNext()) {

            Worker w =
                    db.worker(c.getInt(0));

            if (w == null)
                continue;

            String status =
                    safeText(c.getString(1));

            if (
                    "Present".equalsIgnoreCase(status)
            ) {

                total +=
                        w.dailySalary;

            } else if (
                    "Half Day".equalsIgnoreCase(status)
            ) {

                total +=
                        w.dailySalary * 0.5;
            }
        }

        c.close();

        return total;
    }

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

        showPaymentHistory(p);

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
                        v -> share(projectReport(p))
                )
        );
    }

    void projectDialog(Project old) {

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.VERTICAL
        );

        EditText co =
                field(
                        "Company",
                        old == null ? "" : old.company
                );

        EditText cu =
                field(
                        "Customer",
                        old == null ? "" : old.customer
                );

        EditText ph =
                field(
                        "Phone",
                        old == null ? "" : old.phone
                );

        EditText si =
                field(
                        "Site",
                        old == null ? "" : old.site
                );

        EditText kw =
                field(
                        "Solar kW",
                        old == null ? "" : "" + old.kw
                );

        EditText am =
                field(
                        "Amount",
                        old == null ? "" : "" + old.amount
                );

        EditText da =
                field(
                        "Date",
                        old == null ? today() : old.date
                );

        EditText st =
                field(
                        "Status",
                        old == null ? "Pending" : old.status
                );

        EditText wo =
                field(
                        "Work Detail",
                        old == null ? "" : old.work
                );

        for (
                EditText e :
                new EditText[]{
                        co, cu, ph, si,
                        kw, am, da, st, wo
                }
        ) {
            l.addView(e);
        }

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle(
                                old == null
                                        ? "Add Project"
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

                    if (old == null) {

                        db.addProject(
                                0,
                                co.getText().toString(),
                                cu.getText().toString(),
                                ph.getText().toString(),
                                si.getText().toString(),
                                fmtN(kw.getText().toString()),
                                fmtN(am.getText().toString()),
                                da.getText().toString(),
                                st.getText().toString(),
                                wo.getText().toString()
                        );

                    } else {

                        db.updateProject(
                                old,
                                old.companyId,
                                co.getText().toString(),
                                cu.getText().toString(),
                                ph.getText().toString(),
                                si.getText().toString(),
                                fmtN(kw.getText().toString()),
                                fmtN(am.getText().toString()),
                                da.getText().toString(),
                                st.getText().toString(),
                                wo.getText().toString()
                        );
                    }

                    d.dismiss();
                    projects();
                })
        );

        d.show();
    }

    void projectMenu(Project p) {

        String[] a = {
                "Edit Project",
                "Payments",
                "Expenses",
                "Photos",
                "GPS / Map",
                "PDF",
                "WhatsApp",
                "Delete"
        };

        new AlertDialog.Builder(this)
                .setTitle(p.customer)
                .setItems(a, (d, w) -> {

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
                                "Delete project?",
                                () -> {
                                    db.deleteProject(p.id);
                                    projects();
                                }
                        );
                })
                .show();
    }

    // ---------------------------------------------------------
    // PROJECT REPORT
    // ---------------------------------------------------------

    String projectReport(Project p) {

        double collection =
                db.collection(p.id);

        double pending =
                db.pending(p.id);

        double expense =
                db.expenses(p.id);

        double salary =
                projectSalary(p);

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
        ).append(p.number).append("\n");

        s.append(
                "Company: "
        ).append(p.company).append("\n");

        s.append(
                "Customer: "
        ).append(p.customer).append("\n");

        s.append(
                "Phone: "
        ).append(p.phone).append("\n");

        s.append(
                "Site: "
        ).append(p.site).append("\n");

        s.append(
                "Solar: "
        ).append(fmt(p.kw))
                .append(" kW\n");

        s.append(
                "Project Value: ₹"
        ).append(fmt(p.amount))
                .append("\n");

        s.append(
                "Collection: ₹"
        ).append(fmt(collection))
                .append("\n");

        s.append(
                "Pending: ₹"
        ).append(fmt(pending))
                .append("\n");

        s.append(
                "Expenses: ₹"
        ).append(fmt(expense))
                .append("\n");

        s.append(
                "Site Salary: ₹"
        ).append(fmt(salary))
                .append("\n");

        s.append(
                "Work Days: "
        ).append(projectWorkDays(p))
                .append("\n");

        s.append(
                "Profit: ₹"
        ).append(fmt(profit))
                .append("\n");

        s.append(
                "Date: "
        ).append(p.date)
                .append("\n");

        s.append(
                "Status: "
        ).append(p.status)
                .append("\n");

        s.append(
                "Work: "
        ).append(p.work)
                .append("\n\n");

        s.append(
                "COLLECTION HISTORY\n"
        );

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT date,amount,mode,note " +
                                "FROM payments " +
                                "WHERE project_id=? " +
                                "ORDER BY date DESC,id DESC",
                        new String[]{
                                String.valueOf(p.id)
                        }
                );

        while (c.moveToNext()) {

            s.append(
                    c.getString(0)
            )
                    .append(" | ₹")
                    .append(fmt(c.getDouble(1)))
                    .append(" | ")
                    .append(safeText(c.getString(2)))
                    .append(" | ")
                    .append(safeText(c.getString(3)))
                    .append("\n");
        }

        c.close();

        s.append(
                "\nEXPENSES\n"
        );

        c =
                db.getReadableDatabase().rawQuery(
                        "SELECT date,category,amount,note " +
                                "FROM expenses " +
                                "WHERE project_id=? " +
                                "ORDER BY date DESC,id DESC",
                        new String[]{
                                String.valueOf(p.id)
                        }
                );

        while (c.moveToNext()) {

            s.append(
                    c.getString(0)
            )
                    .append(" | ")
                    .append(
                            safeText(c.getString(1))
                    )
                    .append(" | ₹")
                    .append(fmt(c.getDouble(2)))
                    .append(" | ")
                    .append(
                            safeText(c.getString(3))
                    )
                    .append("\n");
        }

        c.close();

        return s.toString();
    }

    // ---------------------------------------------------------
    // PAYMENTS
    // ---------------------------------------------------------

    void projectPayments(Project p) {

        page("Payments / Collection");

        root.addView(
                card(
                        "Project: " + p.customer +
                                "\nCollection ₹" +
                                fmt(db.collection(p.id)) +
                                "\nPending ₹" +
                                fmt(db.pending(p.id))
                )
        );

        root.addView(
                button(
                        "+ Add Payment",
                        GREEN,
                        v -> paymentDialog(p, null)
                )
        );

        showPaymentHistory(p);
    }

    void showPaymentHistory(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT id,date,amount,mode,note " +
                                "FROM payments " +
                                "WHERE project_id=? " +
                                "ORDER BY date DESC,id DESC",
                        new String[]{
                                String.valueOf(p.id)
                        }
                );

        if (!c.moveToFirst()) {

            root.addView(
                    card("No payment entries.")
            );

            c.close();
            return;
        }

        do {

            int id = c.getInt(0);

            String date =
                    safeText(c.getString(1));

            double amount =
                    c.getDouble(2);

            String mode =
                    safeText(c.getString(3));

            String note =
                    safeText(c.getString(4));

            root.addView(
                    card(
                            "📅 " + date +
                                    "\n💰 ₹" + fmt(amount) +
                                    "\nMode: " + mode +
                                    (
                                            note.isEmpty()
                                                    ? ""
                                                    : "\nNote: " + note
                                    )
                    )
            );

            LinearLayout r =
                    new LinearLayout(this);

            r.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            Button e =
                    button(
                            "✏️ Edit",
                            BLUE,
                            v -> paymentDialog(p, id)
                    );

            Button d =
                    button(
                            "🗑 Delete",
                            RED,
                            v -> confirm(
                                    "Delete this payment?",
                                    () -> {

                                        db.getWritableDatabase()
                                                .delete(
                                                        "payments",
                                                        "id=?",
                                                        new String[]{
                                                                String.valueOf(id)
                                                        }
                                                );

                                        projectPayments(p);
                                    }
                            )
                    );

            r.addView(
                    e,
                    new LinearLayout.LayoutParams(
                            0, dp(62), 1
                    )
            );

            r.addView(
                    d,
                    new LinearLayout.LayoutParams(
                            0, dp(62), 1
                    )
            );

            root.addView(r);

        } while (c.moveToNext());

        c.close();
    }

    void paymentDialog(
            Project p,
            Integer paymentId
    ) {

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.VERTICAL
        );

        l.setPadding(
                dp(8),
                dp(4),
                dp(8),
                dp(4)
        );

        EditText a =
                field("Amount", "");

        EditText date =
                field(
                        "Date YYYY-MM-DD",
                        todayISO()
                );

        EditText note =
                field("Note", "");

        Spinner mode =
                new Spinner(this);

        String[] modes = {
                "Cash",
                "UPI",
                "Bank Transfer",
                "Cheque",
                "Other"
        };

        ArrayAdapter<String> ma =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        modes
                ) {

                    @Override
                    public View getView(
                            int pos,
                            View cv,
                            android.view.ViewGroup par
                    ) {

                        TextView v =
                                (TextView)
                                        super.getView(
                                                pos,
                                                cv,
                                                par
                                        );

                        v.setTextSize(17);
                        v.setTextColor(DARK);
                        v.setTypeface(null, 1);
                        v.setPadding(
                                dp(12),
                                dp(10),
                                dp(12),
                                dp(10)
                        );

                        return v;
                    }

                    @Override
                    public View getDropDownView(
                            int pos,
                            View cv,
                            android.view.ViewGroup par
                    ) {

                        TextView v =
                                (TextView)
                                        super.getDropDownView(
                                                pos,
                                                cv,
                                                par
                                        );

                        v.setTextSize(17);
                        v.setTextColor(DARK);
                        v.setPadding(
                                dp(14),
                                dp(12),
                                dp(14),
                                dp(12)
                        );

                        return v;
                    }
                };

        ma.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        mode.setAdapter(ma);

        l.addView(a);
        l.addView(date);

        l.addView(
                txt(
                        "Payment Mode",
                        17,
                        true
                )
        );

        l.addView(
                mode,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        l.addView(note);

        if (paymentId != null) {

            android.database.Cursor c =
                    db.getReadableDatabase().rawQuery(
                            "SELECT amount,date,mode,note " +
                                    "FROM payments WHERE id=?",
                            new String[]{
                                    String.valueOf(paymentId)
                            }
                    );

            if (c.moveToFirst()) {

                a.setText(
                        String.valueOf(
                                c.getDouble(0)
                        )
                );

                date.setText(
                        c.getString(1)
                );

                note.setText(
                        safeText(c.getString(3))
                );

                for (int i = 0;
                     i < modes.length;
                     i++) {

                    if (
                            modes[i]
                                    .equalsIgnoreCase(
                                            c.getString(2)
                                    )
                    ) {
                        mode.setSelection(i);
                    }
                }
            }

            c.close();
        }

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle(
                                paymentId == null
                                        ? "Add Payment"
                                        : "Edit Payment"
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

                            double val =
                                    fmtN(
                                            a.getText()
                                                    .toString()
                                    );

                            if (val <= 0) {

                                a.setError(
                                        "Enter amount"
                                );

                                return;
                            }

                            android.content.ContentValues cv =
                                    new android.content.ContentValues();

                            cv.put(
                                    "project_id",
                                    p.id
                            );

                            cv.put(
                                    "amount",
                                    val
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

                            d.dismiss();

                            projectPayments(p);
                        })
        );

        d.show();
    }

    void payments() {

        page("Payment Collection");

        for (Project p : db.projects()) {

            root.addView(
                    card(projectSummary(p))
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

    // ---------------------------------------------------------
    // EXPENSES
    // ---------------------------------------------------------

    void showProjectExpenseHistory(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
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
                    card("No expense entries.")
            );

            c.close();
            return;
        }

        do {

            int id = c.getInt(0);

            String date =
                    safeText(c.getString(1));

            String cat =
                    safeText(c.getString(2));

            String note =
                    safeText(c.getString(4));

            double amount =
                    c.getDouble(3);

            root.addView(
                    card(
                            "📅 " + date +
                                    "\n📂 " + cat +
                                    "\n💸 ₹" +
                                    fmt(amount) +
                                    (
                                            note.isEmpty()
                                                    ? ""
                                                    : "\nNote: " + note
                                    )
                    )
            );

            LinearLayout r =
                    new LinearLayout(this);

            r.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            r.addView(
                    button(
                            "✏️ Edit",
                            BLUE,
                            v -> expenseDialog(p, id)
                    ),
                    new LinearLayout.LayoutParams(
                            0, dp(62), 1
                    )
            );

            r.addView(
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
                            0, dp(62), 1
                    )
            );

            root.addView(r);

        } while (c.moveToNext());

        c.close();
    }

    // =========================================================
    // UPDATED: PROJECT WORKERS WITH HALF DAY
    // =========================================================

    void showProjectWorkers(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT a.worker_id,a.status,COUNT(DISTINCT a.date) " +
                                "FROM attendance a " +
                                "WHERE a.status IN ('Present','Half Day') " +
                                "AND (a.site=? OR a.site LIKE ? OR a.site LIKE ?) " +
                                "GROUP BY a.worker_id,a.status",
                        new String[]{
                                p.site,
                                p.number + "%",
                                "%" + p.number + "%"
                        }
                );

        if (!c.moveToFirst()) {

            root.addView(
                    card(
                            "No worker attendance for this project."
                    )
            );

            c.close();
            return;
        }

        HashMap<Integer, Integer> presentMap =
                new HashMap<>();

        HashMap<Integer, Integer> halfDayMap =
                new HashMap<>();

        do {

            int workerId =
                    c.getInt(0);

            String status =
                    safeText(c.getString(1));

            int days =
                    c.getInt(2);

            if (
                    "Present".equalsIgnoreCase(status)
            ) {

                presentMap.put(
                        workerId,
                        days
                );

            } else if (
                    "Half Day".equalsIgnoreCase(status)
            ) {

                halfDayMap.put(
                        workerId,
                        days
                );
            }

        } while (c.moveToNext());

        c.close();

        HashSet<Integer> workerIds =
                new HashSet<>();

        workerIds.addAll(
                presentMap.keySet()
        );

        workerIds.addAll(
                halfDayMap.keySet()
        );

        for (Integer workerId : workerIds) {

            Worker w =
                    db.worker(workerId);

            if (w == null)
                continue;

            int present =
                    presentMap.containsKey(workerId)
                            ? presentMap.get(workerId)
                            : 0;

            int halfDay =
                    halfDayMap.containsKey(workerId)
                            ? halfDayMap.get(workerId)
                            : 0;

            double salary =
                    (present * w.dailySalary)
                            +
                    (halfDay *
                            w.dailySalary *
                            0.5);

            root.addView(
                    card(
                            "👷 " + w.name +

                                    "\nPresent Days: " +
                                    present +

                                    "\nHalf Days: " +
                                    halfDay +

                                    "\nDaily Salary: ₹" +
                                    fmt(w.dailySalary) +

                                    "\nSite Salary: ₹" +
                                    fmt(salary)
                    )
            );
        }
    }

    // =========================================================
    // UPDATED: HALF DAY ALSO COUNTS AS WORK DAY
    // =========================================================

    void showProjectWorkDays(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT DISTINCT date " +
                                "FROM attendance " +
                                "WHERE status IN ('Present','Half Day') " +
                                "AND (site=? OR site LIKE ? OR site LIKE ?) " +
                                "ORDER BY date",
                        new String[]{
                                p.site,
                                p.number + "%",
                                "%" + p.number + "%"
                        }
                );

        if (!c.moveToFirst()) {

            root.addView(
                    card("No work dates recorded.")
            );

            c.close();
            return;
        }

        do {

            root.addView(
                    card(
                            "📅 " +
                                    safeText(
                                            c.getString(0)
                                    ) +
                                    " • Work Day"
                    )
            );

        } while (c.moveToNext());

        c.close();
    }

    void projectExpenses(Project p) {

        page("Expenses");

        root.addView(
                card(
                        "Project: " +
                                p.customer +
                                "\nTotal Expenses ₹" +
                                fmt(db.expenses(p.id)) +
                                "\nProfit ₹" +
                                fmt(db.profit(p.id))
                )
        );

        root.addView(
                button(
                        "+ Add Expense",
                        ORANGE,
                        v -> expenseDialog(p, null)
                )
        );

        addExpenseEntries(
                p.id,
                p
        );
    }

    void expenses() {

        page("Expenses");

        root.addView(
                button(
                        "+ Add Expense",
                        ORANGE,
                        v -> expenseDialog(null, null)
                )
        );

        boolean found = false;

        for (Project p : db.projects()) {

            android.database.Cursor c =
                    db.getReadableDatabase().rawQuery(
                            "SELECT COUNT(*) FROM expenses " +
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
                                p.number +
                                        " • " +
                                        p.customer +
                                        "\nTotal Expenses ₹" +
                                        fmt(
                                                db.expenses(
                                                        p.id
                                                )
                                        ) +
                                        "\nProfit ₹" +
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
                            "No expenses added yet.\n" +
                                    "Tap + Add Expense to add one."
                    )
            );
        }
    }

    void addExpenseEntries(
            int projectId,
            Project project
    ) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT id,amount,category,date,note " +
                                "FROM expenses " +
                                "WHERE project_id=? " +
                                "ORDER BY id DESC",
                        new String[]{
                                String.valueOf(projectId)
                        }
                );

        while (c.moveToNext()) {

            int id =
                    c.getInt(0);

            double amount =
                    c.getDouble(1);

            String category =
                    c.getString(2);

            String date =
                    c.getString(3);

            String note =
                    c.getString(4);

            String detail =
                    "💸 ₹" +
                            fmt(amount) +
                            "\nType: " +
                            safeText(category) +
                            "\nDate: " +
                            safeText(date);

            if (
                    note != null &&
                            !note.trim().isEmpty()
            ) {
                detail +=
                        "\nNote: " + note;
            }

            root.addView(
                    card(detail)
            );

            LinearLayout r =
                    new LinearLayout(this);

            r.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            Button eb =
                    button(
                            "✏️ Edit",
                            BLUE,
                            v -> expenseDialog(
                                    project,
                                    id
                            )
                    );

            Button dbtn =
                    button(
                            "🗑 Delete",
                            RED,
                            v -> confirm(
                                    "Delete this expense?",
                                    () -> {

                                        deleteExpense(id);

                                        if (project != null)
                                            projectExpenses(project);
                                        else
                                            expenses();
                                    }
                            )
                    );

            r.addView(
                    eb,
                    new LinearLayout.LayoutParams(
                            0, dp(62), 1
                    )
            );

            r.addView(
                    dbtn,
                    new LinearLayout.LayoutParams(
                            0, dp(62), 1
                    )
            );

            root.addView(r);
        }

        c.close();
    }

    void expenseDialog(
            Project project,
            Integer expenseId
    ) {

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.VERTICAL
        );

        l.setPadding(
                dp(8),
                dp(4),
                dp(8),
                dp(4)
        );

        ArrayList<Project> ps =
                db.projects();

        Spinner projectSpinner =
                new Spinner(this);

        ArrayList<String> projectNames =
                new ArrayList<>();

        projectNames.add(
                "Select Project / Site"
        );

        for (Project x : ps) {

            projectNames.add(
                    x.number +
                            " • " +
                            x.customer
            );
        }

        ArrayAdapter<String> pa =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        projectNames
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent
                    ) {

                        TextView v =
                                (TextView)
                                        super.getView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        v.setTextSize(17);
                        v.setTextColor(DARK);
                        v.setTypeface(null, 1);
                        v.setPadding(
                                dp(12),
                                dp(10),
                                dp(12),
                                dp(10)
                        );

                        return v;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent
                    ) {

                        TextView v =
                                (TextView)
                                        super.getDropDownView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        v.setTextSize(17);
                        v.setTextColor(DARK);
                        v.setPadding(
                                dp(14),
                                dp(12),
                                dp(14),
                                dp(12)
                        );

                        return v;
                    }
                };

        pa.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        projectSpinner.setAdapter(pa);

        EditText amount =
                field("Amount", "");

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

        ArrayAdapter<String> ta =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        types
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent
                    ) {

                        TextView v =
                                (TextView)
                                        super.getView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        v.setTextSize(17);
                        v.setTextColor(DARK);
                        v.setTypeface(null, 1);
                        v.setPadding(
                                dp(12),
                                dp(10),
                                dp(12),
                                dp(10)
                        );

                        return v;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent
                    ) {

                        TextView v =
                                (TextView)
                                        super.getDropDownView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        v.setTextSize(17);
                        v.setTextColor(DARK);
                        v.setPadding(
                                dp(14),
                                dp(12),
                                dp(14),
                                dp(12)
                        );

                        return v;
                    }
                };

        ta.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        type.setAdapter(ta);

        EditText date =
                field(
                        "Date YYYY-MM-DD",
                        todayISO()
                );

        EditText note =
                field("Note", "");

        l.addView(
                txt(
                        "Project / Site",
                        17,
                        true
                )
        );

        l.addView(
                projectSpinner,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        l.addView(amount);

        l.addView(
                txt(
                        "Expense Type",
                        17,
                        true
                )
        );

        l.addView(
                type,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        l.addView(date);
        l.addView(note);

        if (expenseId != null) {

            android.database.Cursor c =
                    db.getReadableDatabase().rawQuery(
                            "SELECT project_id,amount,category,date,note " +
                                    "FROM expenses WHERE id=?",
                            new String[]{
                                    String.valueOf(
                                            expenseId
                                    )
                            }
                    );

            if (c.moveToFirst()) {

                int oldProjectId =
                        c.getInt(0);

                amount.setText(
                        String.valueOf(
                                c.getDouble(1)
                        )
                );

                String oldType =
                        c.getString(2);

                date.setText(
                        c.getString(3)
                );

                note.setText(
                        c.getString(4)
                );

                for (int i = 0;
                     i < ps.size();
                     i++) {

                    if (
                            ps.get(i).id ==
                                    oldProjectId
                    ) {

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

                    if (
                            types[i]
                                    .equalsIgnoreCase(
                                            oldType
                                    )
                    ) {

                        type.setSelection(i);

                        break;
                    }
                }
            }

            c.close();

        } else if (project != null) {

            for (int i = 0;
                 i < ps.size();
                 i++) {

                if (
                        ps.get(i).id ==
                                project.id
                ) {

                    projectSpinner
                            .setSelection(
                                    i + 1
                            );

                    break;
                }
            }
        }

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle(
                                expenseId == null
                                        ? "Add Expense"
                                        : "Edit Expense"
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

                            int pos =
                                    projectSpinner
                                            .getSelectedItemPosition();

                            if (
                                    pos <= 0 ||
                                            pos - 1 >= ps.size()
                            ) {

                                Toast.makeText(
                                        this,
                                        "Select Project / Site",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            double val =
                                    fmtN(
                                            amount
                                                    .getText()
                                                    .toString()
                                    );

                            if (val <= 0) {

                                amount.setError(
                                        "Enter amount"
                                );

                                return;
                            }

                            Project selected =
                                    ps.get(pos - 1);

                            String cat =
                                    type.getSelectedItem()
                                            .toString();

                            String dt =
                                    date.getText()
                                            .toString()
                                            .trim();

                            String nt =
                                    note.getText()
                                            .toString()
                                            .trim();

                            if (expenseId == null) {

                                db.addExpense(
                                        selected.id,
                                        val,
                                        cat,
                                        dt,
                                        nt
                                );

                            } else {

                                android.content.ContentValues cv =
                                        new android.content.ContentValues();

                                cv.put(
                                        "project_id",
                                        selected.id
                                );

                                cv.put(
                                        "amount",
                                        val
                                );

                                cv.put(
                                        "category",
                                        cat
                                );

                                cv.put(
                                        "date",
                                        dt
                                );

                                cv.put(
                                        "note",
                                        nt
                                );

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

                            d.dismiss();

                            if (
                                    project != null &&
                                            expenseId == null
                            ) {

                                projectExpenses(
                                        project
                                );

                            } else if (
                                    project != null
                            ) {

                                projectExpenses(
                                        selected
                                );

                            } else {

                                expenses();
                            }
                        })
        );

        d.show();
    }

    void deleteExpense(int id) {

        db.getWritableDatabase()
                .delete(
                        "expenses",
                        "id=?",
                        new String[]{
                                String.valueOf(id)
                        }
                );
    }

    String safeText(String s) {
        return s == null ? "" : s;
    }

    // ---------------------------------------------------------
    // WORKERS
    // ---------------------------------------------------------

    void workers() {

        page("Workers / Attendance");

        root.addView(
                button(
                        "+ Add Worker",
                        PURPLE,
                        v -> workerDialog(null)
                )
        );

        for (Worker w : db.workers()) {

            String m =
                    new SimpleDateFormat(
                            "yyyy-MM",
                            Locale.getDefault()
                    ).format(new Date());

            // =================================================
            // UPDATED: HALF DAY COUNT DISPLAY
            // =================================================

            int present =
                    db.presentDays(
                            w.id,
                            m
                    );

            int halfDay =
                    db.halfDayDays(
                            w.id,
                            m
                    );

            int absent =
                    db.absentDays(
                            w.id,
                            m
                    );

            root.addView(
                    card(
                            "👷 " + w.name +

                                    "\nPresent: " +
                                    present +

                                    "  Half Day: " +
                                    halfDay +

                                    "  Absent: " +
                                    absent +

                                    "\nDaily: ₹" +
                                    fmt(w.dailySalary) +

                                    "  Monthly: ₹" +
                                    fmt(w.monthlySalary) +

                                    "\nAdvance: ₹" +
                                    fmt(
                                            db.advances(
                                                    w.id
                                            )
                                    ) +

                                    "\nBalance: ₹" +
