package com.netent.bookstore;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bookstore.daoimpl.BookStoreImpl;

 
@Configuration
public class AppConfig {
    @Bean
    public BookStoreImpl getSampleService() {
        return new BookStoreImpl();
    }
}
