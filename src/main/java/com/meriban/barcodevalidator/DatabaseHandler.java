package com.meriban.barcodevalidator;

import javafx.util.Pair;

import java.io.File;
import java.sql.*;

public class DatabaseHandler {
    // static variable single_instance of type Singleton
    private static DatabaseHandler handler = null;
    private static Connection connection = null;
    public static final int DONE = 1;
    public static final int CONNECTION_CLOSED = 0;
    public static final int NOT_CONNECTED = -1;
    public static final int TABLE_NOT_EXIST = -2;
    public static final int DUPLICATE = -3;
    // private constructor restricted to this class itself
    private DatabaseHandler(){
    }

    // static method to create instance of Singleton class
    public static DatabaseHandler getInstance() {
        if (handler == null)
            handler = new DatabaseHandler();
        return handler;
    }
     public DatabaseHandler connectToDatabase(File dataDirectory, String databaseName) throws SQLException {
        String url = "jdbc:sqlite:"+ dataDirectory.getAbsolutePath() + "\\"+databaseName;
         //System.out.println(url);
             connection = DriverManager.getConnection(url);
             return handler;
     }

     public int write(String barcode, int action) throws SQLException {
        if(connection==null){
            return NOT_CONNECTED;
        }
        if(connection.isClosed()){
            return CONNECTION_CLOSED;
        }
        if(!tableExists("LOG")) {
            return TABLE_NOT_EXIST;
        }else {
            Pair<Integer,ResultSet> results = checkForExistingEntry(barcode);
                if (results == null) {
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO LOG(barcode,date,action) VALUES (?,datetime('now'),?)");
                    preparedStatement.setString(1, barcode);
                    //preparedStatement.setString(2, date);
                    preparedStatement.setInt(2, action);
                    preparedStatement.executeUpdate();
                    return DONE;
                } else {
                    if(results.getKey()==1){
                        return overwrite(barcode, action);
                    }else{
                        return DUPLICATE;
                    }
                }
            }
     }

     private static boolean tableExists(String table) throws SQLException {
        if(connection!=null){
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, table,null);
            if(tables.next()){
                return true;
            }else{
                //TODO prompt for table creation rather than hard coded
                String createTableString = "CREATE TABLE IF NOT EXISTS LOG (\n"
                        + "	id INTEGER PRIMARY KEY,\n"
                        + "	barcode TEXT NOT NULL,\n"
                        + "	date TEXT NOT NULL,\n"
                        + " action INTEGER NOT NULL\n"
                        + ");";
                Statement statement = connection.createStatement();
                statement.execute(createTableString);
                System.out.println("LOG table created in database "+ connection.getCatalog());
                return true;
            }
        }
        return false;
     }

     private static Pair<Integer,ResultSet> checkForExistingEntry(String barcode) throws SQLException {
         Pair<Integer,ResultSet> pair;
         PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) FROM LOG WHERE barcode = ?");
         countStatement.setString(1, barcode);
         ResultSet countResultSet = countStatement.executeQuery();
         int count = 0;
         if (countResultSet.next()) {
             count = countResultSet.getInt(1);
         }
         System.out.println("Barcode "+barcode+"occurs "+count+" times.");
         if(count==0){
             return null;
         }else{
             PreparedStatement queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE barcode = ?");
             queryStatement.setString(1, barcode);
             ResultSet resultSet= queryStatement.executeQuery();
             pair = new Pair<>(count, resultSet);
             return pair;
         }
     }

     private int overwrite(String barcode, int action) throws SQLException {
        PreparedStatement updateStatement = connection.prepareStatement("UPDATE LOG SET date = datetime('now'), action = ? WHERE barcode = ?");
        //updateStatement.setString(1, date);
        updateStatement.setInt(1, action);
        updateStatement.setString(2, barcode);
        updateStatement.executeUpdate();
        return DONE;
     }
     public void closeConnection() throws SQLException {
        connection.close();
     }

     public ResultSet singleDateQuery(String date) throws SQLException {
        ResultSet results;
        PreparedStatement queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE date = date(\""+date+"\")");
        results = queryStatement.executeQuery();
        return results;
     }

     public ResultSet betweenDateQuery (String fromDate, String toDate, boolean removals) throws SQLException {
        ResultSet results;
        PreparedStatement queryStatement;
        if(removals){
            queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE date BETWEEN date(\""+fromDate+"\") AND date(\""+toDate+"\")");
        }else{
            queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE (date BETWEEN date(\""+fromDate+"\") AND date(\""+toDate+"\")) AND (action IS NOT "+ Validator.REMOVE+")");
        }
        results = queryStatement.executeQuery();
        return results;
     }

     public ResultSet betweenDateTimeQuery(String fromDateTime, boolean removals) throws SQLException{
        ResultSet results;
        PreparedStatement queryStatement;
        if(removals){
            queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE date BETWEEN datetime(\""+fromDateTime+"\") AND datetime(\"now\")");
        }else{
            queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE (date BETWEEN datetime(\""+fromDateTime+"\") AND datetime(\"now\")) AND (action IS NOT "+ Validator.REMOVE+")");
        }
        results = queryStatement.executeQuery();
        return results;
     }

}
