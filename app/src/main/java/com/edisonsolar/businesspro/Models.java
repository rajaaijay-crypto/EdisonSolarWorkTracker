package com.edisonsolar.businesspro;

import android.database.Cursor;

class Company {
    int id;
    String name = "";
    String phone = "";
}

class Worker {
    int id;
    String name = "";
    String phone = "";
    double dailySalary = 0;
    double monthlySalary = 0;
}

class Project {
    int id;
    int companyId;
    String number = "";
    String company = "";
    String customer = "";
    String phone = "";
    String site = "";
    String date = "";
    String status = "";
    String work = "";
    double kw = 0;
    double amount = 0;
    double lat = 0;
    double lon = 0;
    boolean hasLoc = false;

    static Project from(Cursor c) {
        Project p = new Project();
        p.id = c.getInt(0);
        p.companyId = c.getInt(1);
        p.number = c.getString(2);
        p.company = c.getString(3);
        p.customer = c.getString(4);
        p.phone = c.getString(5);
        p.site = c.getString(6);
        p.kw = c.getDouble(7);
        p.amount = c.getDouble(8);
        p.date = c.getString(9);
        p.status = c.getString(10);
        p.work = c.getString(11);
        p.lat = c.getDouble(12);
        p.lon = c.getDouble(13);
        p.hasLoc = c.getInt(14) == 1;
        return p;
    }
}


