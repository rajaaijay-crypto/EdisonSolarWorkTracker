package com.edisonsolar.attendance

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Worker(
    val id: Long,
    val name: String,
    val phone: String,
    val salary: Double,
    val type: String
)

class DBHelper(context: Context) :
    android.database.sqlite.SQLiteOpenHelper(
        context,
        "edison.db",
        null,
        2
    ) {

    override fun onCreate(db: android.database.sqlite.SQLiteDatabase) {

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
        // Keep existing data
    }

    fun workers(): List<Worker> {

        val result = mutableListOf<Worker>()

        readableDatabase.rawQuery(
            "SELECT * FROM workers ORDER BY name",
            null
        ).use { cursor ->

            while (cursor.moveToNext()) {

                result.add(
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

        return result
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

    fun saveAttendance(
        workerId: Long,
        date: String,
        status: String,
        intime: String,
        outtime: String,
        site: String,
        note: String
    ) {

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
        values.put("status", status)
        values.put("intime", intime)
        values.put("outtime", outtime)
        values.put("site", site)
        values.put("note", note)

        writableDatabase.insert(
            "attendance",
            null,
            values
        )
    }

    fun addAdvance(
        workerId: Long,
        amount: Double,
        note: String
    ) {

        val values = ContentValues()

        values.put("worker_id", workerId)

        values.put(
            "date",
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())
        )

        values.put("amount", amount)
        values.put("note", note)

        writableDatabase.insert(
            "advances",
            null,
            values
        )
    }

    fun advance(workerId: Long): Double {

        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM advances WHERE worker_id=?",
            arrayOf(workerId.toString())
        ).use { cursor ->

            if (cursor.moveToFirst()) {
                return cursor.getDouble(0)
            }
        }

        return 0.0
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
            18,
            10,
            18
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
            16,
            16,
            16,
            16
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

        val buttons =
            listOf(
                "HOME",
                "WORKERS",
                "ATTENDANCE",
                "SALARY"
            )

        buttons.forEach { buttonText ->

            val b =
                Button(this)

            b.text =
                buttonText

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

        content.addView(
            TextView(this).apply {

                text =
                    "Attendance • Salary • Advance"

                textSize = 25f

                setTextColor(
                    blue
                )

                setPadding(
                    0,
                    0,
                    0,
                    20
                )
            }
        )

        content.addView(
            TextView(this).apply {

                text =
                    """
                    👷 Workers: ${db.workers().size}

                    🟢 Present
                    🔴 Absent
                    🟡 Half Day

                    💰 Salary & Advance Management

                    📍 Site / Customer Details

                    📝 Attendance Notes
                    """.trimIndent()

                textSize = 18f
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
            button("📅 TODAY ATTENDANCE") {
                attendance()
            }
        )

        content.addView(
            button("💰 SALARY & ADVANCE") {
                salary()
            }
        )

        setContentView(root)
    }

    private fun workers() {

        val root =
            layout("Workers")

        content.addView(
            button("＋ ADD WORKER") {
                addWorker()
            }
        )

        val workerList =
            db.workers()

        if (workerList.isEmpty()) {

            content.addView(
                TextView(this).apply {

                    text =
                        "\nNo workers added yet."

                    textSize = 18f
                }
            )
        }

        workerList.forEach { worker ->

            val box =
                LinearLayout(this)

            box.orientation =
                LinearLayout.VERTICAL

            box.setPadding(
                12,
                12,
                12,
                12
            )

            box.setBackgroundColor(
                lightBlue
            )

            val text =
                TextView(this)

            text.text =
                """
                👷 ${worker.name}

                📱 ${worker.phone}

                💰 Salary: ₹${worker.salary}

                Type: ${worker.type}
                """.trimIndent()

            text.textSize = 17f

            box.addView(text)

            box.addView(
                button("✏️ ATTENDANCE / EDIT") {
                    attendanceFor(worker)
                }
            )

            box.addView(
                button("＋ ADD ADVANCE") {
                    addAdvance(worker)
                }
            )

            content.addView(box)

            val space =
                TextView(this)

            space.text = ""

            content.addView(
                space,
                LinearLayout.LayoutParams(
                    -1,
                    10
                )
            )
        }

        setContentView(root)
    }

    private fun addWorker() {

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        val name =
            EditText(this)

        name.hint =
            "Worker Name"

        val phone =
            EditText(this)

        phone.hint =
            "Mobile Number"

        phone.inputType =
            2

        val salary =
            EditText(this)

        salary.hint =
            "Salary"

        salary.inputType =
            2

        val type =
            EditText(this)

        type.hint =
            "Daily / Monthly"

        box.addView(name)
        box.addView(phone)
        box.addView(salary)
        box.addView(type)

        AlertDialog.Builder(this)
            .setTitle("Add Worker")
            .setView(box)
            .setPositiveButton("SAVE") {

                    _, _ ->

                val workerName =
                    name.text.toString().trim()

                if (workerName.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Enter worker name",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                db.addWorker(
                    workerName,
                    phone.text.toString().trim(),
                    salary.text.toString()
                        .toDoubleOrNull()
                        ?: 0.0,
                    type.text.toString()
                        .trim()
                        .ifBlank {
                            "Monthly"
                        }
                )

                Toast.makeText(
                    this,
                    "Worker added",
                    Toast.LENGTH_SHORT
                ).show()

                workers()
            }
            .setNegativeButton(
                "CANCEL",
                null
            )
            .show()
    }

    private fun attendance() {

        val root =
            layout("Today's Attendance")

        val date =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())

        content.addView(
            TextView(this).apply {

                text =
                    "📅 $date"

                textSize = 20f

                setTextColor(
                    blue
                )
            }
        )

        val workerList =
            db.workers()

        if (workerList.isEmpty()) {

            content.addView(
                TextView(this).apply {

                    text =
                        "\nFirst add workers."

                    textSize = 18f
                }
            )
        }

        workerList.forEach { worker ->

            content.addView(
                button(
                    "👷 ${worker.name} — Mark / Edit"
                ) {

                    attendanceFor(worker)
                }
            )
        }

        setContentView(root)
    }

    private fun attendanceFor(
        worker: Worker
    ) {

        val date =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        val status =
            Spinner(this)

        status.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf(
                    "Present",
                    "Absent",
                    "Half Day"
                )
            )

        val intime =
            EditText(this)

        intime.hint =
            "In Time - example 09:00 AM"

        val outtime =
            EditText(this)

        outtime.hint =
            "Out Time - example 06:00 PM"

        val site =
            EditText(this)

        site.hint =
            "Site / Customer"

        val note =
            EditText(this)

        note.hint =
            "Note"

        box.addView(status)
        box.addView(intime)
        box.addView(outtime)
        box.addView(site)
        box.addView(note)

        AlertDialog.Builder(this)
            .setTitle(
                "${worker.name}\n$date"
            )
            .setView(box)
            .setPositiveButton(
                "SAVE"
            ) { _, _ ->

                db.saveAttendance(
                    worker.id,
                    date,
                    status.selectedItem.toString(),
                    intime.text.toString(),
                    outtime.text.toString(),
                    site.text.toString(),
                    note.text.toString()
                )

                Toast.makeText(
                    this,
                    "Attendance saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(
                "CANCEL",
                null
            )
            .show()
    }

    private fun salary() {

        val root =
            layout("Salary & Advance")

        val workerList =
            db.workers()

        if (workerList.isEmpty()) {

            content.addView(
                TextView(this).apply {

                    text =
                        "No workers added yet."

                    textSize = 18f
                }
            )
        }

        workerList.forEach { worker ->

            val advance =
                db.advance(worker.id)

            val balance =
                worker.salary - advance

            val box =
                LinearLayout(this)

            box.orientation =
                LinearLayout.VERTICAL

            box.setPadding(
                12,
                12,
                12,
                12
            )

            box.setBackgroundColor(
                lightBlue
            )

            box.addView(
                TextView(this).apply {

                    text =
                        """
                        👷 ${worker.name}

                        💰 Salary: ₹${worker.salary}

                        💸 Advance: ₹$advance

                        💵 Balance: ₹$balance
                        """.trimIndent()

                    textSize = 18f
                }
            )

            box.addView(
                button("＋ ADD ADVANCE") {
                    addAdvance(worker)
                }
            )

            content.addView(box)

            val space =
                TextView(this)

            space.text = ""

            content.addView(
                space,
                LinearLayout.LayoutParams(
                    -1,
                    10
                )
            )
        }

        setContentView(root)
    }

    private fun addAdvance(
        worker: Worker
    ) {

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        val amount =
            EditText(this)

        amount.hint =
            "Advance Amount"

        amount.inputType =
            2

        val note =
            EditText(this)

        note.hint =
            "Note"

        box.addView(amount)
        box.addView(note)

        AlertDialog.Builder(this)
            .setTitle(
                "Advance - ${worker.name}"
            )
            .setView(box)
            .setPositiveButton(
                "SAVE"
            ) { _, _ ->

                val value =
                    amount.text.toString()
                        .toDoubleOrNull()

                if (value == null || value <= 0) {

                    Toast.makeText(
                        this,
                        "Enter valid amount",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                db.addAdvance(
                    worker.id,
                    value,
                    note.text.toString()
                )

                Toast.makeText(
                    this,
                    "Advance saved",
                    Toast.LENGTH_SHORT
                ).show()

                salary()
            }
            .setNegativeButton(
                "CANCEL",
                null
            )
            .show()
    }
}
