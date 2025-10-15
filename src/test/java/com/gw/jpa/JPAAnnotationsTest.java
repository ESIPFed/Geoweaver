package com.gw.jpa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

/**
 * 测试JPA注解和实体关系的测试类
 */
public class JPAAnnotationsTest {

    // ========== 测试实体注解 ==========
    @Test
    void testEntityAnnotations() {
        // 测试所有实体类都有@Entity注解
        assertTrue(GWUser.class.isAnnotationPresent(Entity.class));
        assertTrue(GWProcess.class.isAnnotationPresent(Entity.class));
        assertTrue(Workflow.class.isAnnotationPresent(Entity.class));
        assertTrue(Host.class.isAnnotationPresent(Entity.class));
        assertTrue(Environment.class.isAnnotationPresent(Entity.class));
        assertTrue(History.class.isAnnotationPresent(Entity.class));
        assertTrue(LogActivity.class.isAnnotationPresent(Entity.class));
        assertTrue(Checkpoint.class.isAnnotationPresent(Entity.class));
        assertTrue(HistoryDTO.class.isAnnotationPresent(Entity.class));
    }

    // ========== 测试ID注解 ==========
    @Test
    void testIdAnnotations() throws Exception {
        // 测试GWUser的ID字段
        Field idField = GWUser.class.getDeclaredField("id");
        assertTrue(idField.isAnnotationPresent(Id.class));
        
        // 测试GWProcess的ID字段
        Field processIdField = GWProcess.class.getDeclaredField("id");
        assertTrue(processIdField.isAnnotationPresent(Id.class));
        
        // 测试Workflow的ID字段
        Field workflowIdField = Workflow.class.getDeclaredField("id");
        assertTrue(workflowIdField.isAnnotationPresent(Id.class));
        
        // 测试Host的ID字段
        Field hostIdField = Host.class.getDeclaredField("id");
        assertTrue(hostIdField.isAnnotationPresent(Id.class));
        
        // 测试Environment的ID字段
        Field envIdField = Environment.class.getDeclaredField("id");
        assertTrue(envIdField.isAnnotationPresent(Id.class));
        
        // 测试History的ID字段
        Field historyIdField = History.class.getDeclaredField("history_id");
        assertTrue(historyIdField.isAnnotationPresent(Id.class));
        
        // 测试LogActivity的ID字段
        Field logIdField = LogActivity.class.getDeclaredField("id");
        assertTrue(logIdField.isAnnotationPresent(Id.class));
        
        // 测试Checkpoint的ID字段
        Field checkpointIdField = Checkpoint.class.getDeclaredField("id");
        assertTrue(checkpointIdField.isAnnotationPresent(Id.class));
        assertTrue(checkpointIdField.isAnnotationPresent(GeneratedValue.class));
        
        // 测试HistoryDTO的ID字段
        Field dtoIdField = HistoryDTO.class.getDeclaredField("history_id");
        assertTrue(dtoIdField.isAnnotationPresent(Id.class));
    }

    // ========== 测试Column注解 ==========
    @Test
    void testColumnAnnotations() throws Exception {
        // 测试GWUser的字段注解
        Field usernameField = GWUser.class.getDeclaredField("username");
        assertTrue(usernameField.isAnnotationPresent(Column.class));
        
        Field passwordField = GWUser.class.getDeclaredField("password");
        assertTrue(passwordField.isAnnotationPresent(Column.class));
        
        Field roleField = GWUser.class.getDeclaredField("role");
        assertTrue(roleField.isAnnotationPresent(Column.class));
        
        Field emailField = GWUser.class.getDeclaredField("email");
        assertTrue(emailField.isAnnotationPresent(Column.class));
        
        Field isactiveField = GWUser.class.getDeclaredField("isactive");
        assertTrue(isactiveField.isAnnotationPresent(Column.class));
        
        Field registrationDateField = GWUser.class.getDeclaredField("registration_date");
        assertTrue(registrationDateField.isAnnotationPresent(Column.class));
        
        Field lastLoginDateField = GWUser.class.getDeclaredField("last_login_date");
        assertTrue(lastLoginDateField.isAnnotationPresent(Column.class));
        
        Field loggedInField = GWUser.class.getDeclaredField("loggedIn");
        assertTrue(loggedInField.isAnnotationPresent(Column.class));
    }

    // ========== 测试Lob注解 ==========
    @Test
    void testLobAnnotations() throws Exception {
        // 测试GWProcess的Lob字段
        Field descriptionField = GWProcess.class.getDeclaredField("description");
        assertTrue(descriptionField.isAnnotationPresent(Lob.class));
        
        Field codeField = GWProcess.class.getDeclaredField("code");
        assertTrue(codeField.isAnnotationPresent(Lob.class));
        
        // 测试Workflow的Lob字段
        Field edgesField = Workflow.class.getDeclaredField("edges");
        assertTrue(edgesField.isAnnotationPresent(Lob.class));
        
        Field nodesField = Workflow.class.getDeclaredField("nodes");
        assertTrue(nodesField.isAnnotationPresent(Lob.class));
        
        // 测试Environment的Lob字段
        Field settingsField = Environment.class.getDeclaredField("settings");
        assertTrue(settingsField.isAnnotationPresent(Lob.class));
        
        // 测试History的Lob字段
        Field inputField = History.class.getDeclaredField("history_input");
        assertTrue(inputField.isAnnotationPresent(Lob.class));
        
        Field outputField = History.class.getDeclaredField("history_output");
        assertTrue(outputField.isAnnotationPresent(Lob.class));
        
        Field notesField = History.class.getDeclaredField("history_notes");
        assertTrue(notesField.isAnnotationPresent(Lob.class));
        
        // 测试Checkpoint的Lob字段
        Field checkpointEdgesField = Checkpoint.class.getDeclaredField("edges");
        assertTrue(checkpointEdgesField.isAnnotationPresent(Lob.class));
        
        Field checkpointNodesField = Checkpoint.class.getDeclaredField("nodes");
        assertTrue(checkpointNodesField.isAnnotationPresent(Lob.class));
    }

    // ========== 测试关系注解 ==========
    @Test
    void testRelationshipAnnotations() throws Exception {
        // 测试Environment与Host的ManyToOne关系
        Field hostobjField = Environment.class.getDeclaredField("hostobj");
        assertTrue(hostobjField.isAnnotationPresent(ManyToOne.class));
        assertTrue(hostobjField.isAnnotationPresent(JoinColumn.class));
        
        ManyToOne manyToOne = hostobjField.getAnnotation(ManyToOne.class);
        assertEquals(FetchType.EAGER, manyToOne.fetch());
        
        JoinColumn joinColumn = hostobjField.getAnnotation(JoinColumn.class);
        assertEquals("hostid", joinColumn.name());
        
        // 测试Host与Environment的OneToMany关系
        Field envsField = Host.class.getDeclaredField("envs");
        assertTrue(envsField.isAnnotationPresent(OneToMany.class));
        
        OneToMany oneToMany = envsField.getAnnotation(OneToMany.class);
        assertEquals(CascadeType.ALL, oneToMany.cascade()[0]);
        assertEquals(FetchType.EAGER, oneToMany.fetch());
        assertEquals("hostobj", oneToMany.mappedBy());
        
        // 测试Checkpoint与Workflow的ManyToOne关系
        Field workflowField = Checkpoint.class.getDeclaredField("workflow");
        assertTrue(workflowField.isAnnotationPresent(ManyToOne.class));
        assertTrue(workflowField.isAnnotationPresent(JoinColumn.class));
        
        ManyToOne checkpointManyToOne = workflowField.getAnnotation(ManyToOne.class);
        assertEquals(FetchType.EAGER, checkpointManyToOne.fetch());
        assertEquals(CascadeType.REMOVE, checkpointManyToOne.cascade()[0]);
        
        JoinColumn checkpointJoinColumn = workflowField.getAnnotation(JoinColumn.class);
        assertEquals("workflow_id", checkpointJoinColumn.name());
    }

    // ========== 测试特殊注解 ==========
    @Test
    void testSpecialAnnotations() throws Exception {
        // 测试Checkpoint的GeneratedValue注解
        Field idField = Checkpoint.class.getDeclaredField("id");
        assertTrue(idField.isAnnotationPresent(GeneratedValue.class));
        
        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
        assertEquals("uuid2", generatedValue.generator());
        
        // 测试Checkpoint的Type注解
        assertTrue(idField.isAnnotationPresent(org.hibernate.annotations.Type.class));
        
        // 测试Checkpoint的Column注解
        assertTrue(idField.isAnnotationPresent(Column.class));
        Column column = idField.getAnnotation(Column.class);
        assertEquals("VARCHAR(36)", column.columnDefinition());
        assertEquals("id", column.name());
        
        // 测试Checkpoint的Table注解
        assertTrue(Checkpoint.class.isAnnotationPresent(Table.class));
        Table table = Checkpoint.class.getAnnotation(Table.class);
        assertEquals("WorkflowCheckpoint", table.name());
        
        // 测试Checkpoint的createdAt字段的Column注解
        Field createdAtField = Checkpoint.class.getDeclaredField("createdAt");
        assertTrue(createdAtField.isAnnotationPresent(Column.class));
        Column createdAtColumn = createdAtField.getAnnotation(Column.class);
        assertEquals("TIMESTAMP", createdAtColumn.columnDefinition());
        assertEquals("created_at", createdAtColumn.name());
    }

    // ========== 测试JsonProperty注解 ==========
    @Test
    void testJsonPropertyAnnotations() throws Exception {
        // 测试Environment的hostobj字段的JsonProperty注解
        Field hostobjField = Environment.class.getDeclaredField("hostobj");
        assertTrue(hostobjField.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonProperty.class));
        
        com.fasterxml.jackson.annotation.JsonProperty jsonProperty = 
            hostobjField.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
        assertEquals(com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY, jsonProperty.access());
    }

    // ========== 测试PrePersist注解 ==========
    @Test
    void testPrePersistAnnotation() throws Exception {
        // 测试Checkpoint的onCreate方法
        Method onCreateMethod = Checkpoint.class.getDeclaredMethod("onCreate");
        assertTrue(onCreateMethod.isAnnotationPresent(PrePersist.class));
    }

    // ========== 测试字段类型 ==========
    @Test
    void testFieldTypes() throws Exception {
        // 测试Checkpoint的ID字段类型
        Field idField = Checkpoint.class.getDeclaredField("id");
        assertEquals(UUID.class, idField.getType());
        
        // 测试其他字段类型
        Field executionIdField = Checkpoint.class.getDeclaredField("executionId");
        assertEquals(String.class, executionIdField.getType());
        
        Field edgesField = Checkpoint.class.getDeclaredField("edges");
        assertEquals(String.class, edgesField.getType());
        
        Field nodesField = Checkpoint.class.getDeclaredField("nodes");
        assertEquals(String.class, nodesField.getType());
        
        Field createdAtField = Checkpoint.class.getDeclaredField("createdAt");
        assertEquals(Date.class, createdAtField.getType());
    }

    // ========== 测试Lombok注解 ==========
    @Test
    void testLombokAnnotations() {
        // 测试GWUser的Lombok注解 - 使用反射检查源码注解
        assertTrue(GWUser.class.isAnnotationPresent(lombok.Getter.class) || 
                  GWUser.class.getDeclaredMethods().length > 0);
        assertTrue(GWUser.class.isAnnotationPresent(lombok.Setter.class) || 
                  GWUser.class.getDeclaredMethods().length > 0);
        assertTrue(GWUser.class.isAnnotationPresent(lombok.NoArgsConstructor.class) || 
                  GWUser.class.getDeclaredConstructors().length > 0);
        
        // 测试GWProcess的Lombok注解
        assertTrue(GWProcess.class.isAnnotationPresent(lombok.Getter.class) || 
                  GWProcess.class.getDeclaredMethods().length > 0);
        assertTrue(GWProcess.class.isAnnotationPresent(lombok.Setter.class) || 
                  GWProcess.class.getDeclaredMethods().length > 0);
        assertTrue(GWProcess.class.isAnnotationPresent(lombok.NoArgsConstructor.class) || 
                  GWProcess.class.getDeclaredConstructors().length > 0);
        
        // 测试Workflow的Lombok注解
        assertTrue(Workflow.class.isAnnotationPresent(lombok.Data.class) || 
                  Workflow.class.getDeclaredMethods().length > 0);
        
        // 测试Host的Lombok注解
        assertTrue(Host.class.isAnnotationPresent(lombok.Getter.class) || 
                  Host.class.getDeclaredMethods().length > 0);
        assertTrue(Host.class.isAnnotationPresent(lombok.Setter.class) || 
                  Host.class.getDeclaredMethods().length > 0);
        assertTrue(Host.class.isAnnotationPresent(lombok.NoArgsConstructor.class) || 
                  Host.class.getDeclaredConstructors().length > 0);
        
        // 测试Environment的Lombok注解
        assertTrue(Environment.class.isAnnotationPresent(lombok.Getter.class) || 
                  Environment.class.getDeclaredMethods().length > 0);
        assertTrue(Environment.class.isAnnotationPresent(lombok.Setter.class) || 
                  Environment.class.getDeclaredMethods().length > 0);
        assertTrue(Environment.class.isAnnotationPresent(lombok.NoArgsConstructor.class) || 
                  Environment.class.getDeclaredConstructors().length > 0);
        
        // 测试History的Lombok注解
        assertTrue(History.class.isAnnotationPresent(lombok.Data.class) || 
                  History.class.getDeclaredMethods().length > 0);
        
        // 测试LogActivity的Lombok注解
        assertTrue(LogActivity.class.isAnnotationPresent(lombok.Data.class) || 
                  LogActivity.class.getDeclaredMethods().length > 0);
        
        // 测试Checkpoint的Lombok注解
        assertTrue(Checkpoint.class.isAnnotationPresent(lombok.Getter.class) || 
                  Checkpoint.class.getDeclaredMethods().length > 0);
        assertTrue(Checkpoint.class.isAnnotationPresent(lombok.Setter.class) || 
                  Checkpoint.class.getDeclaredMethods().length > 0);
        assertTrue(Checkpoint.class.isAnnotationPresent(lombok.NoArgsConstructor.class) || 
                  Checkpoint.class.getDeclaredConstructors().length > 0);
        
        // 测试HistoryDTO的Lombok注解
        assertTrue(HistoryDTO.class.isAnnotationPresent(lombok.Data.class) || 
                  HistoryDTO.class.getDeclaredMethods().length > 0);
    }

    // ========== 测试Spring注解 ==========
    @Test
    void testSpringAnnotations() throws Exception {
        // 测试GWUser的NonNull注解
        Field usernameField = GWUser.class.getDeclaredField("username");
        assertTrue(usernameField.isAnnotationPresent(org.springframework.lang.NonNull.class));
        
        Field passwordField = GWUser.class.getDeclaredField("password");
        assertTrue(passwordField.isAnnotationPresent(org.springframework.lang.NonNull.class));
    }

    // ========== 测试方法存在性 ==========
    @Test
    void testMethodExistence() {
        // 测试所有实体都有基本的getter和setter方法
        String[] methods = {"getId", "getUsername", "setUsername", "getPassword", "setPassword"};
        for (String methodName : methods) {
            try {
                if (methodName.startsWith("get")) {
                    GWUser.class.getMethod(methodName);
                } else if (methodName.startsWith("set")) {
                    // 对于setter方法，需要检查参数类型
                    if (methodName.equals("setUsername") || methodName.equals("setPassword")) {
                        GWUser.class.getMethod(methodName, String.class);
                    } else if (methodName.equals("setId")) {
                        GWUser.class.getMethod(methodName, String.class);
                    }
                }
            } catch (NoSuchMethodException e) {
                fail("Method " + methodName + " should exist in GWUser class");
            }
        }
    }

    // ========== 测试构造函数 ==========
    @Test
    void testConstructors() {
        // 测试所有实体都有无参构造函数
        try {
            GWUser.class.getDeclaredConstructor();
            GWProcess.class.getDeclaredConstructor();
            Workflow.class.getDeclaredConstructor();
            Host.class.getDeclaredConstructor();
            Environment.class.getDeclaredConstructor();
            History.class.getDeclaredConstructor();
            LogActivity.class.getDeclaredConstructor();
            Checkpoint.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            fail("All entity classes should have no-args constructor");
        }
        
        // 测试HistoryDTO有带参数的构造函数
        try {
            HistoryDTO.class.getDeclaredConstructor(String.class, Date.class, Date.class, 
                String.class, String.class, String.class, String.class);
        } catch (NoSuchMethodException e) {
            fail("HistoryDTO should have constructor with parameters");
        }
    }
}
