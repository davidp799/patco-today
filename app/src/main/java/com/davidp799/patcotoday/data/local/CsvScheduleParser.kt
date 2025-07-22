package com.davidp799.patcotoday.data.local

import android.content.Context
import android.util.Log
import com.davidp799.patcotoday.utils.Arrival
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

class CsvScheduleParser(private val context: Context) {

    private val fileManager = FileManager(context)

    data class ScheduleEntry(
        val station: String,
        val arrivalTime: String,
        val departureTime: String
    )

    suspend fun parseScheduleForRoute(
        fromStation: String,
        toStation: String,
        date: String = getCurrentDate()
    ): List<Arrival> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("[ApiDebug]", "Parsing schedule for route: $fromStation -> $toStation on $date")

                // Check for special schedule first
                val specialSchedule = parseSpecialSchedule(fromStation, toStation, date)
                if (specialSchedule.isNotEmpty()) {
                    Log.d("[ApiDebug]", "Using special schedule - Found ${specialSchedule.size} arrivals")
                    return@withContext specialSchedule
                }

                Log.d("[ApiDebug]", "No special schedule found, falling back to regular schedule")
                // Fall back to regular schedule
                val regularSchedule = parseRegularSchedule(fromStation, toStation)
                Log.d("[ApiDebug]", "Regular schedule parsed - Found ${regularSchedule.size} arrivals")
                regularSchedule
            } catch (e: Exception) {
                Log.e("[ApiDebug]", "Exception parsing schedule for route $fromStation -> $toStation: ${e.message}", e)
                e.printStackTrace()
                emptyList<Arrival>()
            }
        }
    }

    private fun parseSpecialSchedule(fromStation: String, toStation: String, date: String): List<Arrival> {
        Log.d("[ApiDebug]", "Checking for special schedule - Date: $date")
        val direction = determineDirection(fromStation, toStation)
        val fileName = if (direction == "eastbound") {
            "special_schedule_eastbound.csv"
        } else {
            "special_schedule_westbound.csv"
        }

        Log.d("[ApiDebug]", "Looking for special schedule file: $fileName for direction: $direction")

        val specialFile = fileManager.getSpecialScheduleFile(date, fileName)
        return if (specialFile != null && specialFile.exists()) {
            Log.d("[ApiDebug]", "Special schedule file found, parsing arrivals")
            parseCsvFile(specialFile, fromStation, toStation)
        } else {
            Log.d("[ApiDebug]", "No special schedule file found")
            emptyList()
        }
    }

    private fun parseRegularSchedule(fromStation: String, toStation: String): List<Arrival> {
        val dayType = getCurrentDayType()
        val direction = determineDirection(fromStation, toStation)
        val fileName = "${dayType}_${direction}.csv"

        Log.d("[ApiDebug]", "Parsing regular schedule - Day type: $dayType, Direction: $direction, File: $fileName")

        val schedulesDir = File(context.filesDir, "schedules/regular")
        val file = File(schedulesDir, fileName)

        return if (file.exists()) {
            Log.d("[ApiDebug]", "Regular schedule file found, parsing arrivals from: ${file.absolutePath}")
            parseCsvFile(file, fromStation, toStation)
        } else {
            Log.w("[ApiDebug]", "Regular schedule file not found: ${file.absolutePath}")
            emptyList()
        }
    }

    private fun parseCsvFile(file: File, fromStation: String, toStation: String): List<Arrival> {
        val arrivals = mutableListOf<Arrival>()

        try {
            Log.d("[ApiDebug]", "Starting CSV parsing for file: ${file.name}")
            val reader = BufferedReader(FileReader(file))
            val lines = reader.readLines()
            reader.close()

            if (lines.isEmpty()) {
                Log.w("[ApiDebug]", "CSV file is empty: ${file.name}")
                return emptyList()
            }

            Log.d("[ApiDebug]", "CSV file has ${lines.size} lines")

            // Parse header to find station columns
            val header = lines[0].split(",").map { it.trim().replace("\"", "") }
            val fromStationIndex = findStationIndex(header, fromStation)
            val toStationIndex = findStationIndex(header, toStation)

            Log.d("[ApiDebug]", "Station indices - From '$fromStation': $fromStationIndex, To '$toStation': $toStationIndex")

            if (fromStationIndex == -1 || toStationIndex == -1) {
                Log.e("[ApiDebug]", "Could not find station columns in CSV header")
                return emptyList()
            }

            // Parse data rows
            var validArrivals = 0
            for (i in 1 until lines.size) {
                val row = lines[i].split(",").map { it.trim().replace("\"", "") }

                if (row.size > maxOf(fromStationIndex, toStationIndex)) {
                    val fromTime = row[fromStationIndex]
                    val toTime = row[toStationIndex]

                    if (fromTime.isNotEmpty() && toTime.isNotEmpty() &&
                        fromTime != "--" && toTime != "--") {

                        val travelTime = calculateTravelTime(fromTime, toTime)
                        arrivals.add(Arrival(
                            arrivalTime = formatTime(fromTime),
                            travelTime = travelTime
                        ))
                        validArrivals++
                    }
                }
            }

            Log.d("[ApiDebug]", "Parsed $validArrivals valid arrivals from ${lines.size - 1} data rows")

            // Filter future arrivals and sort by time
            val filteredArrivals = filterAndSortArrivals(arrivals)
            Log.d("[ApiDebug]", "After filtering for future arrivals: ${filteredArrivals.size} arrivals remaining")

            return filteredArrivals

        } catch (e: Exception) {
            Log.e("[ApiDebug]", "Exception parsing CSV file ${file.name}: ${e.message}", e)
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun findStationIndex(header: List<String>, stationName: String): Int {
        // Try exact match first
        var index = header.indexOf(stationName)
        if (index != -1) return index

        // Try normalized station name matching
        val normalizedStation = normalizeStationName(stationName)
        for (i in header.indices) {
            if (normalizeStationName(header[i]) == normalizedStation) {
                return i
            }
        }

        return -1
    }

    private fun normalizeStationName(name: String): String {
        return name.lowercase()
            .replace("–", "-")
            .replace("&", "and")
            .replace(" ", "")
            .replace("-", "")
    }

    private fun determineDirection(fromStation: String, toStation: String): String {
        val stations = listOf(
            "Lindenwold", "Ashland", "Woodcrest", "Haddonfield", "Westmont",
            "Collingswood", "Ferry Avenue", "Broadway", "City Hall",
            "Franklin Square", "8th & Market", "9–10th & Locust",
            "12–13th & Locust", "15–16th & Locust"
        )

        val fromIndex = stations.indexOf(fromStation)
        val toIndex = stations.indexOf(toStation)

        return if (fromIndex < toIndex) "eastbound" else "westbound"
    }

    private fun getCurrentDayType(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "saturdays"
            Calendar.SUNDAY -> "sundays"
            else -> "weekdays"
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun formatTime(time: String): String {
        // Handle different time formats that might be in the CSV
        return try {
            // If time is already in HH:mm format
            if (time.contains(":") && time.length <= 5) {
                val parts = time.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()

                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour

                String.format("%d:%02d %s", displayHour, minute, amPm)
            } else {
                // If time is in other format, try to parse and convert
                time
            }
        } catch (e: Exception) {
            time // Return original if parsing fails
        }
    }

    private fun calculateTravelTime(fromTime: String, toTime: String): String {
        return try {
            val fromMinutes = parseTimeToMinutes(fromTime)
            val toMinutes = parseTimeToMinutes(toTime)

            val travelMinutes = if (toMinutes >= fromMinutes) {
                toMinutes - fromMinutes
            } else {
                // Handle next day arrival
                (24 * 60 + toMinutes) - fromMinutes
            }

            Log.d("[ApiDebug]", "Calculated travel time: $fromTime -> $toTime = $travelMinutes min")
            "$travelMinutes min"
        } catch (e: Exception) {
            Log.e("[ApiDebug]", "Error calculating travel time from $fromTime to $toTime: ${e.message}")
            "-- min"
        }
    }

    private fun parseTimeToMinutes(time: String): Int {
        val cleanTime = time.replace("\"", "").trim()
        val parts = cleanTime.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun filterAndSortArrivals(arrivals: List<Arrival>): List<Arrival> {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        Log.d("[ApiDebug]", "Filtering arrivals - Current time: $currentHour:$currentMinute ($currentTotalMinutes minutes)")

        val futureArrivals = arrivals.filter { arrival ->
            try {
                val arrivalTime = arrival.arrivalTime
                    .replace(" AM", "")
                    .replace(" PM", "")

                val parts = arrivalTime.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()

                // Convert to 24-hour format
                val hour24 = if (arrival.arrivalTime.contains("PM") && hour != 12) {
                    hour + 12
                } else if (arrival.arrivalTime.contains("AM") && hour == 12) {
                    0
                } else {
                    hour
                }

                val arrivalTotalMinutes = hour24 * 60 + minute

                // Show arrivals from now onwards
                arrivalTotalMinutes >= currentTotalMinutes
            } catch (e: Exception) {
                Log.e("[ApiDebug]", "Error filtering arrival time ${arrival.arrivalTime}: ${e.message}")
                false
            }
        }.sortedBy { arrival ->
            try {
                val arrivalTime = arrival.arrivalTime
                    .replace(" AM", "")
                    .replace(" PM", "")
                val parts = arrivalTime.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                val hour24 = if (arrival.arrivalTime.contains("PM") && hour != 12) {
                    hour + 12
                } else if (arrival.arrivalTime.contains("AM") && hour == 12) {
                    0
                } else {
                    hour
                }
                hour24 * 60 + minute
            } catch (e: Exception) {
                Log.e("[ApiDebug]", "Error sorting arrival time ${arrival.arrivalTime}: ${e.message}")
                0
            }
        }

        Log.d("[ApiDebug]", "Filtered and sorted arrivals: ${futureArrivals.size} future arrivals")
        return futureArrivals
    }
}
