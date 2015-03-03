package com.jenjinstudios.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

/**
 * Used to make database lookups against SQL databases.  An attempt has been made to keep this class as database-
 * agnostic as possible;  assuming the supplied {@code Connection} is a valid SQL connection, this should "just work".
 *
 * @author Caleb Brinkman
 */
public abstract class SqlDbTable<T> implements DbTable<T>
{
	private static final Logger LOGGER = Logger.getLogger(SqlDbTable.class.getName());
	private final Connection connection;
	private final String tableName;

	/**
	 * Construct a new SqlDbTable with the given SQL {@code Connection} and table name.
	 *
	 * @param connection The {@code Connection} used to communicate with the SQL database.
	 * @param tableName The name of the table to query.
	 */
	public SqlDbTable(Connection connection, String tableName) {
		this.connection = connection;
		this.tableName = tableName;
	}

	/**
	 * Given a result set from the backing database, build out the type-correct return value from that result.  This
	 * method is used by the {@code lookup} method, and must be implemented.
	 *
	 * @param resultSet The results from the backing database.
	 *
	 * @return A {@code T} built from the result set.
	 *
	 * @throws java.sql.SQLException If there is an exception when querying the result set.
	 */
	protected abstract T buildFromRow(ResultSet resultSet) throws SQLException;

	/**
	 * Given an object, build a Map using the names of the columns to be updated as the keys, and the records as the
	 * values.
	 *
	 * @param data The object for which to build to map.
	 *
	 * @return The map.
	 */
	protected abstract Map<String, Object> buildFromObject(T data);

	@Override
	public List<T> lookup(Map<String, Object> where) {
		List<T> lookup = new LinkedList<>();
		try
		{
			PreparedStatement statement = getLookupStatement(where);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
			{
				lookup.add(buildFromRow(resultSet));
			}
			resultSet.close();
		} catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "SQL Exception when querying database: ", e);
		}
		return lookup;
	}

	@Override
	public boolean update(Map<String, Object> where, T row) {
		return false;
	}

	private PreparedStatement getLookupStatement(Map<String, Object> where) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName);
		String whereClause = buildWhereClause(where);
		String query = queryBuilder.append(whereClause).toString();
		PreparedStatement statement;
		synchronized (connection)
		{
			statement = connection.prepareStatement(query, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
			int parameterCount = 1;
			for (Entry<String, Object> entry : where.entrySet())
			{
				statement.setObject(parameterCount, entry.getValue());
				parameterCount++;
			}
		}
		return statement;
	}

	private String buildWhereClause(Map<String, Object> where) {
		StringBuilder whereClauseBuilder = new StringBuilder(" WHERE ");
		for (Entry<String, Object> entry : where.entrySet())
		{
			whereClauseBuilder.append(entry.getKey()).append(" = ? AND ");
		}
		int lastAnd = whereClauseBuilder.lastIndexOf("AND");
		whereClauseBuilder.delete(lastAnd, lastAnd + 3);
		return whereClauseBuilder.toString();
	}

}
