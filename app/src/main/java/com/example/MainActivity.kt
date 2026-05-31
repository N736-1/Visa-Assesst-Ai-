package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.VisaApplication
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: VisaViewModel = viewModel()) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Home/Track, 1 = Documents, 2 = AI Chat
    var showApplyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val applications by viewModel.applications.collectAsState()
    val selectedAppId by viewModel.selectedApplicationId.collectAsState()
    val currentLogs by viewModel.currentLogs.collectAsState()

    val selectedApp = applications.firstOrNull { it.uid == selectedAppId }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        topBar = {
            // Top App Bar matching "Professional Polish" html
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar JD
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEADDFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "JD",
                                color = Color(0xFF21005D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text(
                                "VisaAssist AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF21005D),
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                "Virtual Agent Active",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "System Alert: Live Visa agents synchronized.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications indicator",
                            tint = Color(0xFF49454F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFDF7FF)
                )
            )
        },
        bottomBar = {
            // Material 3 Navigation Bar utilizing requested navigation colors
            NavigationBar(
                containerColor = Color(0xFFF3EDF7),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier
                    .testTag("bottom_nav")
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Track") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFF1D192B),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = Color(0xFF49454F),
                        unselectedTextColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Guides") },
                    label = { Text("Guides", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFF1D192B),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = Color(0xFF49454F),
                        unselectedTextColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier.testTag("nav_tab_guides")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Face, contentDescription = "Ask Agent") },
                    label = { Text("AI Agent", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = Color(0xFF1D192B),
                        indicatorColor = Color(0xFFE8DEF8),
                        unselectedIconColor = Color(0xFF49454F),
                        unselectedTextColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier.testTag("nav_tab_agent")
                )
            }
        },
        containerColor = Color(0xFFFDF7FF)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFDF7FF))
        ) {
            when (activeTab) {
                0 -> ApplyAndTrackScreen(
                    viewModel = viewModel,
                    applications = applications,
                    selectedApp = selectedApp,
                    currentLogs = currentLogs,
                    onOpenApply = { showApplyDialog = true }
                )
                1 -> DocumentRepositoryScreen(viewModel = viewModel)
                2 -> ChatBotScreen(viewModel = viewModel)
            }

            // Apply New Visa Dialog
            if (showApplyDialog) {
                ApplyVisaDialog(
                    onDismiss = { showApplyDialog = false },
                    onSubmit = { country, visaType, name, passport, docsCount ->
                        viewModel.submitNewApplication(country, visaType, name, passport, docsCount)
                        showApplyDialog = false
                    }
                )
            }
        }
    }
}

// ==================== TAB 0: APPLY & TRACK SCREEN ====================
@Composable
fun ApplyAndTrackScreen(
    viewModel: VisaViewModel,
    applications: List<VisaApplication>,
    selectedApp: VisaApplication?,
    currentLogs: List<com.example.data.StatusUpdateLog>,
    onOpenApply: () -> Unit
) {
    val context = LocalContext.current
    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown to swap current active app tracking if there are multiple applications
        if (applications.size > 1) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expandedDropdown = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("switch_app_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6750A4)),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tracking: ${selectedApp?.country} ${selectedApp?.visaType} (${selectedApp?.applicantName})",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown expand")
                    }
                }
                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    applications.forEach { app ->
                        DropdownMenuItem(
                            text = {
                                Text("${app.applicantName} - ${app.country} ${app.visaType} (${app.status})")
                            },
                            onClick = {
                                viewModel.selectApplication(app.uid)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Active Application Visa Card matching layout in "Professional Polish" html
        if (selectedApp != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_status_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFFD0BCFF))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (selectedApp.status) {
                                        "Applied" -> "Applied"
                                        "Biometrics" -> "Biometrics Done"
                                        "Interview" -> "Interview Scheduled"
                                        "Decision" -> "Approved / Decided"
                                        else -> "In Progress"
                                    }.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D),
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${selectedApp.country} ${selectedApp.visaType}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D),
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Applicant: ${selectedApp.applicantName}",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Est. Decision", fontSize = 11.sp, color = Color(0xFF49454F))
                            Text(
                                selectedApp.estDecisionDate,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF21005D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Track Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFFD0BCFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(selectedApp.progressPercent)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFF6750A4))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress steps indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProgressStepLabel("Applied", isActive = selectedApp.status == "Applied")
                        ProgressStepLabel("Biometrics", isActive = selectedApp.status == "Biometrics")
                        ProgressStepLabel("Interview", isActive = selectedApp.status == "Interview")
                        ProgressStepLabel("Decision", isActive = selectedApp.status == "Decision")
                    }
                }
            }
        } else {
            // Empty state when absolutely no visa is available
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty applications state icon",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No Active Visas tracked",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D),
                        fontSize = 18.sp
                    )
                    Text(
                        "Start your journey by adding an application below.",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF49454F),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        // Quick Actions Grid (2 columns)
        Text(
            "Quick Tasks",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF49454F),
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Apply New Visa Control
                ActionGridButton(
                    title = "New Application",
                    subtitle = "Initialize Wizard",
                    icon = Icons.Default.Add,
                    onClick = onOpenApply,
                    modifier = Modifier.testTag("action_apply")
                )

                // Delete Application button
                ActionGridButton(
                    title = "Remove Visa",
                    subtitle = "Clear selected records",
                    icon = Icons.Default.Delete,
                    onClick = {
                        if (selectedApp != null) {
                            viewModel.deleteApplication(selectedApp.uid)
                            Toast.makeText(context, "Visa record removed.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Nothing to delete", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("action_remove")
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Fast-Track Update button
                ActionGridButton(
                    title = "Audit Step",
                    subtitle = "Simulate real-time status",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        if (selectedApp != null) {
                            viewModel.triggerSimulatedNextStep(selectedApp.uid)
                            Toast.makeText(context, "Simulating live visa progress update!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clear list. Apply first!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("action_sim_update")
                )

                // Info Action
                ActionGridButton(
                    title = "Support Sync",
                    subtitle = "Verify remote agents",
                    icon = Icons.Default.Info,
                    onClick = {
                        Toast.makeText(context, "Agent Database Online • V7.2.1 Secure Connection", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }

        // Real-time Updates List matching HTML styling
        Text(
            "Recent Official Logs",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF49454F),
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        if (currentLogs.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentLogs.forEachIndexed { index, log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEF7FF))
                            .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Dot status
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (index == 0) Color(0xFF6750A4) else Color(0xFFCAC4D0))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1B1F)
                            )
                            Text(
                                text = "${log.location} • ${log.timeAgo}",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracking event logs triggered yet.",
                    color = Color(0xFF49454F),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProgressStepLabel(text: String, isActive: Boolean) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
        color = if (isActive) Color(0xFF6750A4) else Color(0xFF49454F)
    )
}

@Composable
fun ActionGridButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF7F2FA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                fontSize = 10.sp,
                color = Color(0xFF49454F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==================== TAB 1: SEARCHABLE DOCUMENT REPOSITORY SCREEN ====================
@Composable
fun DocumentRepositoryScreen(viewModel: VisaViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val countryFilter by viewModel.selectedCountryFilter.collectAsState()
    val typeFilter by viewModel.selectedTypeFilter.collectAsState()
    val filteredGuides by viewModel.filteredGuides.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Required Visa Documents Guide",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF21005D)
        )

        // Custom Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("document_search_field"),
            placeholder = { Text("Search documents, requirements...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF49454F)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6750A4),
                unfocusedBorderColor = Color(0xFFCAC4D0),
                focusedContainerColor = Color(0xFFFEF7FF),
                unfocusedContainerColor = Color(0xFFFEF7FF)
            ),
            singleLine = true
        )

        // Filter chips - Country Selector
        Text("Country Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("All", "USA", "United Kingdom").forEach { country ->
                val selected = countryFilter == country
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.updateCountryFilter(country) },
                    label = { Text(country, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8DEF8),
                        selectedLabelColor = Color(0xFF21005D)
                    )
                )
            }
        }

        // Filter chips - Visa Type Selector
        Text("Visa Category Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("All", "Visitor", "Student", "Work").forEach { type ->
                val selected = typeFilter == type
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.updateTypeFilter(type) },
                    label = { Text(type, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8DEF8),
                        selectedLabelColor = Color(0xFF21005D)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Search Result Guide cards list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("guide_documents_list")
        ) {
            if (filteredGuides.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No matching visa requirements found.",
                            color = Color(0xFF49454F),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredGuides, key = { it.id }) { doc ->
                    var isExpanded by remember { mutableStateOf(false) }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color(0xFFFEF7FF)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFEADDFF))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(doc.country, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE8DEF8))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(doc.visaType, fontSize = 9.sp, color = Color(0xFF49454F))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = doc.visaName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1B1F)
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand details check",
                                    tint = Color(0xFF49454F)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = doc.description,
                                fontSize = 12.sp,
                                color = Color(0xFF49454F),
                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Fee: ${doc.fee}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6750A4)
                                )
                                Text(
                                    text = "Time: ${doc.processingTime}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF49454F)
                                )
                            }

                            if (isExpanded) {
                                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFCAC4D0))
                                Text(
                                    "Required Dossier Checklist:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF21005D),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                doc.checklist.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Check bullet",
                                            tint = Color(0xFF6750A4),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(text = item, fontSize = 12.sp, color = Color(0xFF1C1B1F))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val listText = doc.checklist.joinToString("\n") { "• $it" }
                                            val fullText = "${doc.visaName} Docs Requirements:\n$listText"
                                            clipboardManager.setText(AnnotatedString(fullText))
                                            Toast.makeText(context, "Requirements Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                                    ) {
                                        Text("Copy Checklist", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            isExpanded = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF49454F)),
                                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                                    ) {
                                        Text("Collapse", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 2: AI AGENT CHAT SCREEN ====================
@Composable
fun ChatBotScreen(viewModel: VisaViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    val chatErr by viewModel.chatError.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Smooth scroll to newest message automatically
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Virtual Advisory Agent",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21005D)
                )
                Text(
                    "Ask about required documents & processing logs",
                    fontSize = 11.sp,
                    color = Color(0xFF49454F)
                )
            }

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color(0xFF7D5260))
            }
        }

        Divider(color = Color(0xFFCAC4D0))

        // Chat lists
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            text = if (isUser) "You" else "VisaAssist AI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .background(if (isUser) Color(0xFFE8DEF8) else Color(0xFFFEF7FF))
                                .border(
                                    BorderStroke(1.dp, if (isUser) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)),
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            // Render friendly readable text blocks
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = if (isUser) Color(0xFF1D192B) else Color(0xFF1C1B1F),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF6750A4),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Consulting artificial agent rules...",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }
            }
        }

        // Input control row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                placeholder = { Text("Ask about USA/UK visitor & work docs...", fontSize = 12.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6750A4),
                    unfocusedBorderColor = Color(0xFFCAC4D0),
                    focusedContainerColor = Color(0xFFFEF7FF),
                    unfocusedContainerColor = Color(0xFFFEF7FF)
                ),
                maxLines = 3,
                singleLine = false
            )

            FloatingActionButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(textInput.trim())
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_chat_button"),
                shape = CircleShape,
                containerColor = Color(0xFFE8DEF8),
                contentColor = Color(0xFF21005D),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send advice query",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==================== DIALOGS & SHEET COMPONENTS ====================
@Composable
fun ApplyVisaDialog(
    onDismiss: () -> Unit,
    onSubmit: (country: String, visaType: String, name: String, passport: String, docsCount: Int) -> Unit
) {
    var countryOption by remember { mutableStateOf("USA") } // "USA" or "United Kingdom"
    var visaTypeOption by remember { mutableStateOf("B1/B2 Visa") }
    var nameInput by remember { mutableStateOf("") }
    var passportInput by remember { mutableStateOf("") }

    // Completed documents checkboxes state
    val docStates = remember { mutableStateListOf(false, false, false, false) }
    val usTypes = listOf("B1/B2 Visa", "F-1 Student Visa", "H-1B Specialty Work")
    val ukTypes = listOf("Standard Visitor Visa", "UK Student Visa", "Skilled Worker Visa")

    // Update visa default selection depending on country selection
    LaunchedEffect(countryOption) {
        visaTypeOption = if (countryOption == "USA") usTypes.first() else ukTypes.first()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Visa Application Wizard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select target destination country", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("USA", "United Kingdom").forEach { c ->
                        val active = countryOption == c
                        Button(
                            onClick = { countryOption = c },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) Color(0xFFE8DEF8) else Color(0xFFF7F2FA),
                                contentColor = if (active) Color(0xFF21005D) else Color(0xFF49454F)
                            )
                        ) {
                            Text(c, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                Text("Select Visa Route Pathway", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeList = if (countryOption == "USA") usTypes else ukTypes
                    activeList.forEach { actType ->
                        val selected = visaTypeOption == actType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFFD0BCFF) else Color(0xFFF3EDF7))
                                .border(BorderStroke(1.dp, if (selected) Color(0xFF6750A4) else Color(0xFFCAC4D0)), RoundedCornerShape(8.dp))
                                .clickable { visaTypeOption = actType }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = actType.replace(" Visa", "").replace(" Specialty", ""),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color(0xFF21005D) else Color(0xFF49454F),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Applicant Full Name", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_applicant_name"),
                    placeholder = { Text("E.g. John Doe") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFCAC4D0)
                    )
                )

                OutlinedTextField(
                    value = passportInput,
                    onValueChange = { passportInput = it },
                    label = { Text("Passport Serial Number", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_passport_number"),
                    placeholder = { Text("E.g. US902847") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFCAC4D0)
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text("Verify Uploaded Documents Dossier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                val docsChecklist = listOf(
                    "Valid National Passport Check",
                    "Completed portal visa forms (DS-160 / CAS reference)",
                    "Evidence of solvency financials (bank balances)",
                    "Accommodations proof or return carrier bookings"
                )

                docsChecklist.forEachIndexed { i, desc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { docStates[i] = !docStates[i] }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = docStates[i],
                            onCheckedChange = { docStates[i] = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6750A4))
                        )
                        Text(desc, fontSize = 11.sp, color = Color(0xFF1C1B1F))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isBlank() || passportInput.isBlank()) {
                        return@Button
                    }
                    val uploadCount = docStates.count { it }
                    onSubmit(countryOption, visaTypeOption, nameInput, passportInput, uploadCount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_apply_button"),
                enabled = nameInput.isNotBlank() && passportInput.isNotBlank()
            ) {
                Text("Process Application")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF7D5260))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
