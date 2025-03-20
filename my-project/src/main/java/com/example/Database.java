package com.example;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.Properties;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;

public class Database {
    private static String username;
    private static Scanner scanner = new Scanner(System.in);
    private static Connection conn;

    public static void main(String[] args) throws SQLException {

        int lport = 15432;
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
                displayInitialCommands();
                choice = scanner.nextInt();

                // Check if the user entered a valid choice
                while (choice != 1 && choice != 2 && choice != 3) {
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                }

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
                    scanner.nextLine(); // Consume newline
    
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
                            addMovies();
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
                            System.out.println("Invalid choice. Please enter a number between 1 and 17.");
                    }
                }
            }
            
            scanner.nextLine(); // Consume newline


            // while (choice == 1) {
            //     createUser();
            //     System.out.println("Choose an option:");
            //     System.out.println("1. Create an account");
            //     System.out.println("2. Log into an existing account");
            //     choice = scanner.nextInt();

                // if (choice == 1) {
                //     createUser();
                // } else if (choice == 2) {
                //     accessAccount();
                // } else {
                //     System.out.println("Invalid choice. Exiting.");
                // }
            // }



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
            System.out.println("Enter username:");
            String inputtedUsername = scanner.nextLine();
            System.out.println("Enter password:");
            String password = scanner.nextLine();
            System.out.println("Enter first name:");
            String firstName = scanner.nextLine();
            System.out.println("Enter last name:");
            String lastName = scanner.nextLine();
            System.out.println("Enter gender (Male/Female):");
            String gender = scanner.nextLine();
            System.out.println("Enter date of birth (YYYY-MM-DD):");
            String dobString = scanner.nextLine();
            Date dob = Date.valueOf(dobString);
            System.out.println("Enter biography:");
            String biography = scanner.nextLine();
            Date currentDate = new Date(System.currentTimeMillis());
            
           
            String sql = "INSERT INTO users (username, password, first_name, last_name, gender, dob, biography, last_access_date, creation_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
                System.out.println("Account created successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Function to log into an existing user account
    public static void accessAccount() {
        try {
            System.out.println("Enter username:");
            String usernameInput = scanner.nextLine();
            System.out.println("Enter password:");
            String password = scanner.nextLine();

            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, usernameInput);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        username = usernameInput;
                        System.out.println("Login successful. Welcome, " + username + "!");
                    } else {
                        System.out.println("Invalid username or password.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createCollection() {
        // ADD CHECK TO NOT CREATE A COLLECTION WITH A NAME THAT ALREADY EXISTS


        System.out.println("Enter the name of the collection:");
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
            System.out.println("Collection created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void seeCollection() {
        String sql = "SELECT collection_name, quantity FROM collection WHERE owner_username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-30s %-10s%n", "Collection Name", "Quantity");
                System.out.println("-------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-30s %-10d%n", rs.getString("collection_name"), rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void searchMovieByName() {
        System.out.println("Enter the name of the movie:");
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
                     "INNER JOIN rates " +
                     "ON movie.movie_id = rates.movie_id " +
                     "WHERE rates.username = ? AND ra.title = ?";
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
        System.out.println("Enter the name of the release date:");
        String release_date = scanner.nextLine();
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
                     "INNER JOIN rates " +
                     "ON movie.movie_id = rates.movie_id " +
                     "INNER JOIN released_on " +
                     "ON movie.movie_id = released_on.movie_id " +
                     "WHERE rates.username = ? AND released_date = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, release_date);
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

    public static void searchMovieByCast() {
        System.out.println("Enter the name of the cast member:");
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
                     "INNER JOIN casts " +
                     "ON movie.movie_id = casts.movie_id " +
                     "INNER JOIN cast_member " + 
                     "ON casts.genre_id = cast_member.genre_id " +
                     "WHERE rates.username = ? AND cast_member.first_name = ? AND cast_member.last_name = ?";
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

    public static void searchMovieByStudio() {
        System.out.println("Enter the name of the studio:");
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
                     "INNER JOIN rates " +
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
        System.out.println("Enter the genre of the movie:");
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
        String searchSql = "SELECT collection.name FROM collection " +
                           "WHERE owner_username = " + username;
        // try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        //     pstmt.setString(1, username);
        //     pstmt.setString(2, genre);
        //     try (ResultSet rs = pstmt.executeQuery()) {
        //         System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
        //         System.out.println("----------------------------------------------------------------------------------------------------------");
        //         while (rs.next()) {
        //             System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), rs.getDouble("rate"));
        //         }
        //     }
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }
        System.out.println("Enter the name of the collection you want to add the movie to:");
        String collection = scanner.nextLine();
        System.out.println("Enter the name of the movie you want to add:");
        String movie = scanner.nextLine();
        String sql = "INSERT INTO collection (owner_usernamee, collection_name, quantity) " +
                     ;

        // try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        //     pstmt.setString(1, collection);
        //     pstmt.setString(2, movie);
        //     try (ResultSet rs = pstmt.executeQuery()) {
        //         System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", "title", "cast_first", "cast_last", "director_first", "director_last", "length", "mpaa_rating", "rating");
        //         System.out.println("----------------------------------------------------------------------------------------------------------");
        //         while (rs.next()) {
        //             System.out.printf("%-25s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n", rs.getString("title"), rs.getString("cast_first"), rs.getString("cast_last"), rs.getString("director_first"), rs.getString("director_last"), rs.getTime("length"), rs.getString("mpaa_rating"), rs.getDouble("rate"));
        //         }
        //     }
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }
    }

    public static void removeMovies() {

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
        String movieName = "";
        int movieID = -1;
        String sql = "SELECT movie_id FROM movie WHERE title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movieName);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                movieID = rs.getInt("movie_id");
            }
            sql = "INSERT ";
        }   
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void watchCollection() {

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
        String sql = "DELETE FROM * WHERE follower = ? AND followee = ?";
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
        System.out.println("2. See collection");
        System.out.println("3. Search for a movie by name");
        System.out.println("4. Search for a movie by release date");
        System.out.println("5. Search for a movie by cast");
        System.out.println("6. Search for a movie by studio");
        System.out.println("7. Search for a movie by genre");
        System.out.println("8. Add movies to a collection");
        System.out.println("9. Remove movies from a collection");
        System.out.println("10. Modify collection name");
        System.out.println("11. Delete collection");
        System.out.println("12. Rate a movie");
        System.out.println("13. Watch a movie");
        System.out.println("14. Watch a collection");
        System.out.println("15. Follow a user");
        System.out.println("16. Unfollow a user");
        System.out.println("17. Exit");
    }
}