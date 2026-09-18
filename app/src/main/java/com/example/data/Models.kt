package com.example.data

enum class AttendanceStatus(val label: String, val shortLabel: String) {
    PRESENT("Present", "P"),
    ABSENT("Absent", "A"),
    NOT_APPLICABLE("N/A", "N/A")
}

data class Subject(
    val code: String,
    val shortName: String,
    val fullName: String,
    val credits: Int,
    val faculty: String,
    val isLab: Boolean = false,
    val defaultRoom: String = "Room 201"
)

data class RoutineClass(
    val dayOfWeek: String, // "Mon", "Tue", "Wed", "Thu", "Fri"
    val startTime: String, // "09:00"
    val endTime: String,   // "10:00"
    val code: String,
    val shortName: String,
    val fullName: String,
    val faculty: String,
    val room: String,
    val type: String // "theory", "lab", "break", "library", "meeting"
) {
    val isMarkable: Boolean
        get() = type != "break" && type != "library" && type != "meeting"
}

data class User(
    val regNo: String,
    val name: String,
    val password: String = "password123",
    val branch: String = "B.Tech CSE",
    val semester: String = "Semester-I (Section A)",
    val rollNo: String = "01",
    val room: String = "Room 201",
    val university: String = "Usha Martin University"
)

data class AttendanceRecord(
    val id: String, // e.g., "2026-09-18_GSC101_0900"
    val userRegNo: String,
    val date: String, // "YYYY-MM-DD"
    val classCode: String,
    val timeSlot: String,
    val status: AttendanceStatus,
    val isLocked: Boolean = true, // One-time confirmation lock
    val timestamp: Long = System.currentTimeMillis(),
    val locationVerified: Boolean = true,
    val distanceToCampusMeters: Float = 0f
)

data class ChatMessage(
    val id: String,
    val userRegNo: String,
    val authorName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFacultyNotice: Boolean = false,
    val subjectCode: String? = null
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "GENERAL", // "TIME_WINDOW", "LOCATION", "CLASS_ALERT", "NOTICE"
    val isRead: Boolean = false
)

object UmuConstants {
    const val UNIVERSITY_NAME = "USHA MARTIN UNIVERSITY"
    const val FACULTY_NAME = "Faculty of Engineering & Applied Sciences"
    const val PROGRAM_NAME = "B.Tech Computer Science Engineering (2026-2030)"
    const val SEMESTER = "Semester-I (Section A)"
    const val ROOM = "Baitarani Block - Room 201"
    const val NAAC_GRADE = "NAAC GRADE A"

    // Usha Martin University Coordinates (Angara, Ranchi, Jharkhand)
    const val UMU_LATITUDE = 23.3850
    const val UMU_LONGITUDE = 85.5562
    const val MAX_CAMPUS_RADIUS_METERS = 1000f // 1 km radius for campus geofence

    // Official Attendance Allowed Window: 9:00 AM to 04:29 PM
    const val WINDOW_START_HOUR = 9
    const val WINDOW_START_MINUTE = 0
    const val WINDOW_END_HOUR = 16
    const val WINDOW_END_MINUTE = 29

    val ALL_SUBJECTS = listOf(
        Subject("GSC101", "PHY", "Physics", 3, "Dr. Nanda Shakti (NS)", isLab = false),
        Subject("GSC102", "Maths-I", "Mathematics-I", 4, "Dr. Amit Kumar (AK)", isLab = false),
        Subject("GSC103", "BOE", "Biology for Engineers", 3, "Dr. Bhavana Sharma (BS)", isLab = false),
        Subject("GEC101", "BEE", "Basic Electrical Engineering", 3, "Ms. Ankita Rani (AR)", isLab = false),
        Subject("GEC102", "EGD", "Engineering Graphics & Design", 1, "Dr Lalan Kumar (LK)", isLab = false),
        Subject("HSC102", "G&HR", "Gender & Human Rights", 2, "Mr. Shashank Kashyap (SK)", isLab = false),
        Subject("MDC101", "ILW (T)", "Idea Lab Workshop (Theory)", 1, "Ms. Ankita Rani (AR)", isLab = false),
        Subject("GSC111", "PHY Lab", "Physics Lab", 1, "Dr. Nanda Shakti (NS)", isLab = true, defaultRoom = "Physics Lab"),
        Subject("GEC111", "BEE Lab", "Basic Electrical Engineering Lab", 1, "Ms. Ankita Rani (AR)", isLab = true, defaultRoom = "BEE Lab"),
        Subject("GEC112", "EGD Lab", "Engineering Graphics & Design Lab", 2, "Dr Lalan Kumar (LK)", isLab = true, defaultRoom = "Drawing Hall"),
        Subject("GEC113", "DT", "Design Thinking", 1, "Dr. Mohan Prakash (MP)", isLab = false),
        Subject("MDC111", "ILW (P)", "Idea Lab Workshop (Practical)", 2, "Ms. Ankita Rani (AR)", isLab = true, defaultRoom = "Idea Lab")
    )

    val WEEKLY_ROUTINE: Map<String, List<RoutineClass>> = mapOf(
        "Mon" to listOf(
            RoutineClass("Mon", "09:00", "11:00", "GEC111", "BEE Lab (AR)", "Basic Electrical Engineering Lab", "Ms. Ankita Rani (AR)", "BEE Lab", "lab"),
            RoutineClass("Mon", "11:00", "12:00", "GSC102", "Maths-I (AK)", "Mathematics-I", "Dr. Amit Kumar (AK)", "Room 201", "theory"),
            RoutineClass("Mon", "12:00", "12:30", "BREAK", "LUNCH", "Lunch Break", "-", "Cafeteria", "break"),
            RoutineClass("Mon", "12:30", "16:30", "MDC111", "ILW (P)", "Idea Lab Workshop (Practical)", "Ms. Ankita Rani (AR)", "Idea Lab", "lab")
        ),
        "Tue" to listOf(
            RoutineClass("Tue", "09:00", "10:00", "GEC113", "DT (MP)", "Design Thinking", "Dr. Mohan Prakash (MP)", "Room 201", "theory"),
            RoutineClass("Tue", "10:00", "11:00", "GEC101", "BEE (AR)", "Basic Electrical Engineering", "Ms. Ankita Rani (AR)", "Room 201", "theory"),
            RoutineClass("Tue", "11:00", "12:00", "GSC102", "Maths-I (AK)", "Mathematics-I", "Dr. Amit Kumar (AK)", "Room 201", "theory"),
            RoutineClass("Tue", "12:00", "12:30", "BREAK", "LUNCH", "Lunch Break", "-", "Cafeteria", "break"),
            RoutineClass("Tue", "12:30", "13:30", "GSC101", "PHY (NS)", "Physics", "Dr. Nanda Shakti (NS)", "Room 201", "theory"),
            RoutineClass("Tue", "13:30", "14:30", "LIB", "Library", "Library Session", "Library Staff", "Central Library", "library"),
            RoutineClass("Tue", "14:30", "15:30", "HSC102", "G&HR (SK)", "Gender & Human Rights", "Mr. Shashank Kashyap (SK)", "Room 201", "theory"),
            RoutineClass("Tue", "15:30", "16:30", "GEC102", "EGD (LK)", "Engineering Graphics & Design", "Dr Lalan Kumar (LK)", "Room 201", "theory")
        ),
        "Wed" to listOf(
            RoutineClass("Wed", "09:00", "10:00", "MDC101", "ILW (T)", "Idea Lab Workshop (Theory)", "Ms. Ankita Rani (AR)", "Room 201", "theory"),
            RoutineClass("Wed", "10:00", "11:00", "GSC101", "PHY (NS)", "Physics", "Dr. Nanda Shakti (NS)", "Room 201", "theory"),
            RoutineClass("Wed", "11:00", "12:00", "GEC101", "BEE (AR)", "Basic Electrical Engineering", "Ms. Ankita Rani (AR)", "Room 201", "theory"),
            RoutineClass("Wed", "12:00", "12:30", "BREAK", "LUNCH", "Lunch Break", "-", "Cafeteria", "break"),
            RoutineClass("Wed", "12:30", "13:30", "GSC102", "Maths-I (AK)", "Mathematics-I", "Dr. Amit Kumar (AK)", "Room 201", "theory"),
            RoutineClass("Wed", "13:30", "14:30", "LIB", "Library", "Library Session", "Library Staff", "Central Library", "library"),
            RoutineClass("Wed", "14:30", "15:30", "HSC102", "G&HR (SK)", "Gender & Human Rights", "Mr. Shashank Kashyap (SK)", "Room 201", "theory"),
            RoutineClass("Wed", "15:30", "16:30", "GSC103", "BOE (BS)", "Biology for Engineers", "Dr. Bhavana Sharma (BS)", "Room 201", "theory")
        ),
        "Thu" to listOf(
            RoutineClass("Thu", "09:00", "10:00", "GEC101", "BEE (AR)", "Basic Electrical Engineering", "Ms. Ankita Rani (AR)", "Room 201", "theory"),
            RoutineClass("Thu", "10:00", "11:00", "GSC101", "PHY (NS)", "Physics", "Dr. Nanda Shakti (NS)", "Room 201", "theory"),
            RoutineClass("Thu", "11:00", "12:00", "GSC102", "Maths-I (AK)", "Mathematics-I", "Dr. Amit Kumar (AK)", "Room 201", "theory"),
            RoutineClass("Thu", "12:00", "12:30", "BREAK", "LUNCH", "Lunch Break", "-", "Cafeteria", "break"),
            RoutineClass("Thu", "12:30", "13:30", "GEC113", "DT (MP)", "Design Thinking", "Dr. Mohan Prakash (MP)", "Room 201", "theory"),
            RoutineClass("Thu", "13:30", "15:30", "GSC111", "PHY Lab", "Physics Lab", "Dr. Nanda Shakti (NS)", "Physics Lab", "lab"),
            RoutineClass("Thu", "15:30", "16:30", "GSC103", "BOE (BS)", "Biology for Engineers", "Dr. Bhavana Sharma (BS)", "Room 201", "theory")
        ),
        "Fri" to listOf(
            RoutineClass("Fri", "09:00", "10:00", "GSC103", "BOE (BS)", "Biology for Engineers", "Dr. Bhavana Sharma (BS)", "Room 201", "theory"),
            RoutineClass("Fri", "10:00", "12:00", "GEC112", "EGD Lab", "Engineering Graphics & Design Lab", "Dr Lalan Kumar (LK)", "Drawing Hall", "lab"),
            RoutineClass("Fri", "12:00", "12:30", "BREAK", "LUNCH", "Lunch Break", "-", "Cafeteria", "break"),
            RoutineClass("Fri", "12:30", "14:30", "GEC112", "EGD Lab", "Engineering Graphics & Design Lab", "Dr Lalan Kumar (LK)", "Drawing Hall", "lab"),
            RoutineClass("Fri", "14:30", "16:30", "MENTOR", "Mentor-Mentee", "Mentor - Mentee Meeting", "Faculty Mentors", "Room 201", "meeting")
        )
    )
}
