package com.edisonsolar.attendance

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Worker(
    val id: Long,
    val name: String,
    val phone: String,
    val salary: Double,
    val paymentType: String,
    val role: String
)

data class AttendanceRecord(
    val status: String,
    val intime: String,
    val outtime: String,
    val site: String,
    val note: String,
    val latitude: Double,
    val longitude: Double
)

data class Advance(
    val id: Long,
    val workerId: Long,
    val amount: Double,
    val date: String,
    val note: String
)

class DBHelper(context: Context) :
    android.database.sqlite.SQLiteOpenHelper(
        context,
        "edison.db",
        null,
        6
    ) {

    override fun onCreate(
        db: android.database.sqlite.SQLiteDatabase
    ) {
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
                status TEXT DEFAULT 'Absent',
                intime TEXT DEFAULT '',
                outtime TEXT DEFAULT '',
                site TEXT DEFAULT '',
                note TEXT DEFAULT '',
                latitude REAL DEFAULT 0,
                longitude REAL DEFAULT 0,
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
                note TEXT DEFAULT ''
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: android.database.sqlite.SQLiteDatabase,
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
                    "ALTER TABLE attendance ADD COLUMN latitude REAL DEFAULT 0"
                )
            } catch (_: Exception) {
            }

            try {
                db.execSQL(
                    "ALTER TABLE attendance ADD COLUMN longitude REAL DEFAULT 0"
                )
            } catch (_: Exception) {
            }
        }
    }

    fun getWorkers(): MutableList<Worker> {
        val result = mutableListOf<Worker>()

        val cursor = readableDatabase.rawQuery(
            """
            SELECT id,name,phone,salary,
                   COALESCE(payment_type,'Daily'),
                   COALESCE(role,'')
            FROM workers
            ORDER BY name
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            result.add(
                Worker(
                    cursor.getLong(0),
                    cursor.getString(1) ?: "",
                    cursor.getString(2) ?: "",
                    cursor.getDouble(3),
                    cursor.getString(4) ?: "Daily",
                    cursor.getString(5) ?: ""
                )
            )
        }

        cursor.close()
        return result
    }

    fun addWorker(
        name: String,
        phone: String,
        salary: Double,
        paymentType: String,
        role: String
    ) {
        val values = ContentValues()

        values.put("name", name)
        values.put("phone", phone)
        values.put("salary", salary)
        values.put("payment_type", paymentType)
        values.put("role", role)

        writableDatabase.insert(
            "workers",
            null,
            values
        )
    }

    fun updateWorker(
        id: Long,
        name: String,
        phone: String,
        salary: Double,
        paymentType: String,
        role: String
    ) {
        val values = ContentValues()

        values.put("name", name)
        values.put("phone", phone)
        values.put("salary", salary)
        values.put("payment_type", paymentType)
        values.put("role", role)

        writableDatabase.update(
            "workers",
            values,
            "id=?",
            arrayOf(id.toString())
        )
    }

    fun deleteWorker(id: Long) {
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

        writableDatabase.delete(
            "workers",
            "id=?",
            arrayOf(id.toString())
        )
    }

    fun getAttendance(
        workerId: Long,
        date: String
    ): AttendanceRecord? {

        val cursor = readableDatabase.rawQuery(
            """
            SELECT status,intime,outtime,site,note,
                   latitude,longitude
            FROM attendance
            WHERE worker_id=? AND date=?
            LIMIT 1
            """.trimIndent(),
            arrayOf(
                workerId.toString(),
                date
            )
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return null
        }

        val result = AttendanceRecord(
            cursor.getString(0) ?: "Absent",
            cursor.getString(1) ?: "",
            cursor.getString(2) ?: "",
            cursor.getString(3) ?: "",
            cursor.getString(4) ?: "",
            cursor.getDouble(5),
            cursor.getDouble(6)
        )

        cursor.close()
        return result
    }

    fun getAttendanceForDate(
        date: String
    ): HashMap<Long, AttendanceRecord> {

        val result =
            HashMap<Long, AttendanceRecord>()

        val cursor = readableDatabase.rawQuery(
            """
            SELECT worker_id,status,intime,outtime,
                   site,note,latitude,longitude
            FROM attendance
            WHERE date=?
            """.trimIndent(),
            arrayOf(date)
        )

        while (cursor.moveToNext()) {

            result[cursor.getLong(0)] =
                AttendanceRecord(
                    cursor.getString(1) ?: "Absent",
                    cursor.getString(2) ?: "",
                    cursor.getString(3) ?: "",
                    cursor.getString(4) ?: "",
                    cursor.getString(5) ?: "",
                    cursor.getDouble(6),
                    cursor.getDouble(7)
                )
        }

        cursor.close()
        return result
    }

    fun saveAttendance(
        workerId: Long,
        date: String,
        record: AttendanceRecord
    ) {
        val values = ContentValues()

        values.put("worker_id", workerId)
        values.put("date", date)
        values.put("status", record.status)
        values.put("intime", record.intime)
        values.put("outtime", record.outtime)
        values.put("site", record.site)
        values.put("note", record.note)
        values.put("latitude", record.latitude)
        values.put("longitude", record.longitude)

        writableDatabase.insertWithOnConflict(
            "attendance",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getAdvances(
        workerId: Long
    ): MutableList<Advance> {

        val result =
            mutableListOf<Advance>()

        val cursor = readableDatabase.rawQuery(
            """
            SELECT id,worker_id,amount,date,note
            FROM advances
            WHERE worker_id=?
            ORDER BY date DESC,id DESC
            """.trimIndent(),
            arrayOf(workerId.toString())
        )

        while (cursor.moveToNext()) {
            result.add(
                Advance(
                    cursor.getLong(0),
                    cursor.getLong(1),
                    cursor.getDouble(2),
                    cursor.getString(3) ?: "",
                    cursor.getString(4) ?: ""
                )
            )
        }

        cursor.close()
        return result
    }

    fun addAdvance(
        workerId: Long,
        amount: Double,
        date: String,
        note: String
    ) {
        val values = ContentValues()

        values.put("worker_id", workerId)
        values.put("amount", amount)
        values.put("date", date)
        values.put("note", note)

        writableDatabase.insert(
            "advances",
            null,
            values
        )
    }

    fun updateAdvance(
        id: Long,
        workerId: Long,
        amount: Double,
        date: String,
        note: String
    ) {
        val values = ContentValues()

        values.put("amount", amount)
        values.put("date", date)
        values.put("note", note)

        writableDatabase.update(
            "advances",
            values,
            "id=? AND worker_id=?",
            arrayOf(
                id.toString(),
                workerId.toString()
            )
        )
    }

    fun deleteAdvance(
        id: Long,
        workerId: Long
    ) {
        writableDatabase.delete(
            "advances",
            "id=? AND worker_id=?",
            arrayOf(
                id.toString(),
                workerId.toString()
            )
        )
    }

    fun getAdvanceTotal(
        workerId: Long
    ): Double {

        val cursor = readableDatabase.rawQuery(
            """
            SELECT COALESCE(SUM(amount),0)
            FROM advances
            WHERE worker_id=?
            """.trimIndent(),
            arrayOf(workerId.toString())
        )

        var total = 0.0

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }

        cursor.close()
        return total
    }

    fun getMonthCounts(
        workerId: Long,
        year: Int,
        month: Int
    ): Pair<Int, Int> {

        val prefix = String.format(
            Locale.getDefault(),
            "%04d-%02d-",
            year,
            month + 1
        )

        var present = 0
        var half = 0

        val cursor = readableDatabase.rawQuery(
            """
            SELECT status
            FROM attendance
            WHERE worker_id=?
            AND date LIKE ?
            """.trimIndent(),
            arrayOf(
                workerId.toString(),
                "$prefix%"
            )
        )

        while (cursor.moveToNext()) {

            when (cursor.getString(0)) {

                "Present" -> {
                    present++
                }

                "Half Day" -> {
                    half++
                }
            }
        }

        cursor.close()

        return Pair(
            present,
            half
        )
    }
}

class MainActivity : Activity() {

    private lateinit var db: DBHelper
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout

    private val blue =
        Color.rgb(25, 118, 210)

    private val light =
        Color.rgb(245, 248, 252)

    private var selectedDate =
        todayDate()

    companion object {
        const val LOCATION_REQUEST = 101
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        db = DBHelper(this)

        home()
    }

    private fun todayDate(): String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(java.util.Date())
    }

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    private fun money(
        value: Double
    ): String {

        return String.format(
            Locale.getDefault(),
            "%.0f",
            value
        )
    }

    private fun createLayout(
        screen: String
    ): LinearLayout {

        root = LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.setBackgroundColor(
            Color.WHITE
        )

        val header =
            TextView(this)

        header.text = screen
        header.textSize = 20f
        header.setTextColor(
            Color.WHITE
        )

        header.setPadding(
            dp(16),
            dp(14),
            dp(16),
            dp(14)
        )

        header.setBackgroundColor(
            blue
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.setPadding(
            dp(10),
            dp(10),
            dp(10),
            dp(10)
        )

        val scroll =
            ScrollView(this)

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            bottomBar()
        )

        setContentView(root)

        return content
    }

    private fun addTitle(
        value: String
    ) {

        val t =
            TextView(this)

        t.text = value
        t.textSize = 18f
        t.setTextColor(
            Color.DKGRAY
        )

        t.setPadding(
            dp(5),
            dp(5),
            dp(5),
            dp(10)
        )

        content.addView(t)
    }

    private fun addText(
        value: String,
        size: Float = 15f
    ) {

        val t =
            TextView(this)

        t.text = value
        t.textSize = size
        t.setTextColor(
            Color.DKGRAY
        )

        t.setPadding(
            dp(8),
            dp(7),
            dp(8),
            dp(7)
        )

        content.addView(t)
    }

    private fun addTextTo(
        parent: LinearLayout,
        value: String,
        size: Float = 15f
    ) {

        val t =
            TextView(this)

        t.text = value
        t.textSize = size
        t.setTextColor(
            Color.DKGRAY
        )

        t.setPadding(
            dp(6),
            dp(5),
            dp(6),
            dp(5)
        )

        parent.addView(t)
    }

    private fun makeButton(
        value: String,
        action: () -> Unit
    ): Button {

        val b =
            Button(this)

        b.text = value
        b.textSize = 13f
        b.setAllCaps(false)

        b.setOnClickListener {
            action()
        }

        return b
    }

    private fun bottomBar(): LinearLayout {

        val bar =
            LinearLayout(this)

        bar.orientation =
            LinearLayout.HORIZONTAL

        bar.setBackgroundColor(
            Color.rgb(235, 242, 250)
        )

        val home =
            makeButton("HOME") {
                home()
            }

        val workers =
            makeButton("WORKERS") {
                workers()
            }

        val attend =
            makeButton("ATTEND") {
                attendance()
            }

        val salary =
            makeButton("SALARY") {
                salary()
            }

        bar.addView(
            home,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        bar.addView(
            workers,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        bar.addView(
            attend,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        bar.addView(
            salary,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        return bar
    }

    private fun home() {

        createLayout("EDISON")

        addTitle(
            "☀ EDISON SOLAR WORK TRACKER"
        )

        addText(
            "Workers • Attendance • Site • Salary"
        )

        content.addView(
            makeButton("👷 WORKERS") {
                workers()
            }
        )

        content.addView(
            makeButton("📅 ATTENDANCE") {
                attendance()
            }
        )

        content.addView(
            makeButton("📍 SITE SUMMARY") {
                siteSummary()
            }
        )

        content.addView(
            makeButton("💰 SALARY / PENDING") {
                salary()
            }
        )

        content.addView(
            makeButton("💵 ADVANCE") {
                advanceSelect()
            }
        )

        content.addView(
            makeButton("📊 ALL WORKERS REPORT") {
                allWorkersReport()
            }
        )
    }

    private fun workers() {

        createLayout("👷 WORKERS")

        addTitle("WORKERS")

        content.addView(
            makeButton("➕ ADD WORKER") {
                workerDialog(null)
            }
        )

        val list =
            db.getWorkers()

        if (list.isEmpty()) {

            addText(
                "No workers added."
            )

            return
        }

        for (worker in list) {

            val card =
                LinearLayout(this)
