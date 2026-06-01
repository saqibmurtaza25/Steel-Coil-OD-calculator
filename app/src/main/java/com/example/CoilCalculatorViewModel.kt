package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.sqrt

data class Metal(val name: String, val density: Double)

val standardMetals = listOf(
    Metal("Carbon Steel", 7.85),
    Metal("High Carbon Steel", 7.85),
    Metal("Galvanized", 7.85),
    Metal("Stainless Steel", 8.00),
    Metal("Iron", 7.87),
    Metal("Copper", 8.96),
    Metal("Silver", 10.49),
    Metal("Gold", 19.32),
    Metal("Zinc", 7.14),
    Metal("Aluminum", 2.70),
    Metal("Alu-Zinc (Galvalume)", 3.75),
    Metal("Lead", 11.34)
)

class CoilCalculatorViewModel : ViewModel() {

    // Inputs States
    private val _selectedMetal = MutableStateFlow(standardMetals[0])
    val selectedMetal: StateFlow<Metal> = _selectedMetal.asStateFlow()

    private val _weightInput = MutableStateFlow("")
    val weightInput: StateFlow<String> = _weightInput.asStateFlow()

    private val _weightUnit = MutableStateFlow("kg")
    val weightUnit: StateFlow<String> = _weightUnit.asStateFlow()

    private val _widthInput = MutableStateFlow("1219")
    val widthInput: StateFlow<String> = _widthInput.asStateFlow()

    private val _thicknessInput = MutableStateFlow("")
    val thicknessInput: StateFlow<String> = _thicknessInput.asStateFlow()

    private val _idInput = MutableStateFlow("508")
    val idInput: StateFlow<String> = _idInput.asStateFlow()

    // Calculation Result States
    private val _outerDiameter = MutableStateFlow<Double?>(null)
    val outerDiameter: StateFlow<Double?> = _outerDiameter.asStateFlow()

    private val _wallThickness = MutableStateFlow<Double?>(null)
    val wallThickness: StateFlow<Double?> = _wallThickness.asStateFlow()

    private val _totalLength = MutableStateFlow<Double?>(null)
    val totalLength: StateFlow<Double?> = _totalLength.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Setter Methods
    fun selectMetal(metal: Metal) {
        _selectedMetal.value = metal
    }

    fun updateWeight(weight: String) {
        // Allow digit, decimal separator
        _weightInput.value = weight.filter { it.isDigit() || it == '.' }
    }

    fun updateWeightUnit(unit: String) {
        _weightUnit.value = unit
    }

    fun updateWidth(width: String) {
        _widthInput.value = width.filter { it.isDigit() || it == '.' }
    }

    fun updateThickness(thickness: String) {
        _thicknessInput.value = thickness.filter { it.isDigit() || it == '.' }
    }

    fun updateIdInput(id: String) {
        _idInput.value = id.filter { it.isDigit() || it == '.' }
    }

    // Reset Inputs and Outputs
    fun reset() {
        _selectedMetal.value = standardMetals[0]
        _weightInput.value = ""
        _weightUnit.value = "kg"
        _widthInput.value = "1219"
        _thicknessInput.value = ""
        _idInput.value = "508"
        
        _outerDiameter.value = null
        _wallThickness.value = null
        _totalLength.value = null
        _errorMessage.value = null
    }

    // Perform Calculation
    fun calculate() {
        val weightVal = _weightInput.value.toDoubleOrNull()
        if (weightVal == null || weightVal <= 0.0) {
            _errorMessage.value = "Weight must be a positive number greater than 0."
            clearResults()
            return
        }

        val widthVal = _widthInput.value.toDoubleOrNull()
        if (widthVal == null || widthVal <= 0.0) {
            _errorMessage.value = "Coil width must be a positive number greater than 0."
            clearResults()
            return
        }

        val idVal = _idInput.value.toDoubleOrNull()
        if (idVal == null || idVal <= 0.0) {
            _errorMessage.value = "Coil ID must be a positive number greater than 0."
            clearResults()
            return
        }

        val thicknessVal = if (_thicknessInput.value.isNotBlank()) {
            val t = _thicknessInput.value.toDoubleOrNull()
            if (t == null || t <= 0.0) {
                _errorMessage.value = "Thickness must be a positive number."
                clearResults()
                return
            }
            t
        } else {
            null
        }

        val density = _selectedMetal.value.density

        // Conversion logic:
        // Weight input could be KG or MT (Metric Tons)
        // 1 KG = 1000g, 1 MT = 1,000,000g
        val weightInGrams = if (_weightUnit.value == "kg") {
            weightVal * 1000.0
        } else {
            weightVal * 1000000.0
        }

        // Calculation:
        // OD_mm = sqrt( (4000 * Weight_grams) / (pi * Width_mm * Density_g/cm3) + ID_mm^2 )
        val denominator = PI * widthVal * density
        val firstTerm = (4000.0 * weightInGrams) / denominator
        val idSq = idVal * idVal
        val odSq = firstTerm + idSq

        if (odSq < 0.0) {
            _errorMessage.value = "Calculation error: Invalid physical dimensions."
            clearResults()
            return
        }

        val odResult = sqrt(odSq)
        val wallThicknessResult = (odResult - idVal) / 2.0

        // Optional Total Length Calculation:
        // L_meters = (pi * (OD^2 - ID^2)) / (4000 * thickness_mm)
        val totalLengthResult = if (thicknessVal != null) {
            val numerator = PI * (odSq - idSq)
            val divVal = 4000.0 * thicknessVal
            numerator / divVal
        } else {
            null
        }

        // Successfully calculated
        _errorMessage.value = null
        _outerDiameter.value = odResult
        _wallThickness.value = wallThicknessResult
        _totalLength.value = totalLengthResult
    }

    private fun clearResults() {
        _outerDiameter.value = null
        _wallThickness.value = null
        _totalLength.value = null
    }
}
