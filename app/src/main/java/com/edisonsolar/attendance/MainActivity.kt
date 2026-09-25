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
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    val status: String = "Absent",
    val intime: String = "",
    val outtime: String = "",
    val site: String = "",
    val note: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
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

    fun workers(): MutableList<Worker> {
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
                    c.getLong(0),
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

    fun addWorker(
        name: String,
        phone: String,
        salary: Double,
        type: String,
        role: String
    ) {
        val v = ContentValues()

        v.put("name", name)
        v.put("phone", phone)
        v.put("salary", salary)
        v.put("payment_type", type)
        v.put("role", role)

        writableDatabase.insert(
            "workers",
            null,
            v
        )
    }

    fun updateWorker(
        id: Long,
        name: String,
        phone: String,
        salary: Double,
        type: String,
        role: String
    ) {
        val v = ContentValues()

        v.put("name", name)
        v.put("phone", phone)
        v.put("salary", salary)
        v.put("payment_type", type)
        v.put("role", role)

        writableDatabase.update(
            "workers",
            v,
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

        val c = readableDatabase.rawQuery(
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

        if (!c.moveToFirst()) {
            c.close()
            return null
        }

        val result = AttendanceRecord(
            c.getString(0) ?: "Absent",
            c.getString(1) ?: "",
            c.getString(2) ?: "",
            c.getString(3) ?: "",
            c.getString(4) ?: "",
            c.getDouble(5),
            c.getDouble(6)
        )

        c.close()

        return result
    }

    fun attendanceForDate(
        date: String
    ): HashMap<Long, AttendanceRecord> {

        val map =
            HashMap<Long, AttendanceRecord>()

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

            map[c.getLong(0)] =
                AttendanceRecord(
                    c.getString(1) ?: "Absent",
                    c.getString(2) ?: "",
                    c.getString(3) ?: "",
                    c.getString(4) ?: "",
                    c.getString(5) ?: "",
                    c.getDouble(6),
                    c.getDouble(7)
                )
        }

        c.close()

        return map
    }

    fun saveAttendance(
        workerId: Long,
        date: String,
        record: AttendanceRecord
    ) {
        val v = ContentValues()

        v.put("worker_id", workerId)
        v.put("date", date)
        v.put("status", record.status)
        v.put("intime", record.intime)
        v.put("outtime", record.outtime)
        v.put("site", record.site)
        v.put("note", record.note)
        v.put("latitude", record.latitude)
        v.put("longitude", record.longitude)

        writableDatabase.insertWithOnConflict(
            "attendance",
            null,
            v,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun advances(
        workerId: Long
    ): MutableList<Advance> {

        val list =
            mutableListOf<Advance>()

        val c = readableDatabase.rawQuery(
            """
            SELECT id,worker_id,amount,date,note
            FROM advances
            WHERE worker_id=?
            ORDER BY date DESC,id DESC
            """.trimIndent(),
            arrayOf(workerId.toString())
        )

        while (c.moveToNext()) {
            list.add(
                Advance(
                    c.getLong(0),
                    c.getLong(1),
                    c.getDouble(2),
                    c.getString(3) ?: "",
                    c.getString(4) ?: ""
                )
            )
        }

        c.close()

        return list
    }

    fun advanceTotal(
        workerId: Long
    ): Double {

        val c = readableDatabase.rawQuery(
            """
            SELECT COALESCE(SUM(amount),0)
            FROM advances
            WHERE worker_id=?
            """.trimIndent(),
            arrayOf(workerId.toString())
        )

        val total =
            if (c.moveToFirst()) {
                c.getDouble(0)
            } else {
                0.0
            }

        c.close()

        return total
    }

    fun addAdvance(
        workerId: Long,
        amount: Double,
        date: String,
        note: String
    ) {
        val v = ContentValues()

        v.put("worker_id", workerId)
        v.put("amount", amount)
        v.put("date", date)
        v.put("note", note)

        writableDatabase.insert(
            "advances",
            null,
            v
        )
    }

    fun updateAdvance(
        id: Long,
        workerId: Long,
        amount: Double,
        date: String,
        note: String
    ) {
        val v = ContentValues()

        v.put("amount", amount)
        v.put("date", date)
        v.put("note", note)

        writableDatabase.update(
            "advances",
            v,
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

    fun monthCounts(
        workerId: Long,
        year: Int,
        month: Int
    ): Pair<Int, Int> {

        val prefix =
            String.format(
                Locale.getDefault(),
                "%04d-%02d-",
                year,
                month + 1
            )

        var present = 0
        var half = 0

        val c = readableDatabase.rawQuery(
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

        while (c.moveToNext()) {

            when (c.getString(0)) {

                "Present" -> {
                    present++
                }

                "Half Day" -> {
                    half++
                }
            }
        }

        c.close()

        return Pair(
            present,
            half
        )
    }
}

class MainActivity : Activity() {

    private lateinit var db: DBHelper
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout

    private var selectedDate =
        todayDate()

    companion object {
        const val GPS_REQUEST = 501
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        db = DBHelper(this)

        home()
    }

    private fun dp(
        value: Int
    ): Int {
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

    private fun makeScreen(
        title: String
    ) {

        root =
            LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.setBackgroundColor(
            Color.WHITE
        )

        val header =
            TextView(this)

        header.text =
            title

        header.textSize =
            20f

        header.setTextColor(
            Color.WHITE
        )

        header.gravity =
            Gravity.CENTER_VERTICAL

        header.setPadding(
            dp(14),
            dp(12),
            dp(14),
            dp(12)
        )

        header.setBackgroundColor(
            Color.rgb(
                25,
                118,
                210
            )
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        val scroll =
            ScrollView(this)

        body =
            LinearLayout(this)

        body.orientation =
            LinearLayout.VERTICAL

        body.setPadding(
            dp(10),
            dp(10),
            dp(10),
            dp(10)
        )

        scroll.addView(body)

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
    }

    private fun label(
        value: String,
        size: Float = 15f
    ): TextView {

        val t =
            TextView(this)

        t.text =
            value

        t.textSize =
            size

        t.setTextColor(
            Color.DKGRAY
        )

        t.setPadding(
            dp(6),
            dp(5),
            dp(6),
            dp(5)
        )

        return t
    }

    private fun addLabel(
        value: String,
        size: Float = 15f
    ) {
        body.addView(
            label(
                value,
                size
            )
        )
    }

    private fun makeButton(
        value: String,
        action: () -> Unit
    ): Button {

        val b =
            Button(this)

        b.text =
            value

        b.textSize =
            13f

        b.setAllCaps(
            false
        )

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
            Color.rgb(
                235,
                242,
                250
            )
        )

        val a =
            makeButton("HOME") {
                home()
            }

        val b =
            makeButton("WORKERS") {
                workers()
            }

        val c =
            makeButton("ATTEND") {
                attendance()
            }

        val d =
            makeButton("SALARY") {
                salary()
            }

        bar.addView(
            a,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        bar.addView(
            b,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        bar.addView(
            c,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        bar.addView(
            d,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        return bar
    }

    private fun home() {

        makeScreen(
            "EDISON"
        )

        addLabel(
            "☀ EDISON SOLAR WORK TRACKER",
            21f
        )

        addLabel(
            "Workers • Attendance • Site • Salary"
        )

        body.addView(
            makeButton(
                "👷 WORKERS"
            ) {
                workers()
            }
        )

        body.addView(
            makeButton(
                "📅 ATTENDANCE"
            ) {
                attendance()
            }
        )

        body.addView(
            makeButton(
                "📍 SITE SUMMARY"
            ) {
                siteSummary()
            }
        )

        body.addView(
            makeButton(
                "💰 SALARY / PENDING"
            ) {
                salary()
            }
        )

        body.addView(
            makeButton(
                "💵 ADVANCE"
            ) {
                advanceSelect()
            }
        )

        body.addView(
            makeButton(
                "📊 ALL WORKERS REPORT"
            ) {
                allWorkersReport()
            }
        )
    }

    private fun workers() {

        makeScreen(
            "👷 WORKERS"
        )

        addLabel(
            "WORKERS",
            19f
        )

        body.addView(
            makeButton(
                "➕ ADD WORKER"
            ) {
                workerDialog(null)
            }
        )

        val list =
            db.workers()

        if (list.isEmpty()) {

            addLabel(
                "No workers added."
            )

            return
        }

        for (w in list) {

            val card =
                LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
            )

            card.setBackgroundColor(
                Color.rgb(
                    245,
                    248,
                    252
                )
            )

            card.addView(
                label(
                    "👷 ${w.name}",
                    18f
                )
            )

            card.addView(
                label(
                    "📱 ${w.phone}"
                )
            )

            card.addView(
                label(
                    "💼 ${w.role}"
                )
            )

            card.addView(
                label(
                    "💰 ${w.paymentType} : ₹${money(w.salary)}"
                )
            )

            card.addView(
                makeButton(
                    "✏ EDIT"
                ) {
                    workerDialog(w)
                }
            )

            card.addView(
                makeButton(
                    "📅 ATTEND"
                ) {
                    selectedDate =
                        todayDate()

                    workerAttendance(w)
                }
            )

            card.addView(
                makeButton(
                    "💵 ADVANCE"
                ) {
                    advanceHistory(w)
                }
            )

            card.addView(
                makeButton(
                    "🗑 DELETE"
                ) {

                    AlertDialog.Builder(this)
                        .setTitle(
                            "Delete Worker?"
                        )
                        .setMessage(
                            w.name
                        )
                        .setNegativeButton(
                            "NO",
                            null
                        )
                        .setPositiveButton(
                            "YES"
                        ) { _, _ ->

                            db.deleteWorker(
                                w.id
                            )

                            workers()
                        }
                        .show()
                }
            )

            body.addView(card)

            body.addView(
                Space(this),
                LinearLayout.LayoutParams(
                    1,
                    dp(8)
                )
            )
        }
    }

    private fun workerDialog(
        old: Worker?
    ) {

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        box.setPadding(
            dp(12),
            0,
            dp(12),
            0
        )

        val name =
            EditText(this)

        name.hint =
            "Worker Name"

        name.setText(
            old?.name ?: ""
        )

        val phone =
            EditText(this)

        phone.hint =
            "Phone"

        phone.setText(
            old?.phone ?: ""
        )

        val salary =
            EditText(this)

        salary.hint =
            "Salary Amount"

        salary.inputType =
            2

        salary.setText(
            if (old == null) {
                ""
            } else {
                old.salary.toString()
            }
        )

        val type =
            Spinner(this)

        type.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf(
                    "Daily",
                    "Monthly"
                )
            )

        if (
            old?.paymentType ==
            "Monthly"
        ) {
            type.setSelection(1)
        }

        val role =
            EditText(this)

        role.hint =
            "Work Role"

        role.setText(
            old?.role ?: ""
        )

        box.addView(name)
        box.addView(phone)
        box.addView(salary)
        box.addView(type)
        box.addView(role)

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    if (old == null)
                        "ADD WORKER"
                    else
                        "EDIT WORKER"
                )
                .setView(box)
                .setNegativeButton(
                    "CANCEL",
                    null
                )
                .setPositiveButton(
                    "SAVE",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val n =
                    name.text
                        .toString()
                        .trim()

                val s =
                    salary.text
                        .toString()
                        .toDoubleOrNull()
                        ?: 0.0

                val p =
                    type.selectedItem
                        .toString()

                val r =
                    role.text
                        .toString()
                        .trim()

                if (n.isEmpty()) {

                    name.error =
                        "Enter name"

                } else {

                    if (old == null) {

                        db.addWorker(
                            n,
                            phone.text.toString(),
                            s,
                            p,
                            r
                        )

                    } else {

                        db.updateWorker(
                            old.id,
                            n,
                            phone.text.toString(),
                            s,
                            p,
                            r
                        )
                    }

                    dialog.dismiss()

                    workers()
                }
            }
        }

        dialog.show()
    }

    private fun attendance() {

        makeScreen(
            "📅 ATTENDANCE"
        )

        val row =
            LinearLayout(this)

        row.orientation =
            LinearLayout.HORIZONTAL

        val dateText =
            label(
                "📅 $selectedDate",
                17f
            )

        row.addView(
            dateText,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        row.addView(
            makeButton(
                "CHANGE"
            ) {
                pickDate()
            }
        )

        body.addView(row)

        val records =
            db.attendanceForDate(
                selectedDate
            )

        for (w in db.workers()) {

            val saved =
                records[w.id]
                    ?: AttendanceRecord()

            attendanceCard(
                w,
                saved
            )
        }
    }

    private fun attendanceCard(
        w: Worker,
        saved: AttendanceRecord
    ) {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            dp(10),
            dp(8),
            dp(10),
            dp(8)
        )

        card.setBackgroundColor(
            Color.rgb(
                245,
                248,
                252
            )
        )

        card.addView(
            label(
                "👷 ${w.name}",
                17f
            )
        )

        val status =
            Spinner(this)

        status.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf(
                    "Present",
                    "Half Day",
                    "Absent"
                )
            )

        status.setSelection(
            when (saved.status) {

                "Half Day" -> 1

                "Absent" -> 2

                else -> 0
            }
        )

        val site =
            EditText(this)

        site.hint =
            "Site"

        site.setText(
            saved.site
        )

        val note =
            EditText(this)

        note.hint =
            "Note"

        note.setText(
            saved.note
        )

        val intime =
            EditText(this)

        intime.hint =
            "In Time"

        intime.setText(
            saved.intime
        )

        val outtime =
            EditText(this)

        outtime.hint =
            "Out Time"

        outtime.setText(
            saved.outtime
        )

        card.addView(status)
        card.addView(site)
        card.addView(note)
        card.addView(intime)
        card.addView(outtime)

        card.addView(
            makeButton(
                "💾 SAVE ATTENDANCE"
            ) {

                val record =
                    AttendanceRecord(
                        status.selectedItem
                            .toString(),
                        intime.text.toString(),
                        outtime.text.toString(),
                        site.text.toString(),
                        note.text.toString(),
                        saved.latitude,
                        saved.longitude
                    )

                db.saveAttendance(
                    w.id,
                    selectedDate,
                    record
                )

                Toast.makeText(
                    this,
                    "${w.name} saved",
                    Toast.LENGTH_SHORT
                ).show()

                attendance()
            }
        )

        card.addView(
            makeButton(
                "📍 SAVE GPS"
            ) {

                saveGps(
                    w,
                    status.selectedItem
                        .toString(),
                    site.text.toString(),
                    note.text.toString(),
                    intime.text.toString(),
                    outtime.text.toString()
                )
            }
        )

        card.addView(
            makeButton(
                "🗺 OPEN MAP"
            ) {

                openMap(
                    saved.latitude,
                    saved.longitude
                )
            }
        )

        body.addView(card)

        body.addView(
            Space(this),
            LinearLayout.LayoutParams(
                1,
                dp(8)
            )
        )
    }

    private fun pickDate() {

        val cal =
            Calendar.getInstance()

        try {

            cal.time =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).parse(
                    selectedDate
                ) ?: Date()

        } catch (_: Exception) {
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->

                selectedDate =
                    String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        day
                    )

                attendance()
            },
            cal.get(
                Calendar.YEAR
            ),
            cal.get(
                Calendar.MONTH
            ),
            cal.get(
                Calendar.DAY_OF_MONTH
            )
        ).show()
    }

    private fun saveGps(
        w: Worker,
        status: String,
        site: String,
        note: String,
        intime: String,
        outtime: String
    ) {

        if (
            checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                GPS_REQUEST
            )

            Toast.makeText(
                this,
                "GPS permission allow pannunga",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val lm =
            getSystemService(
                LOCATION_SERVICE
            ) as LocationManager

        var location:
            android.location.Location? =
            null

        try {

            location =
                lm.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER
                )

            if (location == null) {

                location =
                    lm.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER
                    )
            }

        } catch (_: SecurityException) {
        }

        if (location == null) {

            Toast.makeText(
                this,
                "GPS location கிடைக்கவில்லை. GPS ON pannunga.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        db.saveAttendance(
            w.id,
            selectedDate,
            AttendanceRecord(
                status,
                intime,
                outtime,
                site,
                note,
                location.latitude,
                location.longitude
            )
        )

        Toast.makeText(
            this,
            "GPS saved",
            Toast.LENGTH_SHORT
        ).show()

        attendance()
    }

    private fun openMap(
        lat: Double,
        lon: Double
    ) {

        if (
            lat == 0.0 &&
            lon == 0.0
        ) {

            Toast.makeText(
                this,
                "GPS save pannala",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        try {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "geo:$lat,$lon?q=$lat,$lon"
                    )
                )
            )

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Maps app கிடைக்கவில்லை",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun workerAttendance(
        w: Worker
    ) {

        makeScreen(
            "📅 ${w.name}"
        )

        addLabel(
            "Date: $selectedDate",
            17f
        )

        val a =
            db.getAttendance(
                w.id,
                selectedDate
            )

        if (a == null) {

            addLabel(
                "Attendance not entered."
            )

        } else {

            addLabel(
                "Status: ${a.status}"
            )

            addLabel(
                "Site: ${a.site}"
            )

            addLabel(
                "In: ${a.intime}"
            )

            addLabel(
                "Out: ${a.outtime}"
            )

            addLabel(
                "Note: ${a.note}"
            )
        }

        body.addView(
            makeButton(
                "EDIT ATTENDANCE"
            ) {
                attendance()
            }
        )

        body.addView(
            makeButton(
                "BACK"
            ) {
                workers()
            }
        )
    }

    private fun siteSummary() {

        makeScreen(
            "📍 SITE SUMMARY"
        )

        addLabel(
            "📅 $selectedDate",
            17f
        )

        val records =
            db.attendanceForDate(
                selectedDate
            )

        val groups =
            LinkedHashMap<
                String,
                MutableList<
                    Pair<
                        Worker,
                        AttendanceRecord
                    >
                >
            >()

        for (w in db.workers()) {

            val a =
                records[w.id]
                    ?: AttendanceRecord()

            val site =
                if (a.site.trim().isEmpty()) {
                    "No Site Assigned"
                } else {
                    a.site.trim()
                }

            groups
                .getOrPut(site) {
                    mutableListOf()
                }
                .add(
                    Pair(
                        w,
                        a
                    )
                )
        }

        var totalPresent =
            0

        var totalHalf =
            0

        var totalAbsent =
            0

        for (
            entry in groups
        ) {

            val site =
                entry.key

            val list =
                entry.value

            var present =
                0

            var half =
                0

            var absent =
                0

            for (item in list) {

                when (
                    item.second.status
                ) {

                    "Present" -> {
                        present++
                    }

                    "Half Day" -> {
                        half++
                    }

                    "Absent" -> {
                        absent++
                    }
                }
            }

            totalPresent += present
            totalHalf += half
            totalAbsent += absent

            val card =
                LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
            )

            card.setBackgroundColor(
                Color.rgb(
                    245,
                    248,
                    252
                )
            )

            card.addView(
                label(
                    "📍 $site",
                    18f
                )
            )

            card.addView(
                label(
                    "🟢 Present: $present"
                )
            )

            card.addView(
                label(
                    "🟡 Half Day: $half"
                )
            )

            card.addView(
                label(
                    "🔴 Absent: $absent"
                )
            )

            card.addView(
                label(
                    "👷 Total: ${list.size}"
                )
            )

            card.addView(
                makeButton(
                    "VIEW WORKERS"
                ) {
                    siteWorkers(
                        site,
                        list
                    )
                }
            )

            body.addView(card)

            body.addView(
                Space(this),
                LinearLayout.LayoutParams(
                    1,
                    dp(8)
                )
            )
        }

        addLabel(
            "📊 TOTAL",
            19f
        )

        addLabel(
            "Present: $totalPresent"
        )

        addLabel(
            "Half Day: $totalHalf"
        )

        addLabel(
            "Absent: $totalAbsent"
        )

        addLabel(
            "Working: ${totalPresent + totalHalf}"
        )
    }

    private fun siteWorkers(
        site: String,
        list: MutableList<
            Pair<
                Worker,
                AttendanceRecord
            >
        >
    ) {

        makeScreen(
            "📍 $site"
        )

        addLabel(
            "Date: $selectedDate",
            17f
        )

        for (item in list) {

            addLabel(
                "👷 ${item.first.name} — ${item.second.status}",
                17f
            )
        }

        body.addView(
            makeButton(
                "BACK"
            ) {
                siteSummary()
            }
        )
    }

    private fun salary() {

        makeScreen(
            "💰 SALARY"
        )

        val now =
            Calendar.getInstance()

        addLabel(
            "Month: ${
                now.get(Calendar.MONTH) + 1
            }-${
                now.get(Calendar.YEAR)
            }",
            17f
        )

        for (w in db.workers()) {

            val counts =
                db.monthCounts(
                    w.id,
                    now.get(
                        Calendar.YEAR
                    ),
                    now.get(
                        Calendar.MONTH
                    )
                )

            val days =
                counts.first +
                    counts.second * 0.5

            val earned =
                if (
                    w.paymentType ==
                    "Daily"
                ) {

                    w.salary * days

                } else {

                    w.salary
                }

            val advance =
                db.advanceTotal(
                    w.id
                )

            val pending =
                earned - advance

            val card =
                LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
            )

            card.setBackgroundColor(
                Color.rgb(
                    245,
                    248,
                    252
                )
            )

            card.addView(
                label(
                    "👷 ${w.name}",
                    18f
                )
            )

            card.addView(
                label(
                    "Payment: ${w.paymentType}"
                )
            )

            card.addView(
                label(
                    "Present: ${counts.first}"
                )
            )

            card.addView(
                label(
                    "Half Day: ${counts.second}"
                )
            )

            card.addView(
                label(
                    "Total Days: ${money(days)}"
                )
            )

            card.addView(
                label(
                    "Earned: ₹${money(earned)}"
                )
            )

            card.addView(
                label(
                    "Advance: ₹${money(advance)}"
                )
            )

            card.addView(
                label(
                    "💰 PENDING: ₹${money(pending)}",
                    18f
                )
            )

            card.addView(
                makeButton(
                    "ADVANCE HISTORY / EDIT"
                ) {
                    advanceHistory(w)
                }
            )

            body.addView(card)

            body.addView(
                Space(this),
                LinearLayout.LayoutParams(
                    1,
                    dp(8)
                )
            )
        }

        body.addView(
            makeButton(
                "ALL WORKERS REPORT"
            ) {
                allWorkersReport()
            }
        )
    }

    private fun advanceSelect() {

        makeScreen(
            "💵 ADVANCE"
        )

        addLabel(
            "Select Worker",
            18f
        )

        for (w in db.workers()) {

            val total =
                db.advanceTotal(
                    w.id
                )

            body.addView(
                makeButton(
                    "${w.name} — ₹${money(total)}"
                ) {
                    advanceHistory(w)
                }
            )
        }
    }

    private fun advanceHistory(
        w: Worker
    ) {

        makeScreen(
            "💵 ${w.name}"
        )

        addLabel(
            "Total Advance: ₹${money(db.advanceTotal(w.id))}",
            18f
        )

        body.addView(
            makeButton(
                "➕ ADD ADVANCE"
            ) {
                advanceDialog(
                    w,
                    null
                )
            }
        )

        val list =
            db.advances(
                w.id
            )

        if (list.isEmpty()) {

            addLabel(
                "No advance records."
            )

            return
        }

        for (a in list) {

            val card =
                LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
            )

            card.setBackgroundColor(
                Color.rgb(
                    245,
                    248,
                    252
                )
            )

            card.addView(
                label(
                    "📅 ${a.date}   ₹${money(a.amount)}",
                    17f
                )
            )

            card.addView(
                label(
                    "Note: ${a.note}"
                )
            )

            card.addView(
                makeButton(
                    "✏ EDIT"
                ) {
                    advanceDialog(
                        w,
                        a
                    )
                }
            )

            card.addView(
                makeButton(
                    "🗑 DELETE"
                ) {

                    AlertDialog.Builder(this)
                        .setTitle(
                            "Delete Advance?"
                        )
                        .setMessage(
                            "₹${money(a.amount)}"
                        )
                        .setNegativeButton(
                            "NO",
                            null
                        )
                        .setPositiveButton(
                            "YES"
                        ) { _, _ ->

                            db.deleteAdvance(
                                a.id,
                                w.id
                            )

                            advanceHistory(
                                w
                            )
                        }
                        .show()
                }
            )

            body.addView(card)

            body.addView(
                Space(this),
                LinearLayout.LayoutParams(
                    1,
                    dp(8)
                )
            )
        }
    }

    private fun advanceDialog(
        w: Worker,
        old: Advance?
    ) {

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        box.setPadding(
            dp(12),
            0,
            dp(12),
            0
        )

        val amount =
            EditText(this)

        amount.hint =
            "Advance Amount"

        amount.inputType =
            2

        amount.setText(
            if (old == null)
                ""
            else
                old.amount.toString()
        )

        val date =
            EditText(this)

        date.hint =
            "Date YYYY-MM-DD"

        date.setText(
            old?.date
                ?: todayDate()
        )

        val note =
            EditText(this)

        note.hint =
            "Note"

        note.setText(
            old?.note ?: ""
        )

        box.addView(amount)
        box.addView(date)
        box.addView(note)

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    if (old == null)
                        "ADD ADVANCE"
                    else
                        "EDIT ADVANCE"
                )
                .setView(box)
                .setNegativeButton(
                    "CANCEL",
                    null
                )
                .setPositiveButton(
                    "SAVE",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val value =
                    amount.text
                        .toString()
                        .toDoubleOrNull()
                        ?: -1.0

                if (value <= 0) {

                    amount.error =
                        "Enter amount"

                } else {

                    if (old == null) {

                        db.addAdvance(
                            w.id,
                            value,
                            date.text.toString(),
                            note.text.toString()
                        )

                    } else {

                        db.updateAdvance(
                            old.id,
                            w.id,
                            value,
                            date.text.toString(),
                            note.text.toString()
                        )
                    }

                    dialog.dismiss()

                    advanceHistory(w)
                }
            }
        }

        dialog.show()
    }

    private fun allWorkersReport() {

        makeScreen(
            "📊 ALL WORKERS REPORT"
        )

        val now =
            Calendar.getInstance()

        addLabel(
            "Month: ${
                now.get(Calendar.MONTH) + 1
            }-${
                now.get(Calendar.YEAR)
            }",
            17f
        )

        addLabel(
            "Worker | Days | Earned | Advance | Pending",
            14f
        )

        for (w in db.workers()) {

            val counts =
                db.monthCounts(
                    w.id,
                    now.get(
                        Calendar.YEAR
                    ),
                    now.get(
                        Calendar.MONTH
                    )
                )

            val days =
                counts.first +
                    counts.second * 0.5

            val earned =
                if (
                    w.paymentType ==
                    "Daily"
                ) {

                    w.salary * days

                } else {

                    w.salary
                }

            val advance =
                db.advanceTotal(
                    w.id
                )

            val pending =
                earned - advance

            val card =
                LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
            )

            card.setBackgroundColor(
                Color.rgb(
                    245,
                    248,
                    252
                )
            )

            card.addView(
                label(
                    "👷 ${w.name}",
                    17f
                )
            )

            card.addView(
                label(
                    "Days: ${money(days)}"
                )
            )

            card.addView(
                label(
                    "Earned: ₹${money(earned)}"
                )
            )

            card.addView(
                label(
                    "Advance: ₹${money(advance)}"
                )
            )

            card.addView(
                label(
                    "Pending: ₹${money(pending)}"
                )
            )

            body.addView(card)

            body.addView(
                Space(this),
                LinearLayout.LayoutParams(
                    1,
                    dp(6)
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode ==
            GPS_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(
                this,
                "GPS permission allowed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

private fun todayDate(): String {

    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(Date())
}
