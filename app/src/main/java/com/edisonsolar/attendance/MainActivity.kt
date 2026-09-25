package com.edisonsolar.attendance

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
    val note: String
)

class DBHelper(context: Context) :
    android.database.sqlite.SQLiteOpenHelper(
        context,
        "edison.db",
        null,
        2
    ) {

    override fun onCreate(
        db: android.database.sqlite.SQLiteDatabase
    ) {

        db.execSQL(
            """
            CREATE TABLE workers(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                phone TEXT,
                salary REAL,
                type TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE attendance(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER,
                date TEXT,
                status TEXT,
                intime TEXT,
                outtime TEXT,
                site TEXT,
                note TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE advances(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER,
                date TEXT,
                amount REAL,
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
        // Existing database is preserved.
    }

    fun getWorkers(): List<Worker> {

        val list = mutableListOf<Worker>()

        readableDatabase.rawQuery(
            "SELECT id,name,phone,salary,type FROM workers ORDER BY name",
            null
        ).use { cursor ->

            while (cursor.moveToNext()) {

                list.add(
                    Worker(
                        id = cursor.getLong(0),
                        name = cursor.getString(1) ?: "",
                        phone = cursor.getString(2) ?: "",
                        salary = cursor.getDouble(3),
                        type = cursor.getString(4) ?: "Monthly"
                    )
                )
            }
        }

        return list
    }

    fun addWorker(
        name: String,
        phone: String,
        salary: Double,
        type: String
    ) {

        val values = ContentValues()

        values.put("name", name)
        values.put("phone", phone)
        values.put("salary", salary)
        values.put("type", type)

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
        type: String
    ) {

        val values = ContentValues()

        values.put("name", name)
        values.put("phone", phone)
        values.put("salary", salary)
        values.put("type", type)

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

    /*
     * IMPORTANT ATTENDANCE SAVE
     *
     * One worker + one date = one record.
     */
    fun saveAttendance(
        workerId: Long,
        date: String,
        status: String,
        intime: String,
        outtime: String,
        site: String,
        note: String
    ) {

        val finalStatus = when (status.trim()) {
            "Present" -> "Present"
            "Absent" -> "Absent"
            "Half Day" -> "Half Day"
            else -> "Absent"
        }

        // Delete only this worker's record for this exact date.
        writableDatabase.delete(
            "attendance",
            "worker_id=? AND date=?",
            arrayOf(
                workerId.toString(),
                date
            )
        )

        val values = ContentValues()

        values.put(
            "worker_id",
            workerId
        )

        values.put(
            "date",
            date
        )

        values.put(
            "status",
            finalStatus
        )

        values.put(
            "intime",
            intime.trim()
        )

        values.put(
            "outtime",
            outtime.trim()
        )

        values.put(
            "site",
            site.trim()
        )

        values.put(
            "note",
            note.trim()
        )

        writableDatabase.insert(
            "attendance",
            null,
            values
        )
    }

    /*
     * Read attendance for EXACT worker + EXACT date.
     */
    fun getAttendance(
        workerId: Long,
        date: String
    ): AttendanceRecord? {

        readableDatabase.rawQuery(
            """
            SELECT status,intime,outtime,site,note
            FROM attendance
            WHERE worker_id=? AND date=?
            ORDER BY id DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(
                workerId.toString(),
                date
            )
        ).use { cursor ->

            if (cursor.moveToFirst()) {

                return AttendanceRecord(
                    status =
                        cursor.getString(0) ?: "Absent",

                    intime =
                        cursor.getString(1) ?: "",

                    outtime =
                        cursor.getString(2) ?: "",

                    site =
                        cursor.getString(3) ?: "",

                    note =
                        cursor.getString(4) ?: ""
                )
            }
        }

        return null
    }

    fun countStatusForDate(
        date: String,
        status: String
    ): Int {

        readableDatabase.rawQuery(
            """
            SELECT COUNT(*)
            FROM attendance
            WHERE date=? AND status=?
            """.trimIndent(),
            arrayOf(
                date,
                status
            )
        ).use { cursor ->

            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }

        return 0
    }

    fun workingDays(
        month: String
    ): Int {

        readableDatabase.rawQuery(
            """
            SELECT COUNT(DISTINCT date)
            FROM attendance
            WHERE date LIKE ?
            AND status IN ('Present','Half Day')
            """.trimIndent(),
            arrayOf(
                "$month%"
            )
        ).use { cursor ->

            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }

        return 0
    }

    fun addAdvance(
        workerId: Long,
        amount: Double,
        note: String
    ) {

        val values = ContentValues()

        values.put(
            "worker_id",
            workerId
        )

        values.put(
            "date",
            currentDate()
        )

        values.put(
            "amount",
            amount
        )

        values.put(
            "note",
            note
        )

        writableDatabase.insert(
            "advances",
            null,
            values
        )
    }

    fun getAdvance(
        workerId: Long
    ): Double {

        readableDatabase.rawQuery(
            """
            SELECT COALESCE(SUM(amount),0)
            FROM advances
            WHERE worker_id=?
            """.trimIndent(),
            arrayOf(
                workerId.toString()
            )
        ).use { cursor ->

            if (cursor.moveToFirst()) {
                return cursor.getDouble(0)
            }
        }

        return 0.0
    }

    private fun currentDate(): String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())
    }
}

class MainActivity : Activity() {

    private lateinit var db: DBHelper
    private lateinit var content: LinearLayout

    private val blue =
        Color.rgb(25, 118, 210)

    private val darkBlue =
        Color.rgb(13, 71, 161)

    private val lightBlue =
        Color.rgb(227, 242, 253)

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        db = DBHelper(this)

        home()
    }

    private fun applySystemBarFix(
        root: View
    ) {

        if (Build.VERSION.SDK_INT >= 30) {

            root.setOnApplyWindowInsetsListener { view, insets ->

                val bars =
                    insets.getInsets(
                        WindowInsets.Type.systemBars()
                    )

                view.setPadding(
                    0,
                    bars.top,
                    0,
                    bars.bottom
                )

                insets
            }
        }
    }

    private fun createLayout(
        title: String
    ): LinearLayout {

        val root =
            LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        applySystemBarFix(root)

        val header =
            TextView(this)

        header.text =
            "⚡ EDISON\n$title"

        header.textSize =
            21f

        header.setTextColor(
            Color.WHITE
        )

        header.setPadding(
            16,
            14,
            10,
            14
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

        val scroll =
            ScrollView(this)

        content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.setPadding(
            12,
            12,
            12,
            12
        )

        scroll.addView(
            content
        )

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        val navigation =
            LinearLayout(this)

        navigation.orientation =
            LinearLayout.HORIZONTAL

        navigation.setBackgroundColor(
            lightBlue
        )

        val names =
            listOf(
                "HOME",
                "WORKERS",
                "ATTENDANCE",
                "SALARY"
            )

        names.forEach { name ->

            val button =
                Button(this)

            button.text =
                name

            button.textSize =
                12f

            button.setPadding(
                1,
                1,
                1,
                1
            )

            button.setOnClickListener {

                when (name) {

                    "HOME" -> home()

                    "WORKERS" -> workers()

                    "ATTENDANCE" ->
                        attendance()

                    "SALARY" ->
                        salary()
                }
            }

            navigation.addView(
                button,
                LinearLayout.LayoutParams(
                    0,
                    -2,
                    1f
                )
            )
        }

        root.addView(
            navigation
        )

        return root
    }

    private fun makeButton(
        text: String,
        action: () -> Unit
    ): Button {

        val button =
            Button(this)

        button.text =
            text

        button.setOnClickListener {
            action()
        }

        return button
    }

    private fun home() {

        val root =
            createLayout(
                "Dashboard"
            )

        val date =
            today()

        val workers =
            db.getWorkers()

        val total =
            workers.size

        val present =
            db.countStatusForDate(
                date,
                "Present"
            )

        val halfDay =
            db.countStatusForDate(
                date,
                "Half Day"
            )

        /*
         * Anyone not marked Present/Half Day
         * is counted as Absent.
         */
        val absent =
            (total - present - halfDay)
                .coerceAtLeast(0)

        val month =
            date.substring(
                0,
                7
            )

        val workingDays =
            db.workingDays(month)

        content.addView(
            TextView(this).apply {

                text =
                    "📊 TODAY • $date"

                textSize =
                    20f

                setTextColor(
                    blue
                )
            }
        )

        addDashboardCard(
            "👷 TOTAL WORKERS",
            total.toString()
        )

        addDashboardCard(
            "🟢 PRESENT",
            present.toString()
        )

        addDashboardCard(
            "🔴 ABSENT",
            absent.toString()
        )

        addDashboardCard(
            "🟡 HALF DAY",
            halfDay.toString()
        )

        addDashboardCard(
            "📅 WORKING DAYS",
            workingDays.toString()
        )

        content.addView(
            makeButton(
                "📅 OPEN ATTENDANCE"
            ) {
                attendance()
            }
        )

        content.addView(
            makeButton(
                "＋ ADD WORKER"
            ) {
                addWorker()
            }
        )

        content.addView(
            makeButton(
                "👷 VIEW WORKERS"
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

        setContentView(root)
    }

    private fun addDashboardCard(
        title: String,
        value: String
    ) {

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.HORIZONTAL

        box.setPadding(
            12,
            12,
            12,
            12
        )

        box.setBackgroundColor(
            lightBlue
        )

        val titleView =
            TextView(this)

        titleView.text =
            title

        titleView.textSize =
            16f

        val valueView =
            TextView(this)

        valueView.text =
            value

        valueView.textSize =
            23f

        valueView.setTextColor(
            blue
        )

        valueView.gravity =
            Gravity.END

        box.addView(
            titleView,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        box.addView(
            valueView,
            LinearLayout.LayoutParams(
                70,
                -2
            )
        )

        content.addView(
            box
        )

        content.addView(
            TextView(this),
            LinearLayout.LayoutParams(
                -1,
                6
            )
        )
    }

    private fun attendance() {

        val root =
            createLayout(
                "Attendance Calendar"
            )

        val selectedDate =
            Calendar.getInstance()

        val dateTitle =
            TextView(this)

        dateTitle.textSize =
            21f

        dateTitle.setTextColor(
            blue
        )

        content.addView(
            dateTitle
        )

        content.addView(
            makeButton(
                "📆 SELECT DATE"
            ) {

                DatePickerDialog(
                    this,
                    object :
                        DatePickerDialog.OnDateSetListener {

                        override fun onDateSet(
                            view: DatePicker?,
                            year: Int,
                            month: Int,
                            dayOfMonth: Int
                        ) {

                            selectedDate.set(
                                year,
                                month,
                                dayOfMonth
                            )

                            updateDateTitle(
                                dateTitle,
                                selectedDate
                            )

                            refreshAttendance(
                                selectedDate
                            )
                        }
                    },
                    selectedDate.get(
                        Calendar.YEAR
                    ),
                    selectedDate.get(
                        Calendar.MONTH
                    ),
                    selectedDate.get(
                        Calendar.DAY_OF_MONTH
                    )
                ).show()
            }
        )

        updateDateTitle(
            dateTitle,
            selectedDate
        )

        refreshAttendance(
            selectedDate
        )

        setContentView(root)
    }

    private fun updateDateTitle(
        title: TextView,
        calendar: Calendar
    ) {

        title.text =
            "📅 " +
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(
                        calendar.time
                    )
    }

    private fun refreshAttendance(
        calendar: Calendar
    ) {

        /*
         * Keep:
         * child 0 = date title
         * child 1 = date button
         */
        while (content.childCount > 2) {

            content.removeViewAt(
                content.childCount - 1
            )
        }

        val date =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(
                calendar.time
            )

        val workers =
            db.getWorkers()

        val present =
            db.countStatusForDate(
                date,
                "Present"
            )

        val halfDay =
            db.countStatusForDate(
                date,
                "Half Day"
            )

        val absent =
            (workers.size -
                    present -
                    halfDay)
                .coerceAtLeast(0)

        val summary =
            TextView(this)

        summary.text =
            """
            🟢 Present: $present
            🟡 Half Day: $halfDay
            🔴 Absent: $absent
            """.trimIndent()

        summary.textSize =
            17f

        content.addView(
            summary
        )

        workers.forEach { worker ->

            addWorkerAttendanceCard(
                worker,
                date
            )
        }
    }

    private fun addWorkerAttendanceCard(
        worker: Worker,
        date: String
    ) {

        val record =
            db.getAttendance(
                worker.id,
                date
            )

        /*
         * No record = Absent.
         * Existing record = exact saved status.
         */
        val status =
            record?.status ?: "Absent"

        val icon =
            when (status) {

                "Present" -> "🟢"

                "Half Day" -> "
