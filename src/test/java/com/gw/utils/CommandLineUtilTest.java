package com.gw.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineUtilTest {

    @Test
    @Timeout(10)
    void testCommandLineTableInstantiation() {
        // Given & When
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();

        // Then
        assertNotNull(table);
    }

    @Test
    @Timeout(10)
    void testCommandLineTableSetHeaders() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] headers = {"Name", "Age", "City"};

        // When
        table.setHeaders(headers);

        // Then
        assertNotNull(table);
        // Note: We can't directly access headers field, but we can test that no exception is thrown
    }

    @Test
    @Timeout(10)
    void testCommandLineTableAddRow() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] row = {"John", "25", "New York"};

        // When
        table.addRow(row);

        // Then
        assertNotNull(table);
        // Note: We can't directly access rows field, but we can test that no exception is thrown
    }

    @Test
    @Timeout(10)
    void testCommandLineTableAddRowWithNullValues() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] row = {"John", null, "New York"};

        // When
        table.addRow(row);

        // Then
        assertNotNull(table);
        // Note: We can't directly access rows field, but we can test that no exception is thrown
    }

    @Test
    @Timeout(10)
    void testCommandLineTableAddRowWithEmptyArray() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] row = {};

        // When
        table.addRow(row);

        // Then
        assertNotNull(table);
        // Note: We can't directly access rows field, but we can test that no exception is thrown
    }

    @Test
    @Timeout(10)
    void testCommandLineTableAddRowWithMultipleRows() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] row1 = {"John", "25", "New York"};
        String[] row2 = {"Jane", "30", "Boston"};
        String[] row3 = {"Bob", "35", "Chicago"};

        // When
        table.addRow(row1);
        table.addRow(row2);
        table.addRow(row3);

        // Then
        assertNotNull(table);
        // Note: We can't directly access rows field, but we can test that no exception is thrown
    }

    @Test
    @Timeout(10)
    void testCommandLineTablePrintWithoutHeaders() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] row = {"John", "25", "New York"};

        // When
        table.addRow(row);

        // Then
        assertThrows(NullPointerException.class, () -> {
            table.print();
        });
    }

    @Test
    @Timeout(10)
    void testCommandLineTablePrintWithHeaders() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] headers = {"Name", "Age", "City"};
        String[] row = {"John", "25", "New York"};

        // When
        table.setHeaders(headers);
        table.addRow(row);

        // Then
        assertDoesNotThrow(() -> {
            table.print();
        });
    }

    @Test
    @Timeout(10)
    void testCommandLineTablePrintWithInconsistentRowLength() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] headers = {"Name", "Age", "City"};
        String[] row = {"John", "25"}; // Missing one column

        // When
        table.setHeaders(headers);
        table.addRow(row);

        // Then
        assertThrows(IllegalArgumentException.class, () -> {
            table.print();
        });
    }

    @Test
    @Timeout(10)
    void testCommandLineTablePrintWithMultipleRows() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] headers = {"Name", "Age", "City"};
        String[] row1 = {"John", "25", "New York"};
        String[] row2 = {"Jane", "30", "Boston"};

        // When
        table.setHeaders(headers);
        table.addRow(row1);
        table.addRow(row2);

        // Then
        assertDoesNotThrow(() -> {
            table.print();
        });
    }

    @Test
    @Timeout(10)
    void testCommandLineTablePrintWithLongValues() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] headers = {"Name", "Description"};
        String[] row = {"John", "This is a very long description that should be handled properly by the table"};

        // When
        table.setHeaders(headers);
        table.addRow(row);

        // Then
        assertDoesNotThrow(() -> {
            table.print();
        });
    }

    @Test
    @Timeout(10)
    void testCommandLineTablePrintWithEmptyValues() {
        // Given
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        String[] headers = {"Name", "Age", "City"};
        String[] row = {"", "", ""};

        // When
        table.setHeaders(headers);
        table.addRow(row);

        // Then
        assertDoesNotThrow(() -> {
            table.print();
        });
    }
}
