package com.bookstore.dao;

import java.io.IOException;
import java.util.List;

import com.bookstore.model.Book;

public interface BookStore {
	public String purchase(Long isbn);

	public String addBook(Book book);

	public List<Book> searchStock(String searchParam, String searchValue);

	public List<String> searchMediaCoverage(Long isbn) throws IOException;

	public List<Book> listBooks();

	public void createIndex();
}
