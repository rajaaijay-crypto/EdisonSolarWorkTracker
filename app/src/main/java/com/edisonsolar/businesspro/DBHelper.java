package com.edisonsolar.businesspro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "edison_solar_manager.db";
    private static final int DB_VERSION = 4;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

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
                        "company_id INTEGER DEFAULT 0," +
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

        db.execSQL(
                "CREATE TABLE workers (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "phone TEXT," +
                        "daily_salary REAL DEFAULT 0," +
                        "monthly_salary REAL DEFAULT 0)"
        );

        db.execSQL(
                "CREATE TABLE attendance (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "worker_id INTEGER," +
                        "date TEXT," +
                        "status TEXT," +
                        "site TEXT," +
                        "note TEXT," +
                        "UNIQUE(worker_id,date))"
        );

        db.execSQL(
                "CREATE TABLE advances (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "worker_id INTEGER," +
                        "amount REAL DEFAULT 0," +
                        "date TEXT," +
                        "note TEXT)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 4) {

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS workers (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "phone TEXT," +
                            "daily_salary REAL DEFAULT 0," +
                            "monthly_salary REAL DEFAULT 0)"
            );

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS attendance (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "worker_id INTEGER," +
                            "date TEXT," +
                            "status TEXT," +
                            "site TEXT," +
                            "note TEXT," +
                            "UNIQUE(worker_id,date))"
            );

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS advances (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "worker_id INTEGER," +
                            "amount REAL DEFAULT 0," +
                            "date TEXT," +
                            "note TEXT)"
            );
        }
    }

    // =========================================================
    // COMPANY
    // =========================================================

    public ArrayList<Company> companies() {

        ArrayList<Company> list = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone " +
                        "FROM companies ORDER BY name",
                null
        );

        while (c.moveToNext()) {

            Company company = new Company();

            company.id = c.getInt(0);
            company.name = safe(c.getString(1));
            company.phone = safe(c.getString(2));

            list.add(company);
        }

        c.close();

        return list;
    }

    public void addCompany(
            String name,
            String phone
    ) {

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("phone", phone);

        getWritableDatabase().insert(
                "companies",
                null,
                values
        );
    }

    public void updateCompany(
            int id,
            String name,
            String phone
    ) {

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("phone", phone);

        getWritableDatabase().update(
                "companies",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public void deleteCompany(int id) {

        getWritableDatabase().delete(
                "companies",
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    // =========================================================
    // PROJECT
    // =========================================================

    public ArrayList<Project> projects() {

        ArrayList<Project> list = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,company_id,number,company," +
                        "customer,phone,site,kw,amount," +
                        "date,status,work,lat,lon,has_location " +
                        "FROM projects ORDER BY id DESC",
                null
        );

        while (c.moveToNext()) {

            list.add(readProject(c));
        }

        c.close();

        return list;
    }

    public Project project(int id) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,company_id,number,company," +
                        "customer,phone,site,kw,amount," +
                        "date,status,work,lat,lon,has_location " +
                        "FROM projects WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        Project project = null;

        if (c.moveToFirst()) {
            project = readProject(c);
        }

        c.close();

        return project;
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

        ContentValues values = new ContentValues();

        values.put("company_id", companyId);
        values.put("number", nextProjectNumber());

        values.put("company", company);
        values.put("customer", customer);
        values.put("phone", phone);
        values.put("site", site);

        values.put("kw", kw);
        values.put("amount", amount);

        values.put("date", date);
        values.put("status", status);
        values.put("work", work);

        getWritableDatabase().insert(
                "projects",
                null,
                values
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

        ContentValues values = new ContentValues();

        values.put("company_id", companyId);
        values.put("company", company);
        values.put("customer", customer);
        values.put("phone", phone);
        values.put("site", site);

        values.put("kw", kw);
        values.put("amount", amount);

        values.put("date", date);
        values.put("status", status);
        values.put("work", work);

        getWritableDatabase().update(
                "projects",
                values,
                "id=?",
                new String[]{String.valueOf(old.id)}
        );
    }

    private String nextProjectNumber() {

        Cursor c = getReadableDatabase().rawQuery(
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
                new String[]{String.valueOf(projectId)}
        );

        db.delete(
                "expenses",
                "project_id=?",
                new String[]{String.valueOf(projectId)}
        );

        db.delete(
                "payments",
                "project_id=?",
                new String[]{String.valueOf(projectId)}
        );

        db.delete(
                "projects",
                "id=?",
                new String[]{String.valueOf(projectId)}
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

        ContentValues values = new ContentValues();

        values.put("project_id", projectId);
        values.put("amount", amount);
        values.put("date", date);
        values.put("mode", "Payment");
        values.put("note", note);

        getWritableDatabase().insert(
                "payments",
                null,
                values
        );
    }

    public double collection(int projectId) {

        return queryDouble(
                "SELECT COALESCE(SUM(amount),0) " +
                        "FROM payments WHERE project_id=?",
                projectId
        );
    }

    public double pending(int projectId) {

        Project p = project(projectId);

        if (p == null) {
            return 0;
        }

        return Math.max(
                0,
                p.amount - collection(projectId)
        );
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

        ContentValues values = new ContentValues();

        values.put("project_id", projectId);
        values.put("category", category);
        values.put("amount", amount);
        values.put("date", date);
        values.put("note", note);

        getWritableDatabase().insert(
                "expenses",
                null,
                values
        );
    }

    public double expenses(int projectId) {

        return queryDouble(
                "SELECT COALESCE(SUM(amount),0) " +
                        "FROM expenses WHERE project_id=?",
                projectId
        );
    }

    public double profit(int projectId) {

        Project p = project(projectId);

        if (p == null) {
            return 0;
        }

        return p.amount - expenses(projectId);
    }

    // =========================================================
    // PHOTOS
    // =========================================================

    public void addPhoto(
            int projectId,
            String uri
    ) {

        ContentValues values = new ContentValues();

        values.put("project_id", projectId);
        values.put("uri", uri);

        getWritableDatabase().insert(
                "photos",
                null,
                values
        );
    }

    public ArrayList<String> photos(
            int projectId
    ) {

        ArrayList<String> list =
                new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT uri FROM photos " +
                        "WHERE project_id=? ORDER BY id DESC",
                new String[]{String.valueOf(projectId)}
        );

        while (c.moveToNext()) {
            list.add(safe(c.getString(0)));
        }

        c.close();

        return list;
    }

    // =========================================================
    // WORKERS
    // =========================================================

    public ArrayList<Worker> workers() {

        ArrayList<Worker> list =
                new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone,daily_salary," +
                        "monthly_salary " +
                        "FROM workers ORDER BY name",
                null
        );

        while (c.moveToNext()) {

            Worker worker = new Worker();

            worker.id = c.getInt(0);
            worker.name = safe(c.getString(1));
            worker.phone = safe(c.getString(2));

            worker.dailySalary = c.getDouble(3);
            worker.monthlySalary = c.getDouble(4);

            list.add(worker);
        }

        c.close();

        return list;
    }

    public Worker worker(int id) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone,daily_salary," +
                        "monthly_salary FROM workers WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        Worker worker = null;

        if (c.moveToFirst()) {

            worker = new Worker();

            worker.id = c.getInt(0);
            worker.name = safe(c.getString(1));
            worker.phone = safe(c.getString(2));

            worker.dailySalary = c.getDouble(3);
            worker.monthlySalary = c.getDouble(4);
        }

        c.close();

        return worker;
    }

    public void saveWorker(Worker worker) {

        ContentValues values =
                new ContentValues();

        values.put("name", worker.name);
        values.put("phone", worker.phone);

        values.put(
                "daily_salary",
                worker.dailySalary
        );

        values.put(
                "monthly_salary",
                worker.monthlySalary
        );

        if (worker.id == 0) {

            getWritableDatabase().insert(
                    "workers",
                    null,
                    values
            );

        } else {

            getWritableDatabase().update(
                    "workers",
                    values,
                    "id=?",
                    new String[]{
                            String.valueOf(worker.id)
                    }
            );
        }
    }

    public void deleteWorker(int workerId) {

        SQLiteDatabase db =
                getWritableDatabase();

        db.delete(
                "attendance",
                "worker_id=?",
                new String[]{
                        String.valueOf(workerId)
                }
        );

        db.delete(
                "advances",
                "worker_id=?",
                new String[]{
                        String.valueOf(workerId)
                }
        );

        db.delete(
                "workers",
                "id=?",
                new String[]{
                        String.valueOf(workerId)
                }
        );
    }

    // =========================================================
    // ATTENDANCE
    // =========================================================

    public void saveAttendance(
            int workerId,
            String date,
            String status,
            String site,
            String note
    ) {

        ContentValues values =
                new ContentValues();

        values.put("worker_id", workerId);
        values.put("date", date);
        values.put("status", status);
        values.put("site", site);
        values.put("note", note);

        getWritableDatabase().insertWithOnConflict(
                "attendance",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public int presentDays(
            int workerId,
            String month
    ) {

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT COUNT(*) FROM attendance " +
                                "WHERE worker_id=? " +
                                "AND status='Present' " +
                                "AND date LIKE ?",
                        new String[]{
                                String.valueOf(workerId),
                                month + "%"
                        }
                );

        int result = 0;

        if (c.moveToFirst()) {
            result = c.getInt(0);
        }

        c.close();

        return result;
    }

    public int absentDays(
            int workerId,
            String month
    ) {

        Cursor c =
                getReadableDatabase().rawQuery(
                        "SELECT COUNT(*) FROM attendance " +
                                "WHERE worker_id=? " +
                                "AND status='Absent' " +
                                "AND date LIKE ?",
                        new String[]{
                                String.valueOf(workerId),
                                month + "%"
                        }
                );

        int result = 0;

        if (c.moveToFirst()) {
            result = c.getInt(0);
        }

        c.close();

        return result;
    }

    // =========================================================
    // ADVANCE
    // =========================================================

    public void saveAdvance(
            int workerId,
            double amount,
            String date,
            String note
    ) {

        ContentValues values =
                new ContentValues();

        values.put("worker_id", workerId);
        values.put("amount", amount);
        values.put("date", date);
        values.put("note", note);

        getWritableDatabase().insert(
                "advances",
                null,
                values
        );
    }

    public double advances(int workerId) {

        return queryDouble(
                "SELECT COALESCE(SUM(amount),0) " +
                        "FROM advances WHERE worker_id=?",
                workerId
        );
    }

    // =========================================================
    // SALARY CALCULATION
    // =========================================================

    public double salaryForMonth(
            Worker worker,
            String month
    ) {

        if (worker.monthlySalary > 0) {

            return worker.monthlySalary;
        }

        int days =
                presentDays(
                        worker.id,
                        month
                );

        return days * worker.dailySalary;
    }

    public double salaryBalance(
            Worker worker,
            String month
    ) {

        double salary =
                salaryForMonth(
                        worker,
                        month
                );

        double advance =
                advances(worker.id);

        return Math.max(
                0,
                salary - advance
        );
    }

    public String workerReport(
            Worker worker,
            String month
    ) {

        int present =
                presentDays(
                        worker.id,
                        month
                );

        int absent =
                absentDays(
                        worker.id,
                        month
                );

        double salary =
                salaryForMonth(
                        worker,
                        month
                );

        double advance =
                advances(worker.id);

        double balance =
                Math.max(
                        0,
                        salary - advance
                );

        return
                "WORKER: " +
                        worker.name +

                        "\nPresent: " +
                        present +

                        "\nAbsent: " +
                        absent +

                        "\nDaily Salary: ₹" +
                        fmt(worker.dailySalary) +

                        "\nMonthly Salary: ₹" +
                        fmt(worker.monthlySalary) +

                        "\nSalary: ₹" +
                        fmt(salary) +

                        "\nAdvance: ₹" +
                        fmt(advance) +

                        "\nBALANCE: ₹" +
                        fmt(balance);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    public String dashboard() {

        double totalKw = 0;
        double totalValue = 0;
        double totalCollection = 0;
        double totalExpenses = 0;

        ArrayList<Project> list =
                projects();

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
                list.size() +

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
    // HELPERS
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

    private String safe(String value) {

        return value == null
                ? ""
                : value;
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
