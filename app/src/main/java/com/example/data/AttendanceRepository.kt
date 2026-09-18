package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class AttendanceStats(
    val overallPercentage: Int,
    val totalAttended: Int,
    val totalMissed: Int,
    val totalConducted: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val isSafeZone: Boolean, // >= 75%
    val classesNeededFor75: Int,
    val classesCanSkip: Int
)

class AttendanceRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("umu_attendance_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: AttendanceRepository? = null

        fun getInstance(context: Context): AttendanceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AttendanceRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        val DEMO_USER = User(
            regNo = "UMU/2026/CSE/001",
            name = "Aman Sharma",
            password = "password123",
            branch = "B.Tech CSE",
            semester = "Semester-I (Section A)",
            rollNo = "CSE-01",
            room = "Baitarani Block - Room 201"
        )
    }

    init {
        // Initialize default user if none exists
        if (getUsers().isEmpty()) {
            saveUser(DEMO_USER)
            setCurrentUser(DEMO_USER)
        }
        // Initialize sample announcements if empty
        if (getMessages().isEmpty()) {
            initSampleMessages()
        }
        if (getNotifications().isEmpty()) {
            initSampleNotifications()
        }
    }

    // --- User Auth ---

    fun getCurrentUser(): User? {
        val json = prefs.getString("current_user_json", null) ?: return null
        return try {
            val obj = JSONObject(json)
            User(
                regNo = obj.getString("regNo"),
                name = obj.getString("name"),
                password = obj.optString("password", "password123"),
                branch = obj.optString("branch", "B.Tech CSE"),
                semester = obj.optString("semester", "Semester-I (Section A)"),
                rollNo = obj.optString("rollNo", "01"),
                room = obj.optString("room", "Room 201")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun setCurrentUser(user: User?) {
        if (user == null) {
            prefs.edit().remove("current_user_json").apply()
        } else {
            val obj = JSONObject().apply {
                put("regNo", user.regNo)
                put("name", user.name)
                put("password", user.password)
                put("branch", user.branch)
                put("semester", user.semester)
                put("rollNo", user.rollNo)
                put("room", user.room)
            }
            prefs.edit().putString("current_user_json", obj.toString()).apply()
        }
    }

    fun getUsers(): Map<String, User> {
        val json = prefs.getString("all_users_json", null) ?: return emptyMap()
        val result = mutableMapOf<String, User>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val u = User(
                    regNo = obj.getString("regNo"),
                    name = obj.getString("name"),
                    password = obj.optString("password", "password123"),
                    branch = obj.optString("branch", "B.Tech CSE"),
                    semester = obj.optString("semester", "Semester-I (Section A)"),
                    rollNo = obj.optString("rollNo", "01"),
                    room = obj.optString("room", "Room 201")
                )
                result[u.regNo.uppercase()] = u
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun saveUser(user: User) {
        val users = getUsers().toMutableMap()
        users[user.regNo.uppercase()] = user
        val arr = JSONArray()
        users.values.forEach { u ->
            val obj = JSONObject().apply {
                put("regNo", u.regNo)
                put("name", u.name)
                put("password", u.password)
                put("branch", u.branch)
                put("semester", u.semester)
                put("rollNo", u.rollNo)
                put("room", u.room)
            }
            arr.put(obj)
        }
        prefs.edit().putString("all_users_json", arr.toString()).apply()
    }

    fun login(regNo: String, pass: String): Result<User> {
        val cleanReg = regNo.trim().uppercase()
        if (cleanReg.isEmpty()) return Result.failure(Exception("Registration number cannot be empty"))
        val users = getUsers()
        val user = users[cleanReg] ?: return Result.failure(Exception("Student not registered. Please Sign Up."))
        if (user.password != pass) return Result.failure(Exception("Incorrect password. Please try again."))
        setCurrentUser(user)
        return Result.success(user)
    }

    fun signUp(regNo: String, name: String, pass: String, branch: String, rollNo: String): Result<User> {
        val cleanReg = regNo.trim().uppercase()
        if (cleanReg.isEmpty()) return Result.failure(Exception("Registration number is required"))
        if (name.isBlank()) return Result.failure(Exception("Student name is required"))
        if (pass.length < 4) return Result.failure(Exception("Password must be at least 4 characters"))

        val users = getUsers()
        if (users.containsKey(cleanReg)) {
            return Result.failure(Exception("Student already registered with this Reg No."))
        }

        val newUser = User(
            regNo = cleanReg,
            name = name.trim(),
            password = pass,
            branch = branch.trim().ifEmpty { "B.Tech CSE" },
            rollNo = rollNo.trim().ifEmpty { cleanReg.split("/").lastOrNull() ?: "01" }
        )
        saveUser(newUser)
        setCurrentUser(newUser)
        return Result.success(newUser)
    }

    fun logout() {
        setCurrentUser(null)
    }

    // --- Attendance Operations ---

    private fun getAttendanceKey(userRegNo: String): String =
        "attendance_${userRegNo.replace("/", "_").uppercase()}"

    fun getAttendanceForDate(date: String, userRegNo: String): Map<String, AttendanceRecord> {
        val all = getAllAttendance(userRegNo)
        return all.filter { it.date == date }.associateBy { "${it.classCode}_${it.timeSlot}" }
    }

    fun getAllAttendance(userRegNo: String): List<AttendanceRecord> {
        val raw = prefs.getString(getAttendanceKey(userRegNo), null) ?: return emptyList()
        val list = mutableListOf<AttendanceRecord>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    AttendanceRecord(
                        id = o.getString("id"),
                        userRegNo = o.getString("userRegNo"),
                        date = o.getString("date"),
                        classCode = o.getString("classCode"),
                        timeSlot = o.getString("timeSlot"),
                        status = AttendanceStatus.valueOf(o.getString("status")),
                        isLocked = o.optBoolean("isLocked", true),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                        locationVerified = o.optBoolean("locationVerified", true),
                        distanceToCampusMeters = o.optDouble("distanceToCampusMeters", 0.0).toFloat()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * Mark Attendance with One-Time Confirmation enforcement and non-editable lock
     */
    fun markAttendance(
        userRegNo: String,
        date: String,
        classCode: String,
        timeSlot: String,
        status: AttendanceStatus,
        locationVerified: Boolean,
        distanceMeters: Float
    ): Result<AttendanceRecord> {
        val existing = getAttendanceForDate(date, userRegNo)["${classCode}_${timeSlot}"]
        if (existing != null && existing.isLocked) {
            return Result.failure(Exception("Attendance is already locked and cannot be edited."))
        }

        // Rule: If location is NOT verified (outside campus), attendance cannot be marked Present!
        if (status == AttendanceStatus.PRESENT && !locationVerified) {
            // Auto mark absent as per requirement
            val record = AttendanceRecord(
                id = "${date}_${classCode}_${timeSlot.replace(":", "")}",
                userRegNo = userRegNo,
                date = date,
                classCode = classCode,
                timeSlot = timeSlot,
                status = AttendanceStatus.ABSENT,
                isLocked = true,
                timestamp = System.currentTimeMillis(),
                locationVerified = false,
                distanceToCampusMeters = distanceMeters
            )
            saveAttendanceRecord(userRegNo, record)
            return Result.success(record)
        }

        val record = AttendanceRecord(
            id = "${date}_${classCode}_${timeSlot.replace(":", "")}",
            userRegNo = userRegNo,
            date = date,
            classCode = classCode,
            timeSlot = timeSlot,
            status = status,
            isLocked = true, // Permanently locked once confirmed
            timestamp = System.currentTimeMillis(),
            locationVerified = locationVerified,
            distanceToCampusMeters = distanceMeters
        )

        saveAttendanceRecord(userRegNo, record)
        return Result.success(record)
    }

    private fun saveAttendanceRecord(userRegNo: String, record: AttendanceRecord) {
        val all = getAllAttendance(userRegNo).toMutableList()
        val index = all.indexOfFirst { it.date == record.date && it.classCode == record.classCode && it.timeSlot == record.timeSlot }
        if (index >= 0) {
            all[index] = record
        } else {
            all.add(record)
        }

        val arr = JSONArray()
        all.forEach { r ->
            val o = JSONObject().apply {
                put("id", r.id)
                put("userRegNo", r.userRegNo)
                put("date", r.date)
                put("classCode", r.classCode)
                put("timeSlot", r.timeSlot)
                put("status", r.status.name)
                put("isLocked", r.isLocked)
                put("timestamp", r.timestamp)
                put("locationVerified", r.locationVerified)
                put("distanceToCampusMeters", r.distanceToCampusMeters.toDouble())
            }
            arr.put(o)
        }
        prefs.edit().putString(getAttendanceKey(userRegNo), arr.toString()).apply()
    }

    fun clearAllAttendance(userRegNo: String) {
        prefs.edit().remove(getAttendanceKey(userRegNo)).apply()
    }

    // --- Statistics & Streaks ---

    fun calculateStats(userRegNo: String): AttendanceStats {
        val all = getAllAttendance(userRegNo)
        var totalP = 0
        var totalA = 0

        val dateGroups = all.groupBy { it.date }
        all.forEach { r ->
            when (r.status) {
                AttendanceStatus.PRESENT -> totalP++
                AttendanceStatus.ABSENT -> totalA++
                AttendanceStatus.NOT_APPLICABLE -> {}
            }
        }

        val total = totalP + totalA
        val pct = if (total > 0) (totalP * 100) / total else 0

        // Calculate streaks (consecutive days with at least 1 Present and 0 Absences)
        val sortedDates = dateGroups.keys.sorted()
        var curStreak = 0
        var bestStreak = 0

        for (d in sortedDates) {
            val records = dateGroups[d] ?: continue
            val hasP = records.any { it.status == AttendanceStatus.PRESENT }
            val hasA = records.any { it.status == AttendanceStatus.ABSENT }

            if (hasP && !hasA) {
                curStreak++
                if (curStreak > bestStreak) bestStreak = curStreak
            } else if (hasA) {
                curStreak = 0
            }
        }

        val isSafe = pct >= 75 || total == 0
        val neededFor75 = if (pct < 75 && total > 0) {
            // (P + X) / (Total + X) >= 0.75  =>  P + X >= 0.75 * Total + 0.75 * X => 0.25 * X >= 0.75 * Total - P
            val needed = Math.ceil((0.75 * total - totalP) / 0.25).toInt()
            if (needed < 1) 1 else needed
        } else 0

        val canSkip = if (pct >= 75 && total > 0) {
            // P / (Total + Y) >= 0.75 => P >= 0.75 * Total + 0.75 * Y => 0.75 * Y <= P - 0.75 * Total
            Math.floor((totalP - 0.75 * total) / 0.75).toInt().coerceAtLeast(0)
        } else 0

        return AttendanceStats(
            overallPercentage = pct,
            totalAttended = totalP,
            totalMissed = totalA,
            totalConducted = total,
            currentStreak = curStreak,
            bestStreak = bestStreak,
            isSafeZone = isSafe,
            classesNeededFor75 = neededFor75,
            classesCanSkip = canSkip
        )
    }

    fun getSubjectWiseStats(userRegNo: String): Map<String, Pair<Int, Int>> {
        val all = getAllAttendance(userRegNo)
        val map = mutableMapOf<String, Pair<Int, Int>>() // code -> (attended, total)

        UmuConstants.ALL_SUBJECTS.forEach { s ->
            val records = all.filter { it.classCode == s.code }
            val p = records.count { it.status == AttendanceStatus.PRESENT }
            val a = records.count { it.status == AttendanceStatus.ABSENT }
            map[s.code] = Pair(p, p + a)
        }
        return map
    }

    // --- Real-time Chat / Announcements ---

    fun getMessages(): List<ChatMessage> {
        val raw = prefs.getString("chat_messages_json", null) ?: return emptyList()
        val list = mutableListOf<ChatMessage>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = o.getString("id"),
                        userRegNo = o.getString("userRegNo"),
                        authorName = o.getString("authorName"),
                        text = o.getString("text"),
                        timestamp = o.getLong("timestamp"),
                        isFacultyNotice = o.optBoolean("isFacultyNotice", false),
                        subjectCode = if (o.has("subjectCode")) o.getString("subjectCode") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedBy { it.timestamp }
    }

    fun sendMessage(user: User, text: String, isNotice: Boolean = false, subjectCode: String? = null): ChatMessage {
        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            userRegNo = user.regNo,
            authorName = user.name,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFacultyNotice = isNotice,
            subjectCode = subjectCode
        )

        val all = getMessages().toMutableList()
        all.add(msg)

        val arr = JSONArray()
        all.forEach { m ->
            val o = JSONObject().apply {
                put("id", m.id)
                put("userRegNo", m.userRegNo)
                put("authorName", m.authorName)
                put("text", m.text)
                put("timestamp", m.timestamp)
                put("isFacultyNotice", m.isFacultyNotice)
                m.subjectCode?.let { put("subjectCode", it) }
            }
            arr.put(o)
        }
        prefs.edit().putString("chat_messages_json", arr.toString()).apply()
        return msg
    }

    private fun initSampleMessages() {
        val samples = listOf(
            ChatMessage(
                id = "msg_1",
                userRegNo = "FACULTY/NS",
                authorName = "Dr. Nanda Shakti (Physics)",
                text = "📢 Notice: Physics Lab manuals for Semester-I have been issued. Please collect them from Baitarani Block Room 104.",
                timestamp = System.currentTimeMillis() - 86400000 * 2,
                isFacultyNotice = true,
                subjectCode = "GSC101"
            ),
            ChatMessage(
                id = "msg_2",
                userRegNo = "FACULTY/AK",
                authorName = "Dr. Amit Kumar (Maths)",
                text = "📢 Maths-I Assignment 1 on Linear Algebra is due next Monday. Attendance is mandatory for the tutorial session.",
                timestamp = System.currentTimeMillis() - 86400000,
                isFacultyNotice = true,
                subjectCode = "GSC102"
            ),
            ChatMessage(
                id = "msg_3",
                userRegNo = "UMU/2026/CSE/001",
                authorName = "Aman Sharma",
                text = "Hello everyone! Does anyone have the notes for Basic Electrical Engineering Tuesday lecture?",
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                isFacultyNotice = false
            ),
            ChatMessage(
                id = "msg_4",
                userRegNo = "UMU/2026/CSE/014",
                authorName = "Priya Singh",
                text = "Yes Aman, I will share the PDF notes in class during the lunch break!",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                isFacultyNotice = false
            )
        )

        val arr = JSONArray()
        samples.forEach { m ->
            val o = JSONObject().apply {
                put("id", m.id)
                put("userRegNo", m.userRegNo)
                put("authorName", m.authorName)
                put("text", m.text)
                put("timestamp", m.timestamp)
                put("isFacultyNotice", m.isFacultyNotice)
                m.subjectCode?.let { put("subjectCode", it) }
            }
            arr.put(o)
        }
        prefs.edit().putString("chat_messages_json", arr.toString()).apply()
    }

    // --- Notifications ---

    fun getNotifications(): List<AppNotification> {
        val raw = prefs.getString("notifications_json", null) ?: return emptyList()
        val list = mutableListOf<AppNotification>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    AppNotification(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        message = o.getString("message"),
                        timestamp = o.getLong("timestamp"),
                        type = o.optString("type", "GENERAL"),
                        isRead = o.optBoolean("isRead", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun addNotification(title: String, message: String, type: String): AppNotification {
        val notif = AppNotification(
            id = "notif_${System.currentTimeMillis()}",
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type,
            isRead = false
        )
        val all = getNotifications().toMutableList()
        all.add(0, notif)

        val arr = JSONArray()
        all.take(20).forEach { n ->
            val o = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("message", n.message)
                put("timestamp", n.timestamp)
                put("type", n.type)
                put("isRead", n.isRead)
            }
            arr.put(o)
        }
        prefs.edit().putString("notifications_json", arr.toString()).apply()
        return notif
    }

    private fun initSampleNotifications() {
        val samples = listOf(
            AppNotification(
                id = "n_1",
                title = "⏰ Attendance Window Active",
                message = "Attendance window is open from 9:00 AM to 04:29 PM. Mark your classes today!",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                type = "TIME_WINDOW",
                isRead = false
            ),
            AppNotification(
                id = "n_2",
                title = "📍 UMU Campus Geofence Active",
                message = "Geofence enabled: Attendance requires physical presence at Usha Martin University Angara campus.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                type = "LOCATION",
                isRead = true
            ),
            AppNotification(
                id = "n_3",
                title = "📊 Minimum 75% Criteria",
                message = "UMU Academic Council requires a minimum of 75% attendance for end-semester exams.",
                timestamp = System.currentTimeMillis() - 86400000,
                type = "GENERAL",
                isRead = true
            )
        )
        val arr = JSONArray()
        samples.forEach { n ->
            val o = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("message", n.message)
                put("timestamp", n.timestamp)
                put("type", n.type)
                put("isRead", n.isRead)
            }
            arr.put(o)
        }
        prefs.edit().putString("notifications_json", arr.toString()).apply()
    }
}
