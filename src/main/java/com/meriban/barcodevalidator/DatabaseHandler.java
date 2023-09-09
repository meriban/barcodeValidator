package com.meriban.barcodevalidator;

import javafx.util.Pair;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;

public class DatabaseHandler {

    private static Connection connectToDatabase(File dataDirectory, String databaseName) throws SQLException {
        String url = "jdbc:sqlite:"+ dataDirectory.getAbsolutePath() + "\\"+databaseName;
        return DriverManager.getConnection(url);
    }
    public static boolean writeToDatabases(HashMap<String, File> databases, String table, String value, Action action ){
        ArrayList<Boolean> done = new ArrayList<>();
        for (String database : databases.keySet()){
            boolean success = write(databases.get(database), database, table, value, action);
            done.add(success);
        }
        return !done.contains(false);
    }
    public static boolean write(File dataDirectory, String databaseName, String table,String value, Action action ){
        try {
            Connection connection = connectToDatabase(dataDirectory,databaseName);
            if(connection !=null && !connection.isClosed()){
                if(tableExists(connection, table)){
                    Pair<Integer,ResultSet> results = checkForExistingEntry(connection, value);
                    if (results == null) {
                        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO LOG(barcode,date,action) VALUES (?,datetime('now'),?)");
                        preparedStatement.setString(1, value);
                        preparedStatement.setInt(2, action.getActionValue());
                        preparedStatement.executeUpdate();
                        connection.close();
                        return true;
                    } else {
                        if(results.getKey()==1){
                            return overwrite(connection, value, action);
                        }else{
                            System.out.println(value + " with action " + action + " could not be written to "+databaseName + " as there is more than one entry with this barcode in table.");
                            connection.close();
                            return false;
                        }
                    }
                }else{
                    System.out.println(value + " with action " + action + " could not be written to " +databaseName + " as SQL table does not exist on writeToDatabases attempt.");
                    connection.close();
                    return false;}
            }else{
                if(connection==null){System.out.println(value + " with action " + action + " could not be written to "+databaseName+" as SQL Connection was null on writeToDatabases attempt."); return false;}
                if(Objects.requireNonNull(connection).isClosed()){System.out.println(value + " with action " + action + " could not be written to "+databaseName+" as SQL Connection was closed on writeToDatabases attempt."); return false;}
            }
            connection.close();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return false;
    }
    private static boolean tableExists(Connection connection, String table) throws SQLException {
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

     private static Pair<Integer,ResultSet> checkForExistingEntry(Connection connection, String barcode) throws SQLException {
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

     private static boolean overwrite(Connection connection, String barcode, Action action) throws SQLException {
        PreparedStatement updateStatement = connection.prepareStatement("UPDATE LOG SET date = datetime('now'), action = ? WHERE barcode = ?");
        updateStatement.setInt(1, action.getActionValue());
        updateStatement.setString(2, barcode);
        updateStatement.executeUpdate();
        connection.close();
        return true;
     }

     public static ResultSet singleDateQuery(File dataDirectory, String databaseName, String date) throws SQLException {
        ResultSet results;
        Connection connection = connectToDatabase(dataDirectory,databaseName);
        PreparedStatement queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE date = date(?)");
        queryStatement.setString(1,date);
        results = queryStatement.executeQuery();
        return results;
     }

     public static ResultSet betweenDateQuery (File dataDirectory, String databaseName, String fromDate, String toDate, boolean removals) throws SQLException {
        ResultSet results;
        Connection connection = connectToDatabase(dataDirectory, databaseName);
        PreparedStatement queryStatement;

        if(removals){
            //queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE date BETWEEN date(\""+fromDate+"\") AND date(\""+toDate+"\")");
            queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE date BETWEEN date(?) AND date(?)");
            queryStatement.setString(1,fromDate);
            queryStatement.setString(2, toDate);
        }else{
            //queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE (date BETWEEN date(\""+fromDate+"\") AND date(\""+toDate+"\")) AND (action IS NOT "+ Action.REMOVE.getActionValue()+")");
            queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE (date BETWEEN date(?) AND date(?)) AND (action IS NOT ?)");
            queryStatement.setString(1,fromDate);
            queryStatement.setString(2,toDate);
            queryStatement.setInt(3,Action.REMOVE.getActionValue());
        }
        results = queryStatement.executeQuery();
        return results;
     }

     public static ResultSet betweenDateTimeQuery(File dataDirectory, String databaseName, LocalDateTime fromDateTime, boolean removals) throws SQLException{
        LocalDateTime nowDT = LocalDateTime.now();
        String now = DateParser.computeDateForQuery(nowDT);

        LocalDateTime fromDT = fromDateTime;
        if(TimeZone.getTimeZone("Europe/London").inDaylightTime(java.sql.Timestamp.valueOf(fromDT))){
            fromDT = DateParser.adjustForDST(fromDT);
        }
        String fromDateTimeString = DateParser.computeDateForQuery(fromDT);

        ResultSet results;
        Connection connection = connectToDatabase(dataDirectory, databaseName);
        PreparedStatement queryStatement;

        if(removals){
            queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE (date BETWEEN datetime(?) AND datetime(?))");
            queryStatement.setString(1,fromDateTimeString);
            queryStatement.setString(2,now);
        }else{
            queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE (date BETWEEN datetime(?) AND datetime(\"now\")) AND (action IS NOT ?)");
            queryStatement.setString(1,fromDateTimeString);
            queryStatement.setInt(2,Action.REMOVE.getActionValue());
        }
        results = queryStatement.executeQuery();
        return results;
     }

     public static ResultSet getAll(File dataDirectory, String databaseName, boolean removals)throws SQLException{
         ResultSet results;
         Connection connection = connectToDatabase(dataDirectory, databaseName);
         PreparedStatement queryStatement;
         if(removals){
             queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG");
         }else{
             queryStatement = connection.prepareStatement("SELECT barcode, date, action FROM LOG WHERE (action IS NOT ?)");
             queryStatement.setInt(1,Action.REMOVE.getActionValue());
         }
         results = queryStatement.executeQuery();
         return results;
     }



}
