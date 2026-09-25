package com.edisonsolar.attendance

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
        // No database structure change required
    }

    fun workers(): List<Worker> {

        val result = mutableListOf<Worker>()

        readableDatabase.rawQuery(
            "SELECT id,name,phone,salary,type FROM workers ORDER BY name",
            null
        ).use { c ->

            while (c.moveToNext()) {

                result.add(
                    Worker(
                        id = c.getLong(0),
                        name = c.getString(1) ?: "",
                        phone = c.getString(2) ?: "",
                        salary = c.getDouble(3),
                        type = c.getString(4) ?: "Monthly"
                    )
                )
            }
        }

        return result
    }

    fun addWorker(
        name: String,
        phone: String,
        salary: Double,
        type: String
    ) {

        val v = ContentValues()

        v.put("name", name)
        v.put("phone", phone)
        v.put("salary", salary)
        v.put("type", type)

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
        type: String
    ) {

        val v = ContentValues()

        v.put("name", name)
        v.put("phone", phone)
        v.put("salary", salary)
        v.put("type", type)

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

    /*
     * IMPORTANT:
     * One worker + one date = one attendance record.
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

        val cleanStatus =
            when (status.trim()) {
                "Present" -> "Present"
                "Absent" -> "Absent"
                "Half Day" -> "Half Day"
                else -> "Absent"
            }

        // Remove old record ONLY for this worker and this date.
        writableDatabase.delete(
            "attendance",
            "worker_id=? AND date=?",
            arrayOf(
                workerId.toString(),
                date
            )
        )

        val values = ContentValues()

        values.put("worker_id", workerId)
        values.put("date", date)
        values.put("status", cleanStatus)
        values.put("intime", intime.trim())
        values.put("outtime", outtime.trim())
        values.put("site", site.trim())
        values.put("note", note.trim())

        writableDatabase.insertOrThrow(
            "attendance",
            null,
            values
        )
    }

    /*
     * Get attendance ONLY for this worker and this exact date.
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
        ).use { c ->

            if (c.moveToFirst()) {

                return AttendanceRecord(
                    status = c.getString(0) ?: "Absent",
                    intime = c.getString(1) ?: "",
                    outtime = c.getString(2) ?: "",
                    site = c.getString(3) ?: "",
                    note = c.getString(4) ?: ""
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
        ).use { c ->

            if (c.moveToFirst()) {
                return c.getInt(0)
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
            arrayOf("$month%")
        ).use { c ->

            if (c.moveToFirst()) {
                return c.getInt(0)
            }
        }

        return 0
    }

    fun addAdvance(
        workerId: Long,
        amount: Double,
        note: String
    ) {

        val v = ContentValues()

        v.put("worker_id", workerId)

        v.put(
            "date",
            today()
        )

        v.put("amount", amount)
        v.put("note", note)

        writableDatabase.insert(
            "advances",
            null,
            v
        )
    }

    fun advance(
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
        ).use { c ->

            if (c.moveToFirst()) {
                return c.getDouble(0)
            }
        }

        return 0.0
    }

    private fun today(): String {

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

    private fun layout(
        title: String
    ): LinearLayout {

        val root =
            LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.setOnApplyWindowInsetsListener { view, insets ->

            val bars =
                insets.getInsets(
                    android.view.WindowInsets.Type.systemBars()
                )

            view.setPadding(
                0,
                bars.top,
                0,
                bars.bottom
            )

            insets
        }

        val header =
            TextView(this)

        header.text =
            "⚡ EDISON\n$title"

        header.textSize = 21f

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

        root.addView(header)

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

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        val nav =
            LinearLayout(this)

        nav.orientation =
            LinearLayout.HORIZONTAL

        nav.setBackgroundColor(
            lightBlue
        )

        listOf(
            "HOME",
            "WORKERS",
            "ATTENDANCE",
            "SALARY"
        ).forEach { name ->

            val b =
                Button(this)

            b.text =
                name

            b.textSize =
                12f

            b.setPadding(
                1,
                1,
                1,
                1
            )

            b.setOnClickListener {

                when (b.text.toString()) {

                    "HOME" ->
                        home()

                    "WORKERS" ->
                        workers()

                    "ATTENDANCE" ->
                        attendance()

                    "SALARY" ->
                        salary()
                }
            }

            nav.addView(
                b,
                LinearLayout.LayoutParams(
                    0,
                    -2,
                    1f
                )
            )
        }

        root.addView(nav)

        return root
    }

    private fun button(
        text: String,
        action: () -> Unit
    ): Button {

        return Button(this).apply {

            this.text = text

            setOnClickListener {
                action()
            }
        }
    }

    private fun home() {

        val root =
            layout("Dashboard")

        val date =
            today()

        val total =
            db.workers().size

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
         * Workers who are not Present or Half Day
         * are treated as Absent for today's dashboard.
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

        addCard(
            "👷 TOTAL WORKERS",
            total.toString()
        )

        addCard(
            "🟢 PRESENT",
            present.toString()
        )

        addCard(
            "🔴 ABSENT",
            absent.toString()
        )

        addCard(
            "🟡 HALF DAY",
            halfDay.toString()
        )

        addCard(
            "📅 WORKING DAYS",
            workingDays.toString()
        )

        content.addView(
            button("📅 OPEN ATTENDANCE") {
                attendance()
            }
        )

        content.addView(
            button("＋ ADD WORKER") {
                addWorker()
            }
        )

        content.addView(
            button("👷 VIEW WORKERS") {
                workers()
            }
        )

        content.addView(
            button("💰 SALARY & ADVANCE") {
                salary()
            }
        )

        setContentView(root)
    }

    private fun addCard(
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

        val left =
            TextView(this)

        left.text =
            title

        left.textSize =
            16f

        val right =
            TextView(this)

        right.text =
            value

        right.textSize =
            23f

        right.setTextColor(
            blue
        )

        right.gravity =
            Gravity.END

        box.addView(
            left,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        box.addView(
            right,
            LinearLayout.LayoutParams(
                70,
                -2
            )
        )

        content.addView(box)

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
            layout("Attendance Calendar")

        val selected =
            Calendar.getInstance()

        val dateText =
            TextView(this)

        dateText.textSize =
            21f

        dateText.setTextColor(
            blue
        )

        content.addView(
            dateText
        )

        fun refreshTitle() {

            dateText.text =
                "📅 " +
                        SimpleDateFormat(
                            "dd MMMM yyyy",
                            Locale.getDefault()
                        ).format(
                            selected.time
                        )
        }

        refreshTitle()

        content.addView(
            button("📆 SELECT DATE") {

                DatePickerDialog(
                    this,
                    { _, year, month, day ->

                        selected.set(
                            year,
                            month,
                            day
                        )

                        refreshTitle()

                        refreshAttendanceList(
                            selected
                        )
                    },
                    selected.get(
                        Calendar.YEAR
                    ),
                    selected.get(
                        Calendar.MONTH
                    ),
                    selected.get(
                        Calendar.DAY_OF_MONTH
                    )
                ).show()
            }
        )

        refreshAttendanceList(
            selected
        )

        setContentView(root)
    }

    private fun refreshAttendanceList(
        selected: Calendar
    ) {

        /*
         * Keep first 2 views:
         * 0 = date
         * 1 = select button
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
                selected.time
            )

        val workers =
            db.workers()

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

        content.addView(
            TextView(this).apply {

                text =
                    """
                    🟢 Present: $present
                    🟡 Half Day: $halfDay
                    🔴 Absent: $absent
                    """.trimIndent()

                textSize =
                    17f
            }
        )

        workers.forEach { worker ->

            val record =
                db.getAttendance(
                    worker.id,
                    date
                )

            /*
             * IMPORTANT:
             * No record = Absent.
             * Existing record uses its actual status.
             */
            val status =
                record?.status ?: "Absent"

            val statusIcon =
                when (status) {

                    "Present" -> "🟢"

                    "Half Day" -> "🟡"

                    "Absent" -> "🔴"

                    else -> "⚪"
                }

            val box =
                LinearLayout(this)

            box.orientation =
                LinearLayout.VERTICAL

            box.setPadding(
                10,
                10,
                10,
                10
            )

            box.setBackgroundColor(
                lightBlue
            )

            val info =
                TextView(this)

            info.text =
                """
                👷 ${worker.name}

                $statusIcon Status: $status

                📍 ${record?.site?.ifBlank {
                    "No site entered"
                } ?: "No attendance record"}
                """.trimIndent()

            info.textSize =
                16f

            box.addView(info)

            box.addView(
                button("👁️ VIEW WORK") {

                    viewWork(
                        worker,
                        date
                    )
                }
            )

            box.addView(
                button("✏️ EDIT ATTENDANCE") {

                    attendanceFor(
                        worker,
                        date
                    )
                }
            )

            content.addView(box)

            content.addView(
                TextView(this),
                LinearLayout.LayoutParams(
                    -1,
                    8
                )
            )
        }
    }

    private fun attendanceFor(
        worker: Worker,
        date: String
    ) {

        /*
         * Read the EXACT worker + EXACT date record.
         */
        val old =
            db.getAttendance(
                worker.id,
                date
            )

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        val status =
            Spinner(this)

        val statusList =
            arrayOf(
                "Present",
                "Absent",
                "Half Day"
            )

        status.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                statusList
            ).apply {

                setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
            }

        /*
         * Very important:
         * Existing status is selected correctly.
         * New/no record defaults to Absent.
         */
        val selectedStatus =
            old?.status ?: "Absent"

        val selectedPosition =
            statusList.indexOf(
                selectedStatus
            ).let {

               
