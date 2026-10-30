package lihatbuku.koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Konfigurasi koneksi JDBC ke basis data MySQL.
 */
public class Koneksi {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "db_lihatbuku";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";

    private static Connection connection = null;

    /**
     * Memperoleh koneksi JDBC ke MySQL.
     * Mengembalikan null jika server database atau driver belum aktif.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
            }
        } catch (ClassNotFoundException | SQLException e) {
            // Driver belum terpasang atau server MySQL offline
            return null;
        }
        return connection;
    }

    public static boolean isConnected() {
        Connection conn = getConnection();
        return conn != null;
    }
}
