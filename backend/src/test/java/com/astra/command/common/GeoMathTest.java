package com.astra.command.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeoMathTest {
    @Test
    void haversineDistanceIsNonZeroForDifferentPoints() {
        assertThat(GeoMath.haversineKm(12.9716, 77.5946, 12.9730, 77.5961)).isBetween(0.2, 0.3);
    }
}
