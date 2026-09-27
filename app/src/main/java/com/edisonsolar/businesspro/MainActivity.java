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

    int projectWorkDays(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
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

        int n =
                c.moveToFirst()
                        ? c.getInt(0)
                        : 0;

        c.close();

        return n;
    }

    double projectSalary(Project p) {

        double total = 0;

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT a.worker_id, COUNT(*) " +
                                "FROM attendance a " +
                                "WHERE a.status='Present' " +
                                "AND (a.site=? OR a.site LIKE ? OR a.site LIKE ?) " +
                                "GROUP BY a.worker_id",
                        new String[]{
                                p.site,
                                p.number + "%",
                                "%" + p.number + "%"
                        }
                );

        while (c.moveToNext()) {

            Worker w =
                    db.worker(c.getInt(0));

            total +=
                    c.getInt(1) *
                            w.dailySalary;
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

// ---------------------------------------------------------
// PAYMENTS / COLLECTION - UPDATED PREMIUM PAGE
// ---------------------------------------------------------

void payments() {

    page("Payment Collection");

    root.addView(
            card(
                    "💰 PAYMENT COLLECTION\n" +
                    "Company & Project wise collection"
            )
    );

    ArrayList<Company> companies = db.companies();
    ArrayList<Project> projects = db.projects();

    // COMPANY SELECT
    root.addView(
            txt("🏢 Select Company", 18, true)
    );

    Spinner companySpinner = new Spinner(this);

    ArrayList<String> companyNames = new ArrayList<>();
    companyNames.add("All Companies");

    for (Company c : companies) {
        companyNames.add(c.name);
    }

    ArrayAdapter<String> companyAdapter =
            new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    companyNames
            ) {
                @Override
                public View getView(
                        int position,
                        View convertView,
                        android.view.ViewGroup parent
                ) {
                    TextView v =
                            (TextView) super.getView(
                                    position,
                                    convertView,
                                    parent
                            );

                    v.setTextSize(18);
                    v.setTextColor(DARK);
                    v.setTypeface(null, 1);
                    v.setPadding(
                            dp(14),
                            dp(12),
                            dp(14),
                            dp(12)
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
                            (TextView) super.getDropDownView(
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

    companyAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
    );

    companySpinner.setAdapter(companyAdapter);

    root.addView(
            companySpinner,
            new LinearLayout.LayoutParams(
                    -1,
                    dp(60)
            )
    );

    // PROJECT SELECT
    root.addView(
            txt("☀️ Select Project", 18, true)
    );

    Spinner projectSpinner = new Spinner(this);

    ArrayList<String> projectNames =
            new ArrayList<>();

    projectNames.add("Select Project");

    for (Project p : projects) {
        projectNames.add(
                p.number +
                        " • " +
                        p.customer +
                        " • " +
                        p.site
        );
    }

    ArrayAdapter<String> projectAdapter =
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
                            (TextView) super.getView(
                                    position,
                                    convertView,
                                    parent
                            );

                    v.setTextSize(18);
                    v.setTextColor(DARK);
                    v.setTypeface(null, 1);
                    v.setPadding(
                            dp(14),
                            dp(12),
                            dp(14),
                            dp(12)
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
                            (TextView) super.getDropDownView(
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

    projectAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
    );

    projectSpinner.setAdapter(projectAdapter);

    root.addView(
            projectSpinner,
            new LinearLayout.LayoutParams(
                    -1,
                    dp(60)
            )
    );

    // SELECTED PROJECT AREA
    LinearLayout selectedArea =
            new LinearLayout(this);

    selectedArea.setOrientation(
            LinearLayout.VERTICAL
    );

    selectedArea.setPadding(
            dp(4),
            dp(10),
            dp(4),
            dp(10)
    );

    root.addView(selectedArea);

    // PROJECT CHANGE
    projectSpinner.setOnItemSelectedListener(
            new android.widget.AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(
                        android.widget.AdapterView<?> parent,
                        View view,
                        int position,
                        long id
                ) {

                    selectedArea.removeAllViews();

                    if (position <= 0 ||
                            position - 1 >= projects.size()) {

                        selectedArea.addView(
                                card(
                                        "☝️ Select a project to view\n" +
                                        "Project Value • Collection • Pending"
                                )
                        );

                        return;
                    }

                    Project p =
                            projects.get(position - 1);

                    showPaymentProjectSummary(
                            selectedArea,
                            p
                    );
                }

                @Override
                public void onNothingSelected(
                        android.widget.AdapterView<?> parent
                ) {
                }
            }
    );
}


// ---------------------------------------------------------
// PROJECT PAYMENT SUMMARY
// ---------------------------------------------------------

void showPaymentProjectSummary(
        LinearLayout area,
        Project p
) {

    double projectValue =
            p.amount;

    double collection =
            db.collection(p.id);

    double pending =
            projectValue - collection;

    if (pending < 0)
        pending = 0;

    // PROJECT NAME
    area.addView(
            card(
                    "☀️ " +
                            p.number +
                            "\n" +
                            p.customer +
                            "\n📍 " +
                            p.site
            )
    );

    // TOTAL PROJECT VALUE
    TextView value =
            card(
                    "💰 TOTAL PROJECT VALUE\n₹" +
                            fmt(projectValue)
            );

    value.setTextSize(20);
    value.setTextColor(DARK);

    area.addView(value);

    // COLLECTION
    TextView collected =
            card(
                    "💵 TOTAL COLLECTION\n₹" +
                            fmt(collection)
            );

    collected.setTextSize(20);
    collected.setTextColor(GREEN);

    area.addView(collected);

    // PENDING
    TextView pendingView =
            card(
                    "🔴 PENDING AMOUNT\n₹" +
                            fmt(pending)
            );

    pendingView.setTextSize(20);
    pendingView.setTextColor(RED);

    area.addView(pendingView);

    // ADD PAYMENT
    area.addView(
            button(
                    "+  ADD COLLECTION",
                    GREEN,
                    v -> paymentDialog(p, null)
            )
    );

    // FULL REPORT
    area.addView(
            button(
                    "📊  FULL PROJECT REPORT",
                    BLUE,
                    v -> projectFullReport(p)
            )
    );

    // COLLECTION HISTORY TITLE
    area.addView(
            txt(
                    "📅 Collection History",
                    21,
                    true
            )
    );

    showPaymentHistory(area, p);
}


// ---------------------------------------------------------
// PAYMENT HISTORY
// ---------------------------------------------------------

void showPaymentHistory(
        LinearLayout area,
        Project p
) {

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
                safeText(c.getString(1));

        double amount =
                c.getDouble(2);

        String mode =
                safeText(c.getString(3));

        String note =
                safeText(c.getString(4));

        area.addView(
                card(
                        "📅 Collection Date: " +
                                date +

                                "\n💰 Amount: ₹" +
                                fmt(amount) +

                                "\n💳 Mode: " +
                                mode +

                                (
                                        note.isEmpty()
                                                ? ""
                                                : "\n📝 Note: " +
                                                note
                                )
                )
        );

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button edit =
                button(
                        "✏️ Edit",
                        BLUE,
                        v -> {

                            paymentDialog(
                                    p,
                                    id
                            );
                        }
                );

        Button delete =
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
                );

        row.addView(
                edit,
                new LinearLayout.LayoutParams(
                        0,
                        dp(62),
                        1
                )
        );

        row.addView(
                delete,
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


// ---------------------------------------------------------
// OLD COMPATIBILITY METHOD
// ---------------------------------------------------------

void showPaymentHistory(Project p) {

    showPaymentHistory(
            root,
            p
    );
}


// ---------------------------------------------------------
// PROJECT PAYMENT PAGE
// ---------------------------------------------------------

void projectPayments(Project p) {

    page("Payments / Collection");

    LinearLayout area =
            new LinearLayout(this);

    area.setOrientation(
            LinearLayout.VERTICAL
    );

    area.setPadding(
            dp(4),
            dp(4),
            dp(4),
            dp(10)
    );

    root.addView(area);

    showPaymentProjectSummary(
            area,
            p
    );
}


// ---------------------------------------------------------
// ADD / EDIT PAYMENT
// ---------------------------------------------------------

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
            dp(14),
            dp(8),
            dp(14),
            dp(8)
    );

    TextView projectTitle =
            txt(
                    "☀️ " +
                            p.number +
                            " • " +
                            p.customer,
                    19,
                    true
            );

    l.addView(projectTitle);

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

                    v.setTextSize(18);
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
            };

    ma.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
    );

    mode.setAdapter(ma);

    EditText note =
            field(
                    "Collection Note",
                    ""
            );

    l.addView(amount);
    l.addView(date);

    l.addView(
            txt(
                    "💳 Payment Mode",
                    17,
                    true
            )
    );

    l.addView(
            mode,
            new LinearLayout.LayoutParams(
                    -1,
                    dp(60)
            )
    );

    l.addView(note);

    // EDIT EXISTING
    if (paymentId != null) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT amount,date,mode,note " +
                                "FROM payments WHERE id=?",
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
                    safeText(
                            c.getString(1)
                    )
            );

            note.setText(
                    safeText(
                            c.getString(3)
                    )
            );

            for (int i = 0;
                 i < modes.length;
                 i++) {

                if (
                        modes[i]
                                .equalsIgnoreCase(
                                        safeText(
                                                c.getString(2)
                                        )
                                )
                ) {

                    mode.setSelection(i);
                    break;
                }
            }
        }

        c.close();
    }

    AlertDialog d =
            new AlertDialog.Builder(this)
                    .setTitle(
                            paymentId == null
                                    ? "Add Collection"
                                    : "Edit Collection"
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

    d.setOnShowListener(x -> {

        Button save =
                d.getButton(
                        AlertDialog.BUTTON_POSITIVE
                );

        save.setTextSize(18);
        save.setTypeface(null, 1);

        save.setOnClickListener(v -> {

            double value =
                    fmtN(
                            amount.getText()
                                    .toString()
                    );

            if (value <= 0) {

                amount.setError(
                        "Enter collection amount"
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

            d.dismiss();

            projectPayments(p);
        });
    });

    d.show();
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

    void showProjectWorkers(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT a.worker_id,COUNT(DISTINCT a.date) " +
                                "FROM attendance a " +
                                "WHERE a.status='Present' " +
                                "AND (a.site=? OR a.site LIKE ? OR a.site LIKE ?) " +
                                "GROUP BY a.worker_id",
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

        do {

            Worker w =
                    db.worker(c.getInt(0));

            int days =
                    c.getInt(1);

            root.addView(
                    card(
                            "👷 " + w.name +
                                    "\nPresent Days: " +
                                    days +
                                    "\nDaily Salary: ₹" +
                                    fmt(w.dailySalary) +
                                    "\nSite Salary: ₹" +
                                    fmt(
                                            days *
                                                    w.dailySalary
                                    )
                    )
            );

        } while (c.moveToNext());

        c.close();
    }

    void showProjectWorkDays(Project p) {

        android.database.Cursor c =
                db.getReadableDatabase().rawQuery(
                        "SELECT DISTINCT date " +
                                "FROM attendance " +
                                "WHERE status='Present' " +
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

            root.addView(
                    card(
                            "👷 " + w.name +
                                    "\nPresent: " +
                                    db.presentDays(
                                            w.id,
                                            m
                                    ) +
                                    "  Absent: " +
                                    db.absentDays(
                                            w.id,
                                            m
                                    ) +

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
                                    fmt(
                                            db.salaryBalance(
                                                    w,
                                                    m
                                            )
                                    )
                    )
            );

            root.addView(
                    button(
                            "✏️ Edit Worker",
                            BLUE,
                            v -> workerDialog(w)
                    )
            );

            root.addView(
                    button(
                            "📅 Attendance",
                            GREEN,
                            v -> attendanceDialog(w)
                    )
            );

            root.addView(
                    button(
                            "💵 Advance",
                            ORANGE,
                            v -> advanceDialog(w)
                    )
            );

            root.addView(
                    button(
                            "🗑 Delete Worker",
                            RED,
                            v -> confirm(
                                    "Delete worker?",
                                    () -> {
                                        db.deleteWorker(w.id);
                                        workers();
                                    }
                            )
                    )
            );
        }

        root.addView(
                button(
                        "📄 Salary PDF / WhatsApp",
                        PURPLE,
                        v -> salaryShare()
                )
        );
    }

    void workerDialog(Worker old) {

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.VERTICAL
        );

        EditText n =
                field(
                        "Worker Name",
                        old == null ? "" : old.name
                );

        EditText p =
                field(
                        "Phone",
                        old == null ? "" : old.phone
                );

        EditText d =
                field(
                        "Daily Salary",
                        old == null
                                ? ""
                                : "" + old.dailySalary
                );

        EditText m =
                field(
                        "Monthly Salary",
                        old == null
                                ? ""
                                : "" + old.monthlySalary
                );

        l.addView(n);
        l.addView(p);
        l.addView(d);
        l.addView(m);

        new AlertDialog.Builder(this)
                .setTitle(
                        old == null
                                ? "Add Worker"
                                : "Edit Worker"
                )
                .setView(l)
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Save",
                        (x, y) -> {

                            Worker w =
                                    old == null
                                            ? new Worker()
                                            : old;

                            w.name =
                                    n.getText()
                                            .toString();

                            w.phone =
                                    p.getText()
                                            .toString();

                            w.dailySalary =
                                    fmtN(
                                            d.getText()
                                                    .toString()
                                    );

                            w.monthlySalary =
                                    fmtN(
                                            m.getText()
                                                    .toString()
                                    );

                            db.saveWorker(w);

                            workers();
                        }
                )
                .show();
    }

    // ---------------------------------------------------------
    // ATTENDANCE
    // ---------------------------------------------------------

    void attendanceDialog(Worker w) {

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

        EditText date =
                field(
                        "Date YYYY-MM-DD",
                        todayISO()
                );

        Spinner site =
                new Spinner(this);

        ArrayList<Project> projectList =
                db.projects();

        ArrayList<String> siteNames =
                new ArrayList<>();

        siteNames.add(
                "Select Project / Site"
        );

        for (Project p : projectList) {

            siteNames.add(
                    p.number +
                            " • " +
                            p.site +
                            " • " +
                            p.customer
            );
        }

        ArrayAdapter<String> siteAdapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        siteNames
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
                        v.setTypeface(null, 1);
                        v.setPadding(
                                dp(14),
                                dp(12),
                                dp(14),
                                dp(12)
                        );

                        return v;
                    }
                };

        siteAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        site.setAdapter(siteAdapter);

        TextView statusLabel =
                txt(
                        "Attendance Status",
                        17,
                        true
                );

        Spinner status =
                new Spinner(this);

        String[] options = {
                "Present",
                "Absent"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        options
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

                        v.setTextSize(18);
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

                        v.setTextSize(18);
                        v.setTextColor(DARK);
                        v.setTypeface(null, 1);
                        v.setPadding(
                                dp(16),
                                dp(12),
                                dp(16),
                                dp(12)
                        );

                        return v;
                    }
                };

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        status.setAdapter(adapter);

        l.addView(date);

        l.addView(statusLabel);

        l.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        l.addView(
                txt(
                        "Project / Site",
                        17,
                        true
                )
        );

        l.addView(
                site,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        AlertDialog d =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Add / Edit Attendance"
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

                            int sitePos =
                                    site.getSelectedItemPosition();

                            if (sitePos <= 0) {

                                Toast.makeText(
                                        this,
                                        "Select Project / Site",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            String selectedStatus =
                                    status.getSelectedItem()
                                            .toString();

                            String selectedSite =
                                    projectList
                                            .get(sitePos - 1)
                                            .number +
                                            " | " +
                                            projectList
                                                    .get(sitePos - 1)
                                                    .site;

                            saveAttendanceRaw(
                                    w.id,
                                    date.getText()
                                            .toString(),
                                    selectedStatus,
                                    selectedSite
                            );

                            d.dismiss();

                            workers();
                        })
        );

        d.show();
    }

    void saveAttendanceRaw(
            int wid,
            String date,
            String status,
            String site
    ) {

        android.content.ContentValues v =
                new android.content.ContentValues();

        v.put(
                "worker_id",
                wid
        );

        v.put(
                "date",
                date
        );

        v.put(
                "status",
                status
        );

        v.put(
                "site",
                site
        );

        v.put(
                "note",
                ""
        );

        db.getWritableDatabase()
                .insertWithOnConflict(
                        "attendance",
                        null,
                        v,
                        android.database.sqlite.SQLiteDatabase
                                .CONFLICT_REPLACE
                );
    }

    void advanceDialog(Worker w) {

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.VERTICAL
        );

        EditText a =
                field(
                        "Advance Amount",
                        ""
                );

        EditText d =
                field(
                        "Date YYYY-MM-DD",
                        todayISO()
                );

        EditText n =
                field(
                        "Note",
                        ""
                );

        l.addView(a);
        l.addView(d);
        l.addView(n);

        new AlertDialog.Builder(this)
                .setTitle(
                        "Salary Advance"
                )
                .setView(l)
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Save",
                        (x, y) -> {

                            db.saveAdvance(
                                    w.id,
                                    fmtN(
                                            a.getText()
                                                    .toString()
                                    ),
                                    d.getText()
                                            .toString(),
                                    n.getText()
                                            .toString()
                            );

                            workers();
                        }
                )
                .show();
    }

    // ---------------------------------------------------------
    // DATE / CALENDAR
    // ---------------------------------------------------------

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

    void salaryShare() {

        String m =
                new SimpleDateFormat(
                        "yyyy-MM",
                        Locale.getDefault()
                ).format(new Date());

        StringBuilder s =
                new StringBuilder(
                        "EDISON SOLAR WORKER SALARY REPORT\n" +
                                "Month: " +
                                m +
                                "\n\n"
                );

        for (Worker w : db.workers()) {

            s.append(
                    db.workerReport(
                            w,
                            m
                    )
            );

            s.append(
                    "\n\n"
            );
        }

        share(
                s.toString()
        );
    }

    void calendar() {

        page("Calendar");

        root.addView(
                button(
                        "📅 Select Date",
                        PURPLE,
                        v -> {

                            Calendar c =
                                    Calendar.getInstance();

                            new DatePickerDialog(
                                    this,
                                    (x, y, m, d) ->
                                            calendarDay(
                                                    String.format(
                                                            Locale.getDefault(),
                                                            "%02d-%02d-%04d",
                                                            d,
                                                            m + 1,
                                                            y
                                                    )
                                            ),
                                    c.get(
                                            Calendar.YEAR
                                    ),
                                    c.get(
                                            Calendar.MONTH
                                    ),
                                    c.get(
                                            Calendar.DAY_OF_MONTH
                                    )
                            ).show();
                        }
                )
        );
    }

    void calendarDay(String date) {

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
                                p.number +
                                        "\n" +
                                        p.company +
                                        "\n" +
                                        p.customer +
                                        "\n" +
                                        p.site +
                                        "\n" +
                                        fmt(p.kw) +
                                        " kW\n" +
                                        p.status
                        )
                );
            }
        }

        if (!found) {

            root.addView(
                    card(
                            "No project on this date."
                    )
            );
        }
    }

    // ---------------------------------------------------------
    // REPORTS
    // ---------------------------------------------------------

    void reports() {

        page(
                "Reports / PDF"
        );

        root.addView(
                card(
                        db.dashboard()
                )
        );

        for (Project p : db.projects()) {

            root.addView(
                    button(
                            "📄 PDF " + p.number,
                            PURPLE,
                            v -> pdf(p)
                    )
            );
        }

        root.addView(
                button(
                        "📱 WhatsApp / Share",
                        GREEN,
                        v -> share(
                                db.dashboard()
                        )
                )
        );
    }

    // ---------------------------------------------------------
    // PHOTOS
    // ---------------------------------------------------------

    void photos(Project p) {

        photoProject = p;

        page(
                "Photos / Gallery"
        );

        root.addView(
                card(
                        p.customer +
                                "\nSaved Photos: " +
                                db.photos(p.id).size()
                )
        );

        root.addView(
                button(
                        "📷 Take Photo",
                        BLUE,
                        v -> takePhoto()
                )
        );

        root.addView(
                button(
                        "🖼 Open Gallery",
                        PURPLE,
                        v -> openGallery()
                )
        );
    }

    void takePhoto() {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
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
                    e.toString(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    void openGallery() {

        startActivityForResult(
                new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ),
                102
        );
    }

    @Override
    protected void onActivityResult(
            int r,
            int c,
            Intent d
    ) {

        super.onActivityResult(
                r,
                c,
                d
        );

        if (c != RESULT_OK)
            return;

        if (
                r == 101 &&
                        photoProject != null &&
                        cameraUri != null
        ) {

            db.addPhoto(
                    photoProject.id,
                    cameraUri.toString()
            );

            photos(photoProject);

        } else if (
                r == 102 &&
                        d != null &&
                        d.getData() != null &&
                        photoProject != null
        ) {

            db.addPhoto(
                    photoProject.id,
                    d.getData().toString()
            );

            photos(photoProject);
        }
    }

    // ---------------------------------------------------------
    // GPS
    // ---------------------------------------------------------

    void gps(Project p) {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    100
            );

            return;
        }

        LocationManager lm =
                (LocationManager)
                        getSystemService(
                                LOCATION_SERVICE
                        );

        try {

            Location l =
                    lm.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER
                    );

            if (l == null) {

                l =
                        lm.getLastKnownLocation(
                                LocationManager.NETWORK_PROVIDER
                        );
            }

            if (l == null) {

                Toast.makeText(
                        this,
                        "Turn on GPS",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "geo:" +
                                            l.getLatitude() +
                                            "," +
                                            l.getLongitude() +
                                            "?q=" +
                                            l.getLatitude() +
                                            "," +
                                            l.getLongitude()
                            )
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

    // ---------------------------------------------------------
    // PDF
    // ---------------------------------------------------------

    void pdf(Project p) {

        PdfDocument doc =
                new PdfDocument();

        try {

            PdfDocument.Page pg =
                    doc.startPage(
                            new PdfDocument.PageInfo.Builder(
                                    595,
                                    842,
                                    1
                            ).create()
                    );

            Paint paint =
                    new Paint();

            paint.setTextSize(13);

            float y = 45;

            for (
                    String line :
                    projectReport(p)
                            .split("\n")
            ) {

                if (y > 800)
                    break;

                pg.getCanvas()
                        .drawText(
                                line,
                                40,
                                y,
                                paint
                        );

                y += 22;
            }

            doc.finishPage(pg);

            File dir =
                    new File(
                            getExternalFilesDir(
                                    Environment.DIRECTORY_DOCUMENTS
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

            doc.writeTo(out);

            out.close();

            Uri u =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() +
                                    ".fileprovider",
                            f
                    );

            Intent i =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            i.setType(
                    "application/pdf"
            );

            i.putExtra(
                    Intent.EXTRA_STREAM,
                    u
            );

            i.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(
                    Intent.createChooser(
                            i,
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

            doc.close();
        }
    }

    // ---------------------------------------------------------
    // SHARE
    // ---------------------------------------------------------

    void share(String s) {

        Intent i =
                new Intent(
                        Intent.ACTION_SEND
                );

        i.setType(
                "text/plain"
        );

        i.putExtra(
                Intent.EXTRA_TEXT,
                s
        );

        startActivity(
                Intent.createChooser(
                        i,
                        "Share / WhatsApp"
                )
        );
    }

    // ---------------------------------------------------------
    // CONFIRM
    // ---------------------------------------------------------

    void confirm(
            String msg,
            Runnable yes
    ) {

        new AlertDialog.Builder(this)
                .setMessage(msg)
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
}
