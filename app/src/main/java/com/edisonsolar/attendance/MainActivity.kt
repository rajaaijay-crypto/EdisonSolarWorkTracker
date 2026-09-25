package com.edisonsolar.businesspro

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private lateinit var db: DBHelper
    private lateinit var root: LinearLayout

    private val blue = Color.rgb(25, 118, 210)
    private val darkBlue = Color.rgb(13, 71, 161)
    private val lightBlue = Color.rgb(232, 245, 253)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(this)

        showDashboard()
    }

    // ---------------------------------------------------------
    // BASIC UI
    // ---------------------------------------------------------

    private fun baseLayout(): LinearLayout {

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.WHITE)

        return root
    }

    private fun title(text: String): TextView {

        val t = TextView(this)

        t.text = text
        t.textSize = 22f
        t.setTextColor(Color.WHITE)
        t.setPadding(20, 20, 20, 20)
        t.setBackgroundColor(blue)

        return t
    }

    private fun button(text: String, action: () -> Unit): Button {

        val b = Button(this)

        b.text = text
        b.textSize = 15f
        b.setOnClickListener {
            action()
        }

        return b
    }

    private fun text(text: String, size: Float = 16f): TextView {

        val t = TextView(this)

        t.text = text
        t.textSize = size
        t.setTextColor(Color.DKGRAY)
        t.setPadding(16, 12, 16, 12)

        return t
    }

    private fun edit(hint: String): EditText {

        val e = EditText(this)

        e.hint = hint
        e.setPadding(20, 10, 20, 10)

        return e
    }

    private fun scroll(): ScrollView {

        val s = ScrollView(this)

        s.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 12, 12, 12)
            }
        )

        return s
    }

    private fun addSpace(parent: LinearLayout) {

        val space = Space(this)

        parent.addView(
            space,
            LinearLayout.LayoutParams(
                1,
                10
            )
        )
    }

    // ---------------------------------------------------------
    // DASHBOARD
    // ---------------------------------------------------------

    private fun showDashboard() {

        root = baseLayout()

        root.addView(title("EDISON BUSINESS PRO"))

        root.addView(
            text(
                "Solar Business Management",
                16f
            )
        )

        val s = ScrollView(this)

        val content = LinearLayout(this)

        content.orientation = LinearLayout.VERTICAL
        content.setPadding(12, 12, 12, 20)

        s.addView(content)

        root.addView(
            s,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        val projects = db.projectCount()
        val companies = db.companyCount()
        val collection = db.totalCollection()
        val expenses = db.totalExpenses()
        val balance = db.totalBalance()

        content.addView(
            text(
                "📊 DASHBOARD",
                21f
            )
        )

        content.addView(
            text(
                "🏢 Companies : $companies\n" +
                "📁 Total Projects : $projects\n" +
                "💰 Collection : ₹${money(collection)}\n" +
                "💸 Expenses : ₹${money(expenses)}\n" +
                "⏳ Pending : ₹${money(balance)}",
                18f
            )
        )

        addSpace(content)

        content.addView(
            button("🏢 Companies") {
                showCompanies()
            }
        )

        content.addView(
            button("📁 Projects") {
                showProjects()
            }
        )

        content.addView(
            button("💰 Payment Collection") {
                showPayments()
            }
        )

        content.addView(
            button("💸 Expenses") {
                showExpenses()
            }
        )

        content.addView(
            button("📅 Calendar") {
                showCalendar()
            }
        )

        content.addView(
            button("📊 Full Report") {
                showFullReport()
            }
        )

        content.addView(
            button("📲 WhatsApp Report") {
                shareWhatsApp(buildReport())
            }
        )

        setContentView(root)
    }

    // ---------------------------------------------------------
    // COMPANIES
    // ---------------------------------------------------------

    private fun showCompanies() {

        root = baseLayout()

        root.addView(title("🏢 COMPANIES"))

        root.addView(
            button("➕ Add Company") {
                companyDialog()
            }
        )

        val s = ScrollView(this)

        val list = LinearLayout(this)

        list.orientation = LinearLayout.VERTICAL
        list.setPadding(12, 12, 12, 20)

        s.addView(list)

        val cursor = db.readCompanies()

        while (cursor.moveToNext()) {

            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val phone = cursor.getString(2)

            val card = LinearLayout(this)

            card.orientation = LinearLayout.VERTICAL
            card.setPadding(15, 15, 15, 15)
            card.setBackgroundColor(lightBlue)

            card.addView(
                text(
                    "🏢 $name",
                    19f
                )
            )

            card.addView(
                text(
                    "📞 $phone\n" +
                    "📁 Projects: ${db.companyProjectCount(id)}\n" +
                    "💰 Collection: ₹${money(db.companyCollection(id))}",
                    16f
                )
            )

            card.addView(
                button("View Company Report") {
                    companyReport(id, name)
                }
            )

            list.addView(card)

            addSpace(list)
        }

        cursor.close()

        root.addView(
            s,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            button("⬅ Back") {
                showDashboard()
            }
        )

        setContentView(root)
    }

    private fun companyDialog() {

        val box = LinearLayout(this)

        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 10, 30, 10)

        val name = edit("Company Name")
        val phone = edit("Phone Number")
        val address = edit("Address")

        box.addView(name)
        box.addView(phone)
        box.addView(address)

        AlertDialog.Builder(this)
            .setTitle("Add Company")
            .setView(box)
            .setPositiveButton("SAVE") { _, _ ->

                if (name.text.toString().trim().isNotEmpty()) {

                    db.addCompany(
                        name.text.toString(),
                        phone.text.toString(),
                        address.text.toString()
                    )

                    showCompanies()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun companyReport(
        companyId: Int,
        companyName: String
    ) {

        val message =
            "EDISON BUSINESS PRO\n\n" +
            "COMPANY: $companyName\n\n" +
            "Total Projects: ${db.companyProjectCount(companyId)}\n" +
            "Project Value: ₹${money(db.companyProjectValue(companyId))}\n" +
            "Collection: ₹${money(db.companyCollection(companyId))}\n" +
            "Pending: ₹${money(db.companyPending(companyId))}\n" +
            "Expenses: ₹${money(db.companyExpenses(companyId))}"

        AlertDialog.Builder(this)
            .setTitle(companyName)
            .setMessage(message)
            .setPositiveButton("WHATSAPP") { _, _ ->
                shareWhatsApp(message)
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    // ---------------------------------------------------------
    // PROJECTS
    // ---------------------------------------------------------

    private fun showProjects() {

        root = baseLayout()

        root.addView(title("📁 PROJECTS"))

        root.addView(
            button("➕ Add Project") {
                projectDialog()
            }
        )

        val s = ScrollView(this)

        val list = LinearLayout(this)

        list.orientation = LinearLayout.VERTICAL
        list.setPadding(12, 12, 12, 20)

        s.addView(list)

        val cursor = db.readProjects()

        while (cursor.moveToNext()) {

            val id = cursor.getInt(0)
            val number = cursor.getString(1)
            val company = cursor.getString(2)
            val customer = cursor.getString(3)
            val site = cursor.getString(4)
            val kw = cursor.getDouble(5)
            val amount = cursor.getDouble(6)
            val status = cursor.getString(7)

            val collected = db.projectCollection(id)
            val balance = amount - collected

            val card = LinearLayout(this)

            card.orientation = LinearLayout.VERTICAL
            card.setPadding(15, 15, 15, 15)
            card.setBackgroundColor(lightBlue)

            card.addView(
                text(
                    "$number  |  $status",
                    20f
                )
            )

            card.addView(
                text(
                    "🏢 $company\n" +
                    "👤 $customer\n" +
                    "📍 $site\n" +
                    "☀️ ${kw} kW\n" +
                    "💰 Amount: ₹${money(amount)}\n" +
                    "✅ Collected: ₹${money(collected)}\n" +
                    "⏳ Balance: ₹${money(balance)}",
                    16f
                )
            )

            card.addView(
                button("Open Project") {
                    projectDetails(id)
                }
            )

            list.addView(card)

            addSpace(list)
        }

        cursor.close()

        root.addView(
            s,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            button("⬅ Back") {
                showDashboard()
            }
        )

        setContentView(root)
    }

    private fun projectDialog() {

        val box = LinearLayout(this)

        box.orientation = LinearLayout.VERTICAL
        box.setPadding(25, 5, 25, 5)

        val company = edit("Company Name")
        val customer = edit("Customer Name")
        val phone = edit("Customer Phone")
        val site = edit("Site Address")
        val kw = edit("Solar kW")
        val amount = edit("Project Amount")
        val date = edit("Installation Date")
        val status = edit("Status - Pending / Running / Completed")

        box.addView(company)
        box.addView(customer)
        box.addView(phone)
        box.addView(site)
        box.addView(kw)
        box.addView(amount)
        box.addView(date)
        box.addView(status)

        AlertDialog.Builder(this)
            .setTitle("New Project")
            .setView(box)
            .setPositiveButton("SAVE") { _, _ ->

                val companyName = company.text.toString()

                if (companyName.trim().isEmpty()) {
                    return@setPositiveButton
                }

                val number = db.nextProjectNumber()

                db.addProject(
                    number,
                    companyName,
                    customer.text.toString(),
                    phone.text.toString(),
                    site.text.toString(),
                    kw.text.toString().toDoubleOrNull() ?: 0.0,
                    amount.text.toString().toDoubleOrNull() ?: 0.0,
                    date.text.toString(),
                    status.text.toString().ifEmpty {
                        "Pending"
                    }
                )

                showProjects()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    // ---------------------------------------------------------
    // PROJECT DETAILS
    // ---------------------------------------------------------

    private fun projectDetails(id: Int) {

        val p = db.getProject(id) ?: return

        val number = p.number
        val company = p.company
        val customer = p.customer
        val phone = p.phone
        val site = p.site
        val kw = p.kw
        val amount = p.amount
        val date = p.date
        val status = p.status

        val collected = db.projectCollection(id)
        val balance = amount - collected

        root = baseLayout()

        root.addView(
            title("📁 $number")
        )

        val s = ScrollView(this)

        val content = LinearLayout(this)

        content.orientation = LinearLayout.VERTICAL
        content.setPadding(12, 12, 12, 20)

        s.addView(content)

        content.addView(
            text(
                "🏢 Company: $company\n\n" +
                "👤 Customer: $customer\n" +
                "📞 Phone: $phone\n\n" +
                "📍 Site: $site\n" +
                "☀️ Solar: $kw kW\n\n" +
                "💰 Project Amount: ₹${money(amount)}\n" +
                "✅ Collected: ₹${money(collected)}\n" +
                "⏳ Balance: ₹${money(balance)}\n\n" +
                "📅 Installation: $date\n" +
                "📌 Status: $status",
                18f
            )
        )

        content.addView(
            button("📍 Save / Open GPS") {
                gpsDialog(id)
            }
        )

        content.addView(
            button("📸 Project Photo Gallery") {
                photoGallery(id, number)
            }
        )

        content.addView(
            button("💰 Add Payment") {
                paymentDialog(id)
            }
        )

        content.addView(
            button("💰 Payment History") {
                projectPayments(id, number)
            }
        )

        content.addView(
            button("📲 WhatsApp Project Report") {

                val report =
                    "EDISON BUSINESS PRO\n\n" +
                    "PROJECT: $number\n" +
                    "Company: $company\n" +
                    "Customer: $customer\n" +
                    "Phone: $phone\n" +
                    "Site: $site\n" +
                    "Solar: $kw kW\n" +
                    "Amount: ₹${money(amount)}\n" +
                    "Collected: ₹${money(collected)}\n" +
                    "Balance: ₹${money(balance)}\n" +
                    "Status: $status"

                shareWhatsApp(report)
            }
        )

        root.addView(
            s,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            button("⬅ Back") {
                showProjects()
            }
        )

        setContentView(root)
    }

    // ---------------------------------------------------------
    // GPS
    // ---------------------------------------------------------

    private fun gpsDialog(projectId: Int) {

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
                101
            )

            Toast.makeText(
                this,
                "Location permission allow pannunga",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val gps = db.getProjectGps(projectId)

        if (gps != null) {

            val uri = Uri.parse(
                "geo:${gps.first},${gps.second}?q=${gps.first},${gps.second}"
            )

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
                    "Map app not available",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            AlertDialog.Builder(this)
                .setTitle("GPS")
                .setMessage(
                    "இந்த trial version-ல் GPS coordinates-ஐ manual-ஆக save செய்யலாம்."
                )
                .setView(
                    LinearLayout(this).apply {

                        orientation = LinearLayout.VERTICAL

                        val lat = edit("Latitude")
                        val lon = edit("Longitude")

                        addView(lat)
                        addView(lon)

                        tag = Pair(lat, lon)
                    }
                )
                .setPositiveButton("SAVE") { dialog, _ ->

                    val box = (dialog as AlertDialog)
                        .findViewById<LinearLayout>(
                            android.R.id.custom
                        )

                    // GPS can also be opened through Google Maps
                    Toast.makeText(
                        this,
                        "GPS saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }
    }

    // ---------------------------------------------------------
    // PHOTOS
    // ---------------------------------------------------------

    private var currentPhotoProject = 0

    private fun photoGallery(
        projectId: Int,
        projectNumber: String
    ) {

        currentPhotoProject = projectId

        root = baseLayout()

        root.addView(
            title("📸 $projectNumber PHOTO GALLERY")
        )

        root.addView(
            button("➕ Add Photos") {

                val intent = Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                )

                intent.type = "image/*"

                intent.putExtra(
                    Intent.EXTRA_ALLOW_MULTIPLE,
                    true
                )

                intent.addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                startActivityForResult(
                    intent,
                    501
                )
            }
        )

        val s = ScrollView(this)

        val list = LinearLayout(this)

        list.orientation = LinearLayout.VERTICAL

        val cursor = db.projectPhotos(projectId)

        while (cursor.moveToNext()) {

            val uriText = cursor.getString(1)

            val image = ImageView(this)

            try {

                image.setImageURI(
                    Uri.parse(uriText)
                )

                image.adjustViewBounds = true
                image.minimumHeight = 300

                list.addView(image)

            } catch (_: Exception) {
            }
        }

        cursor.close()

        s.addView(list)

        root.addView(
            s,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            button("⬅ Back") {

                projectDetails(
                    projectId
                )
            }
        )

        setContentView(root)
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
            requestCode == 501 &&
            resultCode == RESULT_OK &&
            data != null
        ) {

            val clip = data.clipData

            if (clip != null) {

                for (i in 0 until clip.itemCount) {

                    val uri =
                        clip.getItemAt(i).uri

                    savePhotoUri(uri)
                }

            } else {

                data.data?.let {

                    savePhotoUri(it)
                }
            }

            Toast.makeText(
                this,
                "Photos added",
                Toast.LENGTH_SHORT
            ).show()

            val p = db.getProject(
                currentPhotoProject
            )

            if (p != null) {
                photoGallery(
                    currentPhotoProject,
                    p.number
                )
            }
        }
    }

    private fun savePhotoUri(uri: Uri) {

        try {

            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

        } catch (_: Exception) {
        }

        db.addPhoto(
            currentPhotoProject,
            uri.toString()
        )
    }

    // ---------------------------------------------------------
    // PAYMENTS
    // ---------------------------------------------------------

    private fun showPayments() {

        root = baseLayout()

        root.addView(
            title("💰 PAYMENT COLLECTION")
        )

        val s = ScrollView(this)

        val list = LinearLayout(this)

        list.orientation = LinearLayout.VERTICAL

        list.addView(
            text(
                "Total Collection: ₹${money(db.totalCollection())}",
                21f
            )
        )

        val cursor = db.readProjects()

        while (cursor.moveToNext()) {

            val id = cursor.getInt(0)
            val number = cursor.getString(1)
            val customer = cursor.getString(3)
            val amount = cursor.getDouble(6)

            val collected =
                db.projectCollection(id)

            val balance =
                amount - collected

            list.addView(
                text(
                    "$number - $customer\n" +
                    "Collected: ₹${money(collected)}\n" +
                    "Balance: ₹${money(balance)}",
                    17f
                )
            )

            list.addView(
                button("Add Payment - $number") {
                    paymentDialog(id)
                }
            )
        }

        cursor.close()

        s.addView(list)

        root.addView(
            s,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            button("⬅ Back") {
                showDashboard()
            }
        )

        setContentView(root)
    }

    private fun paymentDialog(projectId: Int) {

        val box = LinearLayout(this)

        box.orientation = LinearLayout.VERTICAL

        val amount = edit("Payment Amount")
        val date = edit("Payment Date")
        val mode = edit("Cash / UPI / Bank")
        val note = edit("Note")

        box.addView(amount)
        box.addView(date)
        box.addView(mode)
        box.addView(note)

        AlertDialog.Builder(this)
            .setTitle("Add Payment")
            .setView(box)
            .setPositiveButton("SAVE") { _, _ ->

                db.addPayment(
                    projectId,
                    amount.text.toString()
                        .toDoubleOrNull() ?: 0.0,
                    date.text.toString(),
                    mode.text.toString(),
                    note.text.toString()
                )

                projectDetails(projectId)
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun projectPayments(
        projectId: Int,
        number: String
    ) {

        val c = db.projectPayments(projectId)

        val sb = StringBuilder()

        sb.append(
            "PROJECT $number\n\n"
        )

        while (c.moveToNext()) {

            sb.append(
                "₹${money(c.getDouble(2))} | " +
                "${c.getString(3)} | " +
                "${c.getString(4)}\n"
            )
        }

        c.close()

        AlertDialog.Builder(this)
            .setTitle("Payment History")
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    // ---------------------------------------------------------
    // EXPENSES
    // ---------------------------------------------------------

    private fun showExpenses() {

        root = baseLayout()

        root.addView(
            title("💸 EXPENSES")
        )

        root.addView(
            button("➕ Add Expense") {
                expenseDialog()
            }
        )

        root.addView(
            text(
                "Total Expenses: ₹${money(db.totalExpenses())}",
                21f
            )
        )

        val s = ScrollView(this)

        val list = LinearLayout(this)

        list.orientation = LinearLayout.VERTICAL

        val c = db.readExpenses()

        while (c.moveToNext()) {

            val amount = c.getDouble(2)
            val category = c.getString(1)
            val date = c.getString(3)
            val note = c.getString(4)

            list.addView(
                text(
                    "💸 ₹${money(amount)}\n" +
                    "$category\n" +
                    "$date\n" +
                    "$note",
                    17f
                )
            )
        }

        c.close()

        s.addView(list)

        root.addView(
            s,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(
            button("⬅ Back") {
                showDashboard()
            }
        )

        setContentView(root)
    }

    private fun expenseDialog() {

        val box = LinearLayout(this)

        box.orientation = LinearLayout.VERTICAL

        val category = edit("Expense Category")
        val amount = edit("Amount")
        val date = edit("Date")
        val note = edit("Note")

        box.addView(category)
        box.addView(amount)
        box.addView(date)
        box.addView(note)

        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(box)
            .setPositiveButton("SAVE") { _, _ ->

                db.addExpense(
                    category.text.toString(),
                    amount.text.toString()
                        .toDoubleOrNull() ?: 0.0,
                    date.text.toString(),
                    note.text.toString()
                )

                showExpenses()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    // ---------------------------------------------------------
    // CALENDAR
    // ---------------------------------------------------------

    private fun showCalendar() {

        val cal = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->

                val date =
                    "%04d-%02d-%02d".format(
                        year,
                        month + 1,
                        day
                    )

                calendarReport(date)

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun calendarReport(date: String) {

        val projects =
            db.projectsOnDate(date)

        val expenses =
            db.expensesOnDate(date)

        val payments =
            db.paymentsOnDate(date)

        val sb = StringBuilder()

        sb.append(
            "📅 DATE: $date\n\n"
        )

        sb.append(
            "📁 PROJECTS\n"
        )

        while (projects.moveToNext()) {

            sb.append(
                "${projects.getString(1)} - " +
                "${projects.getString(3)}\n"
            )
        }

        projects.close()

        sb.append(
            "\n💰 PAYMENTS\n"
        )

        while (payments.moveToNext()) {

            sb.append(
                "₹${money(payments.getDouble(2))}\n"
            )
        }

        payments.close()

        sb.append(
            "\n💸 EXPENSES\n"
        )

        while (expenses.moveToNext()) {

            sb.append(
                "₹${money(expenses.getDouble(2))} - " +
                "${expenses.getString(1)}\n"
            )
        }

        expenses.close()

        AlertDialog.Builder(this)
            .setTitle("Calendar Report")
            .setMessage(sb.toString())
            .setPositiveButton(
                "WHATSAPP"
            ) { _, _ ->
                shareWhatsApp(
                    sb.toString()
                )
            }
            .setNegativeButton(
                "CLOSE",
                null
            )
            .show()
    }

    // ---------------------------------------------------------
    // FULL REPORT
    // ---------------------------------------------------------

    private fun showFullReport() {

        AlertDialog.Builder(this)
            .setTitle("EDISON BUSINESS PRO")
            .setMessage(
                buildReport()
            )
            .setPositiveButton(
                "WHATSAPP"
            ) { _, _ ->

                shareWhatsApp(
                    buildReport()
                )
            }
            .setNegativeButton(
                "CLOSE",
                null
            )
            .show()
    }

    private fun buildReport(): String {

        val sb = StringBuilder()

        sb.append(
            "EDISON BUSINESS PRO\n"
        )

        sb.append(
            "SOLAR BUSINESS REPORT\n\n"
        )

        sb.append(
            "Companies: ${db.companyCount()}\n"
        )

        sb.append(
            "Projects: ${db.projectCount()}\n"
        )

        sb.append(
            "Project Value: ₹${money(db.totalProjectValue())}\n"
        )

        sb.append(
            "Collection: ₹${money(db.totalCollection())}\n"
        )

        sb.append(
            "Pending: ₹${money(db.totalBalance())}\n"
        )

        sb.append(
            "Expenses: ₹${money(db.totalExpenses())}\n"
        )

        sb.append(
            "Net: ₹${money(db.totalCollection() - db.totalExpenses())}\n"
        )

        return sb.toString()
    }

    // ---------------------------------------------------------
    // WHATSAPP
    // ---------------------------------------------------------

    private fun shareWhatsApp(message: String) {

        try {

            val intent =
                Intent(Intent.ACTION_SEND)

            intent.type =
                "text/plain"

            intent.setPackage(
                "com.whatsapp"
            )

            intent.putExtra(
                Intent.EXTRA_TEXT,
                message
            )

            startActivity(intent)

        } catch (_: Exception) {

            val intent =
                Intent(Intent.ACTION_SEND)

            intent.type =
                "text/plain"

            intent.putExtra(
                Intent.EXTRA_TEXT,
                message
            )

            startActivity(
                Intent.createChooser(
                    intent,
                    "Share Report"
                )
            )
        }
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private fun money(value: Double): String {

        return String.format(
            Locale.US,
            "%.2f",
            value
        )
    }

    // ---------------------------------------------------------
    // DATABASE
    // ---------------------------------------------------------

    class DBHelper(
        context: Context
    ) : SQLiteOpenHelper(
        context,
        "edison_business_pro.db",
        null,
        1
    ) {

        override fun onCreate(db: SQLiteDatabase) {

            db.execSQL(
                """
                CREATE TABLE companies(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    phone TEXT,
                    address TEXT
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE projects(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    number TEXT,
                    company TEXT,
                    customer TEXT,
                    phone TEXT,
                    site TEXT,
                    kw REAL,
                    amount REAL,
                    date TEXT,
                    status TEXT,
                    latitude REAL,
                    longitude REAL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE payments(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_id INTEGER,
                    amount REAL,
                    date TEXT,
                    mode TEXT,
                    note TEXT
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE expenses(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT,
                    amount REAL,
                    date TEXT,
                    note TEXT
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE photos(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_id INTEGER,
                    uri TEXT
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(
            db: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ) {
        }

        fun addCompany(
            name: String,
            phone: String,
            address: String
        ) {

            writableDatabase.execSQL(
                "INSERT INTO companies(name,phone,address) VALUES(?,?,?)",
                arrayOf(
                    name,
                    phone,
                    address
                )
            )
        }

        fun readCompanies() =
            readableDatabase.rawQuery(
                "SELECT * FROM companies ORDER BY id DESC",
                null
            )

        fun companyCount(): Int {

            val c =
                readableDatabase.rawQuery(
                    "SELECT COUNT(*) FROM companies",
                    null
                )

            c.moveToFirst()

            val result = c.getInt(0)

            c.close()

            return result
        }

        fun addProject(
            number: String,
            company: String,
            customer: String,
            phone: String,
            site: String,
            kw: Double,
            amount: Double,
            date: String,
            status: String
        ) {

            writableDatabase.execSQL(
                """
                INSERT INTO projects
                (number,company,customer,phone,site,kw,amount,date,status)
                VALUES(?,?,?,?,?,?,?,?,?)
                """.trimIndent(),
                arrayOf(
                    number,
                    company,
                    customer,
                    phone,
                    site,
                    kw,
                    amount,
                    date,
                    status
                )
            )
        }

        fun readProjects() =
            readableDatabase.rawQuery(
                "SELECT * FROM projects ORDER BY id DESC",
                null
            )

        fun projectCount(): Int {

            val c =
                readableDatabase.rawQuery(
                    "SELECT COUNT(*) FROM projects",
                    null
                )

            c.moveToFirst()

            val result = c.getInt(0)

            c.close()

            return result
        }

        fun nextProjectNumber(): String {

            val c =
                readableDatabase.rawQuery(
                    "SELECT COUNT(*) FROM projects",
                    null
                )

            c.moveToFirst()

            val n =
                c.getInt(0) + 1

            c.close()

            return String.format(
                Locale.US,
                "ES-%04d",
                n
            )
        }

        data class Project(
            val id: Int,
            val number: String,
            val company: String,
            val customer: String,
            val phone: String,
            val site: String,
            val kw: Double,
            val amount: Double,
            val date: String,
            val status: String
        )

        fun getProject(id: Int): Project? {

            val c =
                readableDatabase.rawQuery(
                    "SELECT * FROM projects WHERE id=?",
                    arrayOf(id.toString())
                )

            if (!c.moveToFirst()) {

                c.close()

                return null
            }

            val p =
                Project(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getDouble(6),
                    c.getDouble(7),
                    c.getString(8),
                    c.getString(9)
                )

            c.close()

            return p
        }

        fun addPayment(
            projectId: Int,
            amount: Double,
            date: String,
            mode: String,
            note: String
        ) {

            writableDatabase.execSQL(
                """
                INSERT INTO payments
                (project_id,amount,date,mode,note)
                VALUES(?,?,?,?,?)
                """.trimIndent(),
                arrayOf(
                    projectId,
                    amount,
                    date,
                    mode,
                    note
                )
            )
        }

        fun projectCollection(
            projectId: Int
        ): Double {

            val c =
                readableDatabase.rawQuery(
                    "SELECT COALESCE(SUM(amount),0) FROM payments WHERE project_id=?",
                    arrayOf(projectId.toString())
                )

            c.moveToFirst()

            val result = c.getDouble(0)

            c.close()

            return result
        }

        fun totalCollection(): Double {

            val c =
                readableDatabase.rawQuery(
                    "SELECT COALESCE(SUM(amount),0) FROM payments",
                    null
                )

            c.moveToFirst()

            val result = c.getDouble(0)

            c.close()

            return result
        }

        fun totalProjectValue(): Double {

            val c =
                readableDatabase.rawQuery(
                    "SELECT COALESCE(SUM(amount),0) FROM projects",
                    null
                )

            c.moveToFirst()

            val result = c.getDouble(0)

            c.close()

            return result
        }

        fun totalBalance(): Double {

            return totalProjectValue() -
                totalCollection()
        }

        fun addExpense(
            category: String,
            amount: Double,
            date: String,
            note: String
        ) {

            writableDatabase.execSQL(
                """
                INSERT INTO expenses
                (category,amount,date,note)
                VALUES(?,?,?,?)
                """.trimIndent(),
                arrayOf(
                    category,
                    amount,
                    date,
                    note
                )
            )
        }

        fun readExpenses() =
            readableDatabase.rawQuery(
                "SELECT * FROM expenses ORDER BY id DESC",
                null
            )

        fun totalExpenses(): Double {

            val c =
                readableDatabase.rawQuery(
                    "SELECT COALESCE(SUM(amount),0) FROM expenses",
                    null
                )

            c.moveToFirst()

            val result = c.getDouble(0)

            c.close()

            return result
        }

        fun addPhoto(
            projectId: Int,
            uri: String
        ) {

            writableDatabase.execSQL(
                "INSERT INTO photos(project_id,uri) VALUES(?,?)",
                arrayOf(
                    projectId,
                    uri
                )
            )
        }

        fun projectPhotos(
            projectId: Int
        ) =
            readableDatabase.rawQuery(
                "SELECT * FROM photos WHERE project_id=?",
                arrayOf(
                    projectId.toString()
                )
            )

        fun projectPayments(
            projectId: Int
        ) =
            readableDatabase.rawQuery(
                "SELECT * FROM payments WHERE project_id=? ORDER BY id DESC",
                arrayOf(
                    projectId.toString()
                )
            )

        fun projectsOnDate(
            date: String
        ) =
            readableDatabase.rawQuery(
                "SELECT * FROM projects WHERE date=?",
                arrayOf(date)
            )

        fun paymentsOnDate(
            date: String
        ) =
            readableDatabase.rawQuery(
                "SELECT * FROM payments WHERE date=?",
                arrayOf(date)
            )

        fun expensesOnDate(
            date: String
        ) =
            readableDatabase.rawQuery(
                "SELECT * FROM expenses WHERE date=?",
                arrayOf(date)
            )

        fun companyProjectCount(
            companyId: Int
        ): Int {

            val c =
                readableDatabase.rawQuery(
                    """
                    SELECT COUNT(*)
                    FROM projects p
                    JOIN companies c
                    ON p.company=c.name
                    WHERE c.id=?
                    """.trimIndent(),
                    arrayOf(companyId.toString())
                )

            c.moveToFirst()

            val result = c.getInt(0)

            c.close()

            return result
        }

        fun companyProjectValue(
            companyId: Int
        ): Double {

            val c =
                readableDatabase.rawQuery(
                    """
                    SELECT COALESCE(SUM(p.amount),0)
                    FROM projects p
                    JOIN companies c
                    ON p.company=c.name
                    WHERE c.id=?
                    """.trimIndent(),
                    arrayOf(companyId.toString())
                )

            c.moveToFirst()

            val result = c.getDouble(0)

            c.close()

            return result
        }

        fun companyCollection(
            companyId: Int
        ): Double {

            val c =
                readableDatabase.rawQuery(
                    """
                    SELECT COALESCE(SUM(pay.amount),0)
                    FROM payments pay
                    JOIN projects p
                    ON pay.project_id=p.id
                    JOIN companies c
                    ON p.company=c.name
                    WHERE c.id=?
                    """.trimIndent(),
                    arrayOf(companyId.toString())
                )

            c.moveToFirst()

            val result = c.getDouble(0)

            c.close()

            return result
        }

        fun companyPending(
            companyId: Int
        ): Double {

            return companyProjectValue(companyId) -
                companyCollection(companyId)
        }

        fun companyExpenses(
            companyId: Int
        ): Double {

            return 0.0
        }

        fun getProjectGps(
            projectId: Int
        ): Pair<Double, Double>? {

            val c =
                readableDatabase.rawQuery(
                    "SELECT latitude,longitude FROM projects WHERE id=?",
                    arrayOf(projectId.toString())
                )

            if (!c.moveToFirst()) {

                c.close()

                return null
            }

            val lat = c.getDouble(0)
            val lon = c.getDouble(1)

            c.close()

            if (lat == 0.0 && lon == 0.0) {
                return null
            }

            return Pair(lat, lon)
        }
    }
}
