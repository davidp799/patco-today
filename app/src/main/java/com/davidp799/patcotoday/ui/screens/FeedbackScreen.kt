package com.davidp799.patcotoday.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.SentimentVeryDissatisfied
import androidx.compose.material.icons.twotone.SentimentDissatisfied
import androidx.compose.material.icons.twotone.SentimentNeutral
import androidx.compose.material.icons.twotone.SentimentSatisfied
import androidx.compose.material.icons.twotone.SentimentVerySatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.davidp799.patcotoday.data.models.FeedbackRequest
import com.davidp799.patcotoday.data.repository.RetrofitInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var feedbackType by remember { mutableStateOf("") }
    val feedbackTypes = listOf("bug", "comment", "feature_request")
    var isFeedbackTypeExpanded by remember { mutableStateOf(false) }

    var rating by remember { mutableStateOf<String?>(null) }

    var description by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }

    var descriptionError by remember { mutableStateOf<String?>(null) }
    var feedbackTypeError by remember { mutableStateOf<String?>(null) }
    var ratingError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    fun validateFields(): Boolean {
        var isValid = true
        if (rating == null) {
            ratingError = "Rating is required"
            isValid = false
        } else {
            ratingError = null
        }
        if (feedbackType.isBlank()) {
            feedbackTypeError = "Feedback type is required"
            isValid = false
        } else {
            feedbackTypeError = null
        }
        if (description.isBlank()) {
            descriptionError = "Description is required"
            isValid = false
        } else {
            descriptionError = null
        }
        return isValid
    }

    fun getTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Feedback") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, // Center children like headers
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Rating (Sentiment Icons)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "How are you liking the app?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Very Dissatisfied Icon
                    IconButton(
                        onClick = {
                            rating = "very_dissatisfied"
                            ratingError = null
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.TwoTone.SentimentVeryDissatisfied,
                            contentDescription = "Very Dissatisfied",
                            tint = if (rating == "very_dissatisfied") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    // Dissatisfied Icon
                    IconButton(
                        onClick = {
                            rating = "dissatisfied"
                            ratingError = null
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.TwoTone.SentimentDissatisfied,
                            contentDescription = "Dissatisfied",
                            tint = if (rating == "dissatisfied") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    // Neutral Icon
                    IconButton(
                        onClick = {
                            rating = "neutral"
                            ratingError = null
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.TwoTone.SentimentNeutral,
                            contentDescription = "Neutral",
                            tint = if (rating == "neutral") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    // Satisfied Icon
                    IconButton(
                        onClick = {
                            rating = "satisfied"
                            ratingError = null
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.TwoTone.SentimentSatisfied,
                            contentDescription = "Satisfied",
                            tint = if (rating == "satisfied") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    // Very Satisfied Icon
                    IconButton(
                        onClick = {
                            rating = "very_satisfied"
                            ratingError = null
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.TwoTone.SentimentVerySatisfied,
                            contentDescription = "Very Satisfied",
                            tint = if (rating == "very_satisfied") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                if (ratingError != null) {
                    Text(
                        ratingError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }


            // Feedback Type Dropdown
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "What kind of feedback is this?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                )
                ExposedDropdownMenuBox(
                    expanded = isFeedbackTypeExpanded,
                    onExpandedChange = { isFeedbackTypeExpanded = !isFeedbackTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = if (feedbackType.isNotBlank()) { // Only format if a type is selected
                            feedbackType.replace("_", " ")
                                .split(" ")
                                .joinToString(" ") { word ->
                                    word.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    }
                                }
                        } else {
                            "" // Keep it blank or use a placeholder like "Select Type" if you prefer
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Feedback Type (Required)") },
                        placeholder = { Text("Select Type") }, // Added a placeholder
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFeedbackTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = feedbackTypeError != null
                    )
                    ExposedDropdownMenu(
                        expanded = isFeedbackTypeExpanded,
                        onDismissRequest = { isFeedbackTypeExpanded = false }
                    ) {
                        feedbackTypes.forEach { type ->
                            val displayText = type.replace("_", " ")
                                .split(" ")
                                .joinToString(" ") { word ->
                                    word.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    }
                                }

                            DropdownMenuItem(
                                text = { Text(displayText) },
                                onClick = {
                                    feedbackType = type
                                    isFeedbackTypeExpanded = false
                                    feedbackTypeError = null
                                }
                            )
                        }
                    }
                }
                if (feedbackTypeError != null) {
                    Text(
                        feedbackTypeError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp).align(Alignment.Start)
                    )
                }
            }

            // Description
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tell us more:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        if (it.isNotBlank()) descriptionError = null
                    },
                    label = { Text("Description (Required)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    isError = descriptionError != null,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    maxLines = 5
                )
                if (descriptionError != null) {
                    Text(
                        descriptionError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp)
                    )
                }
            }


            // Contact Info (Optional)
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "How can we contact you? (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                )
                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Email (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                )
            }


            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    if (validateFields()) {
                        isLoading = true
                        val feedbackData = FeedbackRequest(
                            feedbackType = feedbackType,
                            rating = rating!!,
                            description = description,
                            contactInfo = contactInfo.takeIf { it.isNotBlank() },
                            submittedAt = getTimestamp()
                        )
                        coroutineScope.launch {
                            try {
                                val response = RetrofitInstance.api.submitFeedback(feedbackData)
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Feedback submitted successfully!", Toast.LENGTH_LONG).show()
                                    onNavigateUp()
                                } else {
                                    Toast.makeText(context, "Error submitting feedback: ${response.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Feedback")
                }
            }
        }
    }
}
