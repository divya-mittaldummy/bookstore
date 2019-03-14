package com.bookstore.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.dao.BookStore;
import com.bookstore.exceptionhandler.CustomNotFoundException;
import com.bookstore.exceptionhandler.ResponseMsg;
import com.bookstore.model.Book;

@RestController
public class BookStoreController implements ErrorController {

	private static final String PATH = "/error";

	@ExceptionHandler(CustomNotFoundException.class)
	public ResponseMsg handleNotFoundException(CustomNotFoundException ex) {
		ResponseMsg responseMsg = new ResponseMsg(ex.getMessage());
		return responseMsg;
	}
	
	@RequestMapping(value = PATH)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public String error() {
		return "Internal Server Error";
	}

	@Override
	public String getErrorPath() {
		return PATH;
	}

	@Autowired
	BookStore bookStore;

	@RequestMapping(value = "/addBook", method = RequestMethod.POST)
	public String addBook(@RequestBody Book book) {
		return bookStore.addBook(book);
	}

	@RequestMapping("/searchBooks/{searchParam}/{searchValue}")
	public List<Book> searchBook(@PathVariable("searchParam") String searchParam,
			@PathVariable("searchValue") String searchValue) {

		return bookStore.searchStock(searchParam, searchValue);
	}

	@RequestMapping("/searchMediaCoverage/{isbn}")
	public List<String> searchMediaCoverage(@PathVariable Long isbn) throws IOException {
		return bookStore.searchMediaCoverage(isbn);
	}

	@RequestMapping("/buyBook/{isbn}")
	public String buyBook(@PathVariable("isbn") Long isbn) {
		return bookStore.purchase(isbn);
	}

	@RequestMapping("/listBooks")
	public List<Book> listBooks() {
		return bookStore.listBooks();
	}

}
