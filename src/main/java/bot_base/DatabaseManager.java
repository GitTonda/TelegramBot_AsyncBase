package bot_base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <p>
 * Configuration keys (config.properties):<br>
 * - DB_URL:      jdbc:postgresql://host:port/database<br>
 * - DB_USER:     username<br>
 * - DB_PASSWORD: password<br>
 * <p>
 * It lazily initializes a single Connection and ensures a simple key-value table exists.
 */
public final class DatabaseManager
{
    private static volatile DatabaseManager INSTANCE;

    private final String url;
    private final String user;
    private final String password;

    private Connection conn;

    private DatabaseManager(String url, String user, String password)
    {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DatabaseManager get_instance()
    {
        if (INSTANCE == null)
        {
            synchronized (DatabaseManager.class)
            {
                if (INSTANCE == null)
                {
                    String url = ConfigLoader.get("DB_URL");
                    String user = ConfigLoader.get("DB_USER");
                    String password = ConfigLoader.get("DB_PASSWORD");
                    INSTANCE = new DatabaseManager(url, user, password);
                }
            }
        }
        return INSTANCE;
    }

    public synchronized void init() throws SQLException
    {
        if (conn != null && !conn.isClosed()) return;
        conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(true);
        ensure_kv_table();
    }

    private void ensure_kv_table() throws SQLException
    {
        // TODO add your schema here
        String ddl = """
                CREATE TABLE IF NOT EXISTS example ();
                """;
        try (Statement st = conn.createStatement())
        {
            st.executeUpdate(ddl);
        }
    }

    // TODO add your read/write methods here

    private synchronized void ensure_connected() throws SQLException
    {
        if (conn == null || conn.isClosed()) init();
    }

    public synchronized void close_quietly()
    {
        if (conn != null)
        {
            try
            {
                conn.close();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
