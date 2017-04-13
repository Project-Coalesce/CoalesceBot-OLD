package com.coalesce.utils;

public enum TemperatureUnit {
    FAHRENHEIT() {
        @Override
        public double toCelsius(double from) {
            return (from - 32) / 1.8;
        }

        @Override
        public double toFahrenheit(double from) {
            return from;
        }

        @Override
        public double toKelvin(double from) {
            return (from + 459.69) / 1.8;
        }

        @Override
        public double convert(double from, TemperatureUnit unit) {
            return unit.toFahrenheit(from);
        }
    },
    CELSIUS() {
        public double toCelsius(double from) {
            return from;
        }

        public double toFahrenheit(double from) {
            return (from * 1.8) + 32;
        }

        public double toKelvin(double from) {
            return from + 273.15;
        }

        public double convert(double from, TemperatureUnit unit) {
            return unit.toCelsius(from);
        }
    },
    KELVIN() {
        @Override
        public double toCelsius(double from) {
            return from - 273.15;
        }

        @Override
        public double toKelvin(double from) {
            return from;
        }

        @Override
        public double toFahrenheit(double from) {
            return (from * 1.8) - 459.67;
        }

        @Override
        public double convert(double from, TemperatureUnit unit) {
            return unit.toKelvin(from);
        }
    };

    public double toCelsius(double from) {
        throw new AbstractMethodError();
    }

    public double toFahrenheit(double from) {
        throw new AbstractMethodError();
    }

    public double toKelvin(double from) {
        throw new AbstractMethodError();
    }

    public double convert(double from, TemperatureUnit unit) {
        throw new AbstractMethodError();
    }
}
