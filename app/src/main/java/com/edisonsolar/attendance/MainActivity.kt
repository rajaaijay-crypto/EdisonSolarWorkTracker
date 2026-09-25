package com.edisonsolar.attendance

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
    val type: String
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

class DBHelper(context: Context) :
    android.database.sqlite.SQLiteOpenHelper(
        context,
        "edison.db",
        null,
        4
    ) {

    override fun onCreate(db: android.database.sqlite.SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE workers(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT,
                salary REAL DEFAULT 0,
                type TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE attendance(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                status TEXT NOT NULL,
                intime TEXT,
                outtime TEXT,
                site TEXT,
                note TEXT,
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
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                note TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: android.database.sqlite.SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 4) {

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

    fun addWorker(
        name: String,
        phone: String,
        salary: Double,
        type: String
    ): Long {

        val values = ContentValues()

        values.put("name", name.trim())
        values.put("phone", phone.trim())
        values.put("salary", salary)
        values.put("type", type.trim())

        return writableDatabase.insert(
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
        type: String
    ) {

        val values = ContentValues()

        values.put("name", name.trim())
        values.put("phone", phone.trim())
        values.put("salary", salary)
        values.put("type", type.trim())

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

    fun getWorkers(): MutableList<Worker> {

        val list = mutableListOf<Worker>()

        val cursor = readableDatabase.query(
            "workers",
            null,
            null,
            null,
            null,
            null,
            "name ASC"
        )

        cursor.use {

            while (it.moveToNext()) {

                list.add(
                    Worker(
                        it.getLong(
                            it.getColumnIndexOrThrow("id")
                        ),
                        it.getString(
                            it.getColumnIndexOrThrow("name")
                        ),
                        it.getString(
                            it.getColumnIndexOrThrow("phone")
                        ) ?: "",
                        it.getDouble(
                            it.getColumnIndexOrThrow("salary")
                        ),
                        it.getString(
                            it.getColumnIndexOrThrow("type")
                        ) ?: ""
                    )
                )
            }
        }

        return list
    }

    fun saveAttendance(
        workerId: Long,
        date: String,
        status: String,
        intime: String,
        outtime: String,
        site: String,
        note: String,
        latitude: Double,
        longitude: Double
    ) {

        val finalStatus =
            when (status.trim()) {
                "Present" -> "Present"
                "Absent" -> "Absent"
                "Half Day" -> "Half Day"
                else -> "Absent"
            }

        val values = ContentValues()

        values.put("worker_id", workerId)
        values.put("date", date)
        values.put("status", finalStatus)
        values.put("intime", intime.trim())
        values.put("outtime", outtime.trim())
        values.put("site", site.trim())
        values.put("note", note.trim())
        values.put("latitude", latitude)
        values.put("longitude", longitude)

        writableDatabase.insertWithOnConflict(
            "attendance",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getAttendance(
        workerId: Long,
        date: String
    ): AttendanceRecord? {

        val cursor = readableDatabase.query(
            "attendance",
            null,
            "worker_id=? AND date=?",
            arrayOf(
                workerId.toString(),
                date
            ),
            null,
            null,
            null
        )

        cursor.use {

            if (it.moveToFirst()) {

                return AttendanceRecord(
                    it.getString(
                        it.getColumnIndexOrThrow("status")
                    ) ?: "Absent",

                    it.getString(
                        it.getColumnIndexOrThrow("intime")
                    ) ?: "",

                    it.getString(
                        it.getColumnIndexOrThrow("outtime")
                    ) ?: "",

                    it.getString(
                        it.getColumnIndexOrThrow("site")
                    ) ?: "",

                    it.getString(
                        it.getColumnIndexOrThrow("note")
                    ) ?: "",

                    it.getDouble(
                        it.getColumnIndexOrThrow("latitude")
                    ),

                    it.getDouble(
                        it.getColumnIndexOrThrow("longitude")
                    )
                )
            }
        }

        return null
    }

    fun getAdvanceTotal(workerId: Long): Double {

        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(amount) FROM advances WHERE worker_id=?",
            arrayOf(workerId.toString())
        )

        cursor.use {

            if (it.moveToFirst()) {

                if (!it.isNull(0)) {
                    return it.getDouble(0)
                }
            }
        }

        return 0.0
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
        values.put("note", note.trim())

        writableDatabase.insert(
            "advances",
            null,
            values
        )
    }
}

class MainActivity : Activity() {

    private lateinit var db: DBHelper
    private lateinit var content: LinearLayout

    private var selectedDate: String = todayDate()

    private val blue = Color.rgb(25, 118, 210)
    private val darkBlue = Color.rgb(13, 71, 161)
    private val lightBlue = Color.rgb(232, 242, 252)
    private val green = Color.rgb(46, 125, 50)
    private val red = Color.rgb(198, 40, 40)
    private val orange = Color.rgb(239, 108, 0)

    private var currentWorkerId: Long = -1

    private var currentSitePhoto: Bitmap? = null

    companion object {

        private const val CAMERA_REQUEST = 501
        private const val LOCATION_REQUEST = 502

        fun todayDate(): String {

            return SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())
        }

        fun displayDate(date: String): String {

            return try {

                val input = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )

                val output = SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                )

                output.format(
                    input.parse(date)!!
                )

            } catch (_: Exception) {
                date
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(this)

        home()
    }

    private fun createLayout(title: String): LinearLayout {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.WHITE)

        val header = TextView(this)

        header.text = "⚡ EDISON  $title"
        header.textSize = 18f
        header.setTextColor(Color.WHITE)
        header.gravity = Gravity.CENTER_VERTICAL

        header.setPadding(
            dp(12),
            0,
            dp(8),
            0
        )

        header.setBackgroundColor(blue)

        root.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                dp(54)
            )
        )

        val scroll = ScrollView(this)

        content = LinearLayout(this)

        content.orientation = LinearLayout.VERTICAL

        content.setPadding(
            dp(8),
            dp(8),
            dp(8),
            dp(8)
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        val navigation = LinearLayout(this)

        navigation.orientation = LinearLayout.HORIZONTAL
        navigation.setBackgroundColor(lightBlue)

        addNavigationButton(
            navigation,
            "HOME"
        ) {
            home()
        }

        addNavigationButton(
            navigation,
            "WORKERS"
        ) {
            workers()
        }

        addNavigationButton(
            navigation,
            "ATTEND"
        ) {
            attendance()
        }

        addNavigationButton(
            navigation,
            "SALARY"
        ) {
            salary()
        }

        root.addView(
            navigation,
            LinearLayout.LayoutParams(
                -1,
                dp(50)
            )
        )

        return root
    }

    private fun addNavigationButton(
        parent: LinearLayout,
        name: String,
        action: () -> Unit
    ) {

        val button = Button(this)

        button.text = name
        button.textSize = 10f
        button.setPadding(0, 0, 0, 0)

        button.minHeight = 40
        button.minimumHeight = 40

        button.setOnClickListener {
            action()
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                dp(46),
                1f
            )
        )
    }

    private fun makeButton(
        text: String,
        action: () -> Unit
    ): Button {

        val button = Button(this)

        button.text = text
        button.textSize = 13f

        button.setPadding(
            dp(4),
            0,
            dp(4),
            0
        )

        button.minHeight = dp(42)
        button.minimumHeight = dp(42)

        button.setOnClickListener {
            action()
        }

        return button
    }

    private fun home() {

        setContentView(
            createLayout("HOME")
        )

        addTitle("EDISON SOLAR WORK TRACKER")

        val workers = db.getWorkers()

        var present = 0
        var half = 0
        var absent = 0

        for (worker in workers) {

            val record =
                db.getAttendance(
                    worker.id,
                    todayDate()
                )

            when (record?.status) {

                "Present" -> present++

                "Half Day" -> half++

                else -> absent++
            }
        }

        addInfoCard(
            "👷 TOTAL WORKERS",
            workers.size.toString(),
            blue
        )

        addInfoCard(
            "🟢 PRESENT",
            present.toString(),
            green
        )

        addInfoCard(
            "🟡 HALF DAY",
            half.toString(),
            orange
        )

        addInfoCard(
            "🔴 ABSENT",
            absent.toString(),
            red
        )

        content.addView(
            makeButton(
                "📅 TODAY ATTENDANCE"
            ) {
                attendance()
            }
        )

        content.addView(
            makeButton(
                "👷 MANAGE WORKERS"
            ) {
                workers()
            }
        )

        content.addView(
            makeButton(
                "💰 SALARY & ADVANCE"
            ) {
                salary()
            }
        )
    }

    private fun addTitle(text: String) {

        val tv = TextView(this)

        tv.text = text
        tv.textSize = 18f
        tv.setTextColor(darkBlue)
        tv.setPadding(
            0,
            dp(6),
            0,
            dp(8)
        )

        content.addView(tv)
    }

    private fun addInfoCard(
        title: String,
        value: String,
        color: Int
    ) {

        val box = LinearLayout(this)

        box.orientation = LinearLayout.HORIZONTAL
        box.setPadding(
            dp(12),
            dp(8),
            dp(12),
            dp(8)
        )

        box.setBackgroundColor(
            Color.rgb(245, 248, 252)
        )

        val name = TextView(this)

        name.text = title
        name.textSize = 14f
        name.setTextColor(color)

        val number = TextView(this)

        number.text = value
        number.textSize = 20f
        number.setTextColor(color)
        number.gravity = Gravity.END

        box.addView(
            name,
            LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
            )
        )

        box.addView(
            number,
            LinearLayout.LayoutParams(
                dp(70),
                dp(52)
            )
        )

        val params = LinearLayout.LayoutParams(
            -1,
            dp(64)
        )

        params.setMargins(
            0,
            dp(3),
            0,
            dp(3)
        )

        content.addView(box, params)
    }

    private fun workers() {

        setContentView(
            createLayout("WORKERS")
        )

        addTitle("👷 WORKERS")

        content.addView(
            makeButton(
                "➕ ADD WORKER"
            ) {
                showWorkerDialog(null)
            }
        )

        val list = db.getWorkers()

        if (list.isEmpty()) {

            addText(
                "No workers added yet."
            )

            return
        }

        for (worker in list) {

            val card = LinearLayout(this)

            card.orientation = LinearLayout.VERTICAL

            card.setPadding(
                dp(10),
                dp(6),
                dp(10),
                dp(6)
            )

            card.setBackgroundColor(
                Color.rgb(245, 248, 252)
            )

            val name = TextView(this)

            name.text =
                "👷 ${worker.name}"

            name.textSize = 17f
            name.setTextColor(darkBlue)

            card.addView(name)

            addTextTo(
                card,
                "📞 ${worker.phone}"
            )

            addTextTo(
                card,
                "💰 Salary: ₹${money(worker.salary)}"
            )

            if (worker.type.isNotBlank()) {

                addTextTo(
                    card,
                    "🔧 ${worker.type}"
                )
            }

            val buttons = LinearLayout(this)

            buttons.orientation =
                LinearLayout.HORIZONTAL

            val edit = makeButton(
                "EDIT"
            ) {
                showWorkerDialog(worker)
            }

            val advance = makeButton(
                "ADVANCE"
            ) {
                showAdvanceDialog(worker)
            }

            val delete = makeButton(
                "DELETE"
            ) {
                confirmDelete(worker)
            }

            buttons.addView(
                edit,
                LinearLayout.LayoutParams(
                    0,
                    dp(42),
                    1f
                )
            )

            buttons.addView(
                advance,
                LinearLayout.LayoutParams(
                    0,
                    dp(42),
                    1f
                )
            )

            buttons.addView(
                delete,
                LinearLayout.LayoutParams(
                    0,
                    dp(42),
                    1f
                )
            )

            card.addView(buttons)

            val params = LinearLayout.LayoutParams(
                -1,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(
                0,
                dp(4),
                0,
                dp(4)
            )

            content.addView(card, params)
        }
    }

    private fun showWorkerDialog(worker: Worker?) {

        val layout = LinearLayout(this)

        layout.orientation = LinearLayout.VERTICAL

        layout.setPadding(
            dp(20),
            dp(5),
            dp(20),
            0
        )

        val name = EditText(this)

        name.hint = "Worker Name"

        val phone = EditText(this)

        phone.hint = "Phone"

        phone.inputType =
            android.text.InputType.TYPE_CLASS_PHONE

        val salary = EditText(this)

        salary.hint = "Monthly Salary"

        salary.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        val type = EditText(this)

        type.hint = "Type / Work"

        if (worker != null) {

            name.setText(worker.name)
            phone.setText(worker.phone)
            salary.setText(worker.salary.toString())
            type.setText(worker.type)
        }

        layout.addView(name)
        layout.addView(phone)
        layout.addView(salary)
        layout.addView(type)

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (worker == null)
                    "Add Worker"
                else
                    "Edit Worker"
            )
            .setView(layout)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton(
                if (worker == null)
                    "SAVE"
                else
                    "UPDATE",
                null
            )
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val workerName =
                    name.text.toString().trim()

                if (workerName.isBlank()) {

                    name.error =
                        "Enter worker name"

                    return@setOnClickListener
                }

                val salaryValue =
                    salary.text.toString()
                        .toDoubleOrNull() ?: 0.0

                if (worker == null) {

                    db.addWorker(
                        workerName,
                        phone.text.toString(),
                        salaryValue,
                        type.text.toString()
                    )

                } else {

                    db.updateWorker(
                        worker.id,
                        workerName,
                        phone.text.toString(),
                        salaryValue,
                        type.text.toString()
                    )
                }

                dialog.dismiss()

                workers()
            }
        }

        dialog.show()
    }

    private fun confirmDelete(worker: Worker) {

        AlertDialog.Builder(this)
            .setTitle("Delete Worker?")
            .setMessage(
                "Delete ${worker.name} and attendance records?"
            )
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("DELETE") { _, _ ->

                db.deleteWorker(worker.id)

                workers()
            }
            .show()
    }

    private fun attendance() {

        setContentView(
            createLayout("ATTENDANCE")
        )

        addTitle("📅 ATTENDANCE")

        val dateButton = makeButton(
            "📅 ${displayDate(selectedDate)}"
        ) {
            showDatePicker()
        }

        content.addView(dateButton)

        refreshAttendance()
    }

    private fun showDatePicker() {

        val cal = Calendar.getInstance()

        try {

            val sdf = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

            cal.time =
                sdf.parse(selectedDate) ?: Date()

        } catch (_: Exception) {
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->

                val selected =
                    Calendar.getInstance()

                selected.set(
                    year,
                    month,
                    day
                )

                selectedDate =
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(
                        selected.time
                    )

                attendance()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun refreshAttendance() {

        addText(
            "📅 ${displayDate(selectedDate)}"
        )

        val workers = db.getWorkers()

        if (workers.isEmpty()) {

            addText(
                "No workers found."
            )

            return
        }

        var presentTotal = 0
        var halfTotal = 0
        var absentTotal = 0

        val siteWorkers =
            linkedMapOf<String, MutableList<Worker>>()

        val sitePresent =
            linkedMapOf<String, Int>()

        val siteHalf =
            linkedMapOf<String, Int>()

        val siteAbsent =
            linkedMapOf<String, Int>()

        for (worker in workers) {

            val record =
                db.getAttendance(
                    worker.id,
                    selectedDate
                )

            val status =
                record?.status ?: "Absent"

            when (status) {

                "Present" ->
                    presentTotal++

                "Half Day" ->
                    halfTotal++

                else ->
                    absentTotal++
            }

            val site =
                if (
                    record != null &&
                    record.site.isNotBlank()
                ) {
                    record.site.trim()
                } else {
                    "No Site Assigned"
                }

            if (!siteWorkers.containsKey(site)) {

                siteWorkers[site] =
                    mutableListOf()

                sitePresent[site] = 0
                siteHalf[site] = 0
                siteAbsent[site] = 0
            }

            siteWorkers[site]!!.add(worker)

            when (status) {

                "Present" ->
                    sitePresent[site] =
                        (sitePresent[site] ?: 0) + 1

                "Half Day" ->
                    siteHalf[site] =
                        (siteHalf[site] ?: 0) + 1

                else ->
                    siteAbsent[site] =
                        (siteAbsent[site] ?: 0) + 1
            }
        }

        addInfoCard(
            "🟢 PRESENT",
            presentTotal.toString(),
            green
        )

        addInfoCard(
            "🟡 HALF DAY",
            halfTotal.toString(),
            orange
        )

        addInfoCard(
            "🔴 ABSENT",
            absentTotal.toString(),
            red
        )

        addInfoCard(
            "👷 WORKING",
            (presentTotal + halfTotal).toString(),
            blue
        )

        addText(
            "📍 SITE SUMMARY"
        )

        for (site in siteWorkers.keys) {

            val p =
                sitePresent[site] ?: 0

            val h =
                siteHalf[site] ?: 0

            val a =
                siteAbsent[site] ?: 0

            val total =
                siteWorkers[site]!!.size

            val card = LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
            )

            card.setBackgroundColor(
                Color.rgb(245, 248, 252)
            )

            val title = TextView(this)

            title.text =
                "📍 $site"

            title.textSize = 17f
            title.setTextColor(darkBlue)

            card.addView(title)

            addTextTo(
                card,
                "🟢 Present: $p"
            )

            addTextTo(
                card,
                "🟡 Half Day: $h"
            )

            addTextTo(
                card,
                "🔴 Absent: $a"
            )

            addTextTo(
                card,
                "👷 Total: $total"
            )

            val viewButton = makeButton(
                "VIEW SITE WORKERS"
            ) {

                showSiteWorkers(
                    site,
                    siteWorkers[site] ?: mutableListOf()
                )
            }

            card.addView(viewButton)

            val params =
                LinearLayout.LayoutParams(
                    -1,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

            params.setMargins(
                0,
                dp(5),
                0,
                dp(5)
            )

            content.addView(card, params)
        }

        addText(
            "👷 ALL WORKERS"
        )

        for (worker in workers) {

            val record =
                db.getAttendance(
                    worker.id,
                    selectedDate
                )

            val status =
                record?.status ?: "Absent"

            val statusText =
                when (status) {

                    "Present" ->
                        "🟢 Present"

                    "Half Day" ->
                        "🟡 Half Day"

                    else ->
                        "🔴 Absent"
                }

            val button =
                makeButton(
                    "👷 ${worker.name} — $statusText"
                ) {

                    showAttendanceDialog(
                        worker,
                        record
                    )
                }

            content.addView(button)
        }
    }

    private fun showSiteWorkers(
        site: String,
        workers: List<Worker>
    ) {

        val layout = LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            dp(15),
            dp(10),
            dp(15),
            dp(10)
        )

        val scroll = ScrollView(this)

        scroll.addView(layout)

        for (worker in workers) {

            val record =
                db.getAttendance(
                    worker.id,
                    selectedDate
                )

            val status =
                record?.status ?: "Absent"

            val statusText =
                when (status) {

                    "Present" ->
                        "🟢 Present"

                    "Half Day" ->
                        "🟡 Half Day"

                    else ->
                        "🔴 Absent"
                }

            val button =
                makeButton(
                    "👷 ${worker.name} — $statusText"
                ) {

                    showAttendanceDialog(
                        worker,
                        record
                    )
                }

            layout.addView(button)
        }

        AlertDialog.Builder(this)
            .setTitle(
                "📍 $site"
            )
            .setView(scroll)
            .setPositiveButton(
                "CLOSE",
                null
            )
            .show()
    }

    private fun showAttendanceDialog(
        worker: Worker,
        oldRecord: AttendanceRecord?
    ) {

        val layout = LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            dp(20),
            dp(5),
            dp(20),
            0
        )

        val statusSpinner =
            Spinner(this)

        val statuses =
            arrayOf(
                "Present",
                "Absent",
                "Half Day"
            )

        statusSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                statuses
            )

        val selectedStatus =
            oldRecord?.status ?: "Absent"

        statusSpinner.setSelection(
            statuses.indexOf(selectedStatus)
                .coerceAtLeast(0)
        )

        val intime =
            EditText(this)

        intime.hint = "In Time"

        val outtime =
            EditText(this)

        outtime.hint = "Out Time"

        val site =
            EditText(this)

        site.hint = "Site Name"

        val note =
            EditText(this)

        note.hint = "Work Note"

        val latitude =
            EditText(this)

        latitude.hint = "Latitude"

        val longitude =
            EditText(this)

        longitude.hint = "Longitude"

        latitude.isEnabled = false
        longitude.isEnabled = false

        if (oldRecord != null) {

            intime.setText(
                oldRecord.intime
            )

            outtime.setText(
                oldRecord.outtime
            )

            site.setText(
                oldRecord.site
            )

            note.setText(
                oldRecord.note
            )

            if (oldRecord.latitude != 0.0) {

                latitude.setText(
                    oldRecord.latitude.toString()
                )
            }

            if (oldRecord.longitude != 0.0) {

                longitude.setText(
                    oldRecord.longitude.toString()
                )
            }
        }

        layout.addView(statusSpinner)
        layout.addView(intime)
        layout.addView(outtime)
        layout.addView(site)
        layout.addView(note)
        layout.addView(latitude)
        layout.addView(longitude)

        val gpsButton =
            makeButton(
                "📍 GET CURRENT GPS"
            ) {

                getCurrentLocation(
                    latitude,
                    longitude
                )
            }

        layout.addView(gpsButton)

        val cameraButton =
            makeButton(
                "📷 TAKE SITE PHOTO"
            ) {

                currentWorkerId =
                    worker.id

                openCamera()
            }

        layout.addView(cameraButton)

        val mapButton =
            makeButton(
                "🗺️ OPEN GOOGLE MAPS"
            ) {

                val enteredSite =
                    site.text.toString().trim()

                openMap(
                    enteredSite,
                    latitude.text.toString(),
                    longitude.text.toString()
                )
            }

        layout.addView(mapButton)

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    "👷 ${worker.name}"
                )
                .setView(layout)
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

                val status =
                    statusSpinner
                        .selectedItem
                        .toString()

                val lat =
                    latitude.text.toString()
                        .toDoubleOrNull() ?: 0.0

                val lon =
                    longitude.text.toString()
                        .toDoubleOrNull() ?: 0.0

                db.saveAttendance(
                    worker.id,
                    selectedDate,
                    status,
                    intime.text.toString(),
                    outtime.text.toString(),
                    site.text.toString(),
                    note.text.toString(),
                    lat,
                    lon
                )

                dialog.dismiss()

                attendance()
            }
        }

        dialog.show()
    }

    private fun openCamera() {

        val intent =
            Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
            )

        if (
            intent.resolveActivity(
                packageManager
            ) != null
        ) {

            startActivityForResult(
                intent,
                CAMERA_REQUEST
            )
        } else {

            Toast.makeText(
                this,
                "Camera app not found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == CAMERA_REQUEST &&
            resultCode == RESULT_OK
        ) {

            currentSitePhoto =
                data?.extras?.get("data") as? Bitmap

            if (currentSitePhoto != null) {

                Toast.makeText(
                    this,
                    "📷 Site photo captured",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getCurrentLocation(
        latitude: EditText,
        longitude: EditText
    ) {

        if (
            checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_REQUEST
            )

            Toast.makeText(
                this,
                "GPS permission allow pannunga, then again GET GPS press pannunga.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        try {

            val manager =
                getSystemService(
                    LOCATION_SERVICE
                ) as LocationManager

            val provider =
                when {

                    manager.isProviderEnabled(
                        LocationManager.GPS_PROVIDER
                    ) ->
                        LocationManager.GPS_PROVIDER

                    manager.isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER
                    ) ->
                        LocationManager.NETWORK_PROVIDER

                    else -> null
                }

            if (provider == null) {

                Toast.makeText(
                    this,
                    "Phone GPS ON pannunga",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            val location =
                manager.getLastKnownLocation(
                    provider
                )

            if (location != null) {

                latitude.setText(
                    location.latitude.toString()
                )

                longitude.setText(
                    location.longitude.toString()
                )

                Toast.makeText(
                    this,
                    "📍 GPS location added",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "GPS location கிடைக்கவில்லை. கொஞ்சம் நேரம் கழித்து try pannunga.",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (_: SecurityException) {

            Toast.makeText(
                this,
                "Location permission required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openMap(
        site: String,
        latitude: String,
        longitude: String
    ) {

        val lat =
            latitude.toDoubleOrNull()

        val lon =
            longitude.toDoubleOrNull()

        val uri: Uri

        if (
            lat != null &&
            lon != null &&
            lat != 0.0 &&
            lon != 0.0
        ) {

            uri =
                Uri.parse(
                    "geo:$lat,$lon?q=$lat,$lon"
                )

        } else if (site.isNotBlank()) {

            uri =
                Uri.parse(
                    "geo:0,0?q=" +
                            Uri.encode(site)
                )

        } else {

            Toast.makeText(
                this,
                "Site name அல்லது GPS location enter pannunga",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        try {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    uri
                )
            )

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Google Maps available இல்லை",
                Toast.LENGTH_SHORT
            ).show()
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
            requestCode == LOCATION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(
                this,
                "GPS permission allowed. GET CURRENT GPS press pannunga.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun salary() {

        setContentView(
            createLayout("SALARY")
        )

        addTitle(
            "💰 SALARY & ADVANCE"
        )

        val workers =
            db.getWorkers()

        if (workers.isEmpty()) {

            addText(
                "No workers found."
            )

            return
        }

        for (worker in workers) {

            val advance =
                db.getAdvanceTotal(worker.id)

            val balance =
                worker.salary - advance

            val card =
                LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
            )

            card.setBackgroundColor(
                Color.rgb(245, 248, 252)
            )

            addTextTo(
                card,
                "👷 ${worker.name}"
            )

            addTextTo(
                card,
                "Salary: ₹${money(worker.salary)}"
            )

            addTextTo(
                card,
                "Advance: ₹${money(advance)}"
            )

            addTextTo(
                card,
                "Balance: ₹${money(balance)}"
            )

            val button =
                makeButton(
                    "➕ ADD ADVANCE"
                ) {

                    showAdvanceDialog(worker)
                }

            card.addView(button)

            content.addView(
                card,
                LinearLayout.LayoutParams(
                    -1,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun showAdvanceDialog(
        worker: Worker
    ) {

        val layout =
            LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            dp(20),
            0,
            dp(20),
            0
        )

        val amount =
            EditText(this)

        amount.hint =
            "Advance Amount"

        amount.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        val note =
            EditText(this)

        note.hint =
            "Note"

        layout.addView(amount)
        layout.addView(note)

        AlertDialog.Builder(this)
            .setTitle(
                "Advance - ${worker.name}"
            )
            .setView(layout)
            .setNegativeButton(
                "CANCEL",
                null
            )
            .setPositiveButton(
                "SAVE"
            ) { _, _ ->

                val value =
                    amount.text.toString()
                        .toDoubleOrNull() ?: 0.0

                if (value > 0) {

                    db.addAdvance(
                        worker.id,
                        value,
                        todayDate(),
                        note.text.toString()
                    )
                }

                salary()
            }
            .show()
    }

    private fun addText(text: String) {

        val tv = TextView(this)

        tv.text = text
        tv.textSize = 15f
        tv.setTextColor(Color.DKGRAY)

        tv.setPadding(
            dp(4),
            dp(7),
            dp(4),
            dp(7)
        )

        content.addView(tv)
    }

    private fun addTextTo(
        parent: LinearLayout,
        text: String
    ) {

        val tv = TextView(this)

        tv.text = text
        tv.textSize = 14f
        tv.setTextColor(Color.DKGRAY)

        tv.setPadding(
            0,
            dp(3),
            0,
            dp(3)
        )

        parent.addView(tv)
    }

    private fun money(value: Double): String {

        return String.format(
            Locale.getDefault(),
            "%.2f",
            value
        )
    }

    private fun dp(value: Int): Int {

        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
    }
}
