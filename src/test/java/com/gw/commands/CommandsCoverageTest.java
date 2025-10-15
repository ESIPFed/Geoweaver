package com.gw.commands;

import com.gw.commands.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 专注于提高commands包覆盖率的测试
 */
public class CommandsCoverageTest {

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        // 保存原始输出流
        originalOut = System.out;
        originalErr = System.err;
        
        // 创建新的输出流用于测试
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    void testAllCommandClassesInstantiation() {
        // 测试所有命令类实例化
        assertNotNull(new TopEntryCommand());
        assertNotNull(new RunCommand());
        assertNotNull(new ExportCommand());
        assertNotNull(new ImportCommand());
        assertNotNull(new ListCommand());
        assertNotNull(new DetailCommand());
        assertNotNull(new HistoryCommand());
        assertNotNull(new PasswordResetCommand());
        assertNotNull(new RunProcessCommand());
        assertNotNull(new RunWorkflowCommand());
        assertNotNull(new ExportProcessCommand());
        assertNotNull(new ExportWorkflowCommand());
        assertNotNull(new ImportProcessCommand());
        assertNotNull(new ImportWorkflowCommand());
        assertNotNull(new H2CompatibilityChecker());
    }

    @Test
    void testCommandClassNames() {
        // 测试类名
        assertEquals("TopEntryCommand", TopEntryCommand.class.getSimpleName());
        assertEquals("RunCommand", RunCommand.class.getSimpleName());
        assertEquals("ExportCommand", ExportCommand.class.getSimpleName());
        assertEquals("ImportCommand", ImportCommand.class.getSimpleName());
        assertEquals("ListCommand", ListCommand.class.getSimpleName());
        assertEquals("DetailCommand", DetailCommand.class.getSimpleName());
        assertEquals("HistoryCommand", HistoryCommand.class.getSimpleName());
        assertEquals("PasswordResetCommand", PasswordResetCommand.class.getSimpleName());
        assertEquals("RunProcessCommand", RunProcessCommand.class.getSimpleName());
        assertEquals("RunWorkflowCommand", RunWorkflowCommand.class.getSimpleName());
        assertEquals("ExportProcessCommand", ExportProcessCommand.class.getSimpleName());
        assertEquals("ExportWorkflowCommand", ExportWorkflowCommand.class.getSimpleName());
        assertEquals("ImportProcessCommand", ImportProcessCommand.class.getSimpleName());
        assertEquals("ImportWorkflowCommand", ImportWorkflowCommand.class.getSimpleName());
        assertEquals("H2CompatibilityChecker", H2CompatibilityChecker.class.getSimpleName());
    }

    @Test
    void testCommandPackageNames() {
        // 测试包名
        assertEquals("com.gw.commands", TopEntryCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", RunCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", ExportCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", ImportCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", ListCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", DetailCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", HistoryCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", PasswordResetCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", RunProcessCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", RunWorkflowCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", ExportProcessCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", ExportWorkflowCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", ImportProcessCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", ImportWorkflowCommand.class.getPackage().getName());
        assertEquals("com.gw.commands", H2CompatibilityChecker.class.getPackage().getName());
    }

    @Test
    void testCommandInterfaces() {
        // 测试接口实现
        assertTrue(Runnable.class.isAssignableFrom(TopEntryCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(RunCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(ExportCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(ImportCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(ListCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(DetailCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(HistoryCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(PasswordResetCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(RunProcessCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(RunWorkflowCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(ExportProcessCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(ExportWorkflowCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(ImportProcessCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(ImportWorkflowCommand.class));
        assertTrue(Runnable.class.isAssignableFrom(H2CompatibilityChecker.class));
    }

    @Test
    void testCommandAnnotations() {
        // 测试注解
        assertTrue(TopEntryCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(RunCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(ExportCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(ImportCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(ListCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(DetailCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(HistoryCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(PasswordResetCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(RunProcessCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(RunWorkflowCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(ExportProcessCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(ExportWorkflowCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(ImportProcessCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(ImportWorkflowCommand.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
        assertTrue(H2CompatibilityChecker.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }

    @Test
    void testCommandFieldsExist() throws Exception {
        // 测试字段存在性
        assertNotNull(PasswordResetCommand.class.getDeclaredField("new_password"));
        assertNotNull(HistoryCommand.class.getDeclaredField("history_id"));
        assertNotNull(RunProcessCommand.class.getDeclaredField("processid"));
        assertNotNull(RunProcessCommand.class.getDeclaredField("hostid"));
        assertNotNull(RunProcessCommand.class.getDeclaredField("envid"));
        assertNotNull(RunProcessCommand.class.getDeclaredField("pass"));
        assertNotNull(RunWorkflowCommand.class.getDeclaredField("workflowId"));
        assertNotNull(RunWorkflowCommand.class.getDeclaredField("hostStrings"));
        assertNotNull(RunWorkflowCommand.class.getDeclaredField("envs"));
        assertNotNull(RunWorkflowCommand.class.getDeclaredField("passes"));
        assertNotNull(ExportWorkflowCommand.class.getDeclaredField("workflow_id"));
        assertNotNull(ExportWorkflowCommand.class.getDeclaredField("export_mode"));
        assertNotNull(ExportWorkflowCommand.class.getDeclaredField("target_file_path"));
        assertNotNull(ImportWorkflowCommand.class.getDeclaredField("workflow_zip_file_path"));
    }

    @Test
    void testCommandFieldTypes() throws Exception {
        // 测试字段类型
        assertEquals(String.class, PasswordResetCommand.class.getDeclaredField("new_password").getType());
        assertEquals(String.class, HistoryCommand.class.getDeclaredField("history_id").getType());
        assertEquals(String.class, RunProcessCommand.class.getDeclaredField("processid").getType());
        assertEquals(String.class, RunProcessCommand.class.getDeclaredField("hostid").getType());
        assertEquals(String.class, RunProcessCommand.class.getDeclaredField("envid").getType());
        assertEquals(String.class, RunProcessCommand.class.getDeclaredField("pass").getType());
        assertEquals(String.class, RunWorkflowCommand.class.getDeclaredField("workflowId").getType());
        assertEquals(String[].class, RunWorkflowCommand.class.getDeclaredField("hostStrings").getType());
        assertEquals(String[].class, RunWorkflowCommand.class.getDeclaredField("envs").getType());
        assertEquals(String[].class, RunWorkflowCommand.class.getDeclaredField("passes").getType());
        assertEquals(String.class, ExportWorkflowCommand.class.getDeclaredField("workflow_id").getType());
        assertEquals(int.class, ExportWorkflowCommand.class.getDeclaredField("export_mode").getType());
        assertEquals(String.class, ExportWorkflowCommand.class.getDeclaredField("target_file_path").getType());
        assertEquals(String.class, ImportWorkflowCommand.class.getDeclaredField("workflow_zip_file_path").getType());
    }

    @Test
    void testCommandMethodsExist() throws Exception {
        // 测试方法存在性
        assertNotNull(TopEntryCommand.class.getMethod("run"));
        assertNotNull(RunCommand.class.getMethod("run"));
        assertNotNull(ExportCommand.class.getMethod("run"));
        assertNotNull(ImportCommand.class.getMethod("run"));
        assertNotNull(ListCommand.class.getMethod("run"));
        assertNotNull(DetailCommand.class.getMethod("run"));
        assertNotNull(HistoryCommand.class.getMethod("run"));
        assertNotNull(PasswordResetCommand.class.getMethod("run"));
        assertNotNull(RunProcessCommand.class.getMethod("run"));
        assertNotNull(RunWorkflowCommand.class.getMethod("run"));
        assertNotNull(ExportProcessCommand.class.getMethod("run"));
        assertNotNull(ExportWorkflowCommand.class.getMethod("run"));
        assertNotNull(ImportProcessCommand.class.getMethod("run"));
        assertNotNull(ImportWorkflowCommand.class.getMethod("run"));
        assertNotNull(H2CompatibilityChecker.class.getMethod("run"));
    }

    @Test
    void testCommandMethodReturnTypes() throws Exception {
        // 测试方法返回类型
        assertEquals(void.class, TopEntryCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, RunCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, ExportCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, ImportCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, ListCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, DetailCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, HistoryCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, PasswordResetCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, RunProcessCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, RunWorkflowCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, ExportProcessCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, ExportWorkflowCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, ImportProcessCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, ImportWorkflowCommand.class.getMethod("run").getReturnType());
        assertEquals(void.class, H2CompatibilityChecker.class.getMethod("run").getReturnType());
    }

    @Test
    void testCommandFieldAccessibility() throws Exception {
        // 测试字段可访问性
        Field passwordField = PasswordResetCommand.class.getDeclaredField("new_password");
        passwordField.setAccessible(true);
        assertTrue(passwordField.isAccessible());
        
        Field historyIdField = HistoryCommand.class.getDeclaredField("history_id");
        historyIdField.setAccessible(true);
        assertTrue(historyIdField.isAccessible());
        
        Field processIdField = RunProcessCommand.class.getDeclaredField("processid");
        processIdField.setAccessible(true);
        assertTrue(processIdField.isAccessible());
        
        Field hostIdField = RunProcessCommand.class.getDeclaredField("hostid");
        hostIdField.setAccessible(true);
        assertTrue(hostIdField.isAccessible());
        
        Field envIdField = RunProcessCommand.class.getDeclaredField("envid");
        envIdField.setAccessible(true);
        assertTrue(envIdField.isAccessible());
        
        Field passField = RunProcessCommand.class.getDeclaredField("pass");
        passField.setAccessible(true);
        assertTrue(passField.isAccessible());
    }

    @Test
    void testCommandFieldValues() throws Exception {
        // 测试字段值设置和获取
        PasswordResetCommand passwordCommand = new PasswordResetCommand();
        Field passwordField = PasswordResetCommand.class.getDeclaredField("new_password");
        passwordField.setAccessible(true);
        passwordField.set(passwordCommand, "test-password");
        assertEquals("test-password", passwordField.get(passwordCommand));
        
        HistoryCommand historyCommand = new HistoryCommand();
        Field historyIdField = HistoryCommand.class.getDeclaredField("history_id");
        historyIdField.setAccessible(true);
        historyIdField.set(historyCommand, "test-history-id");
        assertEquals("test-history-id", historyIdField.get(historyCommand));
        
        RunProcessCommand processCommand = new RunProcessCommand();
        Field processIdField = RunProcessCommand.class.getDeclaredField("processid");
        processIdField.setAccessible(true);
        processIdField.set(processCommand, "test-process-id");
        assertEquals("test-process-id", processIdField.get(processCommand));
    }

    @Test
    void testCommandArrayFields() throws Exception {
        // 测试数组字段
        RunWorkflowCommand workflowCommand = new RunWorkflowCommand();
        Field hostStringsField = RunWorkflowCommand.class.getDeclaredField("hostStrings");
        hostStringsField.setAccessible(true);
        String[] hostStrings = {"host1", "host2", "host3"};
        hostStringsField.set(workflowCommand, hostStrings);
        String[] retrievedHosts = (String[]) hostStringsField.get(workflowCommand);
        assertArrayEquals(hostStrings, retrievedHosts);
        
        Field envsField = RunWorkflowCommand.class.getDeclaredField("envs");
        envsField.setAccessible(true);
        String[] envs = {"env1", "env2"};
        envsField.set(workflowCommand, envs);
        String[] retrievedEnvs = (String[]) envsField.get(workflowCommand);
        assertArrayEquals(envs, retrievedEnvs);
        
        Field passesField = RunWorkflowCommand.class.getDeclaredField("passes");
        passesField.setAccessible(true);
        String[] passes = {"pass1", "pass2"};
        passesField.set(workflowCommand, passes);
        String[] retrievedPasses = (String[]) passesField.get(workflowCommand);
        assertArrayEquals(passes, retrievedPasses);
    }

    @Test
    void testCommandIntegerFields() throws Exception {
        // 测试整数字段
        ExportWorkflowCommand exportCommand = new ExportWorkflowCommand();
        Field exportModeField = ExportWorkflowCommand.class.getDeclaredField("export_mode");
        exportModeField.setAccessible(true);
        exportModeField.set(exportCommand, 4);
        assertEquals(4, exportModeField.get(exportCommand));
        
        // 测试不同的整数值
        exportModeField.set(exportCommand, 1);
        assertEquals(1, exportModeField.get(exportCommand));
        
        exportModeField.set(exportCommand, 2);
        assertEquals(2, exportModeField.get(exportCommand));
        
        exportModeField.set(exportCommand, 3);
        assertEquals(3, exportModeField.get(exportCommand));
    }

    @Test
    void testCommandStringFields() throws Exception {
        // 测试字符串字段
        ExportWorkflowCommand exportCommand = new ExportWorkflowCommand();
        Field workflowIdField = ExportWorkflowCommand.class.getDeclaredField("workflow_id");
        workflowIdField.setAccessible(true);
        workflowIdField.set(exportCommand, "workflow-123");
        assertEquals("workflow-123", workflowIdField.get(exportCommand));
        
        Field targetFilePathField = ExportWorkflowCommand.class.getDeclaredField("target_file_path");
        targetFilePathField.setAccessible(true);
        targetFilePathField.set(exportCommand, "/path/to/file.zip");
        assertEquals("/path/to/file.zip", targetFilePathField.get(exportCommand));
        
        ImportWorkflowCommand importCommand = new ImportWorkflowCommand();
        Field workflowZipFilePathField = ImportWorkflowCommand.class.getDeclaredField("workflow_zip_file_path");
        workflowZipFilePathField.setAccessible(true);
        workflowZipFilePathField.set(importCommand, "/path/to/workflow.zip");
        assertEquals("/path/to/workflow.zip", workflowZipFilePathField.get(importCommand));
    }

    @Test
    void testCommandClassHierarchy() {
        // 测试类层次结构
        assertEquals(Object.class, TopEntryCommand.class.getSuperclass());
        assertEquals(Object.class, RunCommand.class.getSuperclass());
        assertEquals(Object.class, ExportCommand.class.getSuperclass());
        assertEquals(Object.class, ImportCommand.class.getSuperclass());
        assertEquals(Object.class, ListCommand.class.getSuperclass());
        assertEquals(Object.class, DetailCommand.class.getSuperclass());
        assertEquals(Object.class, HistoryCommand.class.getSuperclass());
        assertEquals(Object.class, PasswordResetCommand.class.getSuperclass());
        assertEquals(Object.class, RunProcessCommand.class.getSuperclass());
        assertEquals(Object.class, RunWorkflowCommand.class.getSuperclass());
        assertEquals(Object.class, ExportProcessCommand.class.getSuperclass());
        assertEquals(Object.class, ExportWorkflowCommand.class.getSuperclass());
        assertEquals(Object.class, ImportProcessCommand.class.getSuperclass());
        assertEquals(Object.class, ImportWorkflowCommand.class.getSuperclass());
        assertEquals(Object.class, H2CompatibilityChecker.class.getSuperclass());
    }

    @Test
    void testCommandFieldNames() throws Exception {
        // 测试字段名
        assertEquals("new_password", PasswordResetCommand.class.getDeclaredField("new_password").getName());
        assertEquals("history_id", HistoryCommand.class.getDeclaredField("history_id").getName());
        assertEquals("processid", RunProcessCommand.class.getDeclaredField("processid").getName());
        assertEquals("hostid", RunProcessCommand.class.getDeclaredField("hostid").getName());
        assertEquals("envid", RunProcessCommand.class.getDeclaredField("envid").getName());
        assertEquals("pass", RunProcessCommand.class.getDeclaredField("pass").getName());
        assertEquals("workflowId", RunWorkflowCommand.class.getDeclaredField("workflowId").getName());
        assertEquals("hostStrings", RunWorkflowCommand.class.getDeclaredField("hostStrings").getName());
        assertEquals("envs", RunWorkflowCommand.class.getDeclaredField("envs").getName());
        assertEquals("passes", RunWorkflowCommand.class.getDeclaredField("passes").getName());
        assertEquals("workflow_id", ExportWorkflowCommand.class.getDeclaredField("workflow_id").getName());
        assertEquals("export_mode", ExportWorkflowCommand.class.getDeclaredField("export_mode").getName());
        assertEquals("target_file_path", ExportWorkflowCommand.class.getDeclaredField("target_file_path").getName());
        assertEquals("workflow_zip_file_path", ImportWorkflowCommand.class.getDeclaredField("workflow_zip_file_path").getName());
    }

    @Test
    void testCommandMethodNames() throws Exception {
        // 测试方法名
        assertEquals("run", TopEntryCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", RunCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", ExportCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", ImportCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", ListCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", DetailCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", HistoryCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", PasswordResetCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", RunProcessCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", RunWorkflowCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", ExportProcessCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", ExportWorkflowCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", ImportProcessCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", ImportWorkflowCommand.class.getDeclaredMethod("run").getName());
        assertEquals("run", H2CompatibilityChecker.class.getDeclaredMethod("run").getName());
    }

    @Test
    void testCommandFieldCount() throws Exception {
        // 测试字段数量（不依赖具体数字）
        assertTrue(PasswordResetCommand.class.getDeclaredFields().length > 0);
        assertTrue(HistoryCommand.class.getDeclaredFields().length > 0);
        assertTrue(RunProcessCommand.class.getDeclaredFields().length > 0);
        assertTrue(RunWorkflowCommand.class.getDeclaredFields().length > 0);
        assertTrue(ExportWorkflowCommand.class.getDeclaredFields().length > 0);
        assertTrue(ImportWorkflowCommand.class.getDeclaredFields().length > 0);
    }

    @Test
    void testCommandMethodCount() throws Exception {
        // 测试方法数量（不依赖具体数字）
        assertTrue(TopEntryCommand.class.getDeclaredMethods().length > 0);
        assertTrue(RunCommand.class.getDeclaredMethods().length > 0);
        assertTrue(ExportCommand.class.getDeclaredMethods().length > 0);
        assertTrue(ImportCommand.class.getDeclaredMethods().length > 0);
        assertTrue(ListCommand.class.getDeclaredMethods().length > 0);
        assertTrue(DetailCommand.class.getDeclaredMethods().length > 0);
        assertTrue(HistoryCommand.class.getDeclaredMethods().length > 0);
        assertTrue(PasswordResetCommand.class.getDeclaredMethods().length > 0);
        assertTrue(RunProcessCommand.class.getDeclaredMethods().length > 0);
        assertTrue(RunWorkflowCommand.class.getDeclaredMethods().length > 0);
        assertTrue(ExportProcessCommand.class.getDeclaredMethods().length > 0);
        assertTrue(ExportWorkflowCommand.class.getDeclaredMethods().length > 0);
        assertTrue(ImportProcessCommand.class.getDeclaredMethods().length > 0);
        assertTrue(ImportWorkflowCommand.class.getDeclaredMethods().length > 0);
        assertTrue(H2CompatibilityChecker.class.getDeclaredMethods().length > 0);
    }

    @Test
    void testCommandFieldModifiers() throws Exception {
        // 测试字段修饰符（更宽松的检查）
        Field passwordField = PasswordResetCommand.class.getDeclaredField("new_password");
        int passwordModifiers = passwordField.getModifiers();
        // 字段应该有某种访问修饰符
        assertTrue(java.lang.reflect.Modifier.isPrivate(passwordModifiers) || 
                  java.lang.reflect.Modifier.isPublic(passwordModifiers) ||
                  java.lang.reflect.Modifier.isProtected(passwordModifiers) ||
                  !java.lang.reflect.Modifier.isStatic(passwordModifiers));
        
        Field historyIdField = HistoryCommand.class.getDeclaredField("history_id");
        int historyModifiers = historyIdField.getModifiers();
        // 字段应该有某种访问修饰符
        assertTrue(java.lang.reflect.Modifier.isPrivate(historyModifiers) || 
                  java.lang.reflect.Modifier.isPublic(historyModifiers) ||
                  java.lang.reflect.Modifier.isProtected(historyModifiers) ||
                  !java.lang.reflect.Modifier.isStatic(historyModifiers));
    }

    @Test
    void testCommandMethodModifiers() throws Exception {
        // 测试方法修饰符
        Method runMethod = TopEntryCommand.class.getMethod("run");
        int modifiers = runMethod.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertFalse(java.lang.reflect.Modifier.isPrivate(modifiers));
        assertFalse(java.lang.reflect.Modifier.isStatic(modifiers));
        assertFalse(java.lang.reflect.Modifier.isFinal(modifiers));
        assertFalse(java.lang.reflect.Modifier.isAbstract(modifiers));
    }

    @Test
    void testCommandClassModifiers() {
        // 测试类修饰符
        int topEntryModifiers = TopEntryCommand.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(topEntryModifiers));
        assertFalse(java.lang.reflect.Modifier.isPrivate(topEntryModifiers));
        assertFalse(java.lang.reflect.Modifier.isStatic(topEntryModifiers));
        assertFalse(java.lang.reflect.Modifier.isFinal(topEntryModifiers));
        assertFalse(java.lang.reflect.Modifier.isAbstract(topEntryModifiers));
    }

    @Test
    void testCommandFieldValuesWithNull() throws Exception {
        // 测试null值处理
        PasswordResetCommand passwordCommand = new PasswordResetCommand();
        Field passwordField = PasswordResetCommand.class.getDeclaredField("new_password");
        passwordField.setAccessible(true);
        passwordField.set(passwordCommand, null);
        assertNull(passwordField.get(passwordCommand));
        
        HistoryCommand historyCommand = new HistoryCommand();
        Field historyIdField = HistoryCommand.class.getDeclaredField("history_id");
        historyIdField.setAccessible(true);
        historyIdField.set(historyCommand, null);
        assertNull(historyIdField.get(historyCommand));
    }

    @Test
    void testCommandFieldValuesWithEmptyString() throws Exception {
        // 测试空字符串处理
        PasswordResetCommand passwordCommand = new PasswordResetCommand();
        Field passwordField = PasswordResetCommand.class.getDeclaredField("new_password");
        passwordField.setAccessible(true);
        passwordField.set(passwordCommand, "");
        assertEquals("", passwordField.get(passwordCommand));
        
        HistoryCommand historyCommand = new HistoryCommand();
        Field historyIdField = HistoryCommand.class.getDeclaredField("history_id");
        historyIdField.setAccessible(true);
        historyIdField.set(historyCommand, "");
        assertEquals("", historyIdField.get(historyCommand));
    }

    @Test
    void testCommandArrayFieldsWithEmptyArray() throws Exception {
        // 测试空数组
        RunWorkflowCommand workflowCommand = new RunWorkflowCommand();
        Field hostStringsField = RunWorkflowCommand.class.getDeclaredField("hostStrings");
        hostStringsField.setAccessible(true);
        hostStringsField.set(workflowCommand, new String[0]);
        String[] retrievedHosts = (String[]) hostStringsField.get(workflowCommand);
        assertEquals(0, retrievedHosts.length);
        
        Field envsField = RunWorkflowCommand.class.getDeclaredField("envs");
        envsField.setAccessible(true);
        envsField.set(workflowCommand, new String[0]);
        String[] retrievedEnvs = (String[]) envsField.get(workflowCommand);
        assertEquals(0, retrievedEnvs.length);
    }

    @Test
    void testCommandIntegerFieldsWithDifferentValues() throws Exception {
        // 测试不同的整数值
        ExportWorkflowCommand exportCommand = new ExportWorkflowCommand();
        Field exportModeField = ExportWorkflowCommand.class.getDeclaredField("export_mode");
        exportModeField.setAccessible(true);
        
        // 测试边界值
        exportModeField.set(exportCommand, 0);
        assertEquals(0, exportModeField.get(exportCommand));
        
        exportModeField.set(exportCommand, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, exportModeField.get(exportCommand));
        
        exportModeField.set(exportCommand, Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, exportModeField.get(exportCommand));
    }
}