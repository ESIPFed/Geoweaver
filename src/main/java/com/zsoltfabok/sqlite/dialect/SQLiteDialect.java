/*
 * The author disclaims copyright to this source code. In place of
 * a legal notice, here is a blessing:
 *
 * May you do good and not evil.
 * May you find forgiveness for yourself and forgive others.
 * May you share freely, never taking more than you give.
 */
package com.zsoltfabok.sqlite.dialect;

import java.sql.Types;
import java.util.List;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.pagination.AbstractLimitHandler;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitHelper;
import org.hibernate.dialect.unique.DefaultUniqueDelegate;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.exception.DataException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.boot.model.relational.Database;

public class SQLiteDialect extends Dialect {

    private final UniqueDelegate uniqueDelegate;

    public SQLiteDialect() {
        registerColumnType(Types.BIT, "boolean");
        registerColumnType(Types.TINYINT, "tinyint");
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.BIGINT, "bigint");
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.REAL, "real");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.NUMERIC, "numeric");
        registerColumnType(Types.DECIMAL, "decimal");
        registerColumnType(Types.CHAR, "char");
        registerColumnType(Types.VARCHAR, "varchar");
        registerColumnType(Types.LONGVARCHAR, "longvarchar");
        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.TIME, "time");
        registerColumnType(Types.TIMESTAMP, "timestamp");
        registerColumnType(Types.BINARY, "blob");
        registerColumnType(Types.VARBINARY, "blob");
        registerColumnType(Types.LONGVARBINARY, "blob");
        registerColumnType(Types.BLOB, "blob");
        registerColumnType(Types.CLOB, "clob");
        registerColumnType(Types.BOOLEAN, "boolean");

        registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", ""));
        registerFunction("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
        registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
        registerFunction("substring", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
        registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));

        uniqueDelegate = new DefaultUniqueDelegate(this) {
            @Override
            public String getColumnDefinitionUniquenessFragment(org.hibernate.mapping.Column column) {
                return " unique";
            }
        };
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    @Override
    public LimitHandler getLimitHandler() {
        return new AbstractLimitHandler() {
            @Override
            public String processSql(String sql, org.hibernate.engine.spi.RowSelection rowSelection) {
                final boolean hasOffset = LimitHelper.hasFirstRow(rowSelection);
                return sql + (hasOffset ? " limit ? offset ?" : " limit ?");
            }

            @Override
            public boolean supportsLimit() {
                return true;
            }

            @Override
            public boolean bindLimitParametersInReverseOrder() {
                return true;
            }
        };
    }
    
    @Override
    public boolean supportsLimit() {
        return true;
    }
    
    public String getOffsetWorkaround(String sql) {
        return sql;
    }
    
    @Override
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }
    
    @Override
    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }
    
    @Override
    public String getCurrentTimestampSelectString() {
        return "select current_timestamp";
    }
    
    @Override
    public boolean supportsUnionAll() {
        return true;
    }
    
    @Override
    public boolean hasAlterTable() {
        return false;
    }
    
    @Override
    public boolean dropConstraints() {
        return false;
    }
    
    @Override
    public String getDropForeignKeyString() {
        throw new UnsupportedOperationException("No drop foreign key syntax supported by SQLiteDialect");
    }
    
    @Override
    public String getAddForeignKeyConstraintString(
            String constraintName,
            String[] foreignKey,
            String referencedTable,
            String[] primaryKey,
            boolean referencesPrimaryKey) {
        throw new UnsupportedOperationException("No add foreign key syntax supported by SQLiteDialect");
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        throw new UnsupportedOperationException("No add primary key syntax supported by SQLiteDialect");
    }

    @Override
    public String getForUpdateString() {
        return "";
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    @Override
    public String getSelectGUIDString() {
        return "select hex(randomblob(16))";
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public UniqueDelegate getUniqueDelegate() {
        return uniqueDelegate;
    }
} 