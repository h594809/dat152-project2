package no.hvl.dat152.rest.ws.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import no.hvl.dat152.rest.ws.model.Book;

public interface BookRepository extends CrudRepository<Book, Long>, PagingAndSortingRepository<Book, Long> {

    // Finn bok via Optional for service som kaster BookNotFoundException
    Optional<Book> findByIsbn(String isbn);

    // Brukt i update (retur direkte)
    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn")
    Book findBookByISBN(@Param("isbn") String isbn);

    // ✔️ Viktig: bruk Integer (ikke Long) siden Author.authorId er int
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.authorId = :authorId")
    List<Book> findBooksByAuthorId(@Param("authorId") Integer authorId);

    Page<Book> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM Book limit :limit offset :offset", nativeQuery = true)
    List<Book> findAllPaginate(@Param("limit") int limit, @Param("offset") int offset);
}
