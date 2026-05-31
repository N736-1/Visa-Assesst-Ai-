package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class VisaGuideDoc(
    val id: String,
    val country: String, // "USA" or "United Kingdom"
    val visaType: String, // "Visitor", "Student", "Work"
    val visaName: String, // e.g. "B1/B2 Visitor Visa"
    val description: String,
    val fee: String,
    val processingTime: String,
    val checklist: List<String>
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "agent"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class VisaViewModel(application: Application) : AndroidViewModel(application) {

    private val visaDao = AppDatabase.getDatabase(application).visaDao()
    private val repository = VisaRepository(visaDao)

    // --- Active Applications Flow ---
    val applications: StateFlow<List<VisaApplication>> = repository.allApplications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Active Logs Map or Selected Application Logs ---
    private val _selectedApplicationId = MutableStateFlow<Int?>(null)
    val selectedApplicationId: StateFlow<Int?> = _selectedApplicationId.asStateFlow()

    val currentLogs: StateFlow<List<StatusUpdateLog>> = _selectedApplicationId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getLogsForApplication(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Guide Documents Repository State ---
    val visaGuides = listOf(
        VisaGuideDoc(
            id = "us_b1b2",
            country = "USA",
            visaType = "Visitor",
            visaName = "USA B1/B2 Visitor Visa",
            description = "For temporary travel related to business (B1) or leisure/medical (B2). Ideal for tourism, visiting family, or attending conferences.",
            fee = "$185 USD",
            processingTime = "3 - 8 Weeks",
            checklist = listOf(
                "Passport valid for at least 6 months beyond period of stay",
                "Form DS-160 Confirmation Page barcode printout",
                "Visa Application Fee Payment Receipt",
                "U.S. format digital photo (2x2 inches, white background)",
                "Appointment Confirmation Letter",
                "Financial proof: bank statements, tax returns, pay slips",
                "Ties to home country: employment letter, property deeds"
            )
        ),
        VisaGuideDoc(
            id = "us_f1",
            country = "USA",
            visaType = "Student",
            visaName = "USA F-1 Student Visa",
            description = "Required for academic studies at approved US universities, colleges, high schools, or language training programs.",
            fee = "$185 USD + $350 SEVIS Fee",
            processingTime = "2 - 5 Weeks",
            checklist = listOf(
                "Valid Passport with at least 6 months validity",
                "Form I-20 (Certificate of Eligibility) from US Institution",
                "SEVIS I-901 Fee Receipt",
                "Form DS-160 Confirmation Page barcode",
                "Financial statements proving ability to cover 1st year tuition and living costs",
                "Academic transcripts, diplomas, test scores (SAT/GRE/TOEFL)",
                "Intention to depart US upon course completion proof"
            )
        ),
        VisaGuideDoc(
            id = "us_h1b",
            country = "USA",
            visaType = "Work",
            visaName = "USA H-1B Specialty Occupation",
            description = "For skilled professionals in specialized fields (IT, Engineering, Medicine, Finance) supported by an approved employer sponsor.",
            fee = "$460 Base Fee (Paid by Employer)",
            processingTime = "2 - 6 Months",
            checklist = listOf(
                "Approved Form I-129 (Petition for Nonimmigrant Worker)",
                "Form I-797 Approval Notice",
                "Approved Labor Condition Application (LCA) from DOL",
                "Form DS-160 Confirmation Page",
                "Original Employment Offer Letter stating position details",
                "University Degree Certificates, transcripts, and credentials evaluation",
                "Detailed Resume or Curriculum Vitae (CV)",
                "Previous experience letters from past employers"
            )
        ),
        VisaGuideDoc(
            id = "uk_visitor",
            country = "United Kingdom",
            visaType = "Visitor",
            visaName = "UK Standard Visitor Visa",
            description = "For visiting the United Kingdom for holiday, leisure, tourism, business meetings, or short courses.",
            fee = "£115 GBP",
            processingTime = "3 Weeks",
            checklist = listOf(
                "Current valid Passport with at least 1 blank page",
                "Financial Proof: last 3-6 months solid bank statements",
                "Proof of Accommodation: hotel booking or invitation letter",
                "Detailed Travel Itinerary (destinations, flight plans)",
                "Employment Letter stating position, salary, and approved leave date",
                "Proof of ties to home country (family, asset ownership)"
            )
        ),
        VisaGuideDoc(
            id = "uk_student",
            country = "United Kingdom",
            visaType = "Student",
            visaName = "UK Student Visa",
            description = "For students aged 16+ who have been offered a place on a course by a licensed student sponsor in the United Kingdom.",
            fee = "£490 GBP + IHS Healthcare Charge",
            processingTime = "3 - 4 Weeks",
            checklist = listOf(
                "Current Passport",
                "Confirmation of Acceptance for Studies (CAS) reference number",
                "Tuberculosis (TB) test certificate (if applicable)",
                "Proof of financial self-sufficiency (tuition + first 9 months living cost)",
                "English language certificate (Approved secure English language test)",
                "Academic credentials/certificates specified in the CAS letter",
                "Consent letter (if the student is under 18)"
            )
        ),
        VisaGuideDoc(
            id = "uk_work",
            country = "United Kingdom",
            visaType = "Work",
            visaName = "UK Skilled Worker Visa",
            description = "Allows eligible foreign workers to stay in the United Kingdom to perform an authorized role with an approved sponsor.",
            fee = "£719 - £1,500 GBP",
            processingTime = "3 - 8 Weeks",
            checklist = listOf(
                "Valid Passport with a clean page",
                "Certificate of Sponsorship (CoS) reference number from employer",
                "Proof of knowledge of English (Degree taught in English or UKVI IELTS)",
                "Job offer details: salary, job title, trade code, and sponsor license number",
                "Proof of personal savings (unless sponsor certifies maintenance)",
                "Criminal Record Certificate (for healthcare role)",
                "Tuberculosis (TB) test certificate (if applicable)"
            )
        )
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCountryFilter = MutableStateFlow("All") // "All", "USA", "United Kingdom"
    val selectedCountryFilter: StateFlow<String> = _selectedCountryFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("All") // "All", "Visitor", "Student", "Work"
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    val filteredGuides: StateFlow<List<VisaGuideDoc>> = combine(
        _searchQuery, _selectedCountryFilter, _selectedTypeFilter
    ) { query, country, type ->
        visaGuides.filter { doc ->
            val matchesQuery = doc.visaName.contains(query, ignoreCase = true) ||
                    doc.description.contains(query, ignoreCase = true) ||
                    doc.checklist.any { it.contains(query, ignoreCase = true) }
            val matchesCountry = country == "All" || doc.country.equals(country, ignoreCase = true)
            val matchesType = type == "All" || doc.visaType.equals(type, ignoreCase = true)

            matchesQuery && matchesCountry && matchesType
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = visaGuides)

    // --- AI Conversation States ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    init {
        // Pre-populate empty DB with a starter demo visa application
        viewModelScope.launch {
            applications.collectLatest { list ->
                if (list.isEmpty()) {
                    seedStarterApplication()
                } else if (_selectedApplicationId.value == null) {
                    _selectedApplicationId.value = list.first().uid
                }
            }
        }

        // Initialize Chat with greeting
        _chatMessages.value = listOf(
            ChatMessage(
                sender = "agent",
                text = "Welcome to **VisaAssist AI**! I am your interactive virtual agent specializing in USA and United Kingdom visa path requirements. Let me know which visa path you're exploring today!"
            )
        )

        // Start background auto update simulated ticker
        startPeriodicLogSimulator()
    }

    private suspend fun seedStarterApplication() {
        val defaultApp = VisaApplication(
            country = "USA",
            visaType = "B1/B2 Visa",
            applicantName = "John Doe",
            passportNumber = "US9827491",
            status = "Biometrics",
            progressPercent = 0.50f,
            estDecisionDate = "Oct 12, 2026", // Shifted to realistic/future relative date
            documentsUploadedCount = 5
        )
        val uid = repository.insertApplication(defaultApp).toInt()

        val logs = listOf(
            StatusUpdateLog(
                applicationUid = uid,
                title = "Identity & Documents Verified",
                location = "Automatic System Integration",
                timeAgo = "1 day ago",
                isCompleted = true,
                timestamp = System.currentTimeMillis() - 86400000
            ),
            StatusUpdateLog(
                applicationUid = uid,
                title = "Consulate Received Docs",
                location = "London Processing Center",
                timeAgo = "2h ago",
                isCompleted = true,
                timestamp = System.currentTimeMillis() - 7200000
            )
        )
        repository.insertLogs(logs)
        _selectedApplicationId.value = uid
    }

    fun selectApplication(uid: Int) {
        _selectedApplicationId.value = uid
    }

    fun deleteApplication(uid: Int) {
        viewModelScope.launch {
            repository.deleteApplication(uid)
            val currentList = applications.value.filter { it.uid != uid }
            if (currentList.isNotEmpty()) {
                _selectedApplicationId.value = currentList.first().uid
            } else {
                _selectedApplicationId.value = null
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCountryFilter(country: String) {
        _selectedCountryFilter.value = country
    }

    fun updateTypeFilter(type: String) {
        _selectedTypeFilter.value = type
    }

    // --- Submit Application Action ---
    fun submitNewApplication(
        country: String,
        visaType: String,
        applicantName: String,
        passportNumber: String,
        docCount: Int
    ) {
        viewModelScope.launch {
            val app = VisaApplication(
                country = country,
                visaType = visaType,
                applicantName = applicantName,
                passportNumber = passportNumber,
                status = "Applied",
                progressPercent = 0.25f,
                estDecisionDate = "Dec 15, 2026",
                documentsUploadedCount = docCount
            )
            val uid = repository.insertApplication(app).toInt()

            val initLog = StatusUpdateLog(
                applicationUid = uid,
                title = "Application Submitted Online",
                location = "Portal Gateway",
                timeAgo = "Just now",
                isCompleted = true,
                timestamp = System.currentTimeMillis()
            )
            repository.insertLog(initLog)
            _selectedApplicationId.value = uid
        }
    }

    // --- Manual Simulated Trigger State Progress ---
    fun triggerSimulatedNextStep(applicationUid: Int) {
        viewModelScope.launch {
            val app = databaseGetApplication(applicationUid) ?: return@launch
            var newStatus = app.status
            var newPercent = app.progressPercent

            val logTitle: String
            val logLocation: String

            when (app.status) {
                "Applied" -> {
                    newStatus = "Biometrics"
                    newPercent = 0.50f
                    logTitle = "Biometrics Scheduled"
                    logLocation = "National Security Processing Center"
                }
                "Biometrics" -> {
                    newStatus = "Interview"
                    newPercent = 0.75f
                    logTitle = "Consulate Interview Confirmed"
                    logLocation = "Embassy Visa Section"
                }
                "Interview" -> {
                    newStatus = "Decision"
                    newPercent = 1.0f
                    logTitle = "Visa Approved & Dispatched"
                    logLocation = "Passport Courier Office"
                }
                else -> {
                    // Reset to initial loop
                    newStatus = "Applied"
                    newPercent = 0.25f
                    logTitle = "Resetting application status cycle"
                    logLocation = "Local Portal Audit"
                }
            }

            val updatedApp = app.copy(status = newStatus, progressPercent = newPercent)
            repository.updateApplication(updatedApp)

            val log = StatusUpdateLog(
                applicationUid = applicationUid,
                title = logTitle,
                location = logLocation,
                timeAgo = "Just now",
                isCompleted = true,
                timestamp = System.currentTimeMillis()
            )
            repository.insertLog(log)
        }
    }

    private suspend fun databaseGetApplication(uid: Int): VisaApplication? = withContext(Dispatchers.IO) {
        applications.value.firstOrNull { it.uid == uid }
    }

    // --- Background Coroutine Timer for Real-Time Updates ---
    private fun startPeriodicLogSimulator() {
        viewModelScope.launch {
            while (true) {
                delay(40000) // update every 40 seconds
                val activeId = _selectedApplicationId.value
                if (activeId != null) {
                    val app = databaseGetApplication(activeId)
                    if (app != null && app.status != "Decision") {
                        // Progress the application automatically to show real-time changes
                        triggerSimulatedNextStep(activeId)
                    }
                }
            }
        }
    }

    // --- Gemini Chat Implementation ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(sender = "user", text = text)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true
        _chatError.value = null

        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _isChatLoading.value = false
                val errorMsg = ChatMessage(
                    sender = "agent",
                    text = "🚨 **API Key Configuration Error**: No valid Gemini API Key detected. Please input your secure key in the **Secrets panel in AI Studio** to enable interactive chat queries."
                )
                _chatMessages.value = _chatMessages.value + errorMsg
                return@launch
            }

            // Build historical context
            val history = _chatMessages.value.map { msg ->
                Content(parts = listOf(Part(text = if (msg.sender == "user") msg.text else "Agent: ${msg.text}")))
            }

            val request = GenerateContentRequest(
                contents = history,
                systemInstruction = Content(
                    parts = listOf(
                        Part(
                            text = "You are VisaAssist AI, an expert, professional visa assistance agent specializing in USA and UK visa routes (Visitor, Student, H1B/Skilled Worker). Answer any candidate documents, checklists, procedural processes, and pricing detailedly, professionally, and compactly. Be helpful, concise, and structured, maintaining a dignified reassuring consulting tone."
                        )
                    )
                ),
                generationConfig = GenerationConfig(temperature = 0.5f)
            )

            try {
                val response = RetrofitClient.service.generateContent(
                    model = "gemini-3.5-flash",
                    apiKey = apiKey,
                    request = request
                )
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "I apologize, I could not generate a response. Please try reframing your question."

                _isChatLoading.value = false
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = "agent", text = replyText)
            } catch (e: Exception) {
                _isChatLoading.value = false
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "agent",
                    text = "⚠️ **Connection Error**: Failed to fetch advice from Gemini AI. Please check your internet connection or verify your API key credential in AI Studio secrets. Error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = "agent",
                text = "Chat history cleared. How can I guide your USA or UK visa applications today?"
            )
        )
    }
}
