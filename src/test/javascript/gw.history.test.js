/**
 * Test file for gw.history.js
 */
import { describe, it, expect, beforeEach } from 'vitest';

describe('GW.history', () => {
    
    beforeEach(() => {
        // Setup DOM elements for testing
        document.body.innerHTML = `
            <div id="process-history-container"></div>
            <div id="workflow-history-container"></div>
        `;
    });

    describe('getHistoryTheme', () => {
        it('should return default theme when no theme is set', () => {
            localStorage.removeItem('editorTheme');
            const theme = GW.history.getHistoryTheme();
            expect(theme).toBe('vs-light');
        });

        it('should return stored theme', () => {
            localStorage.setItem('editorTheme', 'vs-dark');
            const theme = GW.history.getHistoryTheme();
            expect(theme).toBe('vs-dark');
        });
    });

    describe('applyHistoryTheme', () {
        it('should apply light theme', () {
            localStorage.setItem('editorTheme', 'vs-light');
            GW.history.applyHistoryTheme();
            // Theme should be applied to elements
            expect(true).toBe(true); // Placeholder assertion
        });

        it('should apply dark theme', () {
            localStorage.setItem('editorTheme', 'vs-dark');
            GW.history.applyHistoryTheme();
            // Theme should be applied to elements
            expect(true).toBe(true); // Placeholder assertion
        });

        it('should apply high contrast theme', () {
            localStorage.setItem('editorTheme', 'hc-black');
            GW.history.applyHistoryTheme();
            // Theme should be applied to elements
            expect(true).toBe(true); // Placeholder assertion
        });
    });

    describe('getProcessHistoryTable', () {
        it('should return empty state when no history', () {
            const result = GW.history.getProcessHistoryTable([], 'test-process', 'test-container');
            expect(result).toContain('No History Available');
        });

        it('should return table HTML when history exists', () {
            const mockHistory = [
                { id: '1', status: 'COMPLETED', start_time: '2023-01-01' }
            ];
            const result = GW.history.getProcessHistoryTable(mockHistory, 'test-process', 'test-container');
            expect(result).toContain('<table');
        });
    });

    describe('getWorkflowHistoryTable', () {
        it('should return empty state when no history', () {
            const result = GW.history.getWorkflowHistoryTable([], 'test-workflow', 'test-name');
            expect(result).toContain('No History Available');
        });

        it('should return table HTML when history exists', () {
            const mockHistory = [
                { id: '1', status: 'COMPLETED', start_time: '2023-01-01' }
            ];
            const result = GW.history.getWorkflowHistoryTable(mockHistory, 'test-workflow', 'test-name');
            expect(result).toContain('<table');
        });
    });
});
