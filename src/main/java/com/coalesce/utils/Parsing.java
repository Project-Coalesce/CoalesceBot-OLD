package com.coalesce.utils;

import java.util.Optional;

public class Parsing {
    public static Optional<Double> parseDouble(String sequence) {
        try {
            return Optional.of(Double.parseDouble(sequence));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
