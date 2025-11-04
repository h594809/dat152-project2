// BookRepository.java
package no.hvl.dat152.rest.ws.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import no.hvl.dat152.rest.ws.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findAll(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn")
    Book findBookByISBN(@Param("isbn") String isbn);

    @Query("SELECT b FROM Book b join b.authors a WHERE a.authorId = :authorId")
    List<Book> findBooksByAuthorId(@Param("authorId") int authorId);

    List<Book> findByTitleContaining(String term);
}
