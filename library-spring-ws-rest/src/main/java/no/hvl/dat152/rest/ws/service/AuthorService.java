/**
 * 
 */
package no.hvl.dat152.rest.ws.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.hvl.dat152.rest.ws.exceptions.AuthorNotFoundException;
import no.hvl.dat152.rest.ws.model.Author;
import no.hvl.dat152.rest.ws.model.Book;
import no.hvl.dat152.rest.ws.repository.AuthorRepository;

/**
 * @author tdoy
 */
@Service
public class AuthorService {

	@Autowired
	private AuthorRepository authorRepository;
		
	/**
	 * Finds an author by ID
	 * @param id the ID of the author to find
	 * @return the author if found
	 * @throws AuthorNotFoundException if the author is not found
	 */
	public Author findById(int id) throws AuthorNotFoundException {
		Author author = authorRepository.findById(id)
				.orElseThrow(()-> new AuthorNotFoundException("Author with the id: " + id + " not found!"));
		
		return author;
	}
	
	/**
	 * Creates a new author
	 * @param author the author to create
	 * @return the created author
	 */
	public Author saveAuthor(Author author) {
		return authorRepository.save(author);
	}
	
	/**
	 * Updates an existing author
	 * @param author the author to update
	 * @return the updated author
	 */
	public Author updateAuthor(Author author) {
		return authorRepository.save(author);
	}
	
	/**
	 * Gets all authors with their published books
	 * @return a list of all authors
	 */
	public List<Author> findAll() {
		return (List<Author>) authorRepository.findAll();
	}
	
	/**
	 * Deletes an author by ID
	 * @param id the ID of the author to delete
	 * @throws AuthorNotFoundException if the author is not found
	 */
	public void deleteById(int id) throws AuthorNotFoundException {
		Author author = findById(id);
		authorRepository.delete(author);
	}
	
	/**
	 * Finds all books by a specific author
	 * @param id the ID of the author
	 * @return a set of books by the author
	 * @throws AuthorNotFoundException if the author is not found
	 */
	public Set<Book> findBooksByAuthorId(int id) throws AuthorNotFoundException {
		Author author = findById(id);
		return author.getBooks();
	}
}
