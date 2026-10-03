package com.edisonsolar.businesspro;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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
    DBHelper db; LinearLayout root; Project photoProject; Uri cameraUri;
    final int BLUE=Color.rgb(11,94,215),GREEN=Color.rgb(25,135,84),ORANGE=Color.rgb(240,138,36),PURPLE=Color.rgb(111,66,193),RED=Color.rgb(220,53,69),DARK=Color.rgb(23,50,77),BG=Color.rgb(245,249,253);
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().setNavigationBarColor(Color.WHITE);db=new DBHelper(this);home();}
    @Override public void onBackPressed(){home();}
    TextView txt(String s,float z,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(DARK);t.setTypeface(null,bold?Typeface.BOLD:Typeface.NORMAL);t.setPadding(dp(10),dp(8),dp(10),dp(8));return t;}
    EditText field(String h,String v){EditText e=new EditText(this);e.setHint(h);e.setText(v);e.setTextSize(17);e.setTextColor(DARK);e.setPadding(dp(12),dp(8),dp(12),dp(8));return e;}
    Button button(String s,int color,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextSize(17);b.setTypeface(null,Typeface.BOLD);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setMinHeight(dp(60));b.setOnClickListener(l);android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(dp(16));b.setBackground(g);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(62));p.setMargins(dp(4),dp(7),dp(4),dp(7));b.setLayoutParams(p);return b;}
    TextView card(String s){TextView t=txt(s,17,true);t.setBackgroundColor(Color.WHITE);t.setPadding(dp(14),dp(13),dp(14),dp(13));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(4),dp(6),dp(4),dp(6));t.setLayoutParams(p);return t;}
    void page(String title){ScrollView sv=new ScrollView(this);sv.setFillViewport(true);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(14),dp(14),dp(30));root.setBackgroundColor(BG);LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);Button back=new Button(this);back.setText("←");back.setTextSize(30);back.setTextColor(DARK);back.setBackgroundColor(Color.TRANSPARENT);back.setOnClickListener(v->home());h.addView(back,new LinearLayout.LayoutParams(dp(58),dp(64)));h.addView(txt(title,24,true),new LinearLayout.LayoutParams(0,dp(64),1));root.addView(h);sv.addView(root);setContentView(sv);}
    String safe(String s){return s==null?"":s;} double num(String s){try{return Double.parseDouble(s.trim().replace(",",""));}catch(Exception e){return 0;}} String fmt(double x){return DBHelper.fmt(x);} String today(){return new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault()).format(new Date());} String iso(){return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date());}
    android.database.sqlite.SQLiteDatabase r(){return db.getReadableDatabase();} android.database.sqlite.SQLiteDatabase w(){return db.getWritableDatabase();}
    void confirm(String s,Runnable yes){new AlertDialog.Builder(this).setMessage(s).setNegativeButton("Cancel",null).setPositiveButton("OK",(d,x)->yes.run()).show();}

    void home(){page("EDISON SOLAR MANAGER PRO");ImageView logo=new ImageView(this);int id=getResources().getIdentifier("edison_solar_logo","drawable",getPackageName());if(id!=0)logo.setImageResource(id);logo.setAdjustViewBounds(true);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);root.addView(logo,new LinearLayout.LayoutParams(-1,dp(150)));root.addView(card(dashboardText()));root.addView(button("🏢  Companies",BLUE,v->companies()));root.addView(button("☀️  Projects",BLUE,v->projects()));root.addView(button("👷  Workers / Attendance",PURPLE,v->workers()));root.addView(button("💰  Payment Collection",GREEN,v->payments()));root.addView(button("🧾  Expenses",ORANGE,v->expenses()));root.addView(button("📅  Calendar",PURPLE,v->calendar()));root.addView(button("📊  Reports / PDF",PURPLE,v->reports()));}
    String dashboardText(){
    double kw=0; int pc=0;
    android.database.Cursor c=r().rawQuery("SELECT COALESCE(SUM(kw),0),COUNT(*) FROM projects",null);
    if(c.moveToFirst()){kw=c.getDouble(0);pc=c.getInt(1);} c.close();
    return "📊 DASHBOARD\nTotal Projects: "+pc+"\nTotal Solar: "+fmt(kw)+" kW";
}


    void companies(){page("Companies");root.addView(button("+ Add Company",BLUE,v->companyDialog(0)));android.database.Cursor c=r().rawQuery("SELECT id,name,phone FROM companies ORDER BY name",null);while(c.moveToNext()){int id=c.getInt(0);String name=safe(c.getString(1)),phone=safe(c.getString(2));root.addView(card(name+"\nPhone: "+phone));LinearLayout row=new LinearLayout(this);row.addView(button("✏️ Edit",BLUE,v->companyDialog(id)),new LinearLayout.LayoutParams(0,dp(62),1));row.addView(button("🗑 Delete",RED,v->confirm("Delete company?",()->{w().delete("companies","id=?",new String[]{""+id});companies();})),new LinearLayout.LayoutParams(0,dp(62),1));root.addView(row);}c.close();}
    void companyDialog(int id){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);EditText n=field("Company Name","");EditText p=field("Phone","");if(id>0){android.database.Cursor c=r().rawQuery("SELECT name,phone FROM companies WHERE id=?",new String[]{""+id});if(c.moveToFirst()){n.setText(c.getString(0));p.setText(safe(c.getString(1)));}c.close();}l.addView(n);l.addView(p);AlertDialog d=new AlertDialog.Builder(this).setTitle(id==0?"Add Company":"Edit Company").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{if(n.getText().toString().trim().isEmpty()){n.setError("Required");return;}ContentValues cv=new ContentValues();cv.put("name",n.getText().toString().trim());cv.put("phone",p.getText().toString().trim());if(id==0)w().insert("companies",null,cv);else w().update("companies",cv,"id=?",new String[]{""+id});d.dismiss();companies();}));d.show();}

    ArrayList<Project> projectList(){return db.projects();}
    interface MonthCallback{void onMonth(String month);}
    String monthOfDate(String date){
        if(date==null)return "";
        String d=date.trim();
        if(d.matches("\\d{4}-\\d{2}-\\d{2}"))return d.substring(0,7);
        if(d.matches("\\d{2}-\\d{2}-\\d{4}"))return d.substring(6,10)+"-"+d.substring(3,5);
        return "";
    }
    String currentMonth(){return iso().substring(0,7);}
    void monthPicker(String current,MonthCallback done){
        try{
            String[] a=current.split("-");
            int y=Integer.parseInt(a[0]),m=Integer.parseInt(a[1])-1;
            DatePickerDialog p=new DatePickerDialog(this,(v,yy,mm,dd)->
                done.onMonth(String.format(Locale.US,"%04d-%02d",yy,mm+1)),y,m,1);
            p.show();
        }catch(Exception e){Toast.makeText(this,"Month select error",Toast.LENGTH_SHORT).show();}
    }
    boolean projectInMonth(Project p,String month){return month.equals(monthOfDate(p.date));}
    void projects(){projects(currentMonth());}
void projects(String month){
    page("Projects");
    root.addView(button("📅 Select Month: "+month,BLUE,v->monthPicker(month,m->projects(m))));
    root.addView(button("+ Add Project",PURPLE,v->projectDialog(0)));
    double totalKw=0;int totalProjects=0;
    for(Project p:projectList()){
        if(!projectInMonth(p,month))continue;
        totalProjects++;totalKw+=p.kw;
        root.addView(card(projectSummary(p)));
        root.addView(button("📊 Full Project Report",BLUE,v->projectFullReport(p)));
        root.addView(button("✏️ Edit / Manage",PURPLE,v->projectMenu(p)));
    }
    root.addView(card("MONTH TOTAL • "+month+"\nTotal Projects: "+totalProjects+
        "\nTotal Solar: "+fmt(totalKw)+" kW"));
}

    String projectSummary(Project p){double col=sum("payments",p.id),ex=sum("expenses",p.id),sal=projectSalary(p);return "☀️ PROJECT: "+p.number+"\nCustomer: "+safe(p.customer)+"\nCompany: "+safe(p.company)+"\nSite: "+safe(p.site)+"\n⚡ Solar: "+fmt(p.kw)+" kW\n💰 Collection: ₹"+fmt(col)+"\n💸 Expenses: ₹"+fmt(ex)+"\n👷 Work Days: "+projectDays(p)+"\n👷 Site Salary: ₹"+fmt(sal)+"\n📊 Profit: ₹"+fmt(col-ex-sal)+"\n🔴 Pending: ₹"+fmt(Math.max(0,p.amount-col))+"\n📅 Date: "+safe(p.date);}
    double sum(String table,int pid){android.database.Cursor c=r().rawQuery("SELECT COALESCE(SUM(amount),0) FROM "+table+" WHERE project_id=?",new String[]{""+pid});double x=c.moveToFirst()?c.getDouble(0):0;c.close();return x;}
    int projectDays(Project p){android.database.Cursor c=r().rawQuery("SELECT COUNT(DISTINCT date) FROM attendance WHERE status IN ('Present','Half Day') AND (site=? OR site LIKE ? OR site LIKE ?)",new String[]{p.site,p.number+"%","%"+p.number+"%"});int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
    double projectSalary(Project p){double x=0;android.database.Cursor c=r().rawQuery("SELECT worker_id,status FROM attendance WHERE status IN ('Present','Half Day') AND (site=? OR site LIKE ? OR site LIKE ?)",new String[]{p.site,p.number+"%","%"+p.number+"%"});while(c.moveToNext()){android.database.Cursor q=r().rawQuery("SELECT daily_salary FROM workers WHERE id=?",new String[]{""+c.getInt(0)});if(q.moveToFirst()){double daily=q.getDouble(0);x+="Half Day".equalsIgnoreCase(c.getString(1))?daily*0.5:daily;}q.close();}c.close();return x;}
    void projectDialog(int id){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);String[] v={"","","","","0","0",today(),"Pending",""};if(id>0){android.database.Cursor c=r().rawQuery("SELECT company,customer,phone,site,kw,amount,date,status,work FROM projects WHERE id=?",new String[]{""+id});if(c.moveToFirst())for(int i=0;i<9;i++)v[i]=safe(c.getString(i));c.close();}EditText co=field("Company",v[0]),cu=field("Customer",v[1]),ph=field("Phone",v[2]),si=field("Site",v[3]),kw=field("Solar kW",v[4]),am=field("Project Amount",v[5]),da=field("Date",v[6]),st=field("Status",v[7]),wo=field("Work Detail",v[8]);l.addView(co);l.addView(cu);l.addView(ph);l.addView(si);l.addView(kw);l.addView(am);l.addView(da);l.addView(st);l.addView(wo);AlertDialog d=new AlertDialog.Builder(this).setTitle(id==0?"Add Project":"Edit Project").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(b->{if(cu.getText().toString().trim().isEmpty()){cu.setError("Required");return;}ContentValues cv=new ContentValues();cv.put("company",co.getText().toString());cv.put("customer",cu.getText().toString());cv.put("phone",ph.getText().toString());cv.put("site",si.getText().toString());cv.put("kw",num(kw.getText().toString()));cv.put("amount",num(am.getText().toString()));cv.put("date",da.getText().toString());cv.put("status",st.getText().toString());cv.put("work",wo.getText().toString());if(id==0){long nid=w().insert("projects",null,cv);ContentValues n=new ContentValues();n.put("number",String.format(Locale.US,"ES-%04d",nid));w().update("projects",n,"id=?",new String[]{""+nid});}else w().update("projects",cv,"id=?",new String[]{""+id});d.dismiss();projects();}));d.show();}
    void projectMenu(Project p){String[] a={"✏️ Edit Project","💰 Payments","🧾 Expenses","📷 Photos","📍 GPS / Map","📄 PDF","📱 Share / WhatsApp","🗑 Delete"};new AlertDialog.Builder(this).setTitle(p.customer).setItems(a,(d,i)->{if(i==0)projectDialog(p.id);else if(i==1)projectPayments(p);else if(i==2)projectExpenses(p);else if(i==3)photos(p);else if(i==4)gps(p);else if(i==5)pdf(p);else if(i==6)share(projectReport(p));else confirm("Delete project?",()->{w().delete("projects","id=?",new String[]{""+p.id});projects();});}).show();}

    void projectFullReport(Project p){page("Project Report");root.addView(card(projectSummary(p)));root.addView(txt("💰 Collection History",20,true));showPayments(p);root.addView(txt("💸 Expense History",20,true));showExpenses(p);root.addView(txt("👷 Workers / Salary",20,true));showProjectWorkers(p);root.addView(button("📄 Generate Project PDF",PURPLE,v->pdf(p)));root.addView(button("📱 Share Project Report",GREEN,v->share(projectReport(p))));}

    void showProjectWorkers(Project p){
        android.database.Cursor c = r().rawQuery(
                "SELECT a.worker_id," +
                "SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END)," +
                "SUM(CASE WHEN a.status='Half Day' THEN 1 ELSE 0 END)," +
                "SUM(CASE WHEN a.status='Absent' THEN 1 ELSE 0 END) " +
                "FROM attendance a " +
                "WHERE a.status IN ('Present','Half Day','Absent') " +
                "AND (a.site=? OR a.site LIKE ? OR a.site LIKE ?) " +
                "GROUP BY a.worker_id",
                new String[]{p.site,p.number+"%","%"+p.number+"%"});

        if(!c.moveToFirst()){
            root.addView(card("No worker attendance for this project."));
            c.close();
            return;
        }

        do{
            int wid = c.getInt(0);
            int present = c.getInt(1);
            int halfDay = c.getInt(2);
            int absent = c.getInt(3);

            String name = "Worker";
            double dailySalary = 0;

            android.database.Cursor q = r().rawQuery(
                    "SELECT name,daily_salary FROM workers WHERE id=?",
                    new String[]{""+wid});

            if(q.moveToFirst()){
                name = safe(q.getString(0));
                dailySalary = q.getDouble(1);
            }
            q.close();

            double salary = (present * dailySalary) + (halfDay * dailySalary * 0.5);

            root.addView(card(
                    "👷 " + name +
                    "\nPresent: " + present +
                    "\nHalf Day: " + halfDay +
                    "\nAbsent: " + absent +
                    "\nSalary: ₹" + fmt(salary)
            ));
        }while(c.moveToNext());

        c.close();
    }

    String projectReport(Project p){StringBuilder s=new StringBuilder("EDISON SOLAR MANAGER PRO\n\n");s.append(projectSummary(p)).append("\n\nCOLLECTION HISTORY\n");android.database.Cursor c=r().rawQuery("SELECT date,amount,mode,note FROM payments WHERE project_id=? ORDER BY date,id",new String[]{""+p.id});while(c.moveToNext())s.append(c.getString(0)).append(" | ₹").append(fmt(c.getDouble(1))).append(" | ").append(safe(c.getString(2))).append("\n");c.close();s.append("\nEXPENSE HISTORY\n");c=r().rawQuery("SELECT date,category,amount,note FROM expenses WHERE project_id=? ORDER BY date,id",new String[]{""+p.id});while(c.moveToNext())s.append(c.getString(0)).append(" | ").append(safe(c.getString(1))).append(" | ₹").append(fmt(c.getDouble(2))).append("\n");c.close();return s.toString();}

    void payments(){page("Payment Collection");for(Project p:projectList()){root.addView(card(projectSummary(p)));root.addView(button("💰 Add / Edit Collection",GREEN,v->projectPayments(p)));}}
    void projectPayments(Project p){page("Payments / Collection");root.addView(card(projectSummary(p)));root.addView(button("+ Add Collection",GREEN,v->paymentDialog(p,0)));showPayments(p);}
    void showPayments(Project p){android.database.Cursor c=r().rawQuery("SELECT id,date,amount,mode,note FROM payments WHERE project_id=? ORDER BY date DESC,id DESC",new String[]{""+p.id});if(!c.moveToFirst()){root.addView(card("No collection entries."));c.close();return;}do{int id=c.getInt(0);root.addView(card("📅 "+c.getString(1)+"\n💰 ₹"+fmt(c.getDouble(2))+"\n💳 "+safe(c.getString(3))+"\n"+safe(c.getString(4))));LinearLayout row=new LinearLayout(this);row.addView(button("✏️ Edit",BLUE,v->paymentDialog(p,id)),new LinearLayout.LayoutParams(0,dp(62),1));row.addView(button("🗑 Delete",RED,v->confirm("Delete collection?",()->{w().delete("payments","id=?",new String[]{""+id});projectPayments(p);})),new LinearLayout.LayoutParams(0,dp(62),1));root.addView(row);}while(c.moveToNext());c.close();}
    void paymentDialog(Project p,int id){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);EditText am=field("Collection Amount ₹",""),da=field("Date YYYY-MM-DD",iso()),no=field("Note","");Spinner mode=new Spinner(this);String[] modes={"Cash","UPI","Bank Transfer","Cheque","Other"};ArrayAdapter<String>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,modes);mode.setAdapter(a);if(id>0){android.database.Cursor c=r().rawQuery("SELECT amount,date,mode,note FROM payments WHERE id=?",new String[]{""+id});if(c.moveToFirst()){am.setText(c.getString(0));da.setText(c.getString(1));no.setText(safe(c.getString(3)));for(int i=0;i<modes.length;i++)if(modes[i].equalsIgnoreCase(safe(c.getString(2))))mode.setSelection(i);}c.close();}l.addView(am);l.addView(da);l.addView(mode);l.addView(no);AlertDialog d=new AlertDialog.Builder(this).setTitle(id==0?"Add Collection":"Edit Collection").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{double value=num(am.getText().toString());if(value<=0){am.setError("Enter amount");return;}ContentValues cv=new ContentValues();cv.put("project_id",p.id);cv.put("amount",value);cv.put("date",da.getText().toString());cv.put("mode",mode.getSelectedItem().toString());cv.put("note",no.getText().toString());if(id==0)w().insert("payments",null,cv);else w().update("payments",cv,"id=?",new String[]{""+id});d.dismiss();projectPayments(p);}));d.show();}

    void expenses(){page("Expenses");root.addView(button("+ Add Expense",ORANGE,v->expenseDialog(null,0)));boolean found=false;for(Project p:projectList()){android.database.Cursor c=r().rawQuery("SELECT COUNT(*) FROM expenses WHERE project_id=?",new String[]{""+p.id});int n=c.moveToFirst()?c.getInt(0):0;c.close();if(n>0){found=true;root.addView(card("☀️ "+p.number+" • "+p.customer+"\n💸 ₹"+fmt(sum("expenses",p.id))));showExpenses(p);}}if(!found)root.addView(card("No expenses added yet."));}
    void projectExpenses(Project p){page("Project Expenses");root.addView(card(projectSummary(p)));root.addView(button("+ Add Expense",ORANGE,v->expenseDialog(p,0)));showExpenses(p);}
    void showExpenses(Project p){android.database.Cursor c=r().rawQuery("SELECT id,date,category,amount,note FROM expenses WHERE project_id=? ORDER BY date DESC,id DESC",new String[]{""+p.id});while(c.moveToNext()){int id=c.getInt(0);root.addView(card("📅 "+c.getString(1)+"\n📂 "+safe(c.getString(2))+"\n💸 ₹"+fmt(c.getDouble(3))+"\n"+safe(c.getString(4))));LinearLayout row=new LinearLayout(this);row.addView(button("✏️ Edit",BLUE,v->expenseDialog(p,id)),new LinearLayout.LayoutParams(0,dp(62),1));row.addView(button("🗑 Delete",RED,v->confirm("Delete expense?",()->{w().delete("expenses","id=?",new String[]{""+id});projectExpenses(p);})),new LinearLayout.LayoutParams(0,dp(62),1));root.addView(row);}c.close();}
    void expenseDialog(Project selected,int id){ArrayList<Project> ps=projectList();LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);Spinner sp=new Spinner(this);ArrayList<String> names=new ArrayList<>();names.add("Select Project / Site");for(Project p:ps)names.add(p.number+" • "+p.customer);sp.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,names));EditText am=field("Expense Amount ₹",""),da=field("Date YYYY-MM-DD",iso()),no=field("Note","");Spinner type=new Spinner(this);String[] types={"Other","Material","Labour","Tea","Lunch","Transportation","Fuel","Tools","Travel","Site Expense"};type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,types));if(id>0){android.database.Cursor c=r().rawQuery("SELECT project_id,amount,category,date,note FROM expenses WHERE id=?",new String[]{""+id});if(c.moveToFirst()){int pid=c.getInt(0);for(int i=0;i<ps.size();i++)if(ps.get(i).id==pid)sp.setSelection(i+1);am.setText(c.getString(1));da.setText(c.getString(3));no.setText(safe(c.getString(4)));for(int i=0;i<types.length;i++)if(types[i].equalsIgnoreCase(safe(c.getString(2))))type.setSelection(i);}c.close();}else if(selected!=null)for(int i=0;i<ps.size();i++)if(ps.get(i).id==selected.id)sp.setSelection(i+1);l.addView(sp);l.addView(am);l.addView(type);l.addView(da);l.addView(no);AlertDialog d=new AlertDialog.Builder(this).setTitle(id==0?"Add Expense":"Edit Expense").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{if(sp.getSelectedItemPosition()<=0){Toast.makeText(this,"Select Project",Toast.LENGTH_SHORT).show();return;}double value=num(am.getText().toString());if(value<=0){am.setError("Enter amount");return;}int pid=ps.get(sp.getSelectedItemPosition()-1).id;ContentValues cv=new ContentValues();cv.put("project_id",pid);cv.put("amount",value);cv.put("category",type.getSelectedItem().toString());cv.put("date",da.getText().toString());cv.put("note",no.getText().toString());if(id==0)w().insert("expenses",null,cv);else w().update("expenses",cv,"id=?",new String[]{""+id});d.dismiss();if(selected!=null)projectExpenses(selected);else expenses();}));d.show();}
    void workers(){workers(currentMonth());}
void workers(String month){
    page("Workers / Attendance");
    root.addView(button("+ Add Worker",PURPLE,v->workerDialog(0)));
    root.addView(button("📅 Attendance",BLUE,v->attendance(iso())));
    root.addView(button("💰 Salary / Advance",GREEN,v->salaryReport(month)));
    root.addView(txt("MONTHLY WORKER DETAILS",21,true));
    root.addView(button("📅 Select Month: "+month,ORANGE,v->monthPicker(month,m->workers(m))));
    double ts=0,ta=0,tp=0;
    android.database.Cursor c=r().rawQuery("SELECT id,name,phone,daily_salary,monthly_salary FROM workers ORDER BY name",null);
    while(c.moveToNext()){
        int id=c.getInt(0);double daily=c.getDouble(3),monthly=c.getDouble(4);
        int present=countAtt(id,month,"Present"),half=countAtt(id,month,"Half Day"),absent=countAtt(id,month,"Absent");
        double salary=monthly>0?monthly:(present*daily)+(half*daily*0.5);
        double advance=sumAdvance(id,month),pending=Math.max(0,salary-advance);
        ts+=salary;ta+=advance;tp+=pending;final int wid=id;
        root.addView(card("👷 "+safe(c.getString(1))+"\nPresent: "+present+"\nHalf Day: "+half+
            "\nAbsent: "+absent+"\nDaily Salary: ₹"+fmt(daily)+"\nMonthly Salary: ₹"+fmt(monthly)+
            "\nGross Salary: ₹"+fmt(salary)+"\nAdvance: ₹"+fmt(advance)+"\nPending Balance: ₹"+fmt(pending)));
        root.addView(button("📋 Payment / Advance History",BLUE,v->workerHistory(wid,month)));
        root.addView(button("✏️ Edit Worker",ORANGE,v->workerDialog(wid)));
    }
    c.close();
    root.addView(card("MONTH TOTAL • "+month+"\nTotal Salary: ₹"+fmt(ts)+
        "\nTotal Advance: ₹"+fmt(ta)+"\nTotal Pending: ₹"+fmt(tp)));
}

    void workerDialog(int id){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);EditText n=field("Worker Name",""),p=field("Phone",""),d=field("Daily Salary ₹","0"),m=field("Monthly Salary ₹","0");if(id>0){android.database.Cursor c=r().rawQuery("SELECT name,phone,daily_salary,monthly_salary FROM workers WHERE id=?",new String[]{""+id});if(c.moveToFirst()){n.setText(c.getString(0));p.setText(safe(c.getString(1)));d.setText(c.getString(2));m.setText(c.getString(3));}c.close();}l.addView(n);l.addView(p);l.addView(d);l.addView(m);AlertDialog x=new AlertDialog.Builder(this).setTitle(id==0?"Add Worker":"Edit Worker").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();x.setOnShowListener(q->x.getButton(-1).setOnClickListener(v->{if(n.getText().toString().trim().isEmpty()){n.setError("Required");return;}ContentValues cv=new ContentValues();cv.put("name",n.getText().toString());cv.put("phone",p.getText().toString());cv.put("daily_salary",num(d.getText().toString()));cv.put("monthly_salary",num(m.getText().toString()));if(id==0)w().insert("workers",null,cv);else w().update("workers",cv,"id=?",new String[]{""+id});x.dismiss();workers();}));x.show();}
    void attendance(String date){
    page("Attendance • "+date);
    CalendarView cv=new CalendarView(this);
    try{SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.US);Date dd=f.parse(date);
        if(dd!=null)cv.setDate(dd.getTime(),false,true);}catch(Exception ignored){}
    root.addView(cv,new LinearLayout.LayoutParams(-1,dp(300)));
    root.addView(txt("Selected Date: "+date,20,true));
    root.addView(button("+ Add Attendance",PURPLE,v->attendanceDialog(date,0)));
    android.database.Cursor c=r().rawQuery("SELECT id,name FROM workers ORDER BY name",null);
    while(c.moveToNext()){
        int wid=c.getInt(0);String name=c.getString(1);
        android.database.Cursor q=r().rawQuery("SELECT id,status,site,note FROM attendance WHERE worker_id=? AND date=?",
            new String[]{""+wid,date});
        String st="Not marked",site="",note="";
        if(q.moveToFirst()){st=safe(q.getString(1));site=safe(q.getString(2));note=safe(q.getString(3));}q.close();
        root.addView(card("👷 "+name+"\nStatus: "+st+"\nSite: "+site+(note.isEmpty()?"":"\n"+note)));
        final int fwid=wid;root.addView(button("✏️ Edit Attendance",BLUE,v->attendanceDialog(date,fwid)));
    }
    c.close();
    cv.setOnDateChangeListener((view,year,month,day)->attendance(
        String.format(Locale.US,"%04d-%02d-%02d",year,month+1,day)));
}

    void attendanceDialog(String date,int wid){android.database.Cursor wc=r().rawQuery("SELECT id,name FROM workers ORDER BY name",null);ArrayList<Integer> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();while(wc.moveToNext()){ids.add(wc.getInt(0));names.add(wc.getString(1));}wc.close();if(names.isEmpty()){Toast.makeText(this,"Add worker first",Toast.LENGTH_SHORT).show();return;}LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);Spinner ws=new Spinner(this);ws.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,names));Spinner ss=new Spinner(this);ss.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,new String[]{"Present","Half Day","Absent"}));EditText site=field("Project / Site",""),note=field("Note","");for(int i=0;i<ids.size();i++)if(ids.get(i)==wid)ws.setSelection(i);if(wid>0){ws.setEnabled(false);android.database.Cursor c=r().rawQuery("SELECT status,site,note FROM attendance WHERE worker_id=? AND date=?",new String[]{""+wid,date});if(c.moveToFirst()){String oldStatus=safe(c.getString(0)); if("Half Day".equalsIgnoreCase(oldStatus)) ss.setSelection(1); else if("Absent".equalsIgnoreCase(oldStatus)) ss.setSelection(2); else ss.setSelection(0);site.setText(safe(c.getString(1)));note.setText(safe(c.getString(2)));}c.close();}l.addView(ws);l.addView(ss);l.addView(site);l.addView(note);AlertDialog d=new AlertDialog.Builder(this).setTitle("Attendance • "+date).setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{int worker=ids.get(ws.getSelectedItemPosition());ContentValues cv=new ContentValues();cv.put("worker_id",worker);cv.put("date",date);cv.put("status",ss.getSelectedItem().toString());cv.put("site",site.getText().toString());cv.put("note",note.getText().toString());long exists=0;android.database.Cursor c=r().rawQuery("SELECT id FROM attendance WHERE worker_id=? AND date=?",new String[]{""+worker,date});if(c.moveToFirst())exists=c.getLong(0);c.close();if(exists==0)w().insert("attendance",null,cv);else w().update("attendance",cv,"id=?",new String[]{""+exists});d.dismiss();attendance(date); }));d.show();}
    void salaryReport(String month){
    page("Salary / Advance Report");
    root.addView(button("📅 Select Month: "+month,BLUE,v->monthPicker(month,m->salaryReport(m))));
    salaryCards(month);
}

    String vText(EditText e){return e.getText().toString().trim();}
    void salaryCards(String month){
    double ts=0,ta=0,tp=0;
    android.database.Cursor c=r().rawQuery("SELECT id,name,daily_salary,monthly_salary FROM workers ORDER BY name",null);
    while(c.moveToNext()){
        int id=c.getInt(0);double daily=c.getDouble(2),monthly=c.getDouble(3);
        int present=countAtt(id,month,"Present"),half=countAtt(id,month,"Half Day"),absent=countAtt(id,month,"Absent");
        double sal=monthly>0?monthly:(present*daily)+(half*daily*0.5),adv=sumAdvance(id,month),bal=Math.max(0,sal-adv);
        ts+=sal;ta+=adv;tp+=bal;final int wid=id;
        root.addView(card("👷 "+c.getString(1)+"\nPresent: "+present+"\nHalf Day: "+half+"\nAbsent: "+absent+
            "\nDaily Salary: ₹"+fmt(daily)+"\nMonthly Salary: ₹"+fmt(monthly)+"\nGross Salary: ₹"+fmt(sal)+
            "\nAdvance: ₹"+fmt(adv)+"\nPending Balance: ₹"+fmt(bal)));
        root.addView(button("📋 Payment / Advance History",BLUE,v->workerHistory(wid,month)));
        root.addView(button("➕ Add Salary Advance",ORANGE,v->addAdvance(wid,month)));
        root.addView(button("✏️ Edit Worker Salary",PURPLE,v->workerDialog(wid)));
    }
    c.close();
    root.addView(card("TOTAL SALARY: ₹"+fmt(ts)+"\nTOTAL ADVANCE: ₹"+fmt(ta)+"\nTOTAL PENDING: ₹"+fmt(tp)));
    root.addView(button("📄 Salary PDF",PURPLE,v->createPdf("Salary_"+month,salaryText(month))));
    root.addView(button("📱 Share / WhatsApp",GREEN,v->share(salaryText(month))));
}


    void addAdvance(int workerId,String month){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);
        EditText amount=field("Advance amount ₹",""),date=field("Date YYYY-MM-DD",iso()),note=field("Note","");
        l.addView(amount);l.addView(date);l.addView(note);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Add Salary Advance").setView(l)
            .setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{
            double a=num(amount.getText().toString());String dt=date.getText().toString().trim();
            if(a<=0){amount.setError("Enter amount");return;}if(dt.length()!=10){date.setError("Use YYYY-MM-DD");return;}
            ContentValues cv=new ContentValues();cv.put("worker_id",workerId);cv.put("amount",a);cv.put("date",dt);cv.put("note",note.getText().toString().trim());
            long id=w().insert("advances",null,cv);if(id<0){Toast.makeText(this,"Advance save failed",Toast.LENGTH_LONG).show();return;}
            d.dismiss();salaryReport(month);
        }));d.show();
    }
    void editAdvance(int workerId,int advanceId,String month){
        android.database.Cursor c=r().rawQuery("SELECT amount,date,note FROM advances WHERE id=? AND worker_id=?",
            new String[]{""+advanceId,""+workerId});
        if(!c.moveToFirst()){c.close();return;}double oa=c.getDouble(0);String od=safe(c.getString(1)),on=safe(c.getString(2));c.close();
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);
        EditText amount=field("Advance amount ₹",String.valueOf(oa)),date=field("Date YYYY-MM-DD",od),note=field("Note",on);
        l.addView(amount);l.addView(date);l.addView(note);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Edit Salary Advance").setView(l)
            .setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{
            double a=num(amount.getText().toString());String dt=date.getText().toString().trim();
            if(a<=0){amount.setError("Enter amount");return;}if(dt.length()!=10){date.setError("Use YYYY-MM-DD");return;}
            ContentValues cv=new ContentValues();cv.put("worker_id",workerId);cv.put("amount",a);cv.put("date",dt);cv.put("note",note.getText().toString().trim());
            w().update("advances",cv,"id=? AND worker_id=?",new String[]{""+advanceId,""+workerId});d.dismiss();workerHistory(workerId,month);
        }));d.show();
    }
    void workerHistory(int workerId,String month){
    page("Worker Payment / Advance History");
    android.database.Cursor wc=r().rawQuery("SELECT name,daily_salary,monthly_salary FROM workers WHERE id=?",new String[]{""+workerId});
    if(!wc.moveToFirst()){wc.close();return;}
    String name=safe(wc.getString(0));double daily=wc.getDouble(1),monthly=wc.getDouble(2);wc.close();
    int present=countAtt(workerId,month,"Present"),half=countAtt(workerId,month,"Half Day"),absent=countAtt(workerId,month,"Absent");
    double salary=monthly>0?monthly:(present*daily)+(half*daily*0.5),advance=sumAdvance(workerId,month),pending=Math.max(0,salary-advance);
    root.addView(card("👷 "+name+"\nMonth: "+month+"\nSalary: ₹"+fmt(salary)+"\nAdvance: ₹"+fmt(advance)+"\nPending: ₹"+fmt(pending)));
    root.addView(button("➕ Add Advance",ORANGE,v->addAdvance(workerId,month)));
    root.addView(txt("💰 PAYMENT / WORK HISTORY",20,true));
    android.database.Cursor a=r().rawQuery("SELECT date,status,site,note FROM attendance WHERE worker_id=? AND date LIKE ? ORDER BY date",
        new String[]{""+workerId,month+"%"});
    boolean any=false;
    while(a.moveToNext()){any=true;String st=safe(a.getString(1));double earned="Present".equalsIgnoreCase(st)?daily:("Half Day".equalsIgnoreCase(st)?daily*0.5:0);
        root.addView(card("📅 "+safe(a.getString(0))+"\nStatus: "+st+"\nSite: "+safe(a.getString(2))+"\nEarned: ₹"+fmt(earned)+(safe(a.getString(3)).isEmpty()?"":"\nNote: "+safe(a.getString(3)))));
    }
    a.close();if(!any)root.addView(card("No attendance/payment entries for this month."));
    root.addView(txt("💵 ADVANCE HISTORY",20,true));
    android.database.Cursor ad=r().rawQuery("SELECT id,date,amount,note FROM advances WHERE worker_id=? AND date LIKE ? ORDER BY date,id",
        new String[]{""+workerId,month+"%"});
    boolean has=false;
    while(ad.moveToNext()){has=true;final int advanceId=ad.getInt(0);
        root.addView(card("📅 "+safe(ad.getString(1))+"\nAdvance: ₹"+fmt(ad.getDouble(2))+(safe(ad.getString(3)).isEmpty()?"":"\nNote: "+safe(ad.getString(3)))));
        root.addView(button("✏️ Edit Advance",BLUE,v->editAdvance(workerId,advanceId,month)));
        root.addView(button("🗑 Delete Advance",RED,v->confirm("Delete advance?",()->{w().delete("advances","id=? AND worker_id=?",new String[]{""+advanceId,""+workerId});workerHistory(workerId,month);})));
    }
    ad.close();if(!has)root.addView(card("No advance entries for this month."));
}


    int countAtt(int wid,String month,String status){android.database.Cursor c=r().rawQuery("SELECT COUNT(*) FROM attendance WHERE worker_id=? AND date LIKE ? AND status=?",new String[]{""+wid,month+"%",status});int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
    double sumAdvance(int wid,String month){android.database.Cursor c=r().rawQuery("SELECT COALESCE(SUM(amount),0) FROM advances WHERE worker_id=? AND date LIKE ?",new String[]{""+wid,month+"%"});double x=c.moveToFirst()?c.getDouble(0):0;c.close();return x;}
    String salaryText(String month){
        StringBuilder s=new StringBuilder("EDISON SOLAR WORKER SALARY REPORT\nMonth: "+month+"\n\n");
        android.database.Cursor c=r().rawQuery("SELECT id,name,daily_salary,monthly_salary FROM workers ORDER BY name",null);
        while(c.moveToNext()){
            int id=c.getInt(0); double daily=c.getDouble(2), monthly=c.getDouble(3);
            int present=countAtt(id,month,"Present"), half=countAtt(id,month,"Half Day"), absent=countAtt(id,month,"Absent");
            double sal=monthly>0?monthly:(present*daily)+(half*daily*0.5); double adv=sumAdvance(id,month);
            s.append("WORKER: ").append(c.getString(1)).append("\nPresent: ").append(present).append("\nHalf Day: ").append(half).append("\nAbsent: ").append(absent).append("\nDaily Salary: ₹").append(fmt(daily)).append("\nMonthly Salary: ₹").append(fmt(monthly)).append("\nSalary: ₹").append(fmt(sal)).append("\nAdvance: ₹").append(fmt(adv)).append("\nBALANCE: ₹").append(fmt(Math.max(0,sal-adv))).append("\n\n");
        } c.close(); return s.toString();
    }
    void calendar(){
    page("Calendar / Daily Work");
    CalendarView cv=new CalendarView(this);cv.setDate(System.currentTimeMillis(),false,true);
    root.addView(cv,new LinearLayout.LayoutParams(-1,dp(330)));
    TextView selected=txt("Selected Date: "+iso(),21,true);root.addView(selected);
    showCalendarDate(iso(),selected);
    cv.setOnDateChangeListener((view,year,month,day)->showCalendarDate(
        String.format(Locale.US,"%04d-%02d-%02d",year,month+1,day),selected));
}
    void calendarDate(String date){
    page("Calendar / Daily Work");
    CalendarView cv=new CalendarView(this);
    try{SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.US);Date dd=f.parse(date);if(dd!=null)cv.setDate(dd.getTime(),false,true);}catch(Exception ignored){}
    root.addView(cv,new LinearLayout.LayoutParams(-1,dp(330)));
    TextView selected=txt("Selected Date: "+date,21,true);root.addView(selected);
    showCalendarDate(date,selected);
    cv.setOnDateChangeListener((view,year,month,day)->showCalendarDate(
        String.format(Locale.US,"%04d-%02d-%02d",year,month+1,day),selected));
}

    void showCalendarDate(String date,TextView selected){
        selected.setText("Selected Date: "+date);
        while(root.getChildCount()>3)root.removeViewAt(3);
        root.addView(button("➕ Add Attendance",PURPLE,v->attendance(date)));
        android.database.Cursor c=r().rawQuery(
            "SELECT a.status,a.site,w.name,a.note FROM attendance a LEFT JOIN workers w ON w.id=a.worker_id WHERE a.date=? ORDER BY w.name",
            new String[]{date});
        boolean found=false;
        while(c.moveToNext()){found=true;root.addView(card("👷 "+safe(c.getString(2))+"\nStatus: "+safe(c.getString(0))+"\nSite: "+safe(c.getString(1))+(safe(c.getString(3)).isEmpty()?"":"\n"+safe(c.getString(3)))));}
        c.close();if(!found)root.addView(card("No attendance for this date."));
    }

    void reports(){page("Reports / PDF");root.addView(card(dashboardText()));root.addView(button("📄 All Projects PDF",PURPLE,v->createPdf("Projects",allProjectsText())));root.addView(button("📱 Share Project Summary",GREEN,v->share(allProjectsText())));root.addView(button("💰 Salary Report",BLUE,v->salaryReport(iso().substring(0,7))));}
    String allProjectsText(){StringBuilder s=new StringBuilder();for(Project p:projectList())s.append(projectReport(p)).append("\n----------------\n");return s.toString();}

    void createPdf(String name,String text){try{PdfDocument doc=new PdfDocument();Paint paint=new Paint();paint.setColor(Color.BLACK);paint.setTextSize(dp(11));String[] lines=text.split("\n",-1);int pn=1,y=dp(45);PdfDocument.Page page=doc.startPage(new PdfDocument.PageInfo.Builder(dp(595),dp(842),pn).create());Canvas c=page.getCanvas();c.drawText("EDISON SOLAR",dp(35),dp(25),paint);for(String line:lines){if(y>dp(805)){doc.finishPage(page);pn++;page=doc.startPage(new PdfDocument.PageInfo.Builder(dp(595),dp(842),pn).create());c=page.getCanvas();y=dp(45);}c.drawText(line.length()>85?line.substring(0,85):line,dp(35),y,paint);y+=dp(18);}doc.finishPage(page);File dir=new File(getCacheDir(),"reports");dir.mkdirs();File f=new File(dir,name+".pdf");FileOutputStream out=new FileOutputStream(f);doc.writeTo(out);out.close();doc.close();Uri u=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",f);Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Send PDF"));}catch(Exception e){Toast.makeText(this,"PDF error: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    void createPdf(String name,String text,boolean ignored){createPdf(name,text);}
    void pdf(Project p){createPdf("Project_"+p.number,projectReport(p));}
    void share(String text){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"Share / WhatsApp"));}

    void gps(Project p){if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);return;}LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location loc=null;try{if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);if(loc==null&&lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);}catch(Exception ignored){}if(loc==null){Toast.makeText(this,"Location not available",Toast.LENGTH_LONG).show();return;}Uri u=Uri.parse("geo:"+loc.getLatitude()+","+loc.getLongitude()+"?q="+loc.getLatitude()+","+loc.getLongitude());try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://maps.google.com/?q="+loc.getLatitude()+","+loc.getLongitude())));}}
    void photos(Project p){photoProject=p;page("Project Photos");root.addView(card("☀️ "+p.number+"\n"+p.customer));root.addView(button("📷 Take Photo",BLUE,v->takePhoto()));root.addView(button("🖼 Choose Photo",PURPLE,v->choosePhoto()));android.database.Cursor c=r().rawQuery("SELECT uri FROM photos WHERE project_id=? ORDER BY id DESC",new String[]{""+p.id});while(c.moveToNext())root.addView(card("📷 Saved Photo\n"+safe(c.getString(0))));c.close();}
    void takePhoto(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},102);return;}try{File f=new File(getExternalFilesDir(null),"photo_"+System.currentTimeMillis()+".jpg");cameraUri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",f);Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,201);}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}
    void choosePhoto(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,202);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||photoProject==null)return;Uri u=req==201?cameraUri:(data==null?null:data.getData());if(u!=null){ContentValues cv=new ContentValues();cv.put("project_id",photoProject.id);cv.put("uri",u.toString());w().insert("photos",null,cv);photos(photoProject);}}
}
