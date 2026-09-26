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
