package com.mosbach.demo.data.impl;

import com.mosbach.demo.data.api.Task;
import com.mosbach.demo.data.api.TaskManager;
import com.mosbach.demo.data.api.User;
import com.mosbach.demo.data.api.UserManager;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgresDBTaskManagerImpl implements TaskManager {

    String databaseURL = "jdbc:postgresql://ec2-3-212-29-93.compute-1.amazonaws.com:5432/d112sce6c9cjgs";
    String username = "pnejojqestlulc";
    String password = "f98161fe2a5d07ec609235fcc4dc93b75c1e4707459dab4c47d0d7555b6c53a4";
    BasicDataSource basicDataSource;


    // Singleton
    static PostgresDBTaskManagerImpl postgresDBTaskManager = null;
    private PostgresDBTaskManagerImpl() {
        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(databaseURL);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
    }
    public static PostgresDBTaskManagerImpl getPostgresDBUserManagerImpl() {
        if (postgresDBTaskManager == null)
            postgresDBTaskManager = new PostgresDBTaskManagerImpl();
        return postgresDBTaskManager;
    }

    public void createTaskTable() {

        // Be carefull: It deletes data if table already exists.
        //
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = basicDataSource.getConnection();
            stmt = connection.createStatement();

            // String dropTable = "DROP TABLE tasks";
            // stmt.executeUpdate(dropTable);

            String createTable = "CREATE TABLE tasks (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name varchar(100) NOT NULL, " +
                    "priority int NOT NULL)";

            stmt.executeUpdate(createTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<Task> readAllTasks() {

        final Logger readTaskLogger = Logger.getLogger("ReadTaskLogger");
        readTaskLogger.log(Level.INFO,"Start reading tasks ");

        List<Task> tasks = new ArrayList<>();
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = basicDataSource.getConnection();
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tasks");

            while (rs.next()) {
                tasks.add(
                        new TaskImpl(
                                rs.getString("name"),
                                rs.getInt("priority")
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return
                tasks;
    }


    @Override
    public void addTask(String name, int priority) {

        final Logger createTaskLogger = Logger.getLogger("CreateTaskLogger");
        createTaskLogger.log(Level.INFO,"Start creating task " + name);

        Statement stmt = null;
        Connection connection = null;

        try {
            connection = basicDataSource.getConnection();
            stmt = connection.createStatement();
            String udapteSQL = "INSERT into tasks (name, priority) VALUES (" +
                    "'" + name + "', " +
                    priority + ")";

            stmt.executeUpdate(udapteSQL);

            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}