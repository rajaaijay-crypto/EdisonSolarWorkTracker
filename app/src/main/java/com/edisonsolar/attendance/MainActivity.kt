package com.edisonsolar.attendance

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class Worker(
    val id: Int,
    var name: String,
    var phone: String,
    var salary: Double,
    var paymentType: String,
    var role: String
)

data class AttendanceRecord(
    var status: String = "Present",
    var intime: String = "",
    var outtime: String = "",
    var site: String = "",
    var note: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null
)

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "edison.db", null, 6) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE workers(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT,
                salary REAL DEFAULT 0,
                payment_type TEXT DEFAULT 'Daily',
                role TEXT DEFAULT ''
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE attendance(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                status TEXT DEFAULT 'Present',
                intime TEXT,
                outtime TEXT,
                site TEXT,
                note TEXT,
                latitude REAL,
                longitude REAL,
                UNIQUE(worker_id,date)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE advances(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER NOT NULL,
                amount REAL DEFAULT 0,
                date TEXT,
                note TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 5) {
            try {
                db.execSQL(
                    "ALTER TABLE workers ADD COLUMN payment_type TEXT DEFAULT 'Daily'"
                )
            } catch (_: Exception) {
            }

            try {
                db.execSQL(
                    "ALTER TABLE workers ADD COLUMN role TEXT DEFAULT ''"
                )
            } catch (_: Exception) {
            }
        }

        if (oldVersion < 6) {
            try {
                db.execSQL(
                    "ALTER TABLE attendance ADD COLUMN latitude REAL"
                )
            } catch (_: Exception) {
            }

            try {
                db.execSQL(
                    "ALTER TABLE attendance ADD COLUMN longitude REAL"
                )
            } catch (_: Exception) {
            }
        }
    }

    fun addWorker(
        name: String,
        phone: String,
        salary: Double,
        paymentType: String,
        role: String
    ) {
        writableDatabase.execSQL(
            """
            INSERT INTO workers
            (name,phone,salary,payment_type,role)
            VALUES(?,?,?,?,?)
            """.trimIndent(),
            arrayOf(name, phone, salary, paymentType, role)
        )
    }

    fun updateWorker(
        id: Int,
        name: String,
        phone: String,
        salary: Double,
        paymentType: String,
        role: String
    ) {
        writableDatabase.execSQL(
            """
            UPDATE workers SET
            name=?,
            phone=?,
            salary=?,
            payment_type=?,
            role=?
            WHERE id=?
            """.trimIndent(),
            arrayOf(
                name,
                phone,
                salary,
                paymentType,
                role,
                id
            )
        )
    }

    fun deleteWorker(id: Int) {
        writableDatabase.delete(
            "workers",
            "id=?",
            arrayOf(id.toString())
        )

        writableDatabase.delete(
            "attendance",
            "worker_id=?",
            arrayOf(id.toString())
        )

        writableDatabase.delete(
            "advances",
            "worker_id=?",
            arrayOf(id.toString())
        )
    }

    fun getWorkers(): MutableList<Worker> {

        val list = mutableListOf<Worker>()

        val c = readableDatabase.rawQuery(
            """
            SELECT id,name,phone,salary,
                   COALESCE(payment_type,'Daily'),
                   COALESCE(role,'')
            FROM workers
            ORDER BY name
            """.trimIndent(),
            null
        )

        while (c.moveToNext()) {

            list.add(
                Worker(
                    c.getInt(0),
                    c.getString(1) ?: "",
                    c.getString(2) ?: "",
                    c.getDouble(3),
                    c.getString(4) ?: "Daily",
                    c.getString(5) ?: ""
                )
            )
        }

        c.close()

        return list
    }

    fun saveAttendance(
        workerId: Int,
        date: String,
        record: AttendanceRecord
    ) {

        writableDatabase.execSQL(
            """
            INSERT INTO attendance
            (worker_id,date,status,intime,outtime,site,note,latitude,longitude)
            VALUES(?,?,?,?,?,?,?,?,?)
            ON CONFLICT(worker_id,date)
            DO UPDATE SET
            status=excluded.status,
            intime=excluded.intime,
            outtime=excluded.outtime,
            site=excluded.site,
            note=excluded.note,
            latitude=excluded.latitude,
            longitude=excluded.longitude
            """.trimIndent(),
            arrayOf(
                workerId,
                date,
                record.status,
                record.intime,
                record.outtime,
                record.site,
                record.note,
                record.latitude,
                record.longitude
            )
        )
    }

    fun getAttendance(
        workerId: Int,
        date: String
    ): AttendanceRecord? {

        val c = readableDatabase.rawQuery(
            """
            SELECT status,intime,outtime,site,note,latitude,longitude
            FROM attendance
            WHERE worker_id=? AND date=?
            LIMIT 1
            """.trimIndent(),
            arrayOf(
                workerId.toString(),
                date
            )
        )

        if (!c.moveToFirst()) {
            c.close()
            return null
        }

        val record = AttendanceRecord(
            status = c.getString(0) ?: "Present",
            intime = c.getString(1) ?: "",
            outtime = c.getString(2) ?: "",
            site = c.getString(3) ?: "",
            note = c.getString(4) ?: "",
            latitude =
                if (c.isNull(5)) null else c.getDouble(5),
            longitude =
                if (c.isNull(6)) null else c.getDouble(6)
        )

        c.close()

        return record
    }

    fun getAttendanceForDate(
        date: String
    ): HashMap<Int, AttendanceRecord> {

        val map = HashMap<Int, AttendanceRecord>()

        val c = readableDatabase.rawQuery(
            """
            SELECT worker_id,status,intime,outtime,
                   site,note,latitude,longitude
            FROM attendance
            WHERE date=?
            """.trimIndent(),
            arrayOf(date)
        )

        while (c.moveToNext()) {

            map[c.getInt(0)] = AttendanceRecord(
                status = c.getString(1) ?: "Present",
                intime = c.getString(2) ?: "",
                outtime = c.getString(3) ?: "",
                site = c.getString(4) ?: "",
                note = c.getString(5) ?: "",
                latitude =
                    if (c.isNull(6)) null else c.getDouble(6),
                longitude =
                    if (c.isNull(7)) null else c.getDouble(7)
            )
        }

        c.close()

        return map
    }

    fun getAdvanceTotal(workerId: Int): Double {

        val c = readableDatabase.rawQuery(
            """
            SELECT COALESCE(SUM(amount),0)
            FROM advances
            WHERE worker_id=?
            """.trimIndent(),
            arrayOf(workerId.toString())
        )

        val value =
            if (c.moveToFirst()) c.getDouble(0)
            else 0.0

        c.close()

        return value
    }

    fun addAdvance(
        workerId: Int,
        amount: Double,
        date: String,
        note: String
    ) {

        writableDatabase.execSQL(
            """
            INSERT INTO advances
            (worker_id,amount,date,note)
            VALUES(?,?,?,?)
            """.trimIndent(),
            arrayOf(
                workerId,
                amount,
                date,
                note
            )
        )
    }

    fun updateAdvance(
        id: Int,
        amount: Double,
        date: String,
        note: String
    ) {

        writableDatabase.execSQL(
            """
            UPDATE advances SET
            amount=?,
            date=?,
            note=?
            WHERE id=?
            """.trimIndent(),
            arrayOf(
                amount,
                date,
                note,
                id
            )
        )
    }

    fun deleteAdvance(id: Int) {

        writableDatabase.delete(
            "advances",
            "id=?",
            arrayOf(id.toString())
        )
    }

    fun getAdvances(workerId: Int): ArrayList<String> {

        val list = ArrayList<String>()

        val c = readableDatabase.rawQuery(
            """
            SELECT id,amount,date,note
            FROM advances
            WHERE worker_id=?
            ORDER BY date DESC,id DESC
            """.trimIndent(),
            arrayOf(workerId.toString())
        )

        while (c.moveToNext()) {

            val id = c.getInt(0)
            val amount = c.getDouble(1)
            val date = c.getString(2) ?: ""
            val note = c.getString(3) ?: ""

            list.add(
                "$id|₹${amount.roundToInt()}|$date|$note"
            )
        }

        c.close()

        return list
    }

    fun getAttendanceMonth(
        workerId: Int,
        yearMonth: String
    ): Triple<Int, Int, Int> {

        var present = 0
        var half = 0
        var absent = 0

        val c = readableDatabase.rawQuery(
            """
            SELECT status
            FROM attendance
            WHERE worker_id=?
            AND date LIKE ?
            """.trimIndent(),
            arrayOf(
                workerId.toString(),
                "$yearMonth%"
            )
        )

        while (c.moveToNext()) {

            when (c.getString(0)) {

                "Present" -> present++

                "Half Day" -> half++

                "Absent" -> absent++
            }
        }

        c.close()

        return Triple(
            present,
            half,
            absent
        )
    }
}

class MainActivity : Activity() {

    private lateinit var db: DBHelper

    private lateinit var root: LinearLayout
    private lateinit var title: TextView

    private var selectedDate: String =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())

    private val blue = Color.rgb(25, 118, 210)
    private val green = Color.rgb(46, 125, 50)
    private val red = Color.rgb(198, 40, 40)
    private val orange = Color.rgb(239, 108, 0)
    private val gray = Color.rgb(245, 247, 250)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        db = DBHelper(this)

        showHome()
    }

    private fun baseLayout(
        screenTitle: String
    ) {

        root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.WHITE)

        title = TextView(this)

        title.text = screenTitle
        title.textSize = 20f
        title.setTextColor(Color.WHITE)
        title.setPadding(
            16,
            18,
            16,
            18
        )

        title.setBackgroundColor(blue)

        root.addView(
            title,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        setContentView(root)
    }

    private fun button(
        text: String,
        click: () -> Unit
    ): Button {

        val b = Button(this)

        b.text = text
        b.textSize = 14f
        b.setAllCaps(false)

        b.setOnClickListener {
            click()
        }

        return b
    }

    private fun text(
        value: String,
        size: Float = 16f
    ): TextView {

        val t = TextView(this)

        t.text = value
        t.textSize = size
        t.setTextColor(Color.DKGRAY)
        t.setPadding(
            12,
            10,
            12,
            10
        )

        return t
    }

    private fun showHome() {

        baseLayout("EDISON")

        val scroll = ScrollView(this)

        val box = LinearLayout(this)

        box.orientation = LinearLayout.VERTICAL
        box.setPadding(12, 12, 12, 12)

        box.addView(
            text(
                "EDISON SOLAR WORK TRACKER",
                22f
            )
        )

        box.addView(
            text(
                "Worker • Attendance • Salary • Site"
            )
        )

        box.addView(
            button("👷 WORKERS") {
                showWorkers()
            }
        )

        box.addView(
            button("📅 ATTENDANCE") {
                showAttendance()
            }
        )

        box.addView(
            button("📍 SITE SUMMARY") {
                showSiteSummary()
            }
        )

        box.addView(
            button("💰 SALARY") {
                showSalary()
            }
        )

        box.addView(
            button("💵 ADVANCE") {
                showAdvanceSelect()
            }
        )

        box.addView(
            button("📊 ALL WORKERS REPORT") {
                showAllWorkersReport()
            }
        )

        scroll.addView(box)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            bottomNav()
        )
    }

    private fun bottomNav(): LinearLayout {

        val nav = LinearLayout(this)

        nav.orientation = LinearLayout.HORIZONTAL
        nav.setPadding(4, 4, 4, 4)
        nav.setBackgroundColor(Color.rgb(238, 244, 250))

        val home = button("HOME") {
            showHome()
        }

        val workers = button("WORKERS") {
            show
