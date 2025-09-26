package com.gw.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class SpatialExtentValidatorTest {

    @Test
    @Timeout(10)
    void testValidateWithValidInput() {
        // Given
        String north = "38.0";
        String south = "37.0";
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertTrue(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithInvalidLatitudeNorth() {
        // Given
        String north = "91.0"; // Invalid latitude
        String south = "37.0";
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithInvalidLatitudeSouth() {
        // Given
        String north = "38.0";
        String south = "-91.0"; // Invalid latitude
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithInvalidLongitudeWest() {
        // Given
        String north = "38.0";
        String south = "37.0";
        String west = "-181.0"; // Invalid longitude
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithInvalidLongitudeEast() {
        // Given
        String north = "38.0";
        String south = "37.0";
        String west = "-78.0";
        String east = "181.0"; // Invalid longitude
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithSouthGreaterThanNorth() {
        // Given
        String north = "37.0";
        String south = "38.0"; // South greater than north
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithWestGreaterThanEast() {
        // Given
        String north = "38.0";
        String south = "37.0";
        String west = "-77.0";
        String east = "-78.0"; // East less than west
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithSpatialExtentTooLarge() {
        // Given
        String north = "40.0";
        String south = "37.0";
        String west = "-80.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);
        });
    }

    @Test
    @Timeout(10)
    void testValidateWithInvalidProjection() {
        // Given
        String north = "38.0";
        String south = "37.0";
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:3857"; // Invalid projection
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithInvalidNumberFormat() {
        // Given
        String north = "invalid";
        String south = "37.0";
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);
        });
    }

    @Test
    @Timeout(10)
    void testValidateWithNullInputs() {
        // Given
        String north = null;
        String south = "37.0";
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);
        });
    }

    @Test
    @Timeout(10)
    void testValidateWithEmptyStrings() {
        // Given
        String north = "";
        String south = "37.0";
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);
        });
    }

    @Test
    @Timeout(10)
    void testValidateWithBoundaryValues() {
        // Given
        String north = "37.5";
        String south = "37.0";
        String west = "-77.5";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertTrue(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithExactOneDegreeExtent() {
        // Given
        String north = "38.0";
        String south = "37.0";
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertTrue(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithSmallExtent() {
        // Given
        String north = "37.5";
        String south = "37.0";
        String west = "-77.5";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertTrue(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithZeroExtent() {
        // Given
        String north = "37.0";
        String south = "37.0";
        String west = "-77.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testValidateWithNegativeValues() {
        // Given
        String north = "-37.0";
        String south = "-38.0";
        String west = "-78.0";
        String east = "-77.0";
        String proj = "EPSG:4326";
        String productcategory = "test";

        // When
        boolean result = SpatialExtentValidator.validate(north, south, west, east, proj, productcategory);

        // Then
        assertTrue(result);
    }
}
