package com.edisonsolar.businesspro;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
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
    private DBHelper db;
    private LinearLayout root;
    private String currentPage = "home";
    private Project activeProject;
    private Uri cameraUri;
    private int photoProjectId = -1;
    private static final int PHOTO_REQUEST = 201;
    private static final int CAMERA_REQUEST = 202;
    private static final int BLUE = Color.rgb(11,94,215), GREEN = Color.rgb(25,135,84),
            ORANGE = Color.rgb(240,138,36), PURPLE = Color.rgb(111,66,193),
            RED = Color.rgb(220,53,69), DARK = Color.rgb(23,50,77), BG = Color.rgb(245,249,253);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(13,71,161));
        getWindow().setNavigationBarColor(Color.rgb(13,71,161));
        db = new DBHelper(this);
        home();
    }
    private int dp(int n) { return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
    private String safe(String s) { return s == null ? "" : s; }
    private double num(String s) { try { return Double.parseDouble(s.trim().replace(",","")); } catch(Exception e) { return 0; } }
    private String money(double d) { return String.format(Locale.US,"%,.2f",d); }
    private String today() { return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()); }
    private String month() { return new SimpleDateFormat("yyyy-MM",Locale.US).format(new Date()); }
    private TextView text(String s,int size,boolean bold) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(DARK);
        t.setTypeface(null,bold?Typeface.BOLD:Typeface.NORMAL); t.setPadding(dp(9),dp(9),dp(9),dp(9)); return t;
    }
    private EditText field(String hint,String value) {
        EditText e=new EditText(this); e.setHint(hint); e.setText(value); e.setTextSize(18);
        e.setTextColor(DARK); e.setSingleLine(false); e.setPadding(dp(12),dp(10),dp(12),dp(10)); return e;
    }
    private Button button(String label,int color,View.OnClickListener click) {
        Button b=new Button(this); b.setText(label); b.setTextSize(17); b.setAllCaps(false);
        b.setTypeface(null,Typeface.BOLD); b.setTextColor(Color.WHITE); b.setMinHeight(dp(58));
        android.graphics.drawable.GradientDrawable shape=new android.graphics.drawable.GradientDrawable();
        shape.setColor(color); shape.setCornerRadius(dp(14)); b.setBackground(shape); b.setOnClickListener(click);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(60)); lp.setMargins(dp(3),dp(6),dp(3),dp(6)); b.setLayoutParams(lp); return b;
    }
    private TextView card(String value) {
        TextView t=text(value,17,true); t.setBackgroundColor(Color.WHITE); t.setPadding(dp(15),dp(15),dp(15),dp(15));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(dp(3),dp(7),dp(3),dp(7)); t.setLayoutParams(lp); return t;
    }
    private void page(String title,String id) {
        currentPage=id;
        ScrollView sc=new ScrollView(this); sc.setFillViewport(true);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(12),dp(12),dp(12),dp(35)); root.setBackgroundColor(BG);
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=text("←",31,true); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->home());
        header.addView(back,new LinearLayout.LayoutParams(dp(52),dp(58)));
        TextView heading=text(title,23,true); heading.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(heading,new LinearLayout.LayoutParams(0,dp(65),1)); root.addView(header);
        sc.addView(root); setContentView(sc);
    }
    @Override public void onBackPressed() { if("home".equals(currentPage)) super.onBackPressed(); else home(); }
    private void confirm(String message,Runnable action) { new AlertDialog.Builder(this).setMessage(message).setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->action.run()).show(); }
    private void toast(String s) { Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
    private LinearLayout vertical() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(15),dp(8),dp(15),dp(8)); return l; }
    private void dialog(String title,LinearLayout form,Runnable save) {
        ScrollView scroll=new ScrollView(this); scroll.addView(form);
        AlertDialog d=new AlertDialog.Builder(this).setTitle(title).setView(scroll).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{ try { save.run(); d.dismiss(); } catch(Exception ex) { toast("Check input: "+ex.getMessage()); } })); d.show();
    }
    private Cursor query(String sql,String... args) { return db.getReadableDatabase().rawQuery(sql,args); }
    private double scalar(String sql,String... args) { try(Cursor c=query(sql,args)) { return c.moveToFirst()?c.getDouble(0):0; } }
    private void delete(String table,int id) { db.getWritableDatabase().delete(table,"id=?",new String[]{String.valueOf(id)}); }
    private android.content.ContentValues values(Object... kv) {
        android.content.ContentValues v=new android.content.ContentValues();
        for(int i=0;i<kv.length;i+=2) { String k=(String)kv[i]; Object o=kv[i+1];
            if(o instanceof Integer)v.put(k,(Integer)o); else if(o instanceof Double)v.put(k,(Double)o); else v.put(k,String.valueOf(o)); }
        return v;
    }
    private void save(String table,Integer id,android.content.ContentValues v) {
        if(id==null)db.getWritableDatabase().insertOrThrow(table,null,v);
        else db.getWritableDatabase().update(table,v,"id=?",new String[]{String.valueOf(id)});
    }
    private Project getProject(int id) { for(Project p:db.projects())if(p.id==id)return p; return null; }

    private String totalSolarKw() {
        double total = 0;
        for(Project p : db.projects()) total += p.kw;
        return money(total);
    }

    private void home() {
        page("EDISON SOLAR MANAGER PRO","home");
        ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.edison_solar_logo);
        logo.setAdjustViewBounds(true); logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        root.addView(logo,new LinearLayout.LayoutParams(-1,dp(140)));
        root.addView(card("📊 EDISON SOLAR DASHBOARD\n\nProjects: " + db.projects().size() + "\nTotal Solar: " + totalSolarKw() + " kW"));
        root.addView(button("🏢 Companies",BLUE,v->companies()));
        root.addView(button("☀ Projects",BLUE,v->projects()));
        root.addView(button("👷 Workers / Attendance",PURPLE,v->workers()));
        root.addView(button("💰 Payment Collection",GREEN,v->payments()));
        root.addView(button("🧾 Expenses",ORANGE,v->expenses()));
        root.addView(button("📅 Calendar",PURPLE,v->calendar()));
        root.addView(button("📊 Reports / PDF",PURPLE,v->reports()));
    }
    private void companies() {
        page("Companies","companies"); root.addView(button("+ Add Company",BLUE,v->companyDialog(null)));
        for(Company c:db.companies()) {
            root.addView(card(c.name+"\nPhone: "+safe(c.phone)+"\nProjects: "+(int)scalar("SELECT COUNT(*) FROM projects WHERE company_id=?",String.valueOf(c.id))));
            root.addView(button("Edit "+c.name,BLUE,v->companyDialog(c)));
            root.addView(button("Delete "+c.name,RED,v->confirm("Delete company?",()->{db.deleteCompany(c.id);companies();})));
        }
    }
    private void companyDialog(Company old) {
        LinearLayout l=vertical(); EditText name=field("Company name",old==null?"":safe(old.name));
        EditText phone=field("Phone",old==null?"":safe(old.phone)); l.addView(name);l.addView(phone);
        dialog(old==null?"Add Company":"Edit Company",l,()->{
            if(name.getText().toString().trim().isEmpty())throw new IllegalArgumentException("Company name required");
            if(old==null)db.addCompany(name.getText().toString().trim(),phone.getText().toString().trim());
            else db.updateCompany(old.id,name.getText().toString().trim(),phone.getText().toString().trim()); companies();
        });
    }
    private double projectSalary(Project p) {
        double total=0;
        try(Cursor c=query("SELECT a.worker_id,COUNT(*) FROM attendance a WHERE a.status='Present' AND (a.site=? OR a.site=?) GROUP BY a.worker_id",safe(p.site),safe(p.number))) {
            while(c.moveToNext()) { Worker w=db.worker(c.getInt(0)); if(w!=null)total+=w.dailySalary*c.getInt(1); }
        } return total;
    }
    private int workDays(Project p) { return (int)scalar("SELECT COUNT(DISTINCT date) FROM attendance WHERE status='Present' AND (site=? OR site=?)",safe(p.site),safe(p.number)); }
    private String projectSummary(Project p) {
        double collection=db.collection(p.id),expenses=db.expenses(p.id),salary=projectSalary(p);
        return "☀ "+safe(p.number)+"\nCustomer: "+safe(p.customer)+"\nCompany: "+safe(p.company)+"\nSite: "+safe(p.site)+
            "\nSolar: "+money(p.kw)+" kW\nProject value: ₹"+money(p.amount)+"\nCollection: ₹"+money(collection)+
            "\nExpenses: ₹"+money(expenses)+"\nWork days: "+workDays(p)+"\nSite salary: ₹"+money(salary)+
            "\nProfit (collected basis): ₹"+money(collection-expenses-salary)+"\nPending: ₹"+money(db.pending(p.id))+"\nDate: "+safe(p.date);
    }
    private void projects() {
        page("Projects","projects");root.addView(button("+ Add Project",BLUE,v->projectDialog(null)));
        for(Project p:db.projects()) {root.addView(card(projectSummary(p)));
            root.addView(button("Open full report",BLUE,v->projectFullReport(p)));
            root.addView(button("Edit / Delete / Photos / GPS",PURPLE,v->projectMenu(p)));}
    }
    private void projectDialog(Project old) {
        LinearLayout l=vertical(); ArrayList<Company> companies=db.companies();
        ArrayList<String> labels=new ArrayList<>(); labels.add("No company / manual"); for(Company c:companies)labels.add(c.name);
        Spinner companySpinner=spinner(labels.toArray(new String[0]));
        if(old!=null)for(int i=0;i<companies.size();i++)if(companies.get(i).id==old.companyId)companySpinner.setSelection(i+1);
        EditText company=field("Company name",old==null?"":safe(old.company));
        EditText customer=field("Customer",old==null?"":safe(old.customer));
        EditText phone=field("Phone",old==null?"":safe(old.phone));
        EditText site=field("Site",old==null?"":safe(old.site));
        EditText kw=field("Solar kW",old==null?"":String.valueOf(old.kw));
        EditText amount=field("Project amount",old==null?"":String.valueOf(old.amount));
        EditText date=field("Date YYYY-MM-DD",old==null?today():safe(old.date));
        EditText status=field("Status",old==null?"Pending":safe(old.status));
        EditText work=field("Work detail",old==null?"":safe(old.work));
        l.addView(text("Select company",17,true));l.addView(companySpinner);
        for(View v:new View[]{company,customer,phone,site,kw,amount,date,status,work})l.addView(v);
        dialog(old==null?"Add Project":"Edit Project",l,()->{
            String cust=customer.getText().toString().trim();if(cust.isEmpty())throw new IllegalArgumentException("Customer required");
            int selected=companySpinner.getSelectedItemPosition(),cid=selected==0?0:companies.get(selected-1).id;
            String cname=selected==0?company.getText().toString():companies.get(selected-1).name;
            if(old==null)db.addProject(cid,cname,cust,phone.getText().toString(),site.getText().toString(),num(kw.getText().toString()),num(amount.getText().toString()),date.getText().toString(),status.getText().toString(),work.getText().toString());
            else db.updateProject(old,cid,cname,cust,phone.getText().toString(),site.getText().toString(),num(kw.getText().toString()),num(amount.getText().toString()),date.getText().toString(),status.getText().toString(),work.getText().toString());
            projects();
        });
    }
    private void projectMenu(Project p) {
        String[] items={"Edit project","Payments","Expenses","Photos","GPS / Map","Project PDF","Share report","Delete project"};
        new AlertDialog.Builder(this).setTitle(safe(p.customer)).setItems(items,(d,n)->{
            switch(n) {case 0:projectDialog(p);break;case 1:projectPayments(p);break;case 2:projectExpenses(p);break;
                case 3:photos(p);break;case 4:gps(p);break;case 5:pdf(projectReport(p),"project_"+p.id);break;
                case 6:share(projectReport(p));break;case 7:confirm("Delete project and its related records?",()->{db.deleteProject(p.id);projects();});break;}
        }).show();
    }
    private void payments() {page("Payment Collection","payments");for(Project p:db.projects()){
        root.addView(card(safe(p.number)+" • "+safe(p.customer)+"\nCollected: ₹"+money(db.collection(p.id))+"\nPending: ₹"+money(db.pending(p.id))));
        root.addView(button("Collection history / Add",GREEN,v->projectPayments(p)));}}
    private void projectPayments(Project p) {
        page("Project Payments","payments");root.addView(card(projectSummary(p)));
        root.addView(button("+ Add Collection",GREEN,v->paymentDialog(p,null)));
        try(Cursor c=query("SELECT id,date,amount,mode,note FROM payments WHERE project_id=? ORDER BY id DESC",String.valueOf(p.id))) {
            while(c.moveToNext()) {int id=c.getInt(0);root.addView(card("₹"+money(c.getDouble(2))+"\n"+safe(c.getString(1))+" • "+safe(c.getString(3))+"\n"+safe(c.getString(4))));
                root.addView(button("Edit payment",BLUE,v->paymentDialog(p,id)));
                root.addView(button("Delete payment",RED,v->confirm("Delete payment?",()->{delete("payments",id);projectPayments(p);})));}
        }
    }
    private Spinner spinner(String[] labels) {Spinner s=new Spinner(this);ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,labels);a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);s.setAdapter(a);s.setMinimumHeight(dp(58));return s;}
    private void paymentDialog(Project p,Integer id) {
        LinearLayout l=vertical();EditText amount=field("Amount ₹",""),date=field("Date YYYY-MM-DD",today()),note=field("Note","");
        String[] modes={"Cash","UPI","Bank Transfer","Cheque","Other"};Spinner mode=spinner(modes);
        if(id!=null)try(Cursor c=query("SELECT amount,date,mode,note FROM payments WHERE id=?",String.valueOf(id))){if(c.moveToFirst()){
            amount.setText(String.valueOf(c.getDouble(0)));date.setText(safe(c.getString(1)));note.setText(safe(c.getString(3)));
            for(int i=0;i<modes.length;i++)if(modes[i].equalsIgnoreCase(safe(c.getString(2))))mode.setSelection(i);
        }}
        l.addView(amount);l.addView(date);l.addView(text("Payment mode",17,true));l.addView(mode);l.addView(note);
        dialog(id==null?"Add Collection":"Edit Collection",l,()->{
            double value=num(amount.getText().toString());if(value<=0)throw new IllegalArgumentException("Amount required");
            save("payments",id,values("project_id",p.id,"amount",value,"date",date.getText().toString(),"mode",mode.getSelectedItem().toString(),"note",note.getText().toString()));projectPayments(p);
        });
    }
    private void expenses() {page("Expenses","expenses");root.addView(button("+ Add Expense",ORANGE,v->expenseDialog(null,null)));
        try(Cursor c=query("SELECT e.id,e.project_id,e.category,e.amount,e.date,e.note,p.customer FROM expenses e LEFT JOIN projects p ON p.id=e.project_id ORDER BY e.id DESC")){
            while(c.moveToNext()){int id=c.getInt(0),pid=c.getInt(1);Project p=getProject(pid);
                root.addView(card(safe(c.getString(6))+"\n"+safe(c.getString(2))+" ₹"+money(c.getDouble(3))+"\n"+safe(c.getString(4))+"\n"+safe(c.getString(5))));
                root.addView(button("Edit expense",BLUE,v->expenseDialog(p,id)));
                root.addView(button("Delete expense",RED,v->confirm("Delete expense?",()->{delete("expenses",id);expenses();})));}
        }
    }
    private void projectExpenses(Project p) {page("Project Expenses","expenses");root.addView(card(projectSummary(p)));
        root.addView(button("+ Add Expense",ORANGE,v->expenseDialog(p,null)));
        try(Cursor c=query("SELECT id,category,amount,date,note FROM expenses WHERE project_id=? ORDER BY id DESC",String.valueOf(p.id))){
            while(c.moveToNext()){int id=c.getInt(0);root.addView(card(safe(c.getString(1))+" ₹"+money(c.getDouble(2))+"\n"+safe(c.getString(3))+"\n"+safe(c.getString(4))));
                root.addView(button("Edit expense",BLUE,v->expenseDialog(p,id)));
                root.addView(button("Delete expense",RED,v->confirm("Delete expense?",()->{delete("expenses",id);projectExpenses(p);})));}
        }
    }
    private void expenseDialog(Project selected,Integer id) {
        ArrayList<Project> ps=db.projects();if(ps.isEmpty()){toast("Add a project first");return;}
        LinearLayout l=vertical();ArrayList<String> labels=new ArrayList<>();for(Project p:ps)labels.add(safe(p.number)+" • "+safe(p.customer));
        Spinner project=spinner(labels.toArray(new String[0]));if(selected!=null)for(int i=0;i<ps.size();i++)if(ps.get(i).id==selected.id)project.setSelection(i);
        String[] types={"Other","Material","Labour","Tea","Lunch","Transportation","Fuel","Tools","Travel","Site Expense"};Spinner type=spinner(types);
        EditText amount=field("Expense amount ₹",""),date=field("Date YYYY-MM-DD",today()),note=field("Note","");
        if(id!=null)try(Cursor c=query("SELECT project_id,amount,category,date,note FROM expenses WHERE id=?",String.valueOf(id))){if(c.moveToFirst()){
            for(int i=0;i<ps.size();i++)if(ps.get(i).id==c.getInt(0))project.setSelection(i);
            amount.setText(String.valueOf(c.getDouble(1)));for(int i=0;i<types.length;i++)if(types[i].equalsIgnoreCase(safe(c.getString(2))))type.setSelection(i);
            date.setText(safe(c.getString(3)));note.setText(safe(c.getString(4)));}}
        l.addView(text("Project",17,true));l.addView(project);l.addView(amount);l.addView(text("Expense type",17,true));l.addView(type);l.addView(date);l.addView(note);
        dialog(id==null?"Add Expense":"Edit Expense",l,()->{
            double value=num(amount.getText().toString());if(value<=0)throw new IllegalArgumentException("Amount required");
            int pid=ps.get(project.getSelectedItemPosition()).id;
            save("expenses",id,values("project_id",pid,"amount",value,"category",type.getSelectedItem().toString(),"date",date.getText().toString(),"note",note.getText().toString()));
            if(selected!=null)projectExpenses(selected);else expenses();
        });
    }
    private void workers() {
        page("Workers / Attendance","workers");root.addView(button("+ Add Worker",PURPLE,v->workerDialog(null)));
        root.addView(button("Attendance Calendar",BLUE,v->calendar()));
        for(Worker w:db.workers()) {
            root.addView(card(safe(w.name)+"\nPhone: "+safe(w.phone)+"\nDaily: ₹"+money(w.dailySalary)+"\nMonthly: ₹"+money(w.monthlySalary)+
                "\nPresent (this month): "+present(w.id,month())+"\nAbsent: "+absent(w.id,month())+"\nAdvance: ₹"+money(advances(w.id,month()))+
                "\nPending salary: ₹"+money(salary(w,month())-advances(w.id,month()))));
            root.addView(button("Attendance / Salary / Advance",PURPLE,v->workerDetail(w)));
            root.addView(button("Edit worker",BLUE,v->workerDialog(w)));
            root.addView(button("Delete worker",RED,v->confirm("Delete worker and related attendance / advances?",()->{
                db.getWritableDatabase().delete("attendance","worker_id=?",new String[]{String.valueOf(w.id)});
                db.getWritableDatabase().delete("advances","worker_id=?",new String[]{String.valueOf(w.id)});
                delete("workers",w.id);workers();})));
        }
    }
    private void workerDialog(Worker old) {
        LinearLayout l=vertical();EditText name=field("Worker name",old==null?"":safe(old.name));
        EditText phone=field("Phone",old==null?"":safe(old.phone));
        EditText daily=field("Daily salary ₹",old==null?"0":String.valueOf(old.dailySalary));
        EditText monthly=field("Monthly salary ₹ (0 for daily)",old==null?"0":String.valueOf(old.monthlySalary));
        for(View v:new View[]{name,phone,daily,monthly})l.addView(v);
        dialog(old==null?"Add Worker":"Edit Worker",l,()->{
            if(name.getText().toString().trim().isEmpty())throw new IllegalArgumentException("Worker name required");
            save("workers",old==null?null:old.id,values("name",name.getText().toString().trim(),"phone",phone.getText().toString(),"daily_salary",num(daily.getText().toString()),"monthly_salary",num(monthly.getText().toString())));workers();
        });
    }
    private int present(int id,String m) {return (int)scalar("SELECT COUNT(*) FROM attendance WHERE worker_id=? AND status='Present' AND date LIKE ?",String.valueOf(id),m+"%");}
    private int absent(int id,String m) {return (int)scalar("SELECT COUNT(*) FROM attendance WHERE worker_id=? AND status='Absent' AND date LIKE ?",String.valueOf(id),m+"%");}
    private int halfDay(int id,String m) {return (int)scalar("SELECT COUNT(*) FROM attendance WHERE worker_id=? AND status='Half Day' AND date LIKE ?",String.valueOf(id),m+"%");}
    private double advances(int id,String m) {return scalar("SELECT COALESCE(SUM(amount),0) FROM advances WHERE worker_id=? AND date LIKE ?",String.valueOf(id),m+"%");}
    private double salary(Worker w,String m) {return w.monthlySalary>0?w.monthlySalary:w.dailySalary*(present(w.id,m)+(halfDay(w.id,m)*0.5));}
    private void workerDetail(Worker w) {
        page("Worker: "+safe(w.name),"workers");String m=month();
        root.addView(card("Present: "+present(w.id,m)+"\nHalf Day: "+halfDay(w.id,m)+"\nAbsent: "+absent(w.id,m)+"\nSalary: ₹"+money(salary(w,m))+"\nAdvance: ₹"+money(advances(w.id,m))+"\nBalance: ₹"+money(salary(w,m)-advances(w.id,m))));
        root.addView(button("+ / Edit Attendance",BLUE,v->attendanceDialog(w,null)));
        root.addView(button("+ Salary Advance",ORANGE,v->advanceDialog(w,null)));
        root.addView(button("Worker PDF",PURPLE,v->pdf(workerReport(w,m),"salary_"+w.id)));
        root.addView(button("Share Worker Report",GREEN,v->share(workerReport(w,m))));
        root.addView(text("Attendance History",20,true));
        try(Cursor c=query("SELECT id,date,status,site,note FROM attendance WHERE worker_id=? ORDER BY date DESC",String.valueOf(w.id))){
            while(c.moveToNext()){int id=c.getInt(0);root.addView(card(safe(c.getString(1))+" • "+safe(c.getString(2))+"\nSite: "+safe(c.getString(3))+"\n"+safe(c.getString(4))));
                root.addView(button("Edit attendance",BLUE,v->attendanceDialog(w,id)));
                root.addView(button("Delete attendance",RED,v->confirm("Delete attendance?",()->{delete("attendance",id);workerDetail(w);})));}
        }
        root.addView(text("Advance History",20,true));
        try(Cursor c=query("SELECT id,date,amount,note FROM advances WHERE worker_id=? ORDER BY date DESC",String.valueOf(w.id))){
            while(c.moveToNext()){int id=c.getInt(0);root.addView(card(safe(c.getString(1))+" • ₹"+money(c.getDouble(2))+"\n"+safe(c.getString(3))));
                root.addView(button("Edit advance",BLUE,v->advanceDialog(w,id)));
                root.addView(button("Delete advance",RED,v->confirm("Delete advance?",()->{delete("advances",id);workerDetail(w);})));}
        }
    }
    private void attendanceDialog(Worker w,Integer id) {
        LinearLayout l=vertical();EditText date=field("Date YYYY-MM-DD",today()),site=field("Project / Site",""),note=field("Note","");
        Spinner status=spinner(new String[]{"Present","Absent"});
        if(id!=null)try(Cursor c=query("SELECT date,status,site,note FROM attendance WHERE id=?",String.valueOf(id))){if(c.moveToFirst()){
            date.setText(safe(c.getString(0)));status.setSelection("Absent".equalsIgnoreCase(safe(c.getString(1)))?1:0);
            site.setText(safe(c.getString(2)));note.setText(safe(c.getString(3)));}}
        l.addView(date);l.addView(status);l.addView(site);l.addView(note);
        dialog(id==null?"Add Attendance":"Edit Attendance",l,()->{
            String day=date.getText().toString().trim();if(day.isEmpty())throw new IllegalArgumentException("Date required");
            android.content.ContentValues v=values("worker_id",w.id,"date",day,"status",status.getSelectedItem().toString(),"site",site.getText().toString(),"note",note.getText().toString());
            if(id==null)db.getWritableDatabase().insertWithOnConflict("attendance",null,v,android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
            else db.getWritableDatabase().update("attendance",v,"id=?",new String[]{String.valueOf(id)});workerDetail(w);
        });
    }
    private void advanceDialog(Worker w,Integer id) {
        LinearLayout l=vertical();EditText amount=field("Advance amount ₹",""),date=field("Date YYYY-MM-DD",today()),note=field("Note","");
        if(id!=null)try(Cursor c=query("SELECT amount,date,note FROM advances WHERE id=?",String.valueOf(id))){if(c.moveToFirst()){
            amount.setText(String.valueOf(c.getDouble(0)));date.setText(safe(c.getString(1)));note.setText(safe(c.getString(2)));}}
        l.addView(amount);l.addView(date);l.addView(note);
        dialog(id==null?"Add Advance":"Edit Advance",l,()->{
            double a=num(amount.getText().toString());if(a<=0)throw new IllegalArgumentException("Amount required");
            save("advances",id,values("worker_id",w.id,"amount",a,"date",date.getText().toString(),"note",note.getText().toString()));workerDetail(w);
        });
    }
    private void calendar() {
        page("Attendance Calendar","calendar");
        CalendarView calendarView=new CalendarView(this);
        calendarView.setDate(System.currentTimeMillis(),false,true);
        root.addView(calendarView,new LinearLayout.LayoutParams(-1,dp(330)));
        root.addView(button("This Month Salary Report",PURPLE,v->salaryReportPage()));
        String initial=today();
        showDay(initial);
        calendarView.setOnDateChangeListener((view,year,month,day)->{
            String selected=String.format(Locale.US,"%04d-%02d-%02d",year,month+1,day);
            showDay(selected);
        });
    }

    private LinearLayout calendarDetails;

    private void showDay(String day) {
        if(calendarDetails != null) root.removeView(calendarDetails);
        calendarDetails=vertical();
        calendarDetails.addView(text("Attendance: "+day,20,true));
        calendarDetails.addView(text("Mark attendance for each worker",16,false));

        try(Cursor c=query("SELECT id,name,daily_salary,monthly_salary FROM workers ORDER BY name")){
            int count=0;
            while(c.moveToNext()){
                count++;
                Worker w=new Worker(c.getInt(0),safe(c.getString(1)),safe(c.getString(2)),c.getDouble(3));
                int attendanceId=0;
                String status="Not Marked",site="",note="";
                try(Cursor a=query("SELECT id,status,site,note FROM attendance WHERE worker_id=? AND date=?",
                        String.valueOf(w.id),day)){
                    if(a.moveToFirst()){
                        attendanceId=a.getInt(0);
                        status=safe(a.getString(1));
                        site=safe(a.getString(2));
                        note=safe(a.getString(3));
                    }
                }
                LinearLayout workerBox=vertical();
                workerBox.addView(card("👷 "+safe(w.name)+"\nStatus: "+status+
                    (site.isEmpty()?"":"\nSite: "+site)+(note.isEmpty()?"":"\nNote: "+note)));
                final Worker fw=w;
                final int faid=attendanceId;
                final String fday=day;
                workerBox.addView(button(attendanceId==0?"Mark Attendance":"Edit Attendance",BLUE,
                    v->calendarAttendanceDialog(fw,fday,faid)));
                calendarDetails.addView(workerBox);
            }
            if(count==0) calendarDetails.addView(card("No workers added."));
        }
        root.addView(calendarDetails);
    }

    private void calendarAttendanceDialog(Worker w,String day,int attendanceId) {
        LinearLayout l=vertical();
        l.addView(text("Worker: "+safe(w.name)+"\nDate: "+day,18,true));
        Spinner status=spinner(new String[]{"Present","Half Day","Absent"});
        EditText site=field("Project / Site","");
        EditText note=field("Note","");

        if(attendanceId>0) {
            try(Cursor c=query("SELECT status,site,note FROM attendance WHERE id=?",
                    String.valueOf(attendanceId))){
                if(c.moveToFirst()){
                    String st=safe(c.getString(0));
                    if("Half Day".equalsIgnoreCase(st)) status.setSelection(1);
                    else if("Absent".equalsIgnoreCase(st)) status.setSelection(2);
                    else status.setSelection(0);
                    site.setText(safe(c.getString(1)));
                    note.setText(safe(c.getString(2)));
                }
            }
        }

        l.addView(status);
        l.addView(site);
        l.addView(note);

        dialog(attendanceId==0?"Add Attendance":"Edit Attendance",l,()->{
            android.content.ContentValues v=values(
                "worker_id",w.id,
                "date",day,
                "status",status.getSelectedItem().toString(),
                "site",site.getText().toString(),
                "note",note.getText().toString()
            );
            if(attendanceId==0) {
                android.database.Cursor old=query("SELECT id FROM attendance WHERE worker_id=? AND date=? LIMIT 1",
                    String.valueOf(w.id),day);
                boolean exists=old.moveToFirst();
                old.close();
                if(exists) {
                    db.getWritableDatabase().update("attendance",v,
                        "worker_id=? AND date=?",new String[]{String.valueOf(w.id),day});
                } else {
                    db.getWritableDatabase().insertWithOnConflict("attendance",null,v,
                        android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
                }
            } else {
                db.getWritableDatabase().update("attendance",v,"id=?",
                    new String[]{String.valueOf(attendanceId)});
            }
            calendar();
        });
    }

    private String workerReport(Worker w,String m) {
        return "EDISON SOLAR WORKER SALARY REPORT\nMonth: "+m+"\nWorker: "+safe(w.name)+"\nPresent: "+present(w.id,m)+
            "\nAbsent: "+absent(w.id,m)+"\nDaily Salary: Rs."+money(w.dailySalary)+"\nMonthly Salary: Rs."+money(w.monthlySalary)+
            "\nSalary: Rs."+money(salary(w,m))+"\nAdvance: Rs."+money(advances(w.id,m))+"\nBALANCE: Rs."+money(salary(w,m)-advances(w.id,m));
    }
    private void salaryReportPage() {
        page("Salary Report","reports");String m=month();String report=allSalaryReport(m);root.addView(card(report));
        root.addView(button("Export Salary PDF",PURPLE,v->pdf(report,"salary_"+m)));
        root.addView(button("WhatsApp / Share",GREEN,v->share(report)));
    }
    private String allSalaryReport(String m) {
        StringBuilder s=new StringBuilder("EDISON SOLAR WORKER SALARY REPORT\nMonth: "+m+"\n\n");double total=0;
        for(Worker w:db.workers()){s.append(workerReport(w,m)).append("\n\n");total+=salary(w,m)-advances(w.id,m);}
        s.append("TOTAL PENDING SALARY: Rs.").append(money(total));return s.toString();
    }
    private void reports() {
        page("Reports / PDF","reports");root.addView(button("Worker Salary Report",PURPLE,v->salaryReportPage()));
        root.addView(card(db.dashboard()));for(Project p:db.projects()){
            root.addView(card(projectSummary(p)));
            root.addView(button("Project PDF: "+safe(p.number),BLUE,v->pdf(projectReport(p),"project_"+p.id)));
            root.addView(button("Full Project Report",GREEN,v->projectFullReport(p)));
        }
    }
    private String projectReport(Project p) {
        StringBuilder s=new StringBuilder("EDISON SOLAR MANAGER PRO\nPROJECT REPORT\n\n");s.append(projectSummary(p)).append("\n\nCOLLECTION HISTORY\n");
        try(Cursor c=query("SELECT date,amount,mode,note FROM payments WHERE project_id=? ORDER BY date,id",String.valueOf(p.id))){
            while(c.moveToNext())s.append(safe(c.getString(0))).append(" | Rs.").append(money(c.getDouble(1))).append(" | ").append(safe(c.getString(2))).append(" | ").append(safe(c.getString(3))).append('\n');}
        s.append("\nEXPENSE HISTORY\n");
        try(Cursor c=query("SELECT date,category,amount,note FROM expenses WHERE project_id=? ORDER BY date,id",String.valueOf(p.id))){
            while(c.moveToNext())s.append(safe(c.getString(0))).append(" | ").append(safe(c.getString(1))).append(" | Rs.").append(money(c.getDouble(2))).append(" | ").append(safe(c.getString(3))).append('\n');}
        s.append("\nWORKERS / SITE ATTENDANCE\n");
        try(Cursor c=query("SELECT w.name,a.date,a.status,a.site FROM attendance a JOIN workers w ON w.id=a.worker_id WHERE a.site=? OR a.site=? ORDER BY a.date",safe(p.site),safe(p.number))){
            while(c.moveToNext())s.append(safe(c.getString(1))).append(" | ").append(safe(c.getString(0))).append(" | ").append(safe(c.getString(2))).append('\n');}
        return s.toString();
    }
    private void projectFullReport(Project p) {
        page("Full Project Report","reports");root.addView(card(projectReport(p)));
        root.addView(button("Collection History / Edit",GREEN,v->projectPayments(p)));
        root.addView(button("Expenses / Edit",ORANGE,v->projectExpenses(p)));
        root.addView(button("PDF",PURPLE,v->pdf(projectReport(p),"project_"+p.id)));
        root.addView(button("WhatsApp / Share",GREEN,v->share(projectReport(p))));
    }
    private void share(String value) {
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,value);
        startActivity(Intent.createChooser(i,"Share report"));
    }
    private void pdf(String content,String name) {
        try {
            PdfDocument doc=new PdfDocument();Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(Color.BLACK);paint.setTextSize(12);
            String[] lines=content.replace("₹","Rs.").split("\n",-1);int pageNo=1,index=0;
            do {PdfDocument.PageInfo info=new PdfDocument.PageInfo.Builder(595,842,pageNo).create();PdfDocument.Page page=doc.startPage(info);
                Canvas canvas=page.getCanvas();int y=48;while(index<lines.length&&y<795){String line=lines[index++];
                    while(line.length()>78){canvas.drawText(line.substring(0,78),35,y,paint);y+=18;line=line.substring(78);if(y>=795)break;}
                    if(y<795){canvas.drawText(line,35,y,paint);y+=19;}}
                doc.finishPage(page);pageNo++;}while(index<lines.length);
            File dir=new File(getCacheDir(),"reports");if(!dir.exists()&&!dir.mkdirs())throw new IOException("Cannot create report folder");
            File file=new File(dir,name+"_"+System.currentTimeMillis()+".pdf");try(FileOutputStream out=new FileOutputStream(file)){doc.writeTo(out);}doc.close();
            Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
            Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("application/pdf");intent.putExtra(Intent.EXTRA_STREAM,uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(intent,"Share PDF / WhatsApp"));
        }catch(Exception e){toast("PDF error: "+e.getMessage());}
    }
    private void photos(Project p) {
        page("Project Photos","photos");photoProjectId=p.id;
        root.addView(button("Choose Photo",BLUE,v->{Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(i,PHOTO_REQUEST);}));
        root.addView(button("Open Camera",GREEN,v->camera()));
        try(Cursor c=query("SELECT id,uri FROM photos WHERE project_id=? ORDER BY id DESC",String.valueOf(p.id))){
            while(c.moveToNext()){int id=c.getInt(0);String value=safe(c.getString(1));
                try{ImageView image=new ImageView(this);image.setImageURI(Uri.parse(value));image.setAdjustViewBounds(true);
                    image.setMaxHeight(dp(300));root.addView(image,new LinearLayout.LayoutParams(-1,dp(220)));}catch(Exception ignored){}
                root.addView(button("Open Photo",BLUE,v->{try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(value));i.setType("image/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(i);}catch(Exception e){toast("Cannot open photo");}}));
                root.addView(button("Delete Photo",RED,v->confirm("Delete photo entry?",()->{delete("photos",id);photos(p);})));}
        }
    }
    private void camera() {
        try{File file=File.createTempFile("solar_photo_",".jpg",getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES));
            cameraUri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
            Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);startActivityForResult(i,CAMERA_REQUEST);
        }catch(Exception e){toast("Camera unavailable: "+e.getMessage());}
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);if(result!=RESULT_OK||photoProjectId<0)return;
        Uri uri=request==CAMERA_REQUEST?cameraUri:(data==null?null:data.getData());if(uri==null)return;
        try{if(request==PHOTO_REQUEST)getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
        save("photos",null,values("project_id",photoProjectId,"uri",uri.toString()));Project p=getProject(photoProjectId);if(p!=null)photos(p);
    }
    private void gps(Project p) {
        String place=safe(p.site).trim();if(place.isEmpty())place=safe(p.customer);
        Uri uri=Uri.parse("geo:0,0?q="+Uri.encode(place));Intent intent=new Intent(Intent.ACTION_VIEW,uri);
        try{startActivity(intent);}catch(Exception e){Intent web=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/search/?api=1&query="+Uri.encode(place)));startActivity(web);}
    }
}
