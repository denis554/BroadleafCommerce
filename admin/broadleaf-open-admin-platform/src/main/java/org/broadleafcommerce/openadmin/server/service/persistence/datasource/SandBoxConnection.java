/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.server.service.persistence.datasource;

import org.apache.commons.pool.impl.GenericObjectPool;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

public class SandBoxConnection implements Connection {
	
	private Connection delegate;
	private GenericObjectPool connectionPool;
	
	public SandBoxConnection(Connection delegate, GenericObjectPool connectionPool) {
		this.delegate = delegate;
		this.connectionPool = connectionPool;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return delegate.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return delegate.isWrapperFor(iface);
	}

	public Statement createStatement() throws SQLException {
		return delegate.createStatement();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return delegate.prepareStatement(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return delegate.prepareCall(sql);
	}

	public String nativeSQL(String sql) throws SQLException {
		return delegate.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		delegate.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		return delegate.getAutoCommit();
	}

	public void commit() throws SQLException {
		delegate.commit();
	}

	public void rollback() throws SQLException {
		delegate.rollback();
	}

	public void close() throws SQLException {
		try {
			connectionPool.returnObject(this);
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	public boolean isClosed() throws SQLException {
		return delegate.isClosed();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return delegate.getMetaData();
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		delegate.setReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		return delegate.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		delegate.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {
		return delegate.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		delegate.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		return delegate.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		return delegate.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		delegate.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return delegate.createStatement(resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return delegate.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return delegate.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		delegate.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		delegate.setHoldability(holdability);
	}

	public int getHoldability() throws SQLException {
		return delegate.getHoldability();
	}

	public Savepoint setSavepoint() throws SQLException {
		return delegate.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return delegate.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		delegate.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		delegate.releaseSavepoint(savepoint);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return delegate.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return delegate.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return delegate.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return delegate.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return delegate.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return delegate.prepareStatement(sql, columnNames);
	}

	public Clob createClob() throws SQLException {
		return delegate.createClob();
	}

	public Blob createBlob() throws SQLException {
		return delegate.createBlob();
	}

	public NClob createNClob() throws SQLException {
		return delegate.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return delegate.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		return delegate.isValid(timeout);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		delegate.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		delegate.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		return delegate.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		return delegate.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return delegate.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return delegate.createStruct(typeName, attributes);
	}
}
