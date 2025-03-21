package com.example;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.Properties;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class Database {
    private static String username;
    private static Scanner scanner = new Scanner(System.in);
    private static Connection conn;

    public static void main(String[] args) throws SQLException {

        int lport = 8089;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = "YOUR_CS_USERNAME"; //change to your username
        String password = "YOUR_CS_PASSWORD"; //change to your password
        String databaseName = "p32001_22"; //change to your database name
        try (BufferedReader br = new BufferedReader(new FileReader("my-project/credentials.txt"))) {
            user = br.readLine();
            password = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String driverName = "org.postgresql.Driver";
        Session session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            // System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
            // System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + databaseName;
            // System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");

            // Prompt the user to create an account or log into an existing account
            Scanner scanner = new Scanner(System.in);
            int choice = 0;
            while (choice != 3) {
                spaces();
                displayInitialCommands();
                System.out.print("Enter Choice: ");
                choice = scanner.nextInt();
                printLines();
                
                while (choice != 1 && choice != 2 && choice != 3) {
                    System.out.println("Invalid Choice (Enter A Number Between 1-3)");
                    choice = scanner.nextInt();
                    scanner.nextLine(); 
                }

                spaces();
                if (choice == 1) {
                    createUser();
                } else if (choice == 2) {
                    accessAccount();
                } else if (choice == 3) {
                    System.out.println("Exiting.");
                }

                int accountChoice = 0;
                while (choice == 2) {
                    displayAccountCommands();
    
                    accountChoice = scanner.nextInt();
                    scanner.nextLine();
                    spaces();
                    switch (accountChoice) {
                        case 1:
                            createCollection();
                            break;
                        case 2:
                            seeCollection();
                            break;
                        case 3:
                            searchMovieByName();
                            break;
                        case 4:
                            searchMovieByReleaseDate();
                            break;
                        case 5:
                            searchMovieByCast();
                            break;
                        case 6:
                            searchMovieByStudio();
                            break;
                        case 7:
                            searchMovieByGenre();
                            break;
                        case 8:
                            displayAddCommands();

                            accountChoice = scanner.nextInt();
                            scanner.nextLine();
                            spaces();
                            
                            switch (accountChoice) {
                                case 1:
                                    addMovies();
                                case 2:
                                    contributeTo();
                            }
                            break;
                        case 9:
                            removeMovies();
                            break;
                        case 10:
                            modifyCollectionName();
                            break;
                        case 11:
                            deleteCollection();
                            break;
                        case 12:
                            rateMovie();
                            break;
                        case 13:
                            watchMovie();
                            break;
                        case 14:
                            watchCollection();
                            break;
                        case 15:
                            followUser();
                            break;
                        case 16:
                            unfollowUser();
                            break;
                        case 17:
                            choice = 0;
                            accountChoice = 0;
                            break;
                        default:
                            System.out.println("Invalid Choice (Enter A Number Between 1-17)");
                    }
                    enterToContinue();
                }
            }
            
            scanner.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
    }
    
    // Function to create a new user account
    public static void createUser() {
        try {
            System.out.print("Enter Username: ");
            String inputtedUsername = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
            System.out.print("Enter First Name: ");
            String firstName = scanner.nextLine();
            System.out.print("Enter Last Name: ");
            String lastName = scanner.nextLine();
            System.out.print("Enter Gender (Male/Female): ");
            String gender = scanner.nextLine();
            System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
            String dobString = scanner.nextLine();
            Date dob = Date.valueOf(dobString);
            System.out.print("Enter Biography: ");
            String biography = scanner.nextLine();
            Date currentDate = new Date(System.currentTimeMillis());
            System.out.print("Enter Email (If you have multiple, enter each with a comma in between): ");
            String email = scanner.nextLine();
            ArrayList<String> emailArray = new ArrayList<>(Arrays.asList(email.split(",")));
            
           
            String createAccount = "INSERT INTO users (username, password, first_name, last_name, gender, dob, biography, last_access_date, creation_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(createAccount)) {
                pstmt.setString(1, inputtedUsername);
                pstmt.setString(2, password);
                pstmt.setString(3, firstName);
                pstmt.setString(4, lastName);
                pstmt.setString(5, gender);
                pstmt.setDate(6, dob);
                pstmt.setString(7, biography);
                pstmt.setDate(8, currentDate);
                pstmt.setDate(9, currentDate);
                pstmt.executeUpdate();
                System.out.println("\nACCOUNT CREATED SUCCESSFULLY");
            }
            
            for(int i = 0; i < emailArray.size(); i++) {
                String createEmail = "INSERT INTO email (username, email)" + 
                            "VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(createEmail)) {
                    pstmt.setString(1, inputtedUsername);
                    pstmt.setString(2, emailArray.get(i));
                    pstmt.executeUpdate();
                }
            }
            System.out.println("\nEMAIL(s) CREATED SUCCESSFULLY");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void accessAccount() {
        try {
            System.out.print("Enter Username: ");
            String usernameInput = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, usernameInput);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println();
                    if (rs.next()) {
                        username = usernameInput;
                        System.out.println("Login Successful. Welcome, " + username + "!");
                    } else {
                        System.out.println("Invalid Username/Password.");
                    }
                }
            }

            String sqlUpdate = "UPDATE users SET last_access_date = ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setDate(1, new Date(System.currentTimeMillis()));
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createCollection() {
        printLines();
        System.out.print("Enter Collection Name: ");
        String collectionName = scanner.nextLine();
        String findIdSql = "SELECT collection_id FROM collection ORDER BY collection_id";
        int newId = 1;
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(findIdSql)) {
            while (rs.next()) {
                if (rs.getInt("collection_id") == newId) {
                    newId++;
                } else {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "INSERT INTO collection (collection_id, owner_username, collection_name, quantity)" +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newId);
            pstmt.setString(2, username);
            pstmt.setString(3, collectionName);
            pstmt.setInt(4, 0);
            pstmt.executeUpdate();
            System.out.println("\nCOLLECTION CREATED SUCCESSFULLY");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void seeCollection() {
        String sql = "SELECT collection.collection_name, collection.quantity, SUM(movie.length) AS total_length FROM collection " + 
                     "INNER JOIN consists_of " + 
                     "ON collection.collection_id = consists_of.collection_id " +
                     "INNER JOIN movie " +
                     "ON consists_of.movie_id = movie.movie_id " +
                     "WHERE collection.owner_username = ? " +
                     "GROUP BY collection.collection_id, collection.collection_name, collection.quantity";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-30s %-10s %-10s%n", "Collection Name", "Quantity", "Total Length");
                System.out.println("--------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-30s %-10s %-10s%n", rs.getString("collection_name"), rs.getInt("quantity"), rs.getTime("total_length"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void searchMovieByName() {
        System.out.print("Enter Movie Name: ");
        String movieName = scanner.nextLine();
        String sql = "SELECT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, movie.length, movie.mpaa_rating, rates.rating AS rate " +
                     "FROM movie " +
                     "INNER JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "INNER JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "INNER JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "INNER JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id " +
                     "WHERE rates.username = ? AND movie.title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, movieName);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                System.out.println("----------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), rs.getDouble("rate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void searchMovieByReleaseDate() {
        System.out.print("Enter Release Date: ");
        String release_date_string = scanner.nextLine();
        Date release_date = Date.valueOf(release_date_string);
        String sql1 = "SELECT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, movie.length, movie.mpaa_rating, rates.rating AS rate " +
                     "FROM movie " +
                     "INNER JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "INNER JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "INNER JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "INNER JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id " +
                     "INNER JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "WHERE released_on.release_date = ? ";
        try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setDate(1, release_date);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                System.out.println("----------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                    Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                    System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void searchMovieByCast() {
        System.out.print("Enter Cast Member Name: ");
        String castName = scanner.nextLine();
        String first = castName.split(" ")[0];
        String last = castName.split(" ")[1];
        String sql = "SELECT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, movie.length, movie.mpaa_rating, rates.rating AS rate  " +
                     "FROM movie " +
                     "INNER JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "INNER JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "INNER JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "INNER JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id " +
                     "INNER JOIN casts " +
                     "ON movie.movie_id = casts.movie_id " +
                     "INNER JOIN cast_member " + 
                     "ON casts.genre_id = cast_member.genre_id " +
                     "WHERE rates.username = ? AND cast_member.first_name = ? AND cast_member.last_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, first);
            pstmt.setString(3, last);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                System.out.println("----------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), rs.getDouble("rate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void searchMovieByStudio() {
        System.out.print("Enter Studio Name: ");
        String studio = scanner.nextLine();
        String sql = "SELECT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, movie.length, movie.mpaa_rating, rates.rating AS rate " +
                     "FROM movie " +
                     "INNER JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "INNER JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "INNER JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "INNER JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id " +
                     "INNER JOIN studio " +
                     "ON movie.studio_id = studio.studio_id " +
                     "WHERE rates.username = ? AND studio.name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, studio);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                System.out.println("----------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), rs.getDouble("rate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void searchMovieByGenre() {
        System.out.print("Enter Movie Genre: ");
        String genre = scanner.nextLine();
        String sql = "SELECT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, movie.length, movie.mpaa_rating, rates.rating AS rate " +
                     "FROM movie " +
                     "INNER JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "INNER JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "INNER JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "INNER JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id " +
                     "INNER JOIN has_genre " +
                     "ON movie.movie_id = has_genre.movie_id " +
                     "INNER JOIN genre " + 
                     "ON has_genre.genre_id = genre.genre_id " +
                     "WHERE rates.username = ? AND genre.genre_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, genre);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                System.out.println("----------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), rs.getDouble("rate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void addMovies() {
        int collectionID = 0;
        int movieID = 0;

        String searchSql = "SELECT collection.collection_id, collection.collection_name, collection.quantity " +
                           "FROM collection " +
                           "WHERE owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(searchSql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Your collections: ");
                System.out.println("-----------------");
                while(rs.next()) {
                    System.out.print(rs.getString("collection_name") + ", ");
                }
                System.out.println("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Enter the name of the collection you want to add the movie to:");
        String collectionName = scanner.nextLine();
        System.out.println("Enter the name of the movie you want to add:");
        String movie = scanner.nextLine();

        String findMovieSQL = "SELECT movie.movie_id " +
                     "FROM movie " +
                     "WHERE title = ?";
         try (PreparedStatement pstmt = conn.prepareStatement(findMovieSQL)) {
            pstmt.setString(1, movie);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                movieID = rs.getInt("movie_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String findCollectionSQL = "SELECT collection_id " +
                     "FROM collection  " +
                     "WHERE owner_username = ? AND collection_name = ?";
         try (PreparedStatement pstmt = conn.prepareStatement(findCollectionSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, collectionName);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                collectionID = rs.getInt("collection_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insertSQL = "UPDATE collection " + 
                     "SET quantity = quantity + 1 " +
                     "WHERE collection_id = ? AND owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, collectionID);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            System.out.println("Collection updated successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insertSQL2 = "INSERT INTO consists_of (collection_id, owner_username, movie_id)" +
                     "VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL2)) {
            pstmt.setInt(1, collectionID);
            pstmt.setString(2, username);
            pstmt.setInt(3, movieID);
            pstmt.executeUpdate();
            System.out.println("Movie added to " + collectionName + " successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void contributeTo() {
        System.out.println("Enter Collection Name: ");
        String collectionName = scanner.nextLine();
        System.out.println("Enter Collection ID: ");
        String collectionID = scanner.nextLine();
        System.out.println("Enter Collection Owner's Username: ");
        String ownerUsername = scanner.nextLine();
    }

    public static void removeMovies() {
        System.out.println("Enter the name of the collection you want to delete a movie from:");
        String collectionName = scanner.nextLine();
        System.out.println("Enter the name of the movie you want to delete:");
        String movieName = scanner.nextLine();
        int collectionID = -1;
        int movieID = -1;
        String sql = "SELECT collection_id FROM collection WHERE collection_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collectionName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    collectionID = rs.getInt("collection_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        String movieSql = "SELECT movie_id FROM movies WHERE title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(movieSql)) {
            pstmt.setString(1, movieName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    movieID = rs.getInt("movie_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        String deleteSql = "DELETE FROM collection_name WHERE movie_id = ? AND collection_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, movieID);
            pstmt.setInt(2, collectionID);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Movie '" + movieName + "' deleted successfully from collection '" + collectionName + "'.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void modifyCollectionName() {
        System.out.println("Enter the name of the collection you want to modify:");
        String collectionName = scanner.nextLine();
        System.out.println("Enter the new name of the collection:");
        String newCollectionName = scanner.nextLine();
        String sql = "UPDATE collection SET collection_name = ? WHERE collection_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newCollectionName);
            pstmt.setString(2, collectionName);
            pstmt.executeUpdate();
            System.out.println("Collection updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCollection() {
        System.out.println("Enter the name of the collection:");
        String collectionName = scanner.nextLine();
        
        // Check if collection exists
        String checkSql = "SELECT * FROM collection WHERE owner_username = ? AND collection_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, collectionName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Collection exists, proceed with deletion
                    String deleteSql = "DELETE FROM collection WHERE owner_username = ? AND collection_name = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setString(1, username);
                        deleteStmt.setString(2, collectionName);
                        deleteStmt.executeUpdate();
                        System.out.println("Collection deleted successfully.");
                    }
                } else {
                    // Collection does not exist
                    System.out.println("Collection not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void rateMovie() {
        System.out.println("Enter Movie Name:");
        String movieName = scanner.nextLine();
        System.out.println("Enter Rating:");
        double rating = scanner.nextDouble();
        System.out.println("Recommend?");
        String rec = scanner.nextLine();
        boolean recommend = false;
        if (rec.equals("yes")) {
            recommend = true;
        } else {
            recommend = false;
        }
        scanner.nextLine();
        int movieID = -1;
        String sql = "SELECT movie_id FROM movie WHERE title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movieName);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                movieID = rs.getInt("movie_id");
            }
        }   
        catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        sql = "INSERT INTO rates (username, movie_id, recommend, rating)" +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, movieID);
            pstmt.setBoolean(3, recommend);
            pstmt.setDouble(4, rating);
            pstmt.executeUpdate();
            System.out.println("Rating created successfully.");
        }   
        catch (SQLException e) {
            e.printStackTrace();
        } 
    }

    public static void watchMovie() {
        System.out.println("Enter the name of the movie you want to watch:");
        String movieName = scanner.nextLine();
        int movieID = -1;
        Time movieLength = null;
        
        // Get the movie ID
        String sql = "SELECT movie_id, length FROM movie where title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movieName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    movieID = rs.getInt("movie_id");
                    movieLength = rs.getTime("length");
                } else {
                    System.out.println("Movie not found.");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        
        // Insert into watched
        Timestamp startTime = new Timestamp(System.currentTimeMillis());
        int hours = movieLength.toLocalTime().getHour();
        int minutes = movieLength.toLocalTime().getMinute();
        int seconds = movieLength.toLocalTime().getSecond();
        long movieLengthMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L;
        Timestamp endTime = new Timestamp(startTime.getTime() + movieLengthMillis);
        String insertSql = "INSERT INTO watches (username, movie_id, start_time, end_time) " +
                           "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, movieID);
            pstmt.setTimestamp(3, startTime);
            pstmt.setTimestamp(4, endTime);
            pstmt.executeUpdate();
            System.out.println("Movie watched successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void watchCollection() {
        String searchSql = "SELECT collection.collection_id, collection.collection_name, collection.quantity " +
                           "FROM collection " +
                           "WHERE owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(searchSql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Your collections: ");
                System.out.println("-----------------");
                while(rs.next()) {
                    System.out.print(rs.getString("collection_name") + ", ");
                }
                System.out.println("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        
        System.out.println("Enter the name of the collection you want to watch:");
        String collectionName = scanner.nextLine();
        int collectionID = -1;
        String sql = "SELECT collection_id FROM collection WHERE collection_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collectionName);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                collectionID = rs.getInt("collection_id");
            }
        }   
        catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        ArrayList<Integer> movieArray = new ArrayList<Integer>();
        ArrayList<Time> movieLengthArray = new ArrayList<Time>();

        String getMovieLengthSQL = "SELECT m.movie_id, m.length " +
                     "FROM consists_of c " +
                     "JOIN movie m ON c.movie_id = m.movie_id " +
                     "WHERE c.collection_id = ? AND c.owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(getMovieLengthSQL)) {
            pstmt.setInt(1, collectionID);
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    movieArray.add(rs.getInt("movie_id"));
                    movieLengthArray.add(rs.getTime("length"));
                }
                System.out.println(movieArray.size() + " movies set to watched in collection: " + collectionName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        for(int i = 0; i < movieArray.size(); i++){
            String insertSQL = "INSERT INTO watches (username, movie_id, start_time, end_time) " +
                     "VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                Timestamp startTime = new Timestamp(System.currentTimeMillis());
                int hours = movieLengthArray.get(i).toLocalTime().getHour();
                int minutes = movieLengthArray.get(i).toLocalTime().getMinute();
                int seconds = movieLengthArray.get(i).toLocalTime().getSecond();
                long movieLengthMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L;
                Timestamp endTime = new Timestamp(startTime.getTime() + movieLengthMillis);
                pstmt.setString(1, username);
                pstmt.setInt(2, movieArray.get(i));
                pstmt.setTimestamp(3, startTime);
                pstmt.setTimestamp(4, endTime);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
        System.out.println("Collection watched successfully.");
    }

    public static void followUser() {
        System.out.println("Enter the username of the user you want to follow:");
        String userToFollow = scanner.nextLine();
        String sql = "INSERT INTO follows (follower, followee) " +
                     "VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, userToFollow);
            pstmt.executeUpdate();
            System.out.println("Followed user successfully.");
        }   
        catch (SQLException e) {
            e.printStackTrace();
        } 
    }

    public static void unfollowUser() {
        System.out.println("Enter the username of the user you want to unfollow");
        String userToUnfollow = scanner.nextLine();
        String sql = "DELETE FROM follows WHERE follower = ? AND followee = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, userToUnfollow);
            pstmt.executeUpdate();
            System.out.println("Unfollowed user successfully.");
        }   
        catch (SQLException e) {
            e.printStackTrace();
        } 
    }

    public static void displayInitialCommands() {
        printLines();
        System.out.println("Choose An Option:");
        System.out.println("1. Create Account");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        printLines();
    }

    public static void displayAccountCommands() {
        spaces();
        printLines();
        System.out.println("1. Create Collection");
        System.out.println("2. See Collection");
        System.out.println("3. Search Movie by Name");
        System.out.println("4. Search Movie by Release Date");
        System.out.println("5. Search Movie by Cast");
        System.out.println("6. Search Movie by Studio");
        System.out.println("7. Search Movie by Genre");
        System.out.println("8. Add Movie to Collection");
        System.out.println("9. Remove Movie from Collection");
        System.out.println("10. Modify Collection Name");
        System.out.println("11. Delete Collection");
        System.out.println("12. Rate Movie");
        System.out.println("13. Watch Movie");
        System.out.println("14. Watch Collection");
        System.out.println("15. Follow User");
        System.out.println("16. Unfollow User");
        System.out.println("17. Exit");
        printLines();
        System.out.print("Enter Choice: ");
    }

    public static void displayAddCommands() {
        spaces();
        printLines();
        System.out.println("1. Add to my Collections");
        System.out.println("2. Add to a Different Collection");
        printLines();
        System.out.print("Enter Choice: ");
    }

    public static void printLines() {
        System.out.println("-----------------------------------------");
    }

    public static void enterToContinue() {
        System.out.print("\nPress Enter To Continue... ");
        scanner.nextLine();
    }

    public static void spaces() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
}