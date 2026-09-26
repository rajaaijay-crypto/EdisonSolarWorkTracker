package com.edisonsolar.businesspro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME =
            "edison_solar_manager.db";

    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // =========================================================
    // DATABASE
    // =========================================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE companies (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "phone TEXT)"
        );

        db.execSQL(
                "CREATE TABLE projects (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "company_id INTEGER," +
                        "number TEXT," +
                        "company TEXT," +
                        "customer TEXT," +
                        "phone TEXT," +
                        "site TEXT," +
                        "kw REAL DEFAULT 0," +
                        "amount REAL DEFAULT 0," +
                        "date TEXT," +
                        "status TEXT," +
                        "work TEXT," +
                        "lat REAL DEFAULT 0," +
                        "lon REAL DEFAULT 0," +
                        "has_location INTEGER DEFAULT 0)"
        );

        db.execSQL(
                "CREATE TABLE payments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "project_id INTEGER," +
                        "amount REAL DEFAULT 0," +
                        "date TEXT," +
                        "mode TEXT," +
                        "note TEXT)"
        );

        db.execSQL(
                "CREATE TABLE expenses (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "project_id INTEGER," +
                        "category TEXT," +
                        "amount REAL DEFAULT 0," +
                        "date TEXT," +
                        "note TEXT)"
        );

        db.execSQL(
                "CREATE TABLE photos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "project_id INTEGER," +
                        "uri TEXT)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        db.execSQL(
                "DROP TABLE IF EXISTS photos"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS expenses"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS payments"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS projects"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS companies"
        );

        onCreate(db);
    }

    // =========================================================
    // COMPANY
    // =========================================================

    public ArrayList<Company> companies() {

        ArrayList<Company> list =
                new ArrayList<>();

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT id,name,phone " +
                                "FROM companies " +
                                "ORDER BY name",
                        null
                );

        while (c.moveToNext()) {

            Company x = new Company();

            x.id = c.getInt(0);
            x.name = safe(c.getString(1));
            x.phone = safe(c.getString(2));

            list.add(x);
        }

        c.close();

        return list;
    }

    public void addCompany(
            String name,
            String phone
    ) {

        ContentValues v =
                new ContentValues();

        v.put("name", name);
        v.put("phone", phone);

        getWritableDatabase().insert(
                "companies",
                null,
                v
        );
    }

    public void updateCompany(
            int id,
            String name,
            String phone
    ) {

        ContentValues v =
                new ContentValues();

        v.put("name", name);
        v.put("phone", phone);

        getWritableDatabase().update(
                "companies",
                v,
                "id=?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public void deleteCompany(int id) {

        ArrayList<Integer> ids =
                new ArrayList<>();

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT id FROM projects " +
                                "WHERE company_id=?",
                        new String[]{
                                String.valueOf(id)
                        }
                );

        while (c.moveToNext()) {
            ids.add(c.getInt(0));
        }

        c.close();

        for (Integer projectId : ids) {
            deleteProject(projectId);
        }

        getWritableDatabase().delete(
                "companies",
                "id=?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public int countProjects(int companyId) {

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT COUNT(*) " +
                                "FROM projects " +
                                "WHERE company_id=?",
                        new String[]{
                                String.valueOf(companyId)
                        }
                );

        int result = 0;

        if (c.moveToFirst()) {
            result = c.getInt(0);
        }

        c.close();

        return result;
    }

    public double companyKw(int companyId) {

        return queryDouble(
                "SELECT COALESCE(SUM(kw),0) " +
                        "FROM projects " +
                        "WHERE company_id=?",
                companyId
        );
    }

    public double companyValue(int companyId) {

        return queryDouble(
                "SELECT COALESCE(SUM(amount),0) " +
                        "FROM projects " +
                        "WHERE company_id=?",
                companyId
        );
    }

    public double companyCollection(int companyId) {

        return queryDouble(
                "SELECT COALESCE(SUM(payments.amount),0) " +
                        "FROM payments " +
                        "INNER JOIN projects " +
                        "ON payments.project_id=projects.id " +
                        "WHERE projects.company_id=?",
                companyId
        );
    }

    public double companyExpenses(int companyId) {

        return queryDouble(
                "SELECT COALESCE(SUM(expenses.amount),0) " +
                        "FROM expenses " +
                        "INNER JOIN projects " +
                        "ON expenses.project_id=projects.id " +
                        "WHERE projects.company_id=?",
                companyId
        );
    }

    // =========================================================
    // PROJECTS
    // =========================================================

    public ArrayList<Project> projects() {

        ArrayList<Project> list =
                new ArrayList<>();

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT " +
                                "id,company_id,number,company," +
                                "customer,phone,site,kw,amount," +
                                "date,status,work,lat,lon,has_location " +
                                "FROM projects " +
                                "ORDER BY id DESC",
                        null
                );

        while (c.moveToNext()) {

            list.add(readProject(c));
        }

        c.close();

        return list;
    }

    public Project project(int id) {

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT " +
                                "id,company_id,number,company," +
                                "customer,phone,site,kw,amount," +
                                "date,status,work,lat,lon,has_location " +
                                "FROM projects " +
                                "WHERE id=?",
                        new String[]{
                                String.valueOf(id)
                        }
                );

        Project p = null;

        if (c.moveToFirst()) {
            p = readProject(c);
        }

        c.close();

        return p;
    }

    private Project readProject(Cursor c) {

        Project p = new Project();

        p.id = c.getInt(0);
        p.companyId = c.getInt(1);
        p.number = safe(c.getString(2));
        p.company = safe(c.getString(3));
        p.customer = safe(c.getString(4));
        p.phone = safe(c.getString(5));
        p.site = safe(c.getString(6));
        p.kw = c.getDouble(7);
        p.amount = c.getDouble(8);
        p.date = safe(c.getString(9));
        p.status = safe(c.getString(10));
        p.work = safe(c.getString(11));
        p.lat = c.getDouble(12);
        p.lon = c.getDouble(13);
        p.hasLoc = c.getInt(14) == 1;

        return p;
    }

    public void addProject(
            int companyId,
            String company,
            String customer,
            String phone,
            String site,
            double kw,
            double amount,
            String date,
            String status,
            String work
    ) {

        ContentValues v =
                new ContentValues();

        v.put("company_id", companyId);
        v.put(
                "number",
                nextProjectNumber()
        );
        v.put("company", company);
        v.put("customer", customer);
        v.put("phone", phone);
        v.put("site", site);
        v.put("kw", kw);
        v.put("amount", amount);
        v.put("date", date);
        v.put("status", status);
        v.put("work", work);

        getWritableDatabase().insert(
                "projects",
                null,
                v
        );
    }

    public void updateProject(
            Project old,
            int companyId,
            String company,
            String customer,
            String phone,
            String site,
            double kw,
            double amount,
            String date,
            String status,
            String work
    ) {

        ContentValues v =
                new ContentValues();

        v.put("company_id", companyId);
        v.put("company", company);
        v.put("customer", customer);
        v.put("phone", phone);
        v.put("site", site);
        v.put("kw", kw);
        v.put("amount", amount);
        v.put("date", date);
        v.put("status", status);
        v.put("work", work);

        getWritableDatabase().update(
                "projects",
                v,
                "id=?",
                new String[]{
                        String.valueOf(old.id)
                }
        );
    }

    private String nextProjectNumber() {

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT COUNT(*) FROM projects",
                        null
                );

        int count = 0;

        if (c.moveToFirst()) {
            count = c.getInt(0);
        }

        c.close();

        return String.format(
                Locale.getDefault(),
                "ESP-%04d",
                count + 1
        );
    }

    public void deleteProject(int projectId) {

        SQLiteDatabase db =
                getWritableDatabase();

        db.delete(
                "photos",
                "project_id=?",
                new String[]{
                        String.valueOf(projectId)
                }
        );

        db.delete(
                "expenses",
                "project_id=?",
                new String[]{
                        String.valueOf(projectId)
                }
        );

        db.delete(
                "payments",
                "project_id=?",
                new String[]{
                        String.valueOf(projectId)
                }
        );

        db.delete(
                "projects",
                "id=?",
                new String[]{
                        String.valueOf(projectId)
                }
        );
    }

    // =========================================================
    // PAYMENTS
    // =========================================================

    public void addPayment(
            int projectId,
            double amount,
            String date,
            String note
    ) {

        ContentValues v =
                new ContentValues();

        v.put("project_id", projectId);
        v.put("amount", amount);
        v.put("date", date);
        v.put("mode", "Payment");
        v.put("note", note);

        getWritableDatabase().insert(
                "payments",
                null,
                v
        );
    }

    public double collection(int projectId) {

        return queryDouble(
                "SELECT COALESCE(SUM(amount),0) " +
                        "FROM payments " +
                        "WHERE project_id=?",
                projectId
        );
    }

    public double pending(int projectId) {

        Project p = project(projectId);

        if (p == null) {
            return 0;
        }

        double result =
                p.amount -
                        collection(projectId);

        return Math.max(0, result);
    }

    // =========================================================
    // EXPENSES
    // =========================================================

    public void addExpense(
            int projectId,
            double amount,
            String category,
            String date,
            String note
    ) {

        ContentValues v =
                new ContentValues();

        v.put("project_id", projectId);
        v.put("category", category);
        v.put("amount", amount);
        v.put("date", date);
        v.put("note", note);

        getWritableDatabase().insert(
                "expenses",
                null,
                v
        );
    }

    public double expenses(int projectId) {

        return queryDouble(
                "SELECT COALESCE(SUM(amount),0) " +
                        "FROM expenses " +
                        "WHERE project_id=?",
                projectId
        );
    }

    public double profit(int projectId) {

        Project p = project(projectId);

        if (p == null) {
            return 0;
        }

        return p.amount -
                expenses(projectId);
    }

    // =========================================================
    // PHOTOS
    // =========================================================

    public void addPhoto(
            int projectId,
            String uri
    ) {

        ContentValues v =
                new ContentValues();

        v.put("project_id", projectId);
        v.put("uri", uri);

        getWritableDatabase().insert(
                "photos",
                null,
                v
        );
    }

    public ArrayList<String> photos(int projectId) {

        ArrayList<String> list =
                new ArrayList<>();

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT uri FROM photos " +
                                "WHERE project_id=? " +
                                "ORDER BY id DESC",
                        new String[]{
                                String.valueOf(projectId)
                        }
                );

        while (c.moveToNext()) {
            list.add(
                    safe(c.getString(0))
            );
        }

        c.close();

        return list;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    public String dashboard() {

        double totalKw = 0;
        double totalValue = 0;
        double totalCollection = 0;
        double totalExpenses = 0;

        int projectCount = 0;

        ArrayList<Project> list =
                projects();

        projectCount = list.size();

        for (Project p : list) {

            totalKw += p.kw;
            totalValue += p.amount;
            totalCollection +=
                    collection(p.id);
            totalExpenses +=
                    expenses(p.id);
        }

        double pending =
                Math.max(
                        0,
                        totalValue -
                                totalCollection
                );

        double profit =
                totalValue -
                        totalExpenses;

        return
                "📊 EDISON SOLAR DASHBOARD\n\n" +

                "Projects: " +
                projectCount +

                "\nTotal Solar: " +
                fmt(totalKw) +
                " kW" +

                "\nProject Value: ₹" +
                fmt(totalValue) +

                "\nCollection: ₹" +
                fmt(totalCollection) +

                "\nPending: ₹" +
                fmt(pending) +

                "\nExpenses: ₹" +
                fmt(totalExpenses) +

                "\nSite Profit: ₹" +
                fmt(profit);
    }

    // =========================================================
    // UTILITY
    // =========================================================

    private double queryDouble(
            String sql,
            int id
    ) {

        Cursor c =
                getReadableDatabase().rawQuery(
                        sql,
                        new String[]{
                                String.valueOf(id)
                        }
                );

        double value = 0;

        if (c.moveToFirst()) {
            value = c.getDouble(0);
        }

        c.close();

        return value;
    }

    private String safe(String s) {

        return s == null ? "" : s;
    }

    public static String fmt(double value) {

        if (
                Math.abs(
                        value -
                                Math.round(value)
                ) < 0.00001
        ) {

            return String.format(
                    Locale.getDefault(),
                    "%.0f",
                    value
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.2f",
                value
        );
    }
}
