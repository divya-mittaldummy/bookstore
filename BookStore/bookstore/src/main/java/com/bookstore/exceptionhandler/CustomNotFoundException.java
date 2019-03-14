package com.bookstore.exceptionhandler;

public class CustomNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public CustomNotFoundException(String msg) {
		super(msg);
	}
}
