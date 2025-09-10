package com.davidp799.patcotoday.data.api

import com.davidp799.patcotoday.data.models.FeedbackRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackApiService {
    @POST("v1/feedback") // The relative path from your base URL
    suspend fun submitFeedback(@Body feedbackRequest: FeedbackRequest): Response<Unit> // Assuming the API returns no specific body on success
}
