package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateDarkest
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import com.example.ui.theme.SlateTextColor
import java.util.Locale
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            MobileAds.initialize(this) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        MainScreen(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        AdmobBannerView()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    calculatorViewModel: CoilCalculatorViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // Observe StateFlow values from View Model
    val selectedMetal by calculatorViewModel.selectedMetal.collectAsState()
    val weightInput by calculatorViewModel.weightInput.collectAsState()
    val weightUnit by calculatorViewModel.weightUnit.collectAsState()
    val widthInput by calculatorViewModel.widthInput.collectAsState()
    val thicknessInput by calculatorViewModel.thicknessInput.collectAsState()
    val idInput by calculatorViewModel.idInput.collectAsState()
    val gsmInput by calculatorViewModel.gsmInput.collectAsState()

    val outerDiameter by calculatorViewModel.outerDiameter.collectAsState()
    val wallThickness by calculatorViewModel.wallThickness.collectAsState()
    val totalLength by calculatorViewModel.totalLength.collectAsState()
    val errorMessage by calculatorViewModel.errorMessage.collectAsState()

    // Screen title styled cleanly and compactly for mobile devices
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        AppHeaderSection()

        // Card containing form controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SlateDark
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SPECIFICATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )

                Divider(color = SlateMedium, thickness = 1.dp)

                // 1. Metal Selection Dropdown
                MetalSelectionDropdown(
                    selectedMetal = selectedMetal,
                    onMetalSelected = { calculatorViewModel.selectMetal(it) }
                )

                // Dynamic GSM weight input block for Galvanized Steel
                AnimatedVisibility(
                    visible = selectedMetal.name.contains("Galvanized", ignoreCase = true),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    GalvanizedGsmSelectorBlock(
                        gsmInput = gsmInput,
                        onGsmChanged = { calculatorViewModel.updateGsmInput(it) }
                    )
                }

                // 2. Weight Input & Unit selector
                WeightInputRow(
                    weightValue = weightInput,
                    weightUnit = weightUnit,
                    onWeightChanged = { calculatorViewModel.updateWeight(it) },
                    onUnitChanged = { calculatorViewModel.updateWeightUnit(it) }
                )

                // 3. Coil Width Input
                OutlinedTextField(
                    value = widthInput,
                    onValueChange = { calculatorViewModel.updateWidth(it) },
                    label = { Text("Coil Width (mm)") },
                    placeholder = { Text("Enter coil width (e.g. 1219)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("width_text_field"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SlateTextColor,
                        unfocusedTextColor = SlateTextColor
                    )
                )

                // 4. Material Thickness Input (Optional)
                OutlinedTextField(
                    value = thicknessInput,
                    onValueChange = { calculatorViewModel.updateThickness(it) },
                    label = { Text("Material Thickness (mm) - Optional") },
                    placeholder = { Text("Enter thickness for Total Length") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("thickness_text_field"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SlateTextColor,
                        unfocusedTextColor = SlateTextColor
                    )
                )

                // 5. Coil ID (Dropdown & Writable + Quick Select Pills)
                CoilIdSelectorBlock(
                    idInput = idInput,
                    onIdInputChanged = { calculatorViewModel.updateIdInput(it) }
                )
            }
        }

        // Error message display
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            errorMessage?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reset Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    calculatorViewModel.reset()
                },
                modifier = Modifier
                    .weight(0.4f)
                    .height(52.dp)
                    .testTag("reset_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SlateMedium,
                    contentColor = SlateTextColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Calculate Button (Primary Green Button)
            Button(
                onClick = {
                    focusManager.clearFocus()
                    calculatorViewModel.calculate()
                },
                modifier = Modifier
                    .weight(0.6f)
                    .height(52.dp)
                    .testTag("calculate_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Calculate", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }

        // Results Section & Live Coil 2D Schematic
        if (outerDiameter != null) {
            ResultsDisplaySection(
                od = outerDiameter ?: 0.0,
                wallThickness = wallThickness ?: 0.0,
                totalLength = totalLength,
                isThicknessProvided = thicknessInput.isNotBlank()
            )

            InteractiveCoilPreview(
                od = outerDiameter ?: 0.0,
                id = idInput.toDoubleOrNull() ?: 508.0,
                wallThickness = wallThickness ?: 0.0
            )
        } else {
            // Default placeholder schematic showing user has not calculated yet
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateDark.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, SlateMedium.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InteractiveCoilPreview(
                        od = 1000.0,
                        id = 508.0,
                        wallThickness = 246.0,
                        isPlaceholder = true
                    )
                    Text(
                        text = "Enter weight and dimensions, then click Calculate to view dynamic scale model blueprint.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = SlateLight,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun AppHeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Compact industrial symbol decoration
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Fe",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = SlateDarkest,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "DEVELOPER SAQIB MURTAZA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateLight,
                letterSpacing = 1.5.sp
            )
        }
        Text(
            text = "Coil OD Calculator",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = SlateTextColor,
            lineHeight = 28.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun MetalSelectionDropdown(
    selectedMetal: Metal,
    onMetalSelected: (Metal) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Select Metal / Material",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SlateLight
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("metal_dropdown_trigger")
        ) {
            OutlinedButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SlateMedium),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SlateDarkest,
                    contentColor = SlateTextColor
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedMetal.name} (${selectedMetal.density} g/cm³)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown indicator",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(SlateDark)
            ) {
                standardMetals.forEach { metal ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = metal.name,
                                    color = SlateTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${metal.density} g/cm³",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        },
                        onClick = {
                            onMetalSelected(metal)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WeightInputRow(
    weightValue: String,
    weightUnit: String,
    onWeightChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Coil Weight",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SlateLight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Writable weight amount
            OutlinedTextField(
                value = weightValue,
                onValueChange = onWeightChanged,
                placeholder = { Text("Enter weight (blank)") },
                modifier = Modifier
                    .weight(0.6f)
                    .testTag("weight_text_field"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SlateTextColor,
                    unfocusedTextColor = SlateTextColor
                )
            )

            // Weight Unit selector
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .testTag("weight_unit_dropdown")
            ) {
                OutlinedButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SlateMedium),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SlateDarkest,
                        contentColor = SlateTextColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = weightUnit,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Indicator",
                            tint = SlateLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier.background(SlateDark)
                ) {
                    listOf("kg", "Metric Ton (MT)").forEach { u ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = u,
                                    color = SlateTextColor,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                onUnitChanged(u)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoilIdSelectorBlock(
    idInput: String,
    onIdInputChanged: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Coil ID",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SlateLight
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = idInput,
                onValueChange = onIdInputChanged,
                placeholder = { Text("Enter or select ID (mm)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("id_text_field"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SlateTextColor,
                    unfocusedTextColor = SlateTextColor
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.testTag("id_dropdown_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Standard ID Values",
                            tint = SlateLight
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.background(SlateDark)
            ) {
                listOf("508", "610").forEach { preset ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$preset mm",
                                color = SlateTextColor,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            onIdInputChanged(preset)
                            isExpanded = false
                        }
                    )
                }
            }
        }

        // Quick Select Assist Chips below
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Select:",
                fontSize = 11.sp,
                color = SlateLight,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(2.dp))

            listOf("508", "610").forEach { valSelection ->
                val isSelected = idInput == valSelection
                InputChip(
                    selected = isSelected,
                    onClick = { onIdInputChanged(valSelection) },
                    label = {
                        Text(
                            text = "$valSelection mm",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("id_chip_$valSelection"),
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = SlateDarkest,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        labelColor = SlateTextColor
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalvanizedGsmSelectorBlock(
    gsmInput: String,
    onGsmChanged: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SlateDarkest.copy(alpha = 0.5f))
            .border(1.dp, SlateMedium.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column {
            Text(
                text = "Zinc Coating Weight (GSM)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Dual-side coating weight in g/m²",
                fontSize = 10.sp,
                color = SlateLight
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = gsmInput,
                onValueChange = onGsmChanged,
                label = { Text("Zinc Coating Weight (GSM)") },
                placeholder = { Text("Enter GSM (e.g. 120, 275)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gsm_text_field"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SlateTextColor,
                    unfocusedTextColor = SlateTextColor
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.testTag("gsm_dropdown_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Standard GSM Values",
                            tint = SlateLight
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.background(SlateDark)
            ) {
                listOf("40", "60", "90", "120", "275").forEach { preset ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$preset GSM",
                                color = SlateTextColor,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            onGsmChanged(preset)
                            isExpanded = false
                        }
                    )
                }
            }
        }

        // Quick Select Assist Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Presets:",
                fontSize = 11.sp,
                color = SlateLight,
                fontWeight = FontWeight.Medium
            )

            listOf("40", "90", "120", "275").forEach { valSelection ->
                val isSelected = gsmInput == valSelection
                InputChip(
                    selected = isSelected,
                    onClick = { onGsmChanged(valSelection) },
                    label = {
                        Text(
                            text = "$valSelection GSM",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("gsm_chip_$valSelection"),
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = SlateDark,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        labelColor = SlateTextColor
                    )
                )
            }
        }
    }
}

@Composable
fun ResultsDisplaySection(
    od: Double,
    wallThickness: Double,
    totalLength: Double?,
    isThicknessProvided: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SlateDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CALCULATION RESULTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )

            // Result values
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Single Side OD (Calculated and displayed prominently at the top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Single Side OD", fontSize = 13.sp, color = SlateLight, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Formula: (OD - ID) / 2",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f mm", wallThickness),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SlateTextColor,
                        modifier = Modifier.testTag("wall_thickness_result_text")
                    )
                }

                Divider(color = SlateMedium, thickness = 0.8.dp)

                // 2. Overall Outer Diameter (OD) - placed second
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Outer Diameter (OD)", fontSize = 13.sp, color = SlateLight, fontWeight = FontWeight.Medium)
                        Text("Calculated mm based on input density", fontSize = 10.sp, color = SlateLight.copy(alpha = 0.7f))
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f mm", od),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextColor,
                        modifier = Modifier.testTag("od_result_text")
                    )
                }

                Divider(color = SlateMedium, thickness = 0.8.dp)

                // 3. Total Length
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Length", fontSize = 13.sp, color = SlateLight, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (isThicknessProvided) "Calculated in meters" else "Provide optional thickness",
                            fontSize = 10.sp,
                            color = SlateLight.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = if (totalLength != null) {
                            String.format(Locale.getDefault(), "%.2f m", totalLength)
                        } else {
                            "Total Length: N/A"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalLength != null) MaterialTheme.colorScheme.primary else SlateLight,
                        modifier = Modifier.testTag("total_length_result_text")
                    )
                }
            }
        }
    }
}

/**
 * Custom 2D Steel Coil Schematic widget.
 * Draws concentric layers, measurement indicators, ID/OD/Wall label markers.
 */
@Composable
fun InteractiveCoilPreview(
    od: Double,
    id: Double,
    wallThickness: Double,
    isPlaceholder: Boolean = false
) {
    val steelColor = Color(0xFFB0BEC5)
    val steelGradStart = Color(0xFFECEFF1)
    val steelGradEnd = Color(0xFF78909C)
    
    val primaryHighlight = if (isPlaceholder) SlateLight else MaterialTheme.colorScheme.primary
    val dimTextColor = SlateTextColor.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .background(SlateDarkest, RoundedCornerShape(12.dp))
            .border(1.dp, SlateMedium.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f - 16f // push up slightly to make room for bottom text label

            // Bounds constraints
            val maxDrawRadius = size.height * 0.35f
            val calculatedRadiusRatio = id / od
            
            val outerRadiusPx = maxDrawRadius
            val innerRadiusPx = outerRadiusPx * calculatedRadiusRatio.toFloat().coerceIn(0.2f, 0.8f)

            // 1. Draw outer coil bulk using a refined metallic radial gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isPlaceholder) {
                        listOf(SlateMedium, SlateDark)
                    } else {
                        listOf(steelGradStart, steelGradEnd, SlateDark)
                    },
                    center = Offset(cx - innerRadiusPx * 0.3f, cy - innerRadiusPx * 0.3f), // slight metallic shine angle
                    radius = outerRadiusPx
                ),
                radius = outerRadiusPx,
                center = Offset(cx, cy)
            )

            // 2. Draw wounding rings to signify rolled steel sheet layers
            val ringStep = 5f
            var currentRingRadius = innerRadiusPx + ringStep
            while (currentRingRadius < outerRadiusPx) {
                drawCircle(
                    color = steelColor.copy(alpha = if (isPlaceholder) 0.08f else 0.18f),
                    radius = currentRingRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1f)
                )
                currentRingRadius += ringStep
            }

            // 3. Draw hollow core box (Coil Inner Hole)
            drawCircle(
                color = SlateDarkest,
                radius = innerRadiusPx,
                center = Offset(cx, cy)
            )

            // Inner center border representation
            drawCircle(
                color = primaryHighlight.copy(alpha = 0.4f),
                radius = innerRadiusPx,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )

            // Outer boundary border
            drawCircle(
                color = primaryHighlight,
                radius = outerRadiusPx,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )

            // 4. Architectural Dimension Guides
            // Draw Center Pivot Point
            drawCircle(
                color = primaryHighlight,
                radius = 4f,
                center = Offset(cx, cy)
            )

            // --- ID Dimension Axis (Vertical measuring) ---
            // Draw ID measurement line in the center hole
            val idLineStartY = cy - innerRadiusPx
            val idLineEndY = cy + innerRadiusPx
            drawLine(
                color = primaryHighlight.copy(alpha = 0.8f),
                start = Offset(cx, idLineStartY),
                end = Offset(cx, idLineEndY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
            )
            // Little horizontal ticks at ID boundaries
            drawLine(
                color = primaryHighlight,
                start = Offset(cx - 10f, idLineStartY),
                end = Offset(cx + 10f, idLineStartY),
                strokeWidth = 2f
            )
            drawLine(
                color = primaryHighlight,
                start = Offset(cx - 10f, idLineEndY),
                end = Offset(cx + 10f, idLineEndY),
                strokeWidth = 2f
            )

            // --- Wall Thickness Indicator (Horizontal left boundary measurement) ---
            val wallStart = cx - outerRadiusPx
            val wallEnd = cx - innerRadiusPx
            drawLine(
                color = primaryHighlight,
                start = Offset(wallStart, cy),
                end = Offset(wallEnd, cy),
                strokeWidth = 3f
            )
            // Boundary ticks
            drawLine(
                color = primaryHighlight,
                start = Offset(wallStart, cy - 8f),
                end = Offset(wallStart, cy + 8f),
                strokeWidth = 3f
            )
            drawLine(
                color = primaryHighlight,
                start = Offset(wallEnd, cy - 8f),
                end = Offset(wallEnd, cy + 8f),
                strokeWidth = 3f
            )

            // --- OD Dimension Axis (Dotted right horizontal extension guide) ---
            val odGuideY = cy + outerRadiusPx + 16f
            drawLine(
                color = SlateLight.copy(alpha = 0.5f),
                start = Offset(cx - outerRadiusPx, cy),
                end = Offset(cx - outerRadiusPx, odGuideY + 10f),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
            drawLine(
                color = SlateLight.copy(alpha = 0.5f),
                start = Offset(cx + outerRadiusPx, cy),
                end = Offset(cx + outerRadiusPx, odGuideY + 10f),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
            // Outer Dimension Arrow Line
            drawLine(
                color = SlateLight,
                start = Offset(cx - outerRadiusPx, odGuideY),
                end = Offset(cx + outerRadiusPx, odGuideY),
                strokeWidth = 2f
            )
            // Draw Ticks for Outer width line
            drawLine(
                color = SlateLight,
                start = Offset(cx - outerRadiusPx, odGuideY - 6f),
                end = Offset(cx - outerRadiusPx, odGuideY + 6f),
                strokeWidth = 2f
            )
            drawLine(
                color = SlateLight,
                start = Offset(cx + outerRadiusPx, odGuideY - 6f),
                end = Offset(cx + outerRadiusPx, odGuideY + 6f),
                strokeWidth = 2f
            )
        }

        // Overlay text labels precisely configured as UI layouts
        val calculatedPercent = if (isPlaceholder) "" else " (Scale Preview)"
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // OD bottom dimensions label
            Text(
                text = String.format(Locale.getDefault(), "OD ≈ %.1f mm", od),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(SlateMedium.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            // ID Center Hole dimensions label
            Text(
                text = String.format(Locale.getDefault(), "%.0f mm ID", id),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = SlateLight,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(SlateDarkest.copy(alpha = 0.8f))
                    .padding(2.dp)
            )

            // Side-OD top label (positioned over the left radial thickness)
            Text(
                text = String.format(Locale.getDefault(), "Side OD\n%.1f mm", wallThickness),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = primaryHighlight,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .background(SlateDarkest.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )

            // Blueprint mode watermark tag
            Text(
                text = "COIL BLUEPRINT${calculatedPercent.uppercase()}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = primaryHighlight.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .border(
                        BorderStroke(0.5.dp, primaryHighlight.copy(alpha = 0.5f)),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                letterSpacing = 1.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewMainScreen() {
    MyApplicationTheme(darkTheme = true) {
        Surface(color = SlateDarkest) {
            MainScreen()
        }
    }
}

@Composable
fun AdmobBannerView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp),
        factory = { context ->
            try {
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = "ca-app-pub-2545455463429976/3293849530"
                    loadAd(AdRequest.Builder().build())
                }
            } catch (t: Throwable) {
                android.view.View(context).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            }
        }
    )
}
