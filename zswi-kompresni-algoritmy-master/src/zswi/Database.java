package zswi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static zswi.Main.*;

class Database
{
	private static final Lock errorLock = new ReentrantLock();
	private static boolean error = false;
	
	static boolean errorOccurred()
	{
		boolean e;
		synchronized(errorLock)
		{
			e = error;
			error = false;
		}
		return e;
	}
	
	private static void error()
	{
		errorLock.lock();
		error = true;
		errorLock.unlock();
	}
	
	/**
	 * Reads ulr, name and password for database from config file
	 * and creates database connection
	 *
	 * @return created database connection
	 */
	static Connection connect()
	{
		BufferedReader reader;
		String sqlUrl, sqlName, sqlPassword, mysqlConfig = "mysql_config.txt";
		Connection connection;
		
		try
		{
			reader = new BufferedReader(new FileReader(mysqlConfig));
			sqlUrl = reader.readLine();
			sqlName = reader.readLine();
			sqlPassword = reader.readLine();
			reader.close();
			if(sqlPassword == null)
			{
				printToLog("Missing attributes in " + mysqlConfig, false);
				return null;
			}
		}
		catch(FileNotFoundException e)
		{
			printToLog(mysqlConfig + " was not found", false);
			return null;
		}
		catch(IOException e)
		{
			printToLog("Error while reading " + mysqlConfig, false);
			return null;
		}
		
		try
		{
			Class.forName("com.mysql.cj.jdbc.Driver");
		}
		catch(ClassNotFoundException e)
		{
			printToLog("MySQL driver not found", false);
			return null;
		}
		
		try
		{
			connection = DriverManager.getConnection(
					"jdbc:mysql://" + sqlUrl,
					sqlName,sqlPassword);
		}
		catch(SQLException e)
		{
			printToLog("Error while connecting to database", false);
			return null;
		}
		
		return connection;
	}
	
	/**
	 * Selects and returns all records in given table and column
	 *
	 * @param table table to select from
	 * @param column column to select from
	 * @return list of selected values
	 */
	static List<String> selectAllRecords(String table, String column)
	{
		List<String> records = new ArrayList<>();
		try
		{
			ResultSet resultSet = getAndLockConnection().createStatement().executeQuery(
					"SELECT * FROM " + table + ";");
			while(resultSet.next())
				records.add(resultSet.getString(column));
			resultSet.close();
			return records;
		}
		catch(Exception e)
		{
			printToLog("Error while reading records from database", true);
			return null;
		}
		finally
		{
			unlockConnection();
		}
	}
	
	/**
	 * Selects and returns all records in given table and columns
	 *
	 * @param table table to select from
	 * @param column1 first column to select from
	 * @param column2 second column to select from
	 * @param column3 third column to select from
	 * @return list of selected values (columns are divided by " - ")
	 */
	static List<String> selectAllRecords(String table, String column1, String column2, String column3)
	{
		List<String> records = new ArrayList<>();
		try
		{
			ResultSet resultSet = getAndLockConnection().createStatement().executeQuery(
					"SELECT * FROM " + table + ";");
			while(resultSet.next())
				records.add(resultSet.getString(column1) +
					" - " + resultSet.getString(column2) +
					" - " + resultSet.getString(column3));
			resultSet.close();
			return records;
		}
		catch(Exception e)
		{
			printToLog("Error while reading records from database", true);
			return new ArrayList<>();
		}
		finally
		{
			unlockConnection();
		}
	}
	
	/**
	 * Checks if record exists in given table and with given condition
	 *
	 * @param table table to select from
	 * @param condition condition for selecting
	 * @return true if records exists false otherwise
	 */
	static boolean recordExists(String table, String condition)
	{
		try
		{
			ResultSet resultSet = getAndLockConnection().createStatement().executeQuery(
					"SELECT EXISTS (SELECT 1 FROM " + table + " WHERE (" + condition + "));");
			resultSet.next();
			boolean exists = resultSet.getInt(1) == 1;
			resultSet.close();
			return exists;
		}
		catch(Exception e)
		{
			error();
			printToLog("Error while reading records from database", true);
			return false;
		}
		finally
		{
			unlockConnection();
		}
	}
	
	/**
	 * Checks if any record exists and if it does then
	 * deletes all records from given table and given condition
	 *
	 * @param table table to search in and delete from
	 * @param condition condition for selecting and deleting
	 */
	static void tryDeleteRecords(String table, String condition)
	{
		Connection connection = getAndLockConnection();
		try
		{
			ResultSet resultSet = connection.createStatement().executeQuery(
					"SELECT EXISTS (SELECT 1 FROM " + table + " WHERE (" + condition + "));");
			resultSet.next();
			if(resultSet.getInt(1) == 1)
				connection.prepareStatement(
					"DELETE FROM " + table + " WHERE (" + condition + ");").executeUpdate();
			resultSet.close();
		}
		catch(Exception e)
		{
			printToLog("Error while deleting records from database", true);
		}
		finally
		{
			unlockConnection();
		}
	}
	
	/**
	 * Inserts record and returns id of inserted record
	 *
	 * @param table table to insert into
	 * @param fields fields of inserted record
	 * @param values values of inserted record
	 * @return id of inserted record
	 */
	static int insertAndReturnID(String table, String fields, String values)
	{
		Connection connection = getAndLockConnection();
		try
		{
			connection.createStatement().executeUpdate(
					"INSERT INTO " + table + " (" + fields + ") VALUES (" + values + ");");
			ResultSet resultSet = connection.createStatement().executeQuery(
					"SELECT @@IDENTITY");
			resultSet.next();
			int id = resultSet.getInt(1);
			resultSet.close();
			return id;
		}
		catch(Exception e)
		{
			printToLog("Error while inserting records from database", true);
			return -1;
		}
		finally
		{
			unlockConnection();
		}
	}
	
	/**
	 * Selects and returns all IDs from given table and condition
	 *
	 * @param table table to select from
	 * @param condition condition for selecting
	 * @return list of IDs of selected records
	 */
	static ArrayList<Integer> selectIDs(String table, String condition)
	{
		ArrayList<Integer> ids = new ArrayList<>();
		ResultSet resultSet;
		try
		{
			resultSet = getAndLockConnection().createStatement().executeQuery(
					"SELECT * FROM " + table + " WHERE (" + condition + ");");
			while(resultSet.next())
				ids.add(resultSet.getInt("id"));
			resultSet.close();
			return ids;
		}
		catch(Exception e)
		{
			printToLog("Error while reading from database", true);
			return new ArrayList<>();
		}
		finally
		{
			unlockConnection();
		}
	}
	
	/**
	 * Selects and returns one ID from given table and condition
	 *
	 * @param table table to select from
	 * @param condition condition for selecting
	 * @return id of selected record
	 */
	static int selectID(String table, String condition)
	{
		ResultSet resultSet;
		try
		{
			resultSet = getAndLockConnection().createStatement().executeQuery(
					"SELECT id FROM " + table + " WHERE (" + condition + ");");
			resultSet.next();
			int id = resultSet.getInt(1);
			resultSet.close();
			return id;
		}
		catch(Exception e)
		{
			printToLog("Error while reading from database", true);
			return -1;
		}
		finally
		{
			unlockConnection();
		}
	}
	
	/**
	 * Inserts record into given table
	 *
	 * @param table table to insert into
	 * @param fields fields of inserted record
	 * @param values values of inserted record
	 */
	static void insertRecord(String table, String fields, String values)
	{
		try
		{
			getAndLockConnection().createStatement().executeUpdate(
					"INSERT INTO " + table + " (" + fields + ") VALUES (" + values + ");");
		}
		catch(Exception e)
		{
			error();
			printToLog("Error while inserting records to database", true);
		}
		finally
		{
			unlockConnection();
		}
	}
}
