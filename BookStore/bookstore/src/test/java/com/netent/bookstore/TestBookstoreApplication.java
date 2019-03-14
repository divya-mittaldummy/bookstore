package com.netent.bookstore;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bookstore.application.BookRepository;
import com.bookstore.dao.BookStore;
import com.bookstore.daoimpl.BookStoreImpl;
import com.bookstore.model.Book;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestBookstoreApplication {

	private BookStore bookStore;

	@MockBean
	private BookRepository bookRepository;

	@Before
	public void setUp() {
		bookStore = new BookStoreImpl(bookRepository);
		Book book1 = new Book((long) 33399993, "esse", "Alex", 900);
		Book book2 = new Book((long) 77777777, "C++", "Denis", 800);
		List<Book> all = new LinkedList<Book>();
		all.add(book1);
		all.add(book2);
		Mockito.when(bookRepository.findById((long) 33399993)).thenReturn(Optional.of(book1));
		Mockito.when(bookRepository.findAll()).thenReturn(all);
	}

	@Test
	public void testAddBookWithNewISBN() {
		Book book = new Book((long) 123, "esse", "Alex", 900);
		String addBookStatus = bookStore.addBook(book);
		assertEquals(addBookStatus, "Book has been saved successfully.");
	}

	@Test
	public void testAddBookWithExistingISBN() {
		Book book = new Book((long) 333993, "Java Fundamental", "David", 900);
		String addBookStatus = bookStore.addBook(book);
		assertEquals(addBookStatus, "Book has been saved successfully.");
	}

	@Test
	public void testbuyBookWhenBookIsPresent() {
		String buyBookStatus = bookStore.purchase((long) 33399993);
		assertEquals(buyBookStatus, "Book has been purchased successfully.");
	}

	@Test
	public void testbuyBookWhenBookIsNotPresent() {
		String buyBookStatus = bookStore.purchase((long) 3393);
		assertEquals(buyBookStatus, "Book is out of stock. Please try again later.");
	}

	@Test
	public void testSearchMediaCoverageReturnsNullWhenBookNotPresent() {
		try {
			List<String> searchedTitles = bookStore.searchMediaCoverage((long) 5555);
			assertEquals(searchedTitles.size(), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchMediaCoverageWhenBookIsPresent() {
		try {
			List<String> searchedTitles = bookStore.searchMediaCoverage((long) 33399993);
			assertEquals(searchedTitles.size(), 11);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void listBooks(){
		List<Book> bookList = bookStore.listBooks();
		assertEquals(bookList.size(), 2);
	}
}
