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

    suspend fun parseScheduleForRoute(
        fromStation: String,
        toStation: String,
        date: String = getCurrentDate()
    ): List<Arrival> {
        return withContext(Dispatchers.IO) {
            try {
                // Check for special schedule first
                val specialSchedule = parseSpecialSchedule(fromStation, toStation, date)
                if (specialSchedule.isNotEmpty()) {
                    return@withContext specialSchedule
                }

                // Fall back to regular schedule
                val regularSchedule = parseRegularSchedule(fromStation, toStation)
                regularSchedule
            } catch (e: Exception) {
                Log.e("[CsvScheduleParser]", "Exception parsing schedule for route $fromStation -> $toStation: ${e.message}", e)
                e.printStackTrace()
                emptyList()
            }
        }
    }

    /**
     * Parse schedule from assets folder as fallback when API fails and no local files exist
     */
    suspend fun parseScheduleFromAssets(
        fromStation: String,
        toStation: String
    ): List<Arrival> {
        return withContext(Dispatchers.IO) {
            try {
                val dayType = getCurrentDayType()
                val direction = determineDirection(fromStation, toStation)
                val fileName = "${dayType}-${direction}.csv"
                val arrivals = parseCsvFromAssets(fileName, fromStation, toStation)
                arrivals
            } catch (e: Exception) {
                Log.e("[parseScheduleFromAssets]", "Exception parsing schedule from assets for route $fromStation -> $toStation: ${e.message}", e)
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun parseSpecialSchedule(fromStation: String, toStation: String, date: String): List<Arrival> {
        val direction = determineDirection(fromStation, toStation)
        val fileName = if (direction == "east") {
            "special_schedule_eastbound.csv"
        } else {
            "special_schedule_westbound.csv"
        }

        val specialFile = fileManager.getSpecialScheduleFile(date, fileName)
        return if (specialFile != null && specialFile.exists()) {
            parseCsvFile(specialFile, fromStation, toStation)
        } else {
            emptyList()
        }
    }

    private fun parseRegularSchedule(fromStation: String, toStation: String): List<Arrival> {
        val dayType = getCurrentDayType()
        val direction = determineDirection(fromStation, toStation)
        val fileName = "${dayType}_${direction}.csv"

        val schedulesDir = File(context.filesDir, "schedules/regular")
        val file = File(schedulesDir, fileName)

        return if (file.exists()) {
            parseCsvFile(file, fromStation, toStation)
        } else {
            Log.w("[parseRegularSchedule]", "Regular schedule file not found: ${file.absolutePath}")
            emptyList()
        }
    }

    private fun parseCsvFile(file: File, fromStation: String, toStation: String): List<Arrival> {
        val arrivals = mutableListOf<Arrival>()
        val isSpecialScheduleFile = file.name.contains("special_schedule")

        try {
            val reader = BufferedReader(FileReader(file))
            val lines = reader.readLines()
            reader.close()

            if (lines.isEmpty()) {
                Log.w("[parseCsvFile]", "CSV file is empty: ${file.name}")
                return emptyList()
            }

            // Get station indices from predefined station list instead of CSV header
            val fromStationIndex = getStationIndex(fromStation)
            val toStationIndex = getStationIndex(toStation)

            if (fromStationIndex == -1 || toStationIndex == -1) {
                Log.e("[parseCsvFile]", "Could not find station indices for the given stations")
                return emptyList()
            }

            // Parse all data rows (no header to skip)
            var validArrivals = 0
            var specialArrivals = 0
            for (i in 0 until lines.size) {
                val row = lines[i].split(",").map { it.trim().replace("\"", "") }

                if (row.size > maxOf(fromStationIndex, toStationIndex)) {
                    val fromTime = row[fromStationIndex]
                    val toTime = row[toStationIndex]

                    if (fromTime.isNotEmpty() && toTime.isNotEmpty() &&
                        fromTime != "--" && toTime != "--" &&
                        fromTime.uppercase() != "CLOSED" && toTime.uppercase() != "CLOSED") {

                        val formattedFromTime = formatTime(fromTime)
                        val formattedToTime = formatTime(toTime)

                        // For special schedule files, check the boolean flag at the end
                        val isSpecial = if (isSpecialScheduleFile && row.size > 14) {
                            // The last column should contain "true" or "false"
                            val specialFlag = row.last().lowercase().trim()
                            val isSpecialLine = specialFlag == "true"
                            if (isSpecialLine) {
                                specialArrivals++
                            }
                            isSpecialLine
                        } else {
                            false
                        }

                        arrivals.add(Arrival(
                            arrivalTime = formattedFromTime,
                            destinationTime = formattedToTime,
                            isSpecialSchedule = isSpecial
                        ))
                        validArrivals++
                    }
                }
            }

            // Filter future arrivals and sort by time
            val filteredArrivals = filterAndSortArrivals(arrivals)
            return filteredArrivals

        } catch (e: Exception) {
            Log.e("[parseCsvFile]", "Exception parsing CSV file ${file.name}: ${e.message}", e)
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun parseCsvFromAssets(fileName: String, fromStation: String, toStation: String): List<Arrival> {
        val arrivals = mutableListOf<Arrival>()

        try {
            val inputStream = context.assets.open(fileName)
            val lines = inputStream.bufferedReader().readLines()
            inputStream.close()

            if (lines.isEmpty()) {
                Log.w("[parseCsvFromAssets]", "Assets CSV file is empty: $fileName")
                return emptyList()
            }

            // Get station indices from predefined station list
            val fromStationIndex = getStationIndex(fromStation)
            val toStationIndex = getStationIndex(toStation)

            if (fromStationIndex == -1 || toStationIndex == -1) {
                Log.e("[parseCsvFromAssets]", "Could not find station indices for the given stations")
                return emptyList()
            }

            // Parse all data rows (no header to skip)
            var validArrivals = 0
            for (i in 0 until lines.size) {
                val row = lines[i].split(",").map { it.trim().replace("\"", "") }

                if (row.size > maxOf(fromStationIndex, toStationIndex)) {
                    val fromTime = row[fromStationIndex]
                    val toTime = row[toStationIndex]

                    if (fromTime.isNotEmpty() && toTime.isNotEmpty() &&
                        fromTime != "--" && toTime != "--" &&
                        fromTime.uppercase() != "CLOSED" && toTime.uppercase() != "CLOSED") {

                        val formattedFromTime = formatTime(fromTime)
                        val formattedToTime = formatTime(toTime)

                        arrivals.add(Arrival(
                            arrivalTime = formattedFromTime,
                            destinationTime = formattedToTime,
                            isSpecialSchedule = false
                        ))
                        validArrivals++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("[parseCsvFromAssets]", "Error parsing CSV from assets file $fileName: ${e.message}", e)
        }

        return arrivals
    }

    private fun getStationIndex(stationName: String): Int {
        return getStationList().indexOf(stationName)
    }

    private fun getStationList(): List<String> {
        return listOf(
            "Lindenwold", "Ashland", "Woodcrest", "Haddonfield", "Westmont",
            "Collingswood", "Ferry Avenue", "Broadway", "City Hall",
            "Franklin Square", "8th & Market", "9–10th & Locust",
            "12–13th & Locust", "15–16th & Locust"
        )
    }

    private fun determineDirection(fromStation: String, toStation: String): String {
        val stations = getStationList()
        val fromIndex = stations.indexOf(fromStation)
        val toIndex = stations.indexOf(toStation)

        // If destination index is greater than source index, then it is westbound
        // Else, it is eastbound
        return if (toIndex > fromIndex) "west" else "east"
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
            val cleanTime = time.replace("\"", "").trim()

            // Handle formats like "630A" or "1145P"
            if (cleanTime.matches(Regex("\\d{3,4}[AP]"))) {
                val timeDigits = cleanTime.dropLast(1)
                val amPm = if (cleanTime.endsWith("A")) "AM" else "PM"

                val hour = if (timeDigits.length == 3) {
                    timeDigits.substring(0, 1).toInt()
                } else {
                    timeDigits.substring(0, 2).toInt()
                }

                val minute = if (timeDigits.length == 3) {
                    timeDigits.substring(1).toInt()
                } else {
                    timeDigits.substring(2).toInt()
                }

                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
            }

            // Handle formats like "11:55P" or "6:30A" (partial format from CSV)
            if (cleanTime.matches(Regex("\\d{1,2}:\\d{2}[AP]"))) {
                val parts = cleanTime.split(":")
                val hour = parts[0].toInt()
                val minuteWithAmPm = parts[1]
                val minute = minuteWithAmPm.dropLast(1).toInt()
                val amPm = if (minuteWithAmPm.endsWith("A")) "AM" else "PM"

                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
            }

            // If time is already in HH:mm format (24-hour)
            if (cleanTime.contains(":") && !cleanTime.contains("A") && !cleanTime.contains("P")) {
                val parts = cleanTime.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()

                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour

                String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
            } else {
                // If time is already properly formatted, return as is
                cleanTime
            }
        } catch (e: Exception) {
            Log.e("[formatTime]", "Error formatting time $time: ${e.message}")
            time // Return original if parsing fails
        }
    }

    private fun filterAndSortArrivals(arrivals: List<Arrival>): List<Arrival> {
        // Sort all arrivals by time (don't filter out past arrivals anymore)
        val sortedArrivals = arrivals.sortedBy { arrival ->
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
                Log.e("[filterAndSortArrivals]", "Error sorting arrival time ${arrival.arrivalTime}: ${e.message}")
                0
            }
        }
        return sortedArrivals
    }
}
