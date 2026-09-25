
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
    private static final int DB_VERSION = 1;

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
        db.execSQL("DROP TABLE IF EXISTS photos");
        db.execSQL("DROP TABLE IF EXISTS expenses");
        db.execSQL("DROP TABLE IF EXISTS payments");
        db.execSQL("DROP TABLE IF EXISTS projects");
        db.execSQL("DROP TABLE IF EXISTS companies");

        onCreate(db);
    }

    // ---------------------------------------------------------
    // COMPANY
    // ---------------------------------------------------------

    public ArrayList<Company> companies() {

        ArrayList<Company> list =
                new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor c =
                db.rawQuery(
                        "SELECT id,name,phone FROM companies ORDER BY name",
                        null
                );

        while (c.moveToNext()) {

            Company x = new Company();

            x.id = c.getInt(0);
            x.name = c.getString(1);
            x.phone = c.getString(2);

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

        getWritableDatabase()
                .insert(
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

        getWritableDatabase()
                .update(
                        "companies",
                        v,
                        "id=?",
                        new String[]{
                                String.valueOf(id)
                        }
                );
    }

    public void deleteCompany(int id) {

        SQLiteDatabase db =
                getWritableDatabase();

        Cursor c =
                db.rawQuery(
                        "SELECT id FROM projects WHERE company_id=?",
                        new String[]{
                                String.valueOf(id)
                        }
                );

        ArrayList<Integer> projectIds =
                new ArrayList<>();

        while (c.moveToNext()) {
            projectIds.add(c.getInt(0));
        }

        c.close();

        for (Integer projectId : projectIds) {
            deleteProject(projectId);
        }

        db.delete(
                "companies",
                "id=?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public int countProjects(int companyId) {

        Cursor c =
                getReadableDatabase()
                        .rawQuery(
                                "SELECT COUNT(*) FROM projects WHERE company_id=?",
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
                "SELECT COALESCE(SUM(kw),0) FROM projects WHERE company_id=?",
                companyId
        );
    }

    public double companyValue(int companyId) {

        return queryDouble(
                "SELECT COALESCE(SUM(amount),0) FROM projects WHERE company_id=?",
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

    // ---------------------------------------------------------
    // PROJECT
    // ---------------------------------------------------------

    public ArrayList<Project> projects() {

        ArrayList<Project> list =
                new ArrayList<>();

        Cursor c =
                getReadableDatabase()
                        .rawQuery(
                                "SELECT " +
                                        "id,company_id,number,company,customer," +
                                        "phone,site,kw,amount,date,status,work," +
                                        "lat,lon,has_location " +
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

        Cursor c =
                getReadableDatabase()
                        .rawQuery(
                                "SELECT " +
                                        "id,company_id,number,company,customer," +
                                        "phone,site,kw,amount,date,status,work," +
                                        "lat,lon,has_location " +
                                        "FROM projects WHERE id=?",
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

        getWritableDatabase()
                .insert(
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

        getWritableDatabase()
                .update(
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
                getReadableDatabase()
                        .rawQuery(
                                "SELECT COUNT(*) FROM projects",
                                null
                        );

       
