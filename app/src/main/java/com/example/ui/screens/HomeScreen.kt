package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Medicine
import com.example.ui.MedicineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MedicineViewModel,
    onAddMedicineClick: () -> Unit,
    onEditMedicineClick: (Int) -> Unit,
    onMedicineClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val medicines by viewModel.medicines.collectAsState()
    val hydrationCount by viewModel.hydrationCount.collectAsState()
    val streakDays by viewModel.computedStreak.collectAsState()

    var medicineToDelete by remember { mutableStateOf<Medicine?>(null) }

    // Find the next pending medicine dose
    val nextPendingMedicine = remember(medicines) {
        medicines.firstOrNull { it.status == "Pending" }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicineClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_medicine_fab")
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Medicine",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = Modifier.testTag("home_screen_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("medicines_list"),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // 1. Bento Header
                item {
                    BentoHeader(
                        onProfileClick = {
                            Toast.makeText(context, "Checked for active schedules.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 2. Bento Next Dose Card (Primary Grid Element, Col-span-2)
                item {
                    BentoNextDoseCard(
                        medicine = nextPendingMedicine,
                        onMarkAsTakenClick = { medicine ->
                            viewModel.markAsTaken(medicine)
                            Toast.makeText(context, "Logged ${medicine.name} as Taken! 💊", Toast.LENGTH_SHORT).show()
                        },
                        onAddMedicineClick = onAddMedicineClick,
                        onMedicineClick = onMedicineClick
                    )
                }

                // 3. Bento Side-by-side Stats Grid (Col-span-1 each)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoHydrationCard(
                            count = hydrationCount,
                            onIncrement = {
                                viewModel.incrementHydration()
                                Toast.makeText(context, "Hydration logged! Keep it up! 💧", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BentoStreakCard(
                            streakDays = streakDays,
                            onCardClick = {
                                Toast.makeText(context, "Consecutive days tracking is active! Maintain your streak! 🔥", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Bento Daily Progress Card
                item {
                    MedicineSummaryHeader(medicines = medicines)
                }

                // 5. Section Header Title
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⏳", fontSize = 16.sp)
                            Text(
                                text = "Upcoming Schedule",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                        Text(
                            text = "${medicines.size} scheduled",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                if (medicines.isEmpty()) {
                    // Empty list state
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_empty_state_medicines),
                                    contentDescription = "No medicines",
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(24.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No medicines added yet",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the '+' button below to easily log your daily medicines and setup smart reminders.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Medicine list items
                    items(
                        items = medicines,
                        key = { it.id }
                    ) { medicine ->
                        MedicineItemCard(
                            medicine = medicine,
                            onItemClick = { onMedicineClick(medicine.id) },
                            onMarkAsTakenClick = {
                                viewModel.markAsTaken(medicine)
                                Toast.makeText(context, "Logged ${medicine.name} as Taken! 💊", Toast.LENGTH_SHORT).show()
                            },
                            onMarkAsPendingClick = {
                                viewModel.markAsPending(medicine)
                                Toast.makeText(context, "${medicine.name} status updated to Pending.", Toast.LENGTH_SHORT).show()
                            },
                            onEditClick = { onEditMedicineClick(medicine.id) },
                            onDeleteClick = { medicineToDelete = medicine }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for deletion
    if (medicineToDelete != null) {
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            title = { Text("Delete Medicine Reminder?") },
            text = { Text("Are you sure you want to delete the schedule for ${medicineToDelete?.name}? This will also cancel its notifications.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        medicineToDelete?.let {
                            viewModel.deleteMedicine(it)
                            Toast.makeText(context, "${it.name} reminder removed.", Toast.LENGTH_SHORT).show()
                        }
                        medicineToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BentoHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "MEDICARE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Daily Routine",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Profile bubble
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), shape = CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👤", fontSize = 18.sp)
        }
    }
}

@Composable
fun BentoNextDoseCard(
    medicine: Medicine?,
    onMarkAsTakenClick: (Medicine) -> Unit,
    onAddMedicineClick: () -> Unit,
    onMedicineClick: (Int) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val bg = if (isDark) Color(0xFF1E3A8A) else Color(0xFFD1E4FF)
    val text = if (isDark) Color(0xFFDBEAFE) else Color(0xFF001D36)
    val buttonBg = if (isDark) Color(0xFFDBEAFE) else Color(0xFF0061A4)
    val buttonText = if (isDark) Color(0xFF1E3A8A) else Color(0xFFFFFFFF)
    val borderStroke = if (isDark) Color(0xFFDBEAFE).copy(alpha = 0.1f) else Color(0xFF0061A4).copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(enabled = medicine != null) { medicine?.let { onMedicineClick(it.id) } }
            .testTag("bento_next_dose_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(borderStroke, borderStroke)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(buttonBg, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 18.sp)
                }
                
                // Next Dose badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = buttonBg.copy(alpha = 0.15f)),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (medicine != null) "NEXT DOSE" else "SUMMARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = buttonBg
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (medicine != null) {
                Text(
                    text = medicine.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${medicine.dosage} • Scheduled: ${medicine.time}",
                    fontSize = 14.sp,
                    color = text.copy(alpha = 0.7f)
                )
                if (medicine.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = medicine.notes,
                        fontSize = 12.sp,
                        color = text.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onMarkAsTakenClick(medicine) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBg,
                        contentColor = buttonText
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("bento_mark_taken_btn")
                ) {
                    Text("Mark as Taken", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Text(
                    text = "No pending doses",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You are fully caught up with your medicine schedule today!",
                    fontSize = 13.sp,
                    color = text.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onAddMedicineClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBg,
                        contentColor = buttonText
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("bento_add_med_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Medicine", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun BentoHydrationCard(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color(0xFF1B3B2B) else Color(0xFFE1F1E7)
    val text = if (isDark) Color(0xFFA5D6A7) else Color(0xFF1B5E20)
    val borderStroke = if (isDark) Color(0xFFA5D6A7).copy(alpha = 0.15f) else Color(0xFF81C784).copy(alpha = 0.3f)
    val percent = ((count / 8f) * 100).toInt()

    Card(
        modifier = modifier
            .height(125.dp)
            .clickable { onIncrement() }
            .testTag("bento_hydration_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(borderStroke, borderStroke)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💧", fontSize = 22.sp)
                Text(
                    text = "$percent%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )
            }
            
            Column {
                Text(
                    text = "Hydration",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )
                Text(
                    text = "$count / 8 Glasses",
                    fontSize = 11.sp,
                    color = text.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun BentoStreakCard(
    streakDays: Int,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color(0xFF3E2D1A) else Color(0xFFFFF1D6)
    val text = if (isDark) Color(0xFFFFCC80) else Color(0xFFE65100)
    val borderStroke = if (isDark) Color(0xFFFFCC80).copy(alpha = 0.15f) else Color(0xFFFFB74D).copy(alpha = 0.3f)

    Card(
        modifier = modifier
            .height(125.dp)
            .clickable { onCardClick() }
            .testTag("bento_streak_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(borderStroke, borderStroke)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⭐", fontSize = 22.sp)
                Text(
                    text = "${streakDays}d",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )
            }
            
            Column {
                Text(
                    text = "Streak",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )
                Text(
                    text = if (streakDays > 2) "Perfect Week!" else "Keep it up!",
                    fontSize = 11.sp,
                    color = text.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun MedicineSummaryHeader(medicines: List<Medicine>) {
    val totalCount = medicines.size
    val takenCount = medicines.count { it.status == "Taken" }
    val progressFraction = if (totalCount > 0) takenCount.toFloat() / totalCount else 0f
    val percent = if (totalCount > 0) (progressFraction * 100).toInt() else 0
    
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val borderStroke = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE0E2ED)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("summary_header_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(borderStroke, borderStroke)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (totalCount == 0) {
                    Text(
                        text = "Add medicines to start tracking your progress.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                } else if (takenCount == totalCount) {
                    Text(
                        text = "Fantastic job! All doses taken today! 🎉",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    )
                } else {
                    Text(
                        text = "$takenCount of $totalCount completed today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Percentage Circle Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun MedicineItemCard(
    medicine: Medicine,
    onItemClick: () -> Unit,
    onMarkAsTakenClick: () -> Unit,
    onMarkAsPendingClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isTaken = medicine.status == "Taken"

    val typeIcon = when (medicine.type) {
        "Tablet" -> Icons.Default.Circle
        "Capsule" -> Icons.Default.TripOrigin
        "Syrup" -> Icons.Default.WaterDrop
        "Injection" -> Icons.Default.LocalHospital
        else -> Icons.Default.MedicalServices
    }

    val typeColor = when (medicine.type) {
        "Tablet" -> MaterialTheme.colorScheme.primary
        "Capsule" -> MaterialTheme.colorScheme.secondary
        "Syrup" -> MaterialTheme.colorScheme.tertiary
        "Injection" -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onItemClick() }
            .testTag("medicine_card_${medicine.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTaken) 0.dp else 1.dp),
        border = if (isTaken) null else CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(Color(0xFFE0E2ED), Color(0xFFE0E2ED)))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Rounded Icon Background for Type representation
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(typeColor.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = medicine.type,
                    tint = typeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isTaken) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = medicine.dosage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = medicine.time,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isTaken) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary
                    )
                }

                if (medicine.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = medicine.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Done/Taken Indicator or Toggle Button
                if (isTaken) {
                    Button(
                        onClick = onMarkAsPendingClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("btn_pending_${medicine.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Taken",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Taken", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onMarkAsTakenClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // Standard healing green
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("btn_taken_${medicine.id}")
                    ) {
                        Text("Take", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Edit/Delete triggers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_edit_${medicine.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit schedule",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_delete_${medicine.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
