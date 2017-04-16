package com.coalesce.utils

enum class TemperatureUnit {
    KELVIN() {
        override fun toCelsius(from: Double): Double {
            return from - 273.15
        }

        override fun toKelvin(from: Double): Double {
            return from
        }

        override fun toFahrenheit(from: Double): Double {
            return from * 1.8 - 459.67
        }

        override fun convert(from: Double, unit: TemperatureUnit): Double {
            return unit.toKelvin(from)
        }
    },
    CELSIUS() {
        override fun toCelsius(from: Double): Double {
            return from
        }

        override fun toFahrenheit(from: Double): Double {
            return from * 1.8 + 32
        }

        override fun toKelvin(from: Double): Double {
            return from + 273.15
        }

        override fun convert(from: Double, unit: TemperatureUnit): Double {
            return unit.toCelsius(from)
        }
    },
    FAHRENHEIT() {
        override fun toCelsius(from: Double): Double {
            return (from - 32) / 1.8
        }

        override fun toFahrenheit(from: Double): Double {
            return from
        }

        override fun toKelvin(from: Double): Double {
            return (from + 459.69) / 1.8
        }

        override fun convert(from: Double, unit: TemperatureUnit): Double {
            return unit.toFahrenheit(from)
        }
    };

    open fun toCelsius(from: Double): Double {
        throw AbstractMethodError()
    }

    open fun toFahrenheit(from: Double): Double {
        throw AbstractMethodError()
    }

    open fun toKelvin(from: Double): Double {
        throw AbstractMethodError()
    }

    open fun convert(from: Double, unit: TemperatureUnit): Double {
        throw AbstractMethodError()
    }
}