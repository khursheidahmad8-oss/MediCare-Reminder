package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MedicineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    viewModel: MedicineViewModel,
    medicineId: Int,
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val medicineState = viewModel.getMedicineByIdFlow(medicineId).collectAsState(initial = null)
    val medicine = medicineState.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_detail_back")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (medicine != null) {
                        IconButton(
                            onClick = { onEditClick(medicine.id) },
                            modifier = Modifier.testTag("btn_detail_edit")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        },
        modifier = Modifier.testTag("medicine_detail_scaffold")
    ) { innerPadding ->
        if (medicine == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Medicine Icon and Type card representation
                Card(
                    modifier = Modifier.size(96.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = typeColor.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = medicine.type,
                            tint = typeColor,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Name head and Type label
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = typeColor.copy(alpha = 0.15f)),
                    shape = CircleShape
                ) {
                    Text(
                        text = medicine.type,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Detail attributes list card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Dosage Item
                        DetailRow(
                            icon = Icons.Default.Scale,
                            title = "Dosage",
                            value = medicine.dosage
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Time Item
                        DetailRow(
                            icon = Icons.Default.AccessTime,
                            title = "Scheduled Time",
                            value = medicine.time
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Status Item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ToggleOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isTaken) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = medicine.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTaken) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (medicine.notes.isNotEmpty()) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Notes Item
                            DetailRow(
                                icon = Icons.Default.Notes,
                                title = "Notes",
                                value = medicine.notes
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Adherence Trigger Button
                if (!isTaken) {
                    Button(
                        onClick = {
                            viewModel.markAsTaken(medicine)
                            Toast.makeText(context, "Logged ${medicine.name} as Taken! 💊", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_detail_mark_taken"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MARK AS TAKEN", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.markAsPending(medicine)
                            Toast.makeText(context, "Status updated back to Pending.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_detail_mark_pending"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RESET TO PENDING", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
