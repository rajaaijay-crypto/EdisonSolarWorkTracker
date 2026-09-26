package com.edisonsolar.businesspro;

class Company {

    int id;
    String name;
    String phone;

    Company() {
        id = 0;
        name = "";
        phone = "";
    }
}

class Project {

    int id;
    int companyId;

    String number;
    String company;
    String customer;
    String phone;
    String site;

    double kw;
    double amount;

    String date;
    String status;
    String work;

    double lat;
    double lon;

    boolean hasLoc;

    Project() {
        id = 0;
        companyId = 0;

        number = "";
        company = "";
        customer = "";
        phone = "";
        site = "";

        kw = 0;
        amount = 0;

        date = "";
        status = "";
        work = "";

        lat = 0;
        lon = 0;

        hasLoc = false;
    }
}

class Worker {

    int id;
    String name;
    String phone;

    double dailySalary;
    double monthlySalary;

    Worker() {
        id = 0;
        name = "";
        phone = "";
        dailySalary = 0;
        monthlySalary = 0;
    }
}

class Attendance {

    int id;
    int workerId;

    String date;
    String status;
    String site;
    String note;

    Attendance() {
        id = 0;
        workerId = 0;
        date = "";
        status = "";
        site = "";
        note = "";
    }
}

class Advance {

    int id;
    int workerId;

    double amount;

    String date;
    String note;

    Advance() {
        id = 0;
        workerId = 0;
        amount = 0;
        date = "";
        note = "";
    }
}
