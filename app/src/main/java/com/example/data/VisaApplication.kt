package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visa_applications")
data class VisaApplication(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val country: String, // "USA" or "UK"
    val visaType: String, // e.g., "Visitor (B1/B2)", "Student (F-1)", etc.
    val applicantName: String,
    val passportNumber: String,
    val status: String, // "Applied", "Biometrics", "Interview", "Decision"
    val progressPercent: Float, // e.g., 0.25f, 0.5f, 0.75f, 1.0f
    val submissionDate: Long = System.currentTimeMillis(),
    val estDecisionDate: String,
    val documentsUploadedCount: Int
)
