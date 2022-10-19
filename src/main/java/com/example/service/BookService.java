package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.generated.mapper.BookMapper;
import com.example.generated.model.Book;
import com.example.mapper.MyBookMapper;

@Service
@Transactional
public class BookService {

	@Autowired
	private BookMapper bookMapper;
	@Autowired
	private MyBookMapper myBookMapper;

	public List<Book> selectAndSleep(long timeout, TimeUnit timeUnit) {
		List<Book> books = new ArrayList<>();
		for (int i = 1; i <= 4; i++) {
			sleep(timeout, timeUnit);
			Book book = bookMapper.selectByPrimaryKey(i);
			books.add(book);
		}
		return books;
	}

	public Book selectByIdAndSleep(Integer id, long timeout, TimeUnit timeUnit) {
		Book book = bookMapper.selectByPrimaryKey(id);
		sleep(timeout, timeUnit);
		return book;
	}

	public List<Book> selectByIdsAndSleep(Integer id1, Integer id2, long timeout, TimeUnit timeUnit) {
		Book book1 = bookMapper.selectByPrimaryKey(id1);
		sleep(timeout, timeUnit);
		Book book2 = bookMapper.selectByPrimaryKey(id2);
		return List.of(book1, book2);
	}

	public Book selectByIdForUpdate(Integer id) {
		return myBookMapper.selectByPrimaryKeyForUpdate(id);
	}

	@Async
	public CompletableFuture<Book> selectByIdForUpdateAndSleepAsync(Integer id, long timeout, TimeUnit timeUnit,
			CountDownLatch cdl) {
		Book book = myBookMapper.selectByPrimaryKeyForUpdate(id);
		cdl.countDown();
		sleep(timeout, timeUnit);
		return CompletableFuture.completedFuture(book);
	}

	private static void sleep(long timeout, TimeUnit timeUnit) {
		try {
			timeUnit.sleep(timeout);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public Book selectById(Integer id) {
		return bookMapper.selectByPrimaryKey(id);
	}
}
