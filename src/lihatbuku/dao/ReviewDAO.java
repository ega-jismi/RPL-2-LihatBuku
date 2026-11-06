package lihatbuku.dao;

import java.util.ArrayList;
import java.util.List;
import lihatbuku.koneksi.HibernateUtil;
import lihatbuku.model.Review;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Data Access Object untuk ulasan dan penilaian menggunakan Hibernate ORM.
 */
public class ReviewDAO {

    public List<Review> getReviewsByBookId(int bookId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return new ArrayList<>();
        try (Session session = sf.openSession()) {
            List<Object[]> rows = session.createQuery(
                    "SELECT r, u.fullName FROM Review r, User u WHERE r.userId = u.id AND r.bookId = :b ORDER BY r.id DESC",
                    Object[].class)
                    .setParameter("b", bookId)
                    .list();
            List<Review> list = new ArrayList<>();
            for (Object[] row : rows) {
                Review r = (Review) row[0];
                r.setUserName((String) row[1]);
                list.add(r);
            }
            return list;
        } catch (Exception e) {
            System.err.println("ReviewDAO.getReviewsByBookId error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean addReview(Review review) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            session.persist(review);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("ReviewDAO.addReview error: " + e.getMessage());
            return false;
        }
    }

    public double calculateAverageRating(int bookId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return 0.0;
        try (Session session = sf.openSession()) {
            Double avg = session.createQuery(
                    "SELECT coalesce(avg(cast(r.rating as double)), 0.0) FROM Review r WHERE r.bookId = :b",
                    Double.class)
                    .setParameter("b", bookId)
                    .uniqueResult();
            return (avg != null) ? Math.round(avg * 10.0) / 10.0 : 0.0;
        } catch (Exception e) {
            System.err.println("ReviewDAO.calculateAverageRating error: " + e.getMessage());
            return 0.0;
        }
    }
}
