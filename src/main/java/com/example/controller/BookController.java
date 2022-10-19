package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.generated.model.Book;
import com.example.service.BookService;

@RestController
@RequestMapping("books")
public class BookController {

	@Autowired
	private BookService service;

	@GetMapping("{id}")
	public Book get(@PathVariable Integer id) {
		return service.selectById(id);
	}
}
