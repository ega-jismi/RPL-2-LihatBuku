package lihatbuku.dao;

import lihatbuku.koneksi.HibernateUtil;
import lihatbuku.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Data Access Object untuk entitas pengguna menggunakan Hibernate ORM.
 */
public class UserDAO {

    public User login(String username, String password) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return null;
        try (Session session = sf.openSession()) {
            return session.createQuery("FROM User WHERE username = :u AND password = :p", User.class)
                    .setParameter("u", username)
                    .setParameter("p", password)
                    .uniqueResult();
        } catch (Exception e) {
            System.err.println("UserDAO.login error: " + e.getMessage());
            return null;
        }
    }

    public boolean register(User user) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("REGISTERED");
            }
            session.persist(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("UserDAO.register error: " + e.getMessage());
            return false;
        }
    }

    public boolean isUsernameTaken(String username) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        try (Session session = sf.openSession()) {
            Long count = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :u", Long.class)
                    .setParameter("u", username)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("UserDAO.isUsernameTaken error: " + e.getMessage());
            return false;
        }
    }
}
