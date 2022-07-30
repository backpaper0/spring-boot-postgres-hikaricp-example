package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.TransactionTimedOutException;

import com.example.generated.model.Book;
import com.example.service.BookService;

@SpringBootTest
public class BookTest {

	@Autowired
	BookService bookService;

	/**
	 * トランザクションタイムアウトもクエリータイムアウトも発生させない。
	 * 
	 */
	@Test
	void test() {
		List<Book> actual = bookService.selectAndSleep(0, TimeUnit.MILLISECONDS);

		Book book1 = new Book();
		book1.setId(1);
		book1.setTitle("緋色の研究");

		Book book2 = new Book();
		book2.setId(2);
		book2.setTitle("四つの署名");

		Book book3 = new Book();
		book3.setId(3);
		book3.setTitle("バスカヴィル家の犬");

		Book book4 = new Book();
		book4.setId(4);
		book4.setTitle("恐怖の谷");

		List<Book> expected = List.of(
				book1,
				book2,
				book3,
				book4);

		assertEquals(expected, actual);
	}

	/**
	 * トランザクションタイムアウトを発生させる。
	 * 
	 */
	@Test
	void testTransactionTimeout1() {
		assertThrows(TransactionTimedOutException.class,
				() -> bookService.selectAndSleep(500, TimeUnit.MILLISECONDS));
	}

	/**
	 * トランザクションタイムアウトを発生させない。
	 * 
	 * トランザクションタイムアウトはトランザクション開始から終了までの間に
	 * クエリーを発行するタイミングでタイムアウトのチェックが行われる。
	 * そのため、規定時間が経過していたとしてもその後にクエリーが発行されなければ
	 * エラーにはならない。
	 */
	@Test
	void testTransactionTimeout2() {
		// トランザクションタイムアウトが2秒なのに対して3秒スリープしているが
		// スリープ後は戻り値を返却しているだけでクエリーを発行していないので
		// タイムアウトにはならない。
		Book actual = bookService.selectByIdAndSleep(1, 3, TimeUnit.SECONDS);

		Book expected = new Book();
		expected.setId(1);
		expected.setTitle("緋色の研究");

		assertEquals(expected, actual);
	}

	/**
	 * トランザクションタイムアウトを試す。
	 * 
	 */
	@Test
	void testTransactionTimeout3() {
		// スリープ後にクエリーを発行するタイミングでタイムアウトエラーとなる
		assertThrows(TransactionTimedOutException.class,
				() -> bookService.selectByIdsAndSleep(1, 2, 3, TimeUnit.SECONDS));
	}

	/**
	 * クエリータイムアウトを試す。
	 * 
	 * クエリータイムアウトを発生させている方法は次の通り。
	 * 別スレッドでトランザクションを開始してselect for updateしてからスリープする。
	 * その後、本スレッドでselect for updateするとロックが競合するので待つこととなり、
	 * クエリータイムアウトが発生する。
	 * 
	 */
	@Test
	void testQueryTimeout() {
		Integer id = 1;
		CountDownLatch cdl = new CountDownLatch(1);
		bookService.selectByIdForUpdateAndSleepAsync(id, 2, TimeUnit.SECONDS, cdl);
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertThrows(DataAccessResourceFailureException.class,
				() -> bookService.selectByIdForUpdate(id));
	}
}
