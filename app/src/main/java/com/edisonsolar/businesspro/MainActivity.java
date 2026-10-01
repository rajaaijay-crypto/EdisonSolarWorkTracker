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
    String dashboardText(){double kw=0,col=0,ex=0;int pc=0;android.database.Cursor c=r().rawQuery("SELECT COALESCE(SUM(kw),0),COUNT(*),COALESCE(SUM(amount),0) FROM projects",null);if(c.moveToFirst()){kw=c.getDouble(0);pc=c.getInt(1);}c.close();c=r().rawQuery("SELECT COALESCE(SUM(amount),0) FROM payments",null);if(c.moveToFirst())col=c.getDouble(0);c.close();c=r().rawQuery("SELECT COALESCE(SUM(amount),0) FROM expenses",null);if(c.moveToFirst())ex=c.getDouble(0);c.close();return "📊 DASHBOARD\nProjects: "+pc+"\nSolar: "+fmt(kw)+" kW\nCollection: ₹"+fmt(col)+"\nExpenses: ₹"+fmt(ex)+"\nNet before site salary: ₹"+fmt(col-ex);}

    void companies(){page("Companies");root.addView(button("+ Add Company",BLUE,v->companyDialog(0)));android.database.Cursor c=r().rawQuery("SELECT id,name,phone FROM companies ORDER BY name",null);while(c.moveToNext()){int id=c.getInt(0);String name=safe(c.getString(1)),phone=safe(c.getString(2));root.addView(card(name+"\nPhone: "+phone));LinearLayout row=new LinearLayout(this);row.addView(button("✏️ Edit",BLUE,v->companyDialog(id)),new LinearLayout.LayoutParams(0,dp(62),1));row.addView(button("🗑 Delete",RED,v->confirm("Delete company?",()->{w().delete("companies","id=?",new String[]{""+id});companies();})),new LinearLayout.LayoutParams(0,dp(62),1));root.addView(row);}c.close();}
    void companyDialog(int id){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);EditText n=field("Company Name","");EditText p=field("Phone","");if(id>0){android.database.Cursor c=r().rawQuery("SELECT name,phone FROM companies WHERE id=?",new String[]{""+id});if(c.moveToFirst()){n.setText(c.getString(0));p.setText(safe(c.getString(1)));}c.close();}l.addView(n);l.addView(p);AlertDialog d=new AlertDialog.Builder(this).setTitle(id==0?"Add Company":"Edit Company").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{if(n.getText().toString().trim().isEmpty()){n.setError("Required");return;}ContentValues cv=new ContentValues();cv.put("name",n.getText().toString().trim());cv.put("phone",p.getText().toString().trim());if(id==0)w().insert("companies",null,cv);else w().update("companies",cv,"id=?",new String[]{""+id});d.dismiss();companies();}));d.show();}

    ArrayList<Project> projectList(){return db.projects();}
    void projects(){page("Projects");root.addView(button("+ Add Project",BLUE,v->projectDialog(0)));for(Project p:projectList()){root.addView(card(projectSummary(p)));root.addView(button("📊 Full Project Report",BLUE,v->projectFullReport(p)));root.addView(button("✏️ Edit / Manage",PURPLE,v->projectMenu(p)));}}
    String projectSummary(Project p){double col=sum("payments",p.id),ex=sum("expenses",p.id),sal=projectSalary(p);return "☀️ PROJECT: "+p.number+"\nCustomer: "+safe(p.customer)+"\nCompany: "+safe(p.company)+"\nSite: "+safe(p.site)+"\n⚡ Solar: "+fmt(p.kw)+" kW\n💰 Collection: ₹"+fmt(col)+"\n💸 Expenses: ₹"+fmt(ex)+"\n👷 Work Days: "+projectDays(p)+"\n👷 Site Salary: ₹"+fmt(sal)+"\n📊 Profit: ₹"+fmt(col-ex-sal)+"\n🔴 Pending: ₹"+fmt(Math.max(0,p.amount-col))+"\n📅 Date: "+safe(p.date);}
    double sum(String table,int pid){android.database.Cursor c=r().rawQuery("SELECT COALESCE(SUM(amount),0) FROM "+table+" WHERE project_id=?",new String[]{""+pid});double x=c.moveToFirst()?c.getDouble(0):0;c.close();return x;}
    int projectDays(Project p){android.database.Cursor c=r().rawQuery("SELECT COUNT(DISTINCT date) FROM attendance WHERE status IN ('Present','Half Day') AND (site=? OR site LIKE ? OR site LIKE ?)",new String[]{p.site,p.number+"%","%"+p.number+"%"});int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
    double projectSalary(Project p){
        double total=0;
        android.database.Cursor c=r().rawQuery(
                "SELECT worker_id,status,COUNT(*) FROM attendance " +
                "WHERE status IN ('Present','Half Day') " +
                "AND (site=? OR site LIKE ? OR site LIKE ?) " +
                "GROUP BY worker_id,status",
                new String[]{p.site,p.number+"%","%"+p.number+"%"});
        while(c.moveToNext()){
            int wid=c.getInt(0);
            String st=safe(c.getString(1));
            int days=c.getInt(2);
            android.database.Cursor q=r().rawQuery(
                    "SELECT daily_salary FROM workers WHERE id=?",
                    new String[]{""+wid});
            if(q.moveToFirst()){
                double daily=q.getDouble(0);
                total += days * daily * ("Half Day".equalsIgnoreCase(st)?0.5:1.0);
            }
            q.close();
        }
        c.close();
        return total;
    }

    void workerHistory(int workerId,String month){
        page("Worker Payment / Advance History");
        android.database.Cursor wc=r().rawQuery("SELECT name,daily_salary,monthly_salary FROM workers WHERE id=?",new String[]{""+workerId});
        if(!wc.moveToFirst()){wc.close();return;}
        String name=safe(wc.getString(0)); double daily=wc.getDouble(1), monthly=wc.getDouble(2); wc.close();
        int present=countAtt(workerId,month,"Present"),half=countAtt(workerId,month,"Half Day"),absent=countAtt(workerId,month,"Absent");
        double salary=monthly>0?monthly:(present*daily)+(half*daily*0.5);
        double advance=sumAdvance(workerId,month);
        root.addView(card("👷 "+name+"\nMonth: "+month+"\nSalary: ₹"+fmt(salary)+"\nAdvance: ₹"+fmt(advance)+"\nPending: ₹"+fmt(salary-advance)));
        root.addView(txt("💰 PAYMENT / WORK HISTORY",20,true));
        android.database.Cursor a=r().rawQuery("SELECT date,status,site,note FROM attendance WHERE worker_id=? AND date LIKE ? ORDER BY date",new String[]{""+workerId,month+"%"});
        boolean any=false;
        while(a.moveToNext()){
            any=true; String st=safe(a.getString(1)); double earned="Present".equalsIgnoreCase(st)?daily:("Half Day".equalsIgnoreCase(st)?daily*0.5:0);
            root.addView(card("📅 "+safe(a.getString(0))+"\nStatus: "+st+"\nSite: "+safe(a.getString(2))+"\nEarned: ₹"+fmt(earned)+(safe(a.getString(3)).isEmpty()?"":"\nNote: "+safe(a.getString(3)))));
        }
        a.close(); if(!any) root.addView(card("No attendance/payment entries for this month."));
        root.addView(txt("💵 ADVANCE HISTORY",20,true));
        android.database.Cursor ad=r().rawQuery("SELECT date,amount,note FROM advances WHERE worker_id=? AND date LIKE ? ORDER BY date",new String[]{""+workerId,month+"%"});
        boolean hasAdvance=false;
        while(ad.moveToNext()){hasAdvance=true; root.addView(card("📅 "+safe(ad.getString(0))+"\nAdvance: ₹"+fmt(ad.getDouble(1))+(safe(ad.getString(2)).isEmpty()?"":"\nNote: "+safe(ad.getString(2)))));}
        ad.close(); if(!hasAdvance) root.addView(card("No advance entries for this month."));
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
            s.append("WORKER: ").append(c.getString(1)).append("\nPresent: ").append(present).append("\nHalf Day: ").append(half).append("\nAbsent: ").append(absent).append("\nDaily Salary: ₹").append(fmt(daily)).append("\nMonthly Salary: ₹").append(fmt(monthly)).append("\nSalary: ₹").append(fmt(sal)).append("\nAdvance: ₹").append(fmt(adv)).append("\nBALANCE: ₹").append(fmt(sal-adv)).append("\n\n");
        } c.close(); return s.toString();
    }

    void calendar(){page("Calendar / Daily Work");EditText d=field("Date YYYY-MM-DD",iso());root.addView(d);root.addView(button("🔎 View Date",BLUE,v->calendarDate(vText(d))));calendarDate(iso());}
    void calendarDate(String date){root.addView(txt("DATE: "+date,21,true));android.database.Cursor c=r().rawQuery("SELECT a.status,a.site,w.name FROM attendance a LEFT JOIN workers w ON w.id=a.worker_id WHERE a.date=? ORDER BY w.name",new String[]{date});boolean f=false;while(c.moveToNext()){f=true;root.addView(card("👷 "+safe(c.getString(2))+"\nStatus: "+safe(c.getString(0))+"\nSite: "+safe(c.getString(1))));}c.close();if(!f)root.addView(card("No attendance for this date."));}
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
