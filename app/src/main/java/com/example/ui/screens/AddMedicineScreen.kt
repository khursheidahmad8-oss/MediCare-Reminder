package com.example.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    viewModel: MedicineViewModel,
    medicineId: Int = -1, // -1 means adding new
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("1 Tablet") }
    var timeString by remember { mutableStateOf("08:00 AM") }
    var selectedType by remember { mutableStateOf("Tablet") }
    var notes by remember { mutableStateOf("") }
    var existingStatus by remember { mutableStateOf("Pending") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

    val isEditing = medicineId != -1

    // If editing, load current values from DB
    LaunchedEffect(medicineId) {
        if (isEditing) {
            viewModel.getMedicineByIdFlow(medicineId).collect { medicine ->
                medicine?.let {
                    name = it.name
                    dosage = it.dosage
                    timeString = it.time
                    selectedType = it.type
                    notes = it.notes
                    existingStatus = it.status
                }
            }
        }
    }

    // Presets for Dosage Selection
    val dosagePresets = listOf(
        "1 Tablet", "2 Tablets", "1 Capsule", "2 Capsules",
        "5 ml (1 tsp)", "10 ml (2 tsp)", "1 Injection", "0.5 Tablet"
    )
    var dosageDropdownExpanded by remember { mutableStateOf(false) }

    // Types preset config
    val types = listOf(
        Triple("Tablet", Icons.Default.Circle, MaterialTheme.colorScheme.primary),
        Triple("Capsule", Icons.Default.TripOrigin, MaterialTheme.colorScheme.secondary),
        Triple("Syrup", Icons.Default.WaterDrop, MaterialTheme.colorScheme.tertiary),
        Triple("Injection", Icons.Default.LocalHospital, Color(0xFFE53935))
    )

    // Calendar for Timepicker initialization
    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minuteOfHour ->
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val displayHour = when {
                hourOfDay == 0 -> 12
                hourOfDay > 12 -> hourOfDay - 12
                else -> hourOfDay
            }
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minuteOfHour, amPm)
            timeString = formattedTime
            timeError = null
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false // 12-hour format
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Medicine Reminder" else "Add New Medicine",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = Modifier.testTag("add_medicine_screen_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Medicine Name Input
            Column {
                Text(
                    text = "Medicine Name *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotEmpty()) nameError = null
                    },
                    placeholder = { Text("e.g. Paracetamol, Vitamin D") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_name"),
                    shape = RoundedCornerShape(12.dp),
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Healing, contentDescription = null)
                    }
                )
            }

            // Medicine Type Selection Row
            Column {
                Text(
                    text = "Medicine Type",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { (typeName, icon, color) ->
                        val isSelected = selectedType == typeName
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = typeName }
                                .testTag("type_chip_$typeName"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    color.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = typeName,
                                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = typeName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Dosage field with Dropdown Presets
            Column {
                Text(
                    text = "Dosage",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = dosageDropdownExpanded,
                    onExpandedChange = { dosageDropdownExpanded = !dosageDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("input_dosage"),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dosageDropdownExpanded)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Scale, contentDescription = null)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = dosageDropdownExpanded,
                        onDismissRequest = { dosageDropdownExpanded = false }
                    ) {
                        dosagePresets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset) },
                                onClick = {
                                    dosage = preset
                                    dosageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Select Time Trigger
            Column {
                Text(
                    text = "Select Reminder Time *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = timeString,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() }
                        .testTag("input_time"),
                    enabled = false, // Disabled manual entry to force timepicker click
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (timeError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Pick Time",
                            modifier = Modifier.clickable { timePickerDialog.show() }
                        )
                    }
                )
                if (timeError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Notes field (optional)
            Column {
                Text(
                    text = "Notes (Optional)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("e.g. Take after breakfast, with milk") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("input_notes"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    leadingIcon = {
                        Icon(Icons.Default.Note, contentDescription = null)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Action Button
            Button(
                onClick = {
                    // Validations
                    var isValid = true
                    if (name.trim().isEmpty()) {
                        nameError = "Medicine name is required"
                        isValid = false
                    }
                    if (timeString.trim().isEmpty()) {
                        timeError = "Reminder time is required"
                        isValid = false
                    }

                    if (isValid) {
                        if (isEditing) {
                            viewModel.updateMedicine(
                                id = medicineId,
                                name = name.trim(),
                                dosage = dosage.trim(),
                                time = timeString.trim(),
                                type = selectedType,
                                notes = notes.trim(),
                                status = existingStatus
                            )
                            Toast.makeText(context, "Medicine reminder updated successfully! 💊", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addMedicine(
                                name = name.trim(),
                                dosage = dosage.trim(),
                                time = timeString.trim(),
                                type = selectedType,
                                notes = notes.trim()
                            )
                            Toast.makeText(context, "Medicine reminder added! ⏰", Toast.LENGTH_SHORT).show()
                        }
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("btn_save_medicine"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "UPDATE REMINDER" else "SAVE REMINDER",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
