package lihatbuku.koneksi;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Pengelola siklus hidup Hibernate SessionFactory.
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    private HibernateUtil() {}

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            try {
                Configuration configuration = new Configuration();
                configuration.configure("hibernate.cfg.xml");
                sessionFactory = configuration.buildSessionFactory();
            } catch (Throwable ex) {
                System.err.println("Hibernate SessionFactory initialization failed: " + ex.getMessage());
                sessionFactory = null;
            }
        }
        return sessionFactory;
    }

    public static boolean isConnected() {
        SessionFactory sf = getSessionFactory();
        return sf != null && !sf.isClosed();
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
