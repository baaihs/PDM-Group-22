import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class Database {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://127.0.0.1:5432/p32001_22";
        String username = "";
        String password = "";

        try (BufferedReader br = new BufferedReader(new FileReader("credentials.txt"))) {
            username = br.readLine();
            password = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try {
                Connection con = DriverManager.getConnection(url, username, password);
                Statement st = con.createStatement();
                String sql = "select * from cast_member";
                ResultSet rs = st.executeQuery(sql);
                st.executeQuery(sql);
                rs.next();        
                String result = rs.getString(sql);
                System.out.println(result);
            }
            catch (SQLException ex) {
                System.out.println(ex);
            }
        }
        catch (ClassNotFoundException e) {
            System.out.println(e);
        }
    }
}