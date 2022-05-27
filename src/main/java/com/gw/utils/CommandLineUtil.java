package com.gw.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandLineUtil {
    
    /**
     * This class is used to create a table of data from a list of objects on the command line.
     */
    public static class CommandLineTable {
        private static final String horizontalSeparator = "-";
        private static final String verticalSeparator = "|";
        private static final String joiner = "+";


        private String[] headers;
        private List<String[]> rows = new ArrayList<>();

        public void setHeaders(String[] headers) {
            this.headers = headers;
        }

        public void addRow(String... rowValues) {
            this.rows.add(rowValues);
        }

        private void printLines(int[] columnWidths) {
            for (int i = 0; i < columnWidths.length; i++) {
                String line = String.join("", Collections.nCopies(columnWidths[i] +
                        verticalSeparator.length() + 1, horizontalSeparator));
                System.out.print(joiner + line + (i == columnWidths.length - 1 ? joiner : ""));
            }
            System.out.println();
        }

        private void printRows(Object[] rowCells, int[] maxColumnWidths) {
            for (int i = 0; i < rowCells.length; i++) {
                String s = rowCells[i] == null ? "-" : rowCells[i].toString();
                String verStrTemp = i == rowCells.length - 1 ? verticalSeparator : "";
                
                System.out.printf("%s %-" + maxColumnWidths[i] + "s %s", verticalSeparator, s, verStrTemp);
                
            }
            System.out.println();
        }

        public void print() {
            // Calculate column widths
            int[] maxColumnWidths = Arrays.stream(headers).mapToInt(String::length).toArray();
            for (String[] cells : rows) {
                if (maxColumnWidths == null) {
                    maxColumnWidths = new int[cells.length];
                }
                if (cells.length != maxColumnWidths.length) {
                    throw new IllegalArgumentException("Number of row-cells and headers should be consistent");
                }
                for (int i = 0; i < cells.length; i++) {
                    // Check if cells is null (in some cases it can be)
                    if (cells[i] == null) {
                        maxColumnWidths[i] = Math.max(maxColumnWidths[i], 1);
                    }else {
                        maxColumnWidths[i] = Math.max(maxColumnWidths[i], cells[i].length());
                    }
                }
            }

            // First print the headers
            printLines(maxColumnWidths);
            printRows(headers, maxColumnWidths);
            printLines(maxColumnWidths);

            // Then print the rows
            for (Object row : rows) {
                Object[] rowCells = (Object[]) row;
                printRows(rowCells, maxColumnWidths);
            }

            // Finally print the footer
            printLines(maxColumnWidths);
        }
    }
}
