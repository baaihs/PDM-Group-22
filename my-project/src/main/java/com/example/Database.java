package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Database {
    private static String username;
    private static Scanner scanner = new Scanner(System.in);
    private static Connection conn;

    public static void main(String[] args) throws SQLException {

        int lport = 14289;
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
            boolean loggedIn = false;
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
                    loggedIn = accessAccount();
                    if (!loggedIn) {
                        choice = 0;
                    }
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
                                    break;
                                case 2:
                                    contributeTo();
                                    break;
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
                            displayCollectionCount();
                            break;
                        case 18:
                            displayFollowerCount();
                            break;
                        case 19:
                            displayFollowingCount();
                            break;
                        case 20:
                            displayTop10();
                            break;
                        case 21:
                            top20In90Days();
                            break;
                        case 22:
                            top20AmongFollowers();
                            break;
                        case 23:
                            top5OfTheMonth();
                            break;
                        case 24:
                            displayRecommendationOptions();

                            accountChoice = scanner.nextInt();
                            scanner.nextLine();
                            spaces();
                            
                            switch (accountChoice) {
                                case 1:
                                    displayRecommendationBasedOnPlayHistory();
                                    break;
                                case 2:
                                    displayRecommendationBasedOnOthers();
                                    break;
                            }
                            break;
                        case 25:
                            hypothesis1();
                            break;
                        case 26:
                            hypothesis2();
                            break;
                        case 27:
                            choice = 0;
                            accountChoice = 0;
                            loggedIn = false;
                            break;
                        default:
                            System.out.println("Invalid Choice (Enter A Number Between 1-25)");
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

    public static void hashAllPasswords() {
        String selectSql = "SELECT username, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE username = ?";
        
        try (
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            ResultSet rs = selectStmt.executeQuery()
        ) {
            int updatedCount = 0;
            while (rs.next()) {
                String username = rs.getString("username");
                String plainPassword = rs.getString("password");
    
                // Check if password might already be hashed (optional but recommended)
                if (plainPassword.length() == 64 && plainPassword.matches("[a-fA-F0-9]+")) {
                    continue; // Skip already hashed passwords (SHA-256 is 64 hex characters)
                }
    
                String hashedPassword = generateHashedPassword(plainPassword, username);
    
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, username);
                updateStmt.addBatch();
    
                updatedCount++;
            }
    
            updateStmt.executeBatch();
            System.out.println("Updated " + updatedCount + " password(s) to hashed values.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Function to create a new user account
    public static void createUser() throws NoSuchAlgorithmException, NoSuchProviderException {
        try {
            System.out.print("Enter Username: ");
            String inputtedUsername = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
            String hashedPassword = generateHashedPassword(password, inputtedUsername);


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
            //Date currentDate = new Date(System.currentTimeMillis());
            Timestamp currentDate = new Timestamp(System.currentTimeMillis());
            System.out.print("Enter Email (If you have multiple, enter each with a comma in between): ");
            String email = scanner.nextLine();
            ArrayList<String> emailArray = new ArrayList<>(Arrays.asList(email.split(",")));
            
           
            String createAccount = "INSERT INTO users (username, password, first_name, last_name, gender, dob, biography, last_access_date, creation_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(createAccount)) {
                pstmt.setString(1, inputtedUsername);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, firstName);
                pstmt.setString(4, lastName);
                pstmt.setString(5, gender);
                pstmt.setDate(6, dob);
                pstmt.setString(7, biography);
                // pstmt.setDate(8, currentDate);
                // pstmt.setDate(9, currentDate);
                pstmt.setTimestamp(8, currentDate);
                pstmt.setTimestamp(9, currentDate);
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

    public static String generateHashedPassword(String password, String username) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            String saltedPassword = password + username;
            
            byte[] bytes = md.digest(saltedPassword.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b: bytes) {
                sb.append(String.format("%02x", b));
            }

            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static boolean verifyPassword(String password, String username, String hashedPassword) {
        if(generateHashedPassword(password, username).equals(hashedPassword)) {
            return true;
        }
        return false;
    }

    // Function to access an account through login
    public static boolean accessAccount() {
        try {
            System.out.print("Enter Username: ");
            String usernameInput = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            String hashedPassword = generateHashedPassword(password, usernameInput);
            // boolean correctPassword = verifyPassword(password, usernameInput, hashedPassword);

            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, usernameInput);
                pstmt.setString(2, hashedPassword);
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println();
                    if (rs.next()) {
                        username = usernameInput;
                        System.out.println("Login Successful. Welcome, " + username + "!");
                    } else {
                        System.out.println("Invalid Username/Password.");
                        return false;
                    }
                }
            }

            String sqlUpdate = "UPDATE users SET last_access_date = ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                // pstmt.setDate(1, new Date(System.currentTimeMillis()));
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    // Function to create a collection
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
    
    // Function to view all of the user's collection
    public static void seeCollection() {
        String sql = "SELECT collection.collection_name, collection.quantity, SUM(movie.length) AS total_length FROM collection " + 
                     "LEFT JOIN consists_of " + 
                     "ON collection.collection_id = consists_of.collection_id " +
                     "LEFT JOIN movie " +
                     "ON consists_of.movie_id = movie.movie_id " +
                     "WHERE collection.owner_username = ? " +
                     "GROUP BY collection.collection_id, collection.collection_name, collection.quantity " +
                     "ORDER BY collection.collection_name ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-30s %-10s %-10s%n", "Collection Name", "Quantity", "Total Length");
                System.out.println("--------------------------------------------------------------");
                while (rs.next()) {
                    printLines();
                    System.out.printf("%-30s %-10s %-10s%n", rs.getString("collection_name"), rs.getInt("quantity"), rs.getTime("total_length"));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to search movies by name
    public static void searchMovieByName() {
        System.out.print("Enter Movie Name: ");
        String movieName = scanner.nextLine();
        String sql = "SELECT DISTINCT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, " +
                     "movie.length, movie.mpaa_rating, rates.rating AS rate, released_on.release_date AS release_date " +
                     "FROM movie " +
                     "LEFT JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "LEFT JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "LEFT JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "LEFT JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id AND rates.username = ? " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "LEFT JOIN produced_by " +
                     "ON movie.movie_id = produced_by.movie_id " +
                     "LEFT JOIN studio " +
                     "ON produced_by.studio_id = studio.studio_id " +
                     "LEFT JOIN has_genre " +
                     "ON movie.movie_id = has_genre.movie_id " +
                     "LEFT JOIN genre " + 
                     "ON has_genre.genre_id = genre.genre_id " +
                     "WHERE movie.title = ? " +
                     "ORDER BY movie.title ASC, released_on.release_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, movieName);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Sorted by title, release date ascending");
                System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                printLineTables();
                while (rs.next()) {
                    BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                    Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"));
                }

                while (true) {
                    System.out.println("\nOther Sort Options: ");
                    printLines();
                    displaySearchSortingOptions();
                    System.out.print("Enter A Number Between 1-5: ");
                    int sortingOption = scanner.nextInt();
                    if (sortingOption == 5) {
                        break;
                    }
                    System.out.println("\nAscending or Descending: ");
                    displayAscDescOptions();
                    System.out.print("Enter A Number Between 1-2: ");
                    int ascDescOption = scanner.nextInt();
                    spaces();
                    switch(sortingOption) {
                        case 1:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 2:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 3:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 4:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        default:
                            System.out.println("Invalid Choice (Enter a Number Between 1-5)");   
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to search movies by release date
    public static void searchMovieByReleaseDate() {
        System.out.print("Enter Release Date (YYYY-MM-DD): ");
        String release_date_string = scanner.nextLine();
        Date release_date = Date.valueOf(release_date_string);
        String sql = "SELECT DISTINCT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, " +
                     "movie.length, movie.mpaa_rating, rates.rating AS rate, released_on.release_date AS release_date " +
                     "FROM movie " +
                     "LEFT JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "LEFT JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "LEFT JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "LEFT JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id AND rates.username = ? " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "LEFT JOIN produced_by " +
                     "ON movie.movie_id = produced_by.movie_id " +
                     "LEFT JOIN studio " +
                     "ON produced_by.studio_id = studio.studio_id " +
                     "LEFT JOIN has_genre " +
                     "ON movie.movie_id = has_genre.movie_id " +
                     "LEFT JOIN genre " + 
                     "ON has_genre.genre_id = genre.genre_id " +
                     "WHERE released_on.release_date = ? " +
                     "ORDER BY movie.title ASC, released_on.release_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setDate(2, release_date);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Sorted by title, release date ascending");
                System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                printLineTables();
                while (rs.next()) {
                    BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                    Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"));
                }

                while (true) {
                    System.out.println("\nOther Sort Options: ");
                    printLines();
                    displaySearchSortingOptions();
                    System.out.print("Enter A Number Between 1-5: ");
                    int sortingOption = scanner.nextInt();
                    if (sortingOption == 5) {
                        break;
                    }
                    System.out.println("\nAscending or Descending: ");
                    displayAscDescOptions();
                    System.out.print("Enter A Number Between 1-2: ");
                    int ascDescOption = scanner.nextInt();
                    spaces();
                    switch(sortingOption) {
                        case 1:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 2:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 3:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 4:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        default:
                            System.out.println("Invalid Choice (Enter a Number Between 1-5)");   
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to search movies by cast
    public static void searchMovieByCast() {
        System.out.print("Enter Cast Member Full Name: ");
        String castName = scanner.nextLine();
        String[] nameArray = castName.split(" ");
        String first = nameArray[0];
        String last = nameArray[1];
        String sql = "SELECT DISTINCT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, " +
                     "movie.length, movie.mpaa_rating, rates.rating AS rate, released_on.release_date AS release_date " +
                     "FROM movie " +
                     "LEFT JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "LEFT JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "LEFT JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "LEFT JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id AND rates.username = ? " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "LEFT JOIN produced_by " +
                     "ON movie.movie_id = produced_by.movie_id " +
                     "LEFT JOIN studio " +
                     "ON produced_by.studio_id = studio.studio_id " +
                     "LEFT JOIN has_genre " +
                     "ON movie.movie_id = has_genre.movie_id " +
                     "LEFT JOIN genre " + 
                     "ON has_genre.genre_id = genre.genre_id " +
                     "WHERE cast_member.first_name = ? AND cast_member.last_name = ? " + 
                     "ORDER BY movie.title ASC, released_on.release_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, first);
            pstmt.setString(3, last);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Sorted by title, release date ascending");
                System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                printLineTables();
                while (rs.next()) {
                    BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                    Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"));
                }

                while (true) {
                    System.out.println("\nOther Sort Options: ");
                    printLines();
                    displaySearchSortingOptions();
                    System.out.print("Enter A Number Between 1-5: ");
                    int sortingOption = scanner.nextInt();
                    if (sortingOption == 5) {
                        break;
                    }
                    System.out.println("\nAscending or Descending: ");
                    displayAscDescOptions();
                    System.out.print("Enter A Number Between 1-2: ");
                    int ascDescOption = scanner.nextInt();
                    spaces();
                    switch(sortingOption) {
                        case 1:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 2:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 3:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 4:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        default:
                            System.out.println("Invalid Choice (Enter a Number Between 1-5)");   
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to search movies by studio
    public static void searchMovieByStudio() {
        System.out.print("Enter Studio Full Name: ");
        String studio = scanner.nextLine();
        String sql = "SELECT DISTINCT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, " +
                     "movie.length, movie.mpaa_rating, rates.rating AS rate, released_on.release_date AS release_date, studio.name AS studio_name " +
                     "FROM movie " +
                     "LEFT JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "LEFT JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "LEFT JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "LEFT JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id AND rates.username = ? " +
                     "LEFT JOIN produced_by " +
                     "ON movie.movie_id = produced_by.movie_id " +
                     "LEFT JOIN studio " +
                     "ON produced_by.studio_id = studio.studio_id " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "LEFT JOIN has_genre " +
                     "ON movie.movie_id = has_genre.movie_id " +
                     "LEFT JOIN genre " + 
                     "ON has_genre.genre_id = genre.genre_id " +
                     "WHERE studio.name = ? " + 
                     "ORDER BY movie.title ASC, released_on.release_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, studio);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Sorted by title, release date ascending");
                System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                printLineTables();
                while (rs.next()) {
                    BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                    Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"));
                }

                while (true) {
                    System.out.println("\nOther Sort Options: ");
                    printLines();
                    displaySearchSortingOptions();
                    System.out.print("Enter A Number Between 1-5: ");
                    int sortingOption = scanner.nextInt();
                    if (sortingOption == 5) {
                        break;
                    }
                    System.out.println("\nAscending or Descending: ");
                    displayAscDescOptions();
                    System.out.print("Enter A Number Between 1-2: ");
                    int ascDescOption = scanner.nextInt();
                    spaces();
                    switch(sortingOption) {
                        case 1:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 2:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 3:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 4:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        default:
                            System.out.println("Invalid Choice (Enter a Number Between 1-5)");   
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to search movies by genre
    public static void searchMovieByGenre() {
        System.out.print("Enter Movie Genre: ");
        String genre = scanner.nextLine();
        String sql = "SELECT DISTINCT movie.title, cast_member.first_name AS cast_first, cast_member.last_name AS cast_last, director.first_name AS director_first, director.last_name AS director_last, " +
                     "movie.length, movie.mpaa_rating, rates.rating AS rate, released_on.release_date AS release_date, genre.genre_name AS genre_name " +
                     "FROM movie " +
                     "LEFT JOIN casts " + 
                     "ON movie.movie_id = casts.movie_id " + 
                     "LEFT JOIN cast_member " +
                     "ON casts.member_id = cast_member.member_id " +
                     "LEFT JOIN directed_by " +
                     "ON movie.movie_id = directed_by.movie_id " +
                     "LEFT JOIN director " +
                     "ON directed_by.director_id = director.director_id " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id AND rates.username = ? " +
                     "LEFT JOIN has_genre " +
                     "ON movie.movie_id = has_genre.movie_id " +
                     "LEFT JOIN genre " + 
                     "ON has_genre.genre_id = genre.genre_id " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "LEFT JOIN produced_by " +
                     "ON movie.movie_id = produced_by.movie_id " +
                     "LEFT JOIN studio " +
                     "ON produced_by.studio_id = studio.studio_id " +
                     "WHERE genre.genre_name = ? " +
                     "ORDER BY movie.title ASC, released_on.release_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, genre);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Sorted by title, release date ascending");
                System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                printLineTables();
                while (rs.next()) {
                    BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                    Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"));
                }

                while (true) {
                    System.out.println("\nOther Sort Options: ");
                    printLines();
                    displaySearchSortingOptions();
                    System.out.print("Enter A Number Between 1-5: ");
                    int sortingOption = scanner.nextInt();
                    if (sortingOption == 5) {
                        break;
                    }
                    System.out.println("\nAscending or Descending: ");
                    displayAscDescOptions();
                    System.out.print("Enter A Number Between 1-2: ");
                    int ascDescOption = scanner.nextInt();
                    spaces();
                    switch(sortingOption) {
                        case 1:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 2:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 3:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        case 4:
                            sortMovies(pstmt, sortingOption, ascDescOption);
                            break;
                        default:
                            System.out.println("Invalid Choice (Enter a Number Between 1-5)");   
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void sortMovies(PreparedStatement pstmt, int sortBy, int ascOrDesc) {
        String sql = pstmt.toString();
        String sortByString = "";
        String ascOrDescString = "";
        if (sortBy == 1) {
            sortByString = "movie.title";
        }
        if (sortBy == 2) {
            sortByString = "studio.name";
            if (!sql.contains(sortByString)) {
                String[] sqlArray2 = sql.split("FROM");
                sql = sqlArray2[0] + ", studio.name AS studio_name FROM" + sqlArray2[1];
            }
        }
        if (sortBy == 3) {
            sortByString = "genre.genre_name";
            if (!sql.contains(sortByString)) {
                String[] sqlArray2 = sql.split("FROM");
                sql = sqlArray2[0] + ", genre.genre_name AS genre_name FROM" + sqlArray2[1];
            }
        }
        if (sortBy == 4) {
            sortByString = "released_on.release_date";
        }
        if (ascOrDesc == 1) {
            ascOrDescString = "ASC";
        } else {
            ascOrDescString = "DESC";
        }
        String[] sqlArray = sql.split("ORDER BY");
        sql = sqlArray[0] + "ORDER BY " + sortByString + " " + ascOrDescString;
        try (PreparedStatement pstmt2 = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt2.executeQuery()) {
                if (sortBy == 1) {
                    System.out.println("Sorting By Movie Name " + ascOrDescString);
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
                    printLineTables();
                    while (rs.next()) {
                        BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                        Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                        System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"));
                    }
                }
                if (sortBy == 2) {
                    System.out.println("Sorting By Movie Studio " + ascOrDescString);
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating", "studio");
                    printLineTables();
                    while (rs.next()) {
                        BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                        Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                        System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"), rs.getString("studio_name"));
                    }
                }
                if (sortBy == 3) {
                    System.out.println("Sorting By Movie Genre " + ascOrDescString);
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating", "genre");
                    printLineTables();
                    while (rs.next()) {
                        BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                        Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                        System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"), rs.getString("genre_name"));
                    }
                }
                if (sortBy == 4) {
                    System.out.println("Sorting By Movie Release Year " + ascOrDescString);
                    System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating", "release_year");
                    printLineTables();
                    while (rs.next()) {
                        BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                        Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                        System.out.printf("%-40s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), (rating != null ? rating.toString() : "NULL"), rs.getString("release_date"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to add a movie to a collection
    public static void addMovies() {
        int collectionID = 0;
        int movieID = 0;

        String searchSql = "SELECT collection.collection_id, collection.collection_name, collection.quantity " +
                           "FROM collection " +
                           "WHERE owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(searchSql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("My Collections: ");
                printLines();
                rs.next();
                System.out.print(rs.getString("collection_name"));
                while(rs.next()) {
                    System.out.print(", " + rs.getString("collection_name"));
                }
                System.out.println("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.print("Enter Name of Collection To Add Movie To: ");
        String collectionName = scanner.nextLine();
        System.out.print("Enter Movie Name: ");
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
            System.out.println("COLLECTION UPDATED SUCCESSFULLY");
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
            System.out.println("MOVIE ADDED TO " + collectionName + " SUCCESSFULLY");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String searchSQL = "SELECT COUNT(*) as count FROM contribute_to WHERE collection_id = ? AND username = ? AND owner_username = ?";
        boolean exists = false;
        try (PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {
            pstmt.setInt(1, collectionID);
            pstmt.setString(2, username);
            pstmt.setString(3, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    exists = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if(!exists) {
            String insertSQL3 = "INSERT INTO contribute_to (collection_id, username, owner_username)" +
                     "VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL3)) {
                pstmt.setInt(1, collectionID);
                pstmt.setString(2, username);
                pstmt.setString(3, username);
                pstmt.executeUpdate();
                System.out.println("MOVIE ADDED TO CONTRIBUTIONS SUCCESSFULLY");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Function to add a movie to a collection that isn't owned by the user
    public static void contributeTo() {
        System.out.print("Enter Collection ID: ");
        int collectionID = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Collection Owner's Username: ");
        String ownerUsername = scanner.nextLine();
        System.out.print("Enter Movie Name: ");
        String movie = scanner.nextLine();

        int movieID = -1;
        String collectionName = "";
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

        String findCollectionSQL = "SELECT collection_name " +
                     "FROM collection  " +
                     "WHERE owner_username = ? AND collection_id = ?";
         try (PreparedStatement pstmt = conn.prepareStatement(findCollectionSQL)) {
            pstmt.setString(1, ownerUsername);
            pstmt.setInt(2, collectionID);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                collectionName = rs.getString("collection_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insertSQL = "UPDATE collection " + 
                     "SET quantity = quantity + 1 " +
                     "WHERE collection_id = ? AND owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, collectionID);
            pstmt.setString(2, ownerUsername);
            pstmt.executeUpdate();
            System.out.println("COLLECTION UPDATED SUCCESSFULLY");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insertSQL2 = "INSERT INTO consists_of (collection_id, owner_username, movie_id)" +
                     "VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL2)) {
            pstmt.setInt(1, collectionID);
            pstmt.setString(2, ownerUsername);
            pstmt.setInt(3, movieID);
            pstmt.executeUpdate();
            System.out.println("MOVIE ADDED TO " + collectionName + " SUCCESSFULLY");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        String searchSQL = "SELECT COUNT(*) as count FROM contribute_to WHERE collection_id = ? AND username = ? AND owner_username = ?";
        boolean exists = false;
        try (PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {
            pstmt.setInt(1, collectionID);
            pstmt.setString(2, username);
            pstmt.setString(3, ownerUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    exists = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if(!exists) {
            String insertSQL3 = "INSERT INTO contribute_to (collection_id, username, owner_username)" +
                     "VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL3)) {
                pstmt.setInt(1, collectionID);
                pstmt.setString(2, username);
                pstmt.setString(3, ownerUsername);
                pstmt.executeUpdate();
                System.out.println("MOVIE ADDED TO CONTRIBUTIONS SUCCESSFULLY");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Function to remove a movie from a user's collection
    public static void removeMovies() {
        System.out.print("Enter Collection Name To Remove Movies From: ");
        String collectionName = scanner.nextLine();
        System.out.print("Enter Movie Name to Remove: ");
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
                System.out.println(collectionID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        String movieSql = "SELECT movie_id FROM movie WHERE title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(movieSql)) {
            pstmt.setString(1, movieName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    movieID = rs.getInt("movie_id");
                }
                System.out.println(movieID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        String deleteSql = "DELETE FROM consists_of WHERE movie_id = ? AND collection_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, movieID);
            pstmt.setInt(2, collectionID);
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("executed");
            if (rowsDeleted > 0) {
                System.out.println("MOVIE '" + movieName + "' DELETED SUCCESSFULLY FROM COLLECTION '" + collectionName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to change a user's collection's name
    public static void modifyCollectionName() {
        System.out.print("Enter Collection Name To Modify: ");
        String collectionName = scanner.nextLine();
        System.out.print("Enter New Collection Name: ");
        String newCollectionName = scanner.nextLine();
        String sql = "UPDATE collection SET collection_name = ? WHERE collection_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newCollectionName);
            pstmt.setString(2, collectionName);
            pstmt.executeUpdate();
            System.out.println("COLLECTION UPDATED SUCCESSFULLY");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to delete a user's collection
    public static void deleteCollection() {
        String searchSql = "SELECT collection.collection_id, collection.collection_name, collection.quantity " +
                           "FROM collection " +
                           "WHERE owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(searchSql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("My Collections: ");
                printLines();
                if (rs.next()) {
                    System.out.print(rs.getString("collection_name"));
                }
                else {
                    System.out.println("NO COLLECTIONS FOUND");
                    return;
                }
                while(rs.next()) {
                    System.out.print(", " + rs.getString("collection_name"));
                }
                System.out.println("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.print("Enter Collection To Delete: ");
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
                        System.out.println("COLLECTION DELETED SUCCESSFULLY");
                    }
                } else {
                    // Collection does not exist
                    System.out.println("COLLECTION NOT FOUND");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to rate a movie
    public static void rateMovie() {
        System.out.print("Enter Movie Name: ");
        String movieName = scanner.nextLine();
        System.out.print("Enter Rating (0.0 to 5.0): ");
        double rating = scanner.nextDouble();
        System.out.print("Recommend? (Yes/No): ");
        String rec = scanner.nextLine();
        boolean recommend = false;
        if (rec.equals("Yes")) {
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
            System.out.println("RATING CREATED SUCCESSFULLY");
        }   
        catch (SQLException e) {
            e.printStackTrace();
        } 
    }

    // Function to watch a movie
    public static void watchMovie() {
        System.out.print("Enter Movie To Watch: ");
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
                    System.out.println("MOVIE NOT FOUND");
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
            System.out.println("MOVIE WATCHED SUCCESSFULLY");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    // Function to watch all movies in a collection
    public static void watchCollection() {
        String searchSql = "SELECT collection.collection_id, collection.collection_name, collection.quantity " +
                           "FROM collection " +
                           "WHERE owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(searchSql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("My Collections: ");
                printLines();
                if (rs.next()) {
                    System.out.print(rs.getString("collection_name"));
                }
                else {
                    System.out.println("NO COLLECTIONS FOUND");
                    return;
                }
                System.out.print(rs.getString("collection_name"));
                while(rs.next()) {
                    System.out.print(", " + rs.getString("collection_name"));
                }
                System.out.println("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        
        System.out.print("Enter Collection To Watch:");
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
                System.out.println(movieArray.size() + " MOVIES SET TO WATCHED IN COLLECTION " + collectionName);
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
        System.out.println("COLLECTION WATCHED SUCCESSFULLY");
    }

    // Function to follow a user
    public static void followUser() {
        System.out.print("Enter Username Of User To Follow: ");
        String userToFollow = scanner.nextLine();
        String sql = "INSERT INTO follows (follower, followee) " +
                     "VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, userToFollow);
            pstmt.executeUpdate();
            System.out.println("FOLLOWED USER SUCCESSFULLY");
        }   
        catch (SQLException e) {
            e.printStackTrace();
        } 
    }

    // Function to unfollow a user
    public static void unfollowUser() {
        System.out.print("Enter Username Of User To Unfollow: ");
        String userToUnfollow = scanner.nextLine();
        String sql = "DELETE FROM follows WHERE follower = ? AND followee = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, userToUnfollow);
            pstmt.executeUpdate();
            System.out.println("UNFOLLOWED USER SUCCESSFULLY");
        }   
        catch (SQLException e) {
            e.printStackTrace();
        } 
    }

    // Function to display the number of collection a user has
    public static void displayCollectionCount() {
        String sql = "SELECT COUNT(*) AS count FROM collection WHERE collection.owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                int count = rs.getInt("count");
                if (count == 1 ) {
                    System.out.println(username + " has " + count + " collection.");
                }
                else if (count == 0) {
                    System.out.println("No collections found.");
                }
                else {
                    System.out.println(username + " has " + count + " collections.");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayFollowerCount() {
        String sql = "SELECT COUNT(*) AS count FROM follows WHERE followee = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                int count = rs.getInt("count");
                if (count == 1) {
                    System.out.println(username + " has " + count + " follower.");
                }
                else if (count == 0) {
                    System.out.println("No followers found.");
                }
                else {
                    System.out.println(username + " has " + count + " followers.");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void displayFollowingCount() {
        String sql = "SELECT COUNT(*) AS count FROM follows WHERE follower = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                int count = rs.getInt("count");
                if (count == 1) {
                    System.out.println(username + " is following " + count + " user.");
                }
                else if (count == 0) {
                    System.out.println("No user followed.");
                }
                else {
                    System.out.println(username + " is following " + count + " users.");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayTop10() {
        String sql = "SELECT movie.title AS title, movie.length AS length, rates.rating AS rate FROM movie " +
                     "LEFT JOIN rates " +
                     "ON movie.movie_id = rates.movie_id " +
                     "WHERE rates.username = ? " +
                     "ORDER BY rating DESC " +
                     "LIMIT 10";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-43s %-15s %-15s %n", "title", "length", "rating");
                printLineTables();
                int count = 1;
                while (rs.next()) {
                    BigDecimal ratingBD = (BigDecimal) rs.getObject("rate");
                    Double rating = (ratingBD != null) ? ratingBD.doubleValue() : null;
                    if (count == 10) {
                        System.out.printf(count++ + ". %-39s %-15s %-15s %n", rs.getString("title"), rs.getTime("length"), (rating != null ? rating.toString() : "NULL"));
                    }
                    else {
                        System.out.printf(count++ + ". %-40s %-15s %-15s %n", rs.getString("title"), rs.getTime("length"), (rating != null ? rating.toString() : "NULL"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void top20In90Days() {
        String sql = "SELECT movie.title AS title, movie.length AS length, COUNT(watches.movie_id) AS watches, released_on.release_date AS release_date FROM movie " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "LEFT JOIN watches " +
                     "ON movie.movie_id = watches.movie_id " +
                     "WHERE released_on.release_date >= CURRENT_DATE - INTERVAL '90 DAY' " +
                     "GROUP BY movie.title, movie.length, watches.movie_id, released_on.release_date " +
                     "ORDER BY watches DESC " +
                     "LIMIT 20";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-43s %-15s %-15s %-15s%n", "title", "length", "watches", "release_date");
                printLineTables();
                int count = 1;
                while (rs.next()) {
                    if (count >= 10) {
                        System.out.printf(count++ + ". %-39s %-15s %-15s %-15s%n", rs.getString("title"), rs.getTime("length"), rs.getInt("watches"), rs.getDate("release_date"));
                    }
                    else {
                        System.out.printf(count++ + ". %-40s %-15s %-15s %-15s%n", rs.getString("title"), rs.getTime("length"), rs.getInt("watches"), rs.getDate("release_date"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void top20AmongFollowers() {
        String sql = "SELECT movie.title AS title, movie.length AS length, COUNT(watches.movie_id) AS watches, follows.follower AS follower FROM movie " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "LEFT JOIN watches " +
                     "ON movie.movie_id = watches.movie_id " +
                     "LEFT JOIN follows " +
                     "ON watches.username = follows.follower " +
                     "WHERE follows.followee = ?" +
                     "GROUP BY movie.title, movie.length, watches.movie_id, follows.follower " +
                     "ORDER BY watches DESC " +
                     "LIMIT 20";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-43s %-15s %-15s %-15s%n", "title", "length", "watches", "follower");
                printLineTables();
                int count = 1;
                while (rs.next()) {
                    if (count >= 10) {
                        System.out.printf(count++ + ". %-39s %-15s %-15s %-15s%n", rs.getString("title"), rs.getTime("length"), rs.getInt("watches"), rs.getString("follower"));
                    }
                    else {
                        System.out.printf(count++ + ". %-40s %-15s %-15s %-15s%n", rs.getString("title"), rs.getTime("length"), rs.getInt("watches"), rs.getString("follower"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void top5OfTheMonth() {
        String sql = "SELECT movie.title AS title, movie.length AS length, movie.online_rating AS rating, released_on.release_date AS release_date FROM movie " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "WHERE released_on.release_date >= DATE_TRUNC('month', CURRENT_DATE) " +
                     "ORDER BY movie.online_rating DESC " +
                     "LIMIT 5";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-40s %-15s %-15s %-15s%n", "title", "length", "rating", "release_date");
                printLineTables();
                int count = 1;
                while (rs.next()) {
                    System.out.printf(count++ + ". %-39s %-15s %-15s %-15s%n", rs.getString("title"), rs.getTime("length"), rs.getDouble("rating"), rs.getDate("release_date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Recommends movies that contians the genre of the user's most watched genre,
    //  the online_rating of the movie is greater than the user's average rating their watched genre,
    //  and contains a cast member that they watched the most
    //  IF there aren't enough tables to show 10 recommendations, then it shows more movies with the same 
    //  criteria as above, EXCLUDING the cast member requirement
    public static void displayRecommendationBasedOnPlayHistory() {
        String sql = "WITH most_watched_genre AS (" + 
                        "SELECT hg.genre_id AS genre " +
                        "FROM has_genre hg " +
                        "JOIN watches w ON hg.movie_id = w.movie_id " +
                        "WHERE w.username = ? " +
                        "GROUP BY hg.genre_id " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 1" +
                    "), " +
                    "avg_rating_in_genre AS (" +
                        "SELECT AVG(m.online_rating) AS avg_rating " +
                        "FROM movie m " +
                        "JOIN has_genre hg ON m.movie_id = hg.movie_id " +
                        "JOIN watches w ON m.movie_id = w.movie_id " +
                        "WHERE w.username = ? AND hg.genre_id = (SELECT genre_id FROM most_watched_genre)" +
                    "), " +
                    "most_watched_actor AS (" +
                        "SELECT c.member_id " +
                        "FROM casts c " +
                        "JOIN watches w ON c.movie_id = w.movie_id " +
                        "WHERE w.username = ? " +
                        "GROUP BY c.member_id " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 1" +
                    ")" +
                    "SELECT DISTINCT m.title AS title, m.length AS length, m.online_rating AS rating, r.release_date AS release_date " +
                    "FROM movie m " +
                    "JOIN released_on r ON m.movie_id = r.movie_id " +
                    "JOIN has_genre hg ON m.movie_id = hg.movie_id " +
                    "JOIN casts c ON m.movie_id = c.movie_id " +
                    "WHERE hg.genre_id = (SELECT genre FROM most_watched_genre) " +
                    "AND m.online_rating > (SELECT avg_rating FROM avg_rating_in_genre) " +
                    "AND c.member_id = (SELECT member_id FROM most_watched_actor) " +
                    "AND m.movie_id NOT IN (" +
                    "    SELECT movie_id FROM watches WHERE username = ? " +
                    ") " + 
                    "ORDER BY m.online_rating DESC, r.release_date DESC " +
                    "LIMIT 10";

        String sql2 = "WITH most_watched_genre AS (" + 
                        "SELECT hg.genre_id AS genre " +
                        "FROM has_genre hg " +
                        "JOIN watches w ON hg.movie_id = w.movie_id " +
                        "WHERE w.username = ? " +
                        "GROUP BY hg.genre_id " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 1" +
                    "), " +
                    "avg_rating_in_genre AS (" +
                        "SELECT AVG(m.online_rating) AS avg_rating " +
                        "FROM movie m " +
                        "JOIN has_genre hg ON m.movie_id = hg.movie_id " +
                        "JOIN watches w ON m.movie_id = w.movie_id " +
                        "WHERE w.username = ? AND hg.genre_id = (SELECT genre_id FROM most_watched_genre)" +
                    ") " +
                    "SELECT DISTINCT m.title AS title, m.length AS length, m.online_rating AS rating, r.release_date AS release_date " +
                    "FROM movie m " +
                    "JOIN released_on r ON m.movie_id = r.movie_id " +
                    "JOIN has_genre hg ON m.movie_id = hg.movie_id " +
                    "WHERE hg.genre_id = (SELECT genre FROM most_watched_genre) " +
                    "AND m.online_rating > (SELECT avg_rating FROM avg_rating_in_genre) " +
                    "AND m.movie_id NOT IN (" +
                    "    SELECT movie_id FROM watches WHERE username = ? " +
                    ") " + 
                    "ORDER BY m.online_rating DESC, r.release_date DESC " +
                    "LIMIT ?";

        int limitCount = 0;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the username in all required places
            pstmt.setString(1, username); // for most_watched_genre
            pstmt.setString(2, username); // for avg_rating_in_genre
            pstmt.setString(3, username); // for most_watched_actor
            pstmt.setString(4, username); // for NOT IN watched
    
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-40s %-15s %-15s %-15s%n", "title", "length", "rating", "release_date");
                printLineTables();
                while (rs.next()) {
                    limitCount++;
                    System.out.printf("%-39s %-15s %-15s %-15s%n",
                        rs.getString("title"),
                        rs.getTime("length"), 
                        rs.getDouble("rating"),
                        rs.getDate("release_date")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        
        try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
            // Set the username in all required places
            pstmt.setString(1, username); // for most_watched_genre
            pstmt.setString(2, username); // for avg_rating_in_genre
            pstmt.setString(3, username); // for NOT IN watched
            pstmt.setInt(4, 10 - limitCount); // for LIMIT
    
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("%-39s %-15s %-15s %-15s%n",
                        rs.getString("title"),
                        rs.getTime("length"), 
                        rs.getDouble("rating"),
                        rs.getDate("release_date")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Recommends movies based on users with similar interestes as the user, where the movies don't have to be
    // of the same genre as the user's top genre
    public static void displayRecommendationBasedOnOthers() {
        String sql = "WITH user_top_genre AS (" +
                        "SELECT hg.genre_id AS genre " +
                        "FROM has_genre hg " +
                        "JOIN watches w ON hg.movie_id = w.movie_id " +
                        "WHERE w.username = ? " +
                        "GROUP BY hg.genre_id " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 1" +
                    "), " +
                    "all_users_top_genre AS (" +
                        "SELECT w.username, hg.genre_id, COUNT(*) AS watch_count, " +
                        "RANK() OVER (PARTITION BY w.username ORDER BY COUNT(*) DESC) AS genre_rank " +
                        "FROM watches w " +
                        "JOIN has_genre hg ON w.movie_id = hg.movie_id " +
                        "GROUP BY w.username, hg.genre_id" +
                    "), " +
                    "similar_users AS (" +
                        "SELECT username " +
                        "FROM all_users_top_genre " +
                        "WHERE genre_rank = 1 " +
                        "AND genre_id = (SELECT genre FROM user_top_genre) " +
                        "AND NOT username = ?" +
                    "), " +
                    "recommended_movies AS (" +
                        "SELECT DISTINCT m.movie_id, m.title AS title, m.length AS length, m.online_rating AS rating, r.release_date AS release_date " +
                        "FROM watches w " +
                        "JOIN movie m ON w.movie_id = m.movie_id " +
                        "JOIN released_on r ON m.movie_id = r.movie_id " +
                        "WHERE w.username IN (SELECT username FROM similar_users) " +
                        "AND m.movie_id NOT IN (" +
                            "SELECT movie_id FROM watches WHERE username = ?" +
                        ")" +
                    ") " +
                    "SELECT * FROM recommended_movies " +
                    "ORDER BY rating DESC " +
                    "LIMIT 10";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);  // for user_top_genre
            pstmt.setString(2, username);  // for filtering similar_users (exclude self)
            pstmt.setString(3, username);  // for excluding already watched movies

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-40s %-15s %-15s %-15s%n", "title", "length", "rating", "release_date");
                printLineTables();
                while (rs.next()) {
                    System.out.printf("%-39s %-15s %-15.1f %-15s%n",
                        rs.getString("title"),
                        rs.getTime("length"),
                        rs.getDouble("rating"),
                        rs.getDate("release_date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to display the initial commands upon running
    public static void displayInitialCommands() {
        printLines();
        System.out.println("Choose An Option:");
        System.out.println("1. Create Account");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        printLines();
    }

    // Function to display account commands
    public static void displayAccountCommands() {
        spaces();
        printLines();
        System.out.println("1.  Create Collection");
        System.out.println("2.  See Collection");
        System.out.println("3.  Search Movie by Name");
        System.out.println("4.  Search Movie by Release Date");
        System.out.println("5.  Search Movie by Cast");
        System.out.println("6.  Search Movie by Studio");
        System.out.println("7.  Search Movie by Genre");
        System.out.println("8.  Add Movie to Collection");
        System.out.println("9.  Remove Movie from Collection");
        System.out.println("10. Modify Collection Name");
        System.out.println("11. Delete Collection");
        System.out.println("12. Rate Movie");
        System.out.println("13. Watch Movie");
        System.out.println("14. Watch Collection");
        System.out.println("15. Follow User");
        System.out.println("16. Unfollow User");
        System.out.println("17. Display Collection Count");
        System.out.println("18. Display Follower Count");
        System.out.println("19. Display Following Count");
        System.out.println("20. Display Top 10 Movies");
        System.out.println("21. Display Top 20 Movies In The Last 90 Days");
        System.out.println("22. Display Top 20 Movies Among Followers");
        System.out.println("23. Display Top 5 Movies Of The Month");
        System.out.println("24. Display Movie Recommendations");
        System.out.println("25. Hypothesis 1: Enjoy Most");
        System.out.println("26. Hypothesis 2: 1990s vs 2020s");
        System.out.println("27. Exit");
        printLines();
        System.out.print("Enter Choice: ");
    }

    // Function to display the add movie commands
    public static void displayAddCommands() {
        spaces();
        printLines();
        System.out.println("1. Add to My Collection");
        System.out.println("2. Add to Different Collection");
        printLines();
        System.out.print("Enter Choice: ");
    }

    // Function to display the search sort options
    public static void displaySearchSortingOptions() {
        System.out.println("1. Movie Name");
        System.out.println("2. Movie Studio");
        System.out.println("3. Movie Genre");
        System.out.println("4. Movie Release Year");
        System.out.println("5. Exit");
    }

    public static void displayAscDescOptions() {
        System.out.println("1. Ascending");
        System.out.println("2. Descending");
    }

    public static void displayRecommendationOptions() {
        System.out.println("1. Recommendations Based on Your Play History");
        System.out.println("2. Recommendations Based on the Play History of Similar Users");
    }

    // Function to display lines for formatting
    public static void printLines() {
        System.out.println("-----------------------------------------");
    }

    // Function to display lines for the formatting of tables
    public static void printLineTables() {
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    }

    // Function to display the enter to continue feature
    public static void enterToContinue() {
        System.out.print("\nPress Enter To Continue... ");
        scanner.nextLine();
    }

    // Function to display spaces for formatting
    public static void spaces() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    public static void hypothesis1() {
        String sql = "SELECT genre.genre_name AS genre, AVG(movie.online_rating) as avg_rating_genre FROM movie " +
                     "LEFT JOIN has_genre " + 
                     "ON movie.movie_id = has_genre.movie_id " +
                     "LEFT JOIN genre " +
                     "ON has_genre.genre_id = genre.genre_id " +
                     "GROUP BY genre.genre_name " + 
                     "ORDER BY avg_rating_genre DESC ";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean getFirst = false;
                String enjoyGenre = "";
                System.out.printf("%-40s %-15s %n", "genre", "avg_rating_genre");
                printLineTables();
                while (rs.next()) {
                    System.out.printf("%-40s %-15s%n", rs.getString("genre"), rs.getDouble("avg_rating_genre"));
                    if (!getFirst) {
                        getFirst = true;
                        enjoyGenre = rs.getString("genre");
                    }
                }
                printLineTables();
                System.out.println("ON AVERAGE, USERS ENJOY " + enjoyGenre + " THE MOST");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void hypothesis2() {
        double avgRating1990s = 0.0;
        double avgRating2020s = 0.0;
        double avgRating1990sGenre = 0.0;
        double avgRating2020sGenre = 0.0;
        String sql = "SELECT AVG(movie.online_rating) as avg_rating FROM movie " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "WHERE released_on.release_date >= '1990-01-01' AND released_on.release_date < '2000-01-01' ";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                avgRating1990s = rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "SELECT AVG(movie.online_rating) as avg_rating FROM movie " +
                     "LEFT JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "WHERE released_on.release_date >= '2020-01-01' AND released_on.release_date < '2026-01-01' ";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                avgRating2020s = rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        sql = "SELECT AVG(movie.online_rating) as avg_rating FROM movie " +
              "LEFT JOIN released_on " +
              "ON movie.movie_id = released_on.movie_id " +
              "LEFT JOIN has_genre " + 
              "ON movie.movie_id = has_genre.movie_id " +
              "LEFT JOIN genre " +
              "ON has_genre.genre_id = genre.genre_id " +
              "WHERE (released_on.release_date >= '1990-01-01' AND released_on.release_date < '2000-01-01') " +
              "AND (genre.genre_name = 'Action' OR genre.genre_name = 'Sci-Fi')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                avgRating1990sGenre = rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "SELECT AVG(movie.online_rating) as avg_rating FROM movie " +
              "LEFT JOIN released_on " +
              "ON movie.movie_id = released_on.movie_id " +
              "LEFT JOIN has_genre " + 
              "ON movie.movie_id = has_genre.movie_id " +
              "LEFT JOIN genre " +
              "ON has_genre.genre_id = genre.genre_id " +
              "WHERE (released_on.release_date >= '2020-01-01' AND released_on.release_date < '2026-01-01') " + 
              "AND (genre.genre_name = 'Action' OR genre.genre_name = 'Sci-Fi')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                avgRating2020sGenre = rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        System.out.println("1990s Avg Rating: " + avgRating1990s);
        System.out.println("2020s Avg Rating: " + avgRating2020s);
        System.out.println("1990s Avg Rating (Action/Sci-Fi): " + avgRating1990sGenre);
        System.out.println("2020s Avg Rating (Action/Sci-Fi): " + avgRating2020sGenre);

        System.out.println();
        if (avgRating1990s > avgRating2020s) {
            System.out.println("1990s movies are rated higher than 2020s movies.");
        } else if (avgRating1990s < avgRating2020s) {
            System.out.println("2020s movies are rated higher than 1990s movies.");
        } else {
            System.out.println("Both decades have the same average rating.");
        }
        if (Math.abs(avgRating1990s - avgRating2020s) > 0.5) {
            System.out.println("The difference in average ratings is significant.");
        } else {
            System.out.println("The difference in average ratings is not significant.");
        }

        System.out.println();
        if (avgRating1990sGenre > avgRating2020sGenre) {
            System.out.println("1990s Action/Sci-Fi movies are rated higher than 2020s Action/Sci-Fi movies.");
        } else if (avgRating1990sGenre < avgRating2020sGenre) {
            System.out.println("2020s Action/Sci-Fi movies are rated higher than 1990s Action/Sci-Fi movies.");
        } else {
            System.out.println("Both decades have the same average rating for Action/Sci-Fi movies.");
        }
        if (Math.abs(avgRating1990s - avgRating2020s) > 0.5) {
            System.out.println("The difference in average ratings is significant.");
        } else {
            System.out.println("The difference in average ratings is not significant.");
        }
    }
}