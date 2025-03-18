import java.sql.*;

public class Database {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://127.0.0.1:5432/p32001_22";
        String username = "";
        String password = "";

        Connection con = DriverManager.getConnection(url, username, password);
        Statement st = con.createStatement();
        String sql = "select * from cast_member";
        ResultSet rs = st.executeQuery(sql);
        st.executeQuery(sql);
        rs.next();        
        String result = rs.getString(sql);
        System.out.println(result);
    }
}