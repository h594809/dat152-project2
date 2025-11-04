// BookService.java
package no.hvl.dat152.rest.ws.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import no.hvl.dat152.rest.ws.exceptions.BookNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UpdateBookFailedException;
import no.hvl.dat152.rest.ws.model.Author;
import no.hvl.dat152.rest.ws.model.Book;
import no.hvl.dat152.rest.ws.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Book findByISBN(String isbn) throws BookNotFoundException {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with isbn = " + isbn + " not found!"));
    }

    public Book updateBook(Book book, String isbn) throws BookNotFoundException, UpdateBookFailedException {
        Book existing = bookRepository.findBookByISBN(isbn);
        if (existing == null) throw new BookNotFoundException("Book with isbn = " + isbn + " not found!");
        book.setId(existing.getId());
        book.setIsbn(isbn);
        try {
            return bookRepository.save(book);
        } catch (Exception e) {
            throw new UpdateBookFailedException("Failed to update book with ISBN: " + isbn);
        }
    }

    public List<Book> findAllPaginate(Pageable page) {
        return bookRepository.findAll(page).getContent();
    }

    public Set<Author> findAuthorsOfBookByISBN(String isbn) throws BookNotFoundException {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with isbn = " + isbn + " not found!"));
        return book.getAuthors();
    }

    public void deleteById(long id) throws BookNotFoundException {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book with id = " + id + " not found!");
        }
        bookRepository.deleteById(id);
    }

    public void deleteByISBN(String isbn) throws BookNotFoundException {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with isbn = " + isbn + " not found!"));
        bookRepository.delete(book);
    }
}
