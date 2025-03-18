import java.sql.*;

public class Database {
    public static void main(String[] args) {
        String url = "jdbc:postgressql://localhost:5432/p32001_22";
        String username = "";
        String password = "";

        Connection con = DriverManager.getConnection(url, username, password);
        Statement st = con.createStatement();
        String sql = "";
        st.executeQuery(sql);
    }
}