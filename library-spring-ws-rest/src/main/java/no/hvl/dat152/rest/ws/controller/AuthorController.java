/**
 * 
 */
package no.hvl.dat152.rest.ws.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.hvl.dat152.rest.ws.exceptions.AuthorNotFoundException;
import no.hvl.dat152.rest.ws.model.Author;
import no.hvl.dat152.rest.ws.model.Book;
import no.hvl.dat152.rest.ws.service.AuthorService;

/**
 * 
 */
@RestController
@RequestMapping("/elibrary/api/v1")
public class AuthorController {

	@Autowired
	private AuthorService authorService;
	
	/**
	 * Gets all authors with their published books
	 * @return a list of all authors
	 */
	@GetMapping("/authors")
	public ResponseEntity<Object> getAllAuthors() {
		List<Author> authors = authorService.findAll();
		
		if (authors.isEmpty())
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
		return new ResponseEntity<>(authors, HttpStatus.OK);
	}
	
	/**
	 * Gets an author by ID with their published books
	 * @param id the ID of the author
	 * @return the author if found
	 * @throws AuthorNotFoundException if author not found
	 */
	@GetMapping("/authors/{id}")
	public ResponseEntity<Author> getAuthor(@PathVariable int id) throws AuthorNotFoundException {
		Author author = authorService.findById(id);
		return new ResponseEntity<>(author, HttpStatus.OK);
	}
	
	/**
	 * Gets all books by a specific author
	 * @param id the ID of the author
	 * @return the list of books by the author
	 * @throws AuthorNotFoundException if author not found
	 */
	@GetMapping("/authors/{id}/books")
	public ResponseEntity<Object> getBooksByAuthorId(@PathVariable int id) throws AuthorNotFoundException {
		Set<Book> books = authorService.findBooksByAuthorId(id);
		
		if (books.isEmpty())
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
		return new ResponseEntity<>(books, HttpStatus.OK);
	}
	
	/**
	 * Creates a new author
	 * @param author the author to create
	 * @return the created author
	 */
	@PostMapping("/authors")
	public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
		Author newAuthor = authorService.saveAuthor(author);
		return new ResponseEntity<>(newAuthor, HttpStatus.CREATED);
	}
	
	/**
	 * Updates an existing author
	 * @param id the ID of the author to update
	 * @param author the updated author data
	 * @return the updated author
	 * @throws AuthorNotFoundException if author not found
	 */
	@PutMapping("/authors/{id}")
	public ResponseEntity<Author> updateAuthor(@PathVariable int id, @RequestBody Author author) throws AuthorNotFoundException {
		// Sjekk at forfatteren finnes
		authorService.findById(id);
		
		// Sett riktig ID
		author.setAuthorId(id);
		
		Author updatedAuthor = authorService.updateAuthor(author);
		return new ResponseEntity<>(updatedAuthor, HttpStatus.OK);
	}
}
