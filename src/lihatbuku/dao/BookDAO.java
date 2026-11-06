package lihatbuku.dao;

import java.util.ArrayList;
import java.util.List;
import lihatbuku.koneksi.HibernateUtil;
import lihatbuku.model.Book;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Data Access Object untuk entitas buku menggunakan Hibernate ORM.
 */
public class BookDAO {

    public List<Book> getAllBooks() {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return new ArrayList<>();
        try (Session session = sf.openSession()) {
            return session.createQuery("FROM Book ORDER BY id DESC", Book.class).list();
        } catch (Exception e) {
            System.err.println("BookDAO.getAllBooks error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Book> searchBooks(String query, String genre) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return new ArrayList<>();
        String q = (query == null) ? "" : query.trim().toLowerCase();
        String g = (genre == null) ? "Semua Kategori" : genre.trim();

        try (Session session = sf.openSession()) {
            StringBuilder hql = new StringBuilder("FROM Book WHERE 1=1 ");
            if (!q.isEmpty()) {
                hql.append("AND (LOWER(title) LIKE :q OR LOWER(author) LIKE :q) ");
            }
            if (!g.isEmpty() && !"Semua Kategori".equalsIgnoreCase(g)) {
                hql.append("AND genre = :genre ");
            }
            hql.append("ORDER BY id DESC");

            var queryObj = session.createQuery(hql.toString(), Book.class);
            if (!q.isEmpty()) {
                queryObj.setParameter("q", "%" + q + "%");
            }
            if (!g.isEmpty() && !"Semua Kategori".equalsIgnoreCase(g)) {
                queryObj.setParameter("genre", g);
            }
            return queryObj.list();
        } catch (Exception e) {
            System.err.println("BookDAO.searchBooks error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Book getBookById(int id) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return null;
        try (Session session = sf.openSession()) {
            return session.find(Book.class, id);
        } catch (Exception e) {
            System.err.println("BookDAO.getBookById error: " + e.getMessage());
            return null;
        }
    }

    public void updateRating(int bookId, double newRating, int newReviewCount) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return;
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            Book b = session.find(Book.class, bookId);
            if (b != null) {
                b.setRating(newRating);
                b.setReviewCount(newReviewCount);
                session.merge(b);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("BookDAO.updateRating error: " + e.getMessage());
        }
    }

    public boolean addBook(Book book) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            session.persist(book);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("BookDAO.addBook error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBook(Book book) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            session.merge(book);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("BookDAO.updateBook error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBook(int bookId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM ReadingHistory WHERE bookId = :id")
                    .setParameter("id", bookId)
                    .executeUpdate();
            session.createMutationQuery("DELETE FROM Review WHERE bookId = :id")
                    .setParameter("id", bookId)
                    .executeUpdate();
            Book b = session.find(Book.class, bookId);
            if (b != null) {
                session.remove(b);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("BookDAO.deleteBook error: " + e.getMessage());
            return false;
        }
    }
}
