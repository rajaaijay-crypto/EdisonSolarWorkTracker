package com.edisonsolar.attendance

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

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

        db.execSQL("""
            CREATE TABLE workers(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                phone TEXT,
                salary REAL,
                type TEXT
            )
        """.trimIndent())

        db.execSQL("""
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
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE advances(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER,
                date TEXT,
                amount REAL,
                note TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(
        db: android.database.sqlite.SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
    }

    fun workers(): List<Worker> {

        val result = mutableListOf<Worker>()

        readableDatabase.rawQuery(
            "SELECT * FROM workers ORDER BY name",
            null
        ).use { c ->

            while (c.moveToNext()) {

                result.add(
                    Worker(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getDouble(3),
                        c.getString(4)
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

        val v = ContentValues()

        v.put("worker_id", workerId)
        v.put("date", date)
        v.put("status", status)
        v.put("intime", intime)
        v.put("outtime", outtime)
        v.put("site", site)
        v.put("note", note)

        writableDatabase.insert(
            "attendance",
            null,
            v
        )
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
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())
        )

        v.put("amount", amount)
        v.put("note", note)

        writableDatabase.insert(
            "advances",
            null,
            v
        )
    }

    fun advance(workerId: Long): Double {

        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM advances WHERE worker_id=?",
            arrayOf(workerId.toString())
        ).use { c ->

            if (c.moveToFirst()) {
                return c.getDouble(0)
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
        header.setTextColor(Color.WHITE)

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

        nav.setBackgroundColor(
            lightBlue
        )

        listOf(
            "HOME",
            "WORKERS",
            "ATTENDANCE",
            "SALARY"
        ).forEach {

            val b =
                Button(this)

            b.text = it

            b.setOnClickListener {

                when (it.text) {

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

                setTextColor(blue)
            }
        )

        content.addView(
            TextView(this).apply {

                text =
                    "\nWorkers: ${db.workers().size}\n\n" +
                    "🟢 Present\n" +
                    "🔴 Absent\n" +
                    "🟡 Half Day\n\n" +
                    "Salary & Advance management"

                textSize = 18f
            }
        )

        content.addView(
            button("＋ ADD WORKER") {
                workers()
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

        db.workers().forEach { worker ->

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

            content.addView(box)
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

        val salary =
            EditText(this)

        salary.hint =
            "Salary"

        salary.inputType = 2

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

                db.addWorker(
                    name.text.toString(),
                    phone.text.toString(),
                    salary.text.toString()
                        .toDoubleOrNull()
                        ?: 0.0,
                    type.text.toString()
                        .ifBlank {
                            "Monthly"
                        }
                )

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
            }
        )

        db.workers().forEach {

            content.addView(
                button(
                    "👷 ${it.name} — Mark / Edit"
                ) {

                    attendanceFor(it)
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
            "In Time"

        val outtime =
            EditText(this)

        outtime.hint =
            "Out Time"

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

        db.workers().forEach {

            val advance =
                db.advance(it.id)

            val balance =
                it.salary - advance

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
                        👷 ${it.name}
                        
                        Salary: ₹${it.salary}
                        Advance: ₹$advance
                        Balance: ₹$balance
                        """.trimIndent()

                    textSize = 18f
                }
            )

            box.addView(
                button("＋ ADD ADVANCE") {

                    addAdvance(it)
                }
            )

            content.addView(box)
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

        amount.inputType = 2

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

                db.addAdvance(
                    worker.id,
                    amount.text.toString()
                        .toDoubleOrNull()
                        ?: 0.0,
                    note.text.toString()
                )

                salary()
            }
            .setNegativeButton(
                "CANCEL",
                null
            )
            .show()
    }
}
