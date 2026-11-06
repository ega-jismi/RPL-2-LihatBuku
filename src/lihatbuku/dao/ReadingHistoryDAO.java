package lihatbuku.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lihatbuku.koneksi.HibernateUtil;
import lihatbuku.model.ReadingHistory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Data Access Object untuk penanda riwayat bacaan menggunakan Hibernate ORM.
 */
public class ReadingHistoryDAO {

    public List<ReadingHistory> getHistoryByUserId(int userId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return new ArrayList<>();
        try (Session session = sf.openSession()) {
            List<Object[]> rows = session.createQuery(
                    "SELECT h, b.title, b.genre, b.totalPages FROM ReadingHistory h, Book b "
                    + "WHERE h.bookId = b.id AND h.userId = :u ORDER BY h.lastReadAt DESC",
                    Object[].class)
                    .setParameter("u", userId)
                    .list();
            List<ReadingHistory> list = new ArrayList<>();
            for (Object[] row : rows) {
                ReadingHistory h = (ReadingHistory) row[0];
                h.setBookTitle((String) row[1]);
                h.setBookGenre((String) row[2]);
                h.setTotalPages((Integer) row[3]);
                list.add(h);
            }
            return list;
        } catch (Exception e) {
            System.err.println("ReadingHistoryDAO.getHistoryByUserId error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean saveOrUpdateHistory(int userId, int bookId, String bookTitle, String bookGenre, int lastPage, int totalPages) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            ReadingHistory hist = session.createQuery(
                    "FROM ReadingHistory WHERE userId = :u AND bookId = :b", ReadingHistory.class)
                    .setParameter("u", userId)
                    .setParameter("b", bookId)
                    .uniqueResult();

            if (hist != null) {
                hist.setLastPage(lastPage);
                hist.setLastReadAtDateTime(LocalDateTime.now());
                session.merge(hist);
            } else {
                ReadingHistory newHist = new ReadingHistory();
                newHist.setUserId(userId);
                newHist.setBookId(bookId);
                newHist.setLastPage(lastPage);
                newHist.setLastReadAtDateTime(LocalDateTime.now());
                session.persist(newHist);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("ReadingHistoryDAO.saveOrUpdateHistory error: " + e.getMessage());
            return false;
        }
    }

    public int getLastReadPage(int userId, int bookId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return 1;
        try (Session session = sf.openSession()) {
            Integer page = session.createQuery(
                    "SELECT h.lastPage FROM ReadingHistory h WHERE h.userId = :u AND h.bookId = :b", Integer.class)
                    .setParameter("u", userId)
                    .setParameter("b", bookId)
                    .uniqueResult();
            return (page != null && page > 0) ? page : 1;
        } catch (Exception e) {
            System.err.println("ReadingHistoryDAO.getLastReadPage error: " + e.getMessage());
            return 1;
        }
    }
}
