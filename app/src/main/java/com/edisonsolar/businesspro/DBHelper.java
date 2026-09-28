package com.edisonsolar.businesspro;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "edison_manager_pro.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE companies(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT)");
        db.execSQL("CREATE TABLE projects(id INTEGER PRIMARY KEY AUTOINCREMENT,company_id INTEGER DEFAULT 0,number TEXT,company TEXT,customer TEXT,phone TEXT,site TEXT,kw REAL DEFAULT 0,amount REAL DEFAULT 0,date TEXT,status TEXT,work TEXT,lat REAL DEFAULT 0,lon REAL DEFAULT 0,has_location INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,project_id INTEGER,amount REAL DEFAULT 0,date TEXT,mode TEXT,note TEXT)");
        db.execSQL("CREATE TABLE expenses(id INTEGER PRIMARY KEY AUTOINCREMENT,project_id INTEGER,category TEXT,amount REAL DEFAULT 0,date TEXT,note TEXT)");
        db.execSQL("CREATE TABLE photos(id INTEGER PRIMARY KEY AUTOINCREMENT,project_id INTEGER,uri TEXT)");
        db.execSQL("CREATE TABLE workers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT,daily_salary REAL DEFAULT 0,monthly_salary REAL DEFAULT 0)");
        db.execSQL("CREATE TABLE attendance(id INTEGER PRIMARY KEY AUTOINCREMENT,worker_id INTEGER NOT NULL,date TEXT NOT NULL,status TEXT NOT NULL,site TEXT,note TEXT,UNIQUE(worker_id,date))");
        db.execSQL("CREATE TABLE advances(id INTEGER PRIMARY KEY AUTOINCREMENT,worker_id INTEGER NOT NULL,amount REAL DEFAULT 0,date TEXT NOT NULL,note TEXT)");
    }

    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
        // Version 1 is the complete schema used by the paired MainActivity.
    }

    private SQLiteDatabase w(){ return getWritableDatabase(); }

    public ArrayList<Project> projects(){
        ArrayList<Project> list=new ArrayList<>();
        Cursor c=w().rawQuery("SELECT id,company_id,number,company,customer,phone,site,kw,amount,date,status,work,lat,lon,has_location FROM projects ORDER BY id DESC",null);
        while(c.moveToNext()) list.add(Project.from(c));
        c.close(); return list;
    }

    public Project project(int id){
        Cursor c=w().rawQuery("SELECT id,company_id,number,company,customer,phone,site,kw,amount,date,status,work,lat,lon,has_location FROM projects WHERE id=?",new String[]{""+id});
        Project p=c.moveToFirst()?Project.from(c):null; c.close(); return p;
    }

    public static String fmt(double x){ return String.format(Locale.getDefault(),"%.2f",x); }

    public void addAdvance(int workerId,double amount,String date,String note){
        w().execSQL("INSERT INTO advances(worker_id,amount,date,note) VALUES(?,?,?,?)",new Object[]{workerId,amount,date,note});
    }

    public void updateAdvance(int id,int workerId,double amount,String date,String note){
        w().execSQL("UPDATE advances SET worker_id=?,amount=?,date=?,note=? WHERE id=?",new Object[]{workerId,amount,date,note,id});
    }

    public void deleteAdvance(int id){ w().delete("advances","id=?",new String[]{""+id}); }
}

class Project {
    int id,companyId;
    String number,company,customer,phone,site,date,status,work;
    double kw,amount,lat,lon;
    boolean hasLocation;

    static Project from(Cursor c){
        Project p=new Project();
        p.id=c.getInt(0);
        p.companyId=c.getInt(1);
        p.number=c.getString(2);
        p.company=c.getString(3);
        p.customer=c.getString(4);
        p.phone=c.getString(5);
        p.site=c.getString(6);
        p.kw=c.getDouble(7);
        p.amount=c.getDouble(8);
        p.date=c.getString(9);
        p.status=c.getString(10);
        p.work=c.getString(11);
        p.lat=c.getDouble(12);
        p.lon=c.getDouble(13);
        p.hasLocation=c.getInt(14)!=0;
        return p;
    }
}
