package com.bookstore.daoimpl;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.application.BookRepository;
import com.bookstore.dao.BookStore;
import com.bookstore.exceptionhandler.CustomNotFoundException;
import com.bookstore.exceptionhandler.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.model.Post;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BookStoreImpl implements BookStore {

	@Autowired
	BookRepository bookRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public BookStoreImpl() {
	}

	public BookStoreImpl(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	AtomicInteger count = new AtomicInteger(1);

	@Transactional(rollbackFor = Exception.class)
	public String addBook(Book book) {
		Optional<Book> bookOptional = bookRepository.findById(book.getIsbn());
		if (bookOptional.isPresent()) {
			book.setCount(book.getCount() + bookOptional.get().getCount());
		}
		bookRepository.save(book);
		return "Book has been saved successfully.";
	}

	public List<String> searchMediaCoverage(Long isbn) throws IOException {
		Optional<Book> optionalBook = bookRepository.findById(isbn);
		if (optionalBook.isPresent()) {
			final Book book = optionalBook.get();

			ObjectMapper mapper = new ObjectMapper();
			try {
				Post[] usrPost = mapper.readValue(new URL("http://jsonplaceholder.typicode.com/posts"), Post[].class);
				List<String> titleList = Arrays.stream(usrPost)
						.filter(post -> post.getTitle().contains(book.getTitle())
								|| post.getBody().contains(book.getTitle()))
						.map(post -> post.getTitle()).collect(Collectors.toList());
				return titleList;
			} catch (IOException e) {
				throw new ResourceNotFoundException("http://jsonplaceholder.typicode.com/posts", null, null);
			}
		}
		return Collections.emptyList();
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30, rollbackFor = IOException.class)
	public String purchase(Long isbn) {
		Optional<Book> optionalBook = bookRepository.findById(isbn);
		if (optionalBook.isPresent()) {
			Book book = optionalBook.get();
			if (book.getCount().equals(1)) {
				bookRepository.delete(book);
			} else {
				book.setCount(book.getCount() - 1);
			}
			return "Book has been purchased successfully.";
		}
		return "Book is out of stock. Please try again later.";
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
	public List<Book> searchStock(String searchParam, String searchValue) {
		if (count.get() == 1) {
			createIndex();
			count.set(0);
		}

		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

		QueryBuilder builder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();

		Query luceneQuery = null;

		if (searchParam.equalsIgnoreCase("ISBN")) {
			luceneQuery = builder.keyword().onFields(searchParam).matching(searchValue).createQuery();
		} else if (searchParam.equalsIgnoreCase("Title") || searchParam.equalsIgnoreCase("Author")) {
			luceneQuery = builder.keyword().fuzzy().onFields(searchParam).matching(searchValue).createQuery();
		} else {
			throw new ConstraintViolationException("Allowed criterias for searching are ISBN, Title and author", null);
		}
		FullTextQuery q = fullTextEntityManager.createFullTextQuery(luceneQuery, Book.class);

		@SuppressWarnings("unchecked")
		List<Book> result = q.getResultList();
		if (result == null || result.isEmpty()) {
			throw new CustomNotFoundException("Not found any book with " + searchParam + " " + searchValue);
		}
		return result;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public List<Book> listBooks() {
		return bookRepository.findAll();
	}

	public void createIndex() {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
		try {
			fullTextEntityManager.createIndexer().startAndWait();
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
	}
}
