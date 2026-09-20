package com.ojuaw.omniconvert.converter

enum class Category(val displayName: String) {
    LENGTH("Length"),
    WEIGHT("Weight / Mass"),
    TEMPERATURE("Temperature"),
    SPEED("Speed"),
    DIGITAL("Digital Storage"),
    VOLUME("Volume"),
    AREA("Area")
}

data class UnitItem(val name: String, val toBaseFactor: Double)

object ConversionEngine {

    val unitsByCategory = mapOf(
        Category.LENGTH to listOf(
            UnitItem("Meters (m)", 1.0),
            UnitItem("Kilometers (km)", 1000.0),
            UnitItem("Centimeters (cm)", 0.01),
            UnitItem("Millimeters (mm)", 0.001),
            UnitItem("Miles (mi)", 1609.344),
            UnitItem("Yards (yd)", 0.9144),
            UnitItem("Feet (ft)", 0.3048),
            UnitItem("Inches (in)", 0.0254)
        ),
        Category.WEIGHT to listOf(
            UnitItem("Kilograms (kg)", 1.0),
            UnitItem("Grams (g)", 0.001),
            UnitItem("Milligrams (mg)", 0.000001),
            UnitItem("Metric Tons (t)", 1000.0),
            UnitItem("Pounds (lb)", 0.45359237),
            UnitItem("Ounces (oz)", 0.02834952),
            UnitItem("Stones (st)", 6.350293)
        ),
        Category.TEMPERATURE to listOf(
            UnitItem("Celsius (°C)", 1.0),
            UnitItem("Fahrenheit (°F)", 1.0),
            UnitItem("Kelvin (K)", 1.0)
        ),
        Category.SPEED to listOf(
            UnitItem("Kilometers per hour (km/h)", 1.0),
            UnitItem("Miles per hour (mph)", 1.60934),
            UnitItem("Meters per second (m/s)", 3.6),
            UnitItem("Knots (kn)", 1.852)
        ),
        Category.DIGITAL to listOf(
            UnitItem("Bytes (B)", 1.0),
            UnitItem("Kilobytes (KB)", 1024.0),
            UnitItem("Megabytes (MB)", 1048576.0),
            UnitItem("Gigabytes (GB)", 1073741824.0),
            UnitItem("Terabytes (TB)", 1099511627776.0)
        ),
        Category.VOLUME to listOf(
            UnitItem("Liters (L)", 1.0),
            UnitItem("Milliliters (mL)", 0.001),
            UnitItem("Gallons (US gal)", 3.78541),
            UnitItem("Quarts (US qt)", 0.946353),
            UnitItem("Fluid Ounces (fl oz)", 0.0295735)
        ),
        Category.AREA to listOf(
            UnitItem("Square Meters (m²)", 1.0),
            UnitItem("Square Kilometers (km²)", 1000000.0),
            UnitItem("Square Feet (ft²)", 0.092903),
            UnitItem("Acres (ac)", 4046.86),
            UnitItem("Hectares (ha)", 10000.0)
        )
    )

    fun convert(category: Category, value: Double, fromUnit: String, toUnit: String): Double {
        if (category == Category.TEMPERATURE) {
            return convertTemperature(value, fromUnit, toUnit)
        }

        val list = unitsByCategory[category] ?: return value
        val fromItem = list.find { it.name == fromUnit } ?: return value
        val toItem = list.find { it.name == toUnit } ?: return value

        val baseValue = value * fromItem.toBaseFactor
        return baseValue / toItem.toBaseFactor
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        val celsius = when {
            from.contains("Celsius") -> value
            from.contains("Fahrenheit") -> (value - 32.0) * (5.0 / 9.0)
            from.contains("Kelvin") -> value - 273.15
            else -> value
        }

        return when {
            to.contains("Celsius") -> celsius
            to.contains("Fahrenheit") -> (celsius * (9.0 / 5.0)) + 32.0
            to.contains("Kelvin") -> celsius + 273.15
            else -> celsius
        }
    }
}