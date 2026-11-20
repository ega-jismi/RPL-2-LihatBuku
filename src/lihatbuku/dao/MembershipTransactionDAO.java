package lihatbuku.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lihatbuku.koneksi.HibernateUtil;
import lihatbuku.model.MembershipTransaction;
import lihatbuku.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Data Access Object untuk entitas transaksi membership menggunakan Hibernate ORM.
 */
public class MembershipTransactionDAO {

    public boolean saveTransaction(MembershipTransaction tx) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        Transaction transaction = null;
        try (Session session = sf.openSession()) {
            transaction = session.beginTransaction();
            session.persist(tx);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("MembershipTransactionDAO.saveTransaction error: " + e.getMessage());
            return false;
        }
    }

    public MembershipTransaction getActiveMembership(int userId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return null;
        try (Session session = sf.openSession()) {
            LocalDate today = LocalDate.now();
            List<MembershipTransaction> list = session.createQuery(
                    "FROM MembershipTransaction WHERE userId = :uid AND status = 'SUCCESS' AND endDate >= :today ORDER BY endDate DESC",
                    MembershipTransaction.class)
                    .setParameter("uid", userId)
                    .setParameter("today", today)
                    .setMaxResults(1)
                    .list();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("MembershipTransactionDAO.getActiveMembership error: " + e.getMessage());
            return null;
        }
    }

    public List<MembershipTransaction> getTransactionsByUserId(int userId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return new ArrayList<>();
        try (Session session = sf.openSession()) {
            return session.createQuery(
                    "FROM MembershipTransaction WHERE userId = :uid ORDER BY id DESC",
                    MembershipTransaction.class)
                    .setParameter("uid", userId)
                    .list();
        } catch (Exception e) {
            System.err.println("MembershipTransactionDAO.getTransactionsByUserId error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<MembershipTransaction> getAllTransactions() {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return new ArrayList<>();
        try (Session session = sf.openSession()) {
            List<MembershipTransaction> list = session.createQuery(
                    "FROM MembershipTransaction ORDER BY id DESC",
                    MembershipTransaction.class)
                    .list();
            for (MembershipTransaction tx : list) {
                User u = session.find(User.class, tx.getUserId());
                if (u != null) {
                    tx.setUserName((u.getFullName() != null && !u.getFullName().isEmpty()) ? u.getFullName() : u.getUsername());
                } else {
                    tx.setUserName("Pengguna #" + tx.getUserId());
                }
            }
            return list;
        } catch (Exception e) {
            System.err.println("MembershipTransactionDAO.getAllTransactions error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
