package org.example;

import org.example.domain.json.Author;
import org.example.domain.json.BookEntity;
import org.example.domain.json.Publisher;
import org.example.util.HibernateUtil;
import org.hibernate.SessionFactory;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class JsonTypeTest {
	@ClassRule
	private static MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer(DockerImageName.parse("public.ecr.aws/docker/library/mysql:8.0-oracle")
		.asCompatibleSubstituteFor("mysql"));
	private SessionFactory sessionFactory;

	@BeforeAll
	static void startDb() {
		MY_SQL_CONTAINER.start();
	}

	@BeforeEach
	public void setUp() {
		sessionFactory = HibernateUtil.setUp(MY_SQL_CONTAINER.getJdbcUrl(), MY_SQL_CONTAINER.getUsername(), MY_SQL_CONTAINER.getUsername(), BookEntity.class);
	}

	@AfterEach
	public void tearDown(){
		sessionFactory.close();
	}

	@AfterAll
	static void stopDb() {
		MY_SQL_CONTAINER.stop();
	}

	@Test
	public void containerRunning() {
		assertThat(MY_SQL_CONTAINER.isRunning()).isTrue();
	}

	@Test
	void testInsertBook(){
		var bookId = UUID.randomUUID();
		var book = new BookEntity();
		book.setName("Hibernate Mindset");
		book.setId(bookId);
		var authors = List.of(new Author("Carol", "Dweck"), new Author("Gavin", "King"));
		book.setAuthors(authors);
		var publisher = new Publisher("Manning");
		book.setPublisher(publisher);
		sessionFactory.inTransaction(session -> session.persist(book));
		sessionFactory.inTransaction(session -> assertThat(session.get(BookEntity.class, bookId).getAuthors()).isEqualTo(authors));
		sessionFactory.inTransaction(session -> assertThat(session.get(BookEntity.class, bookId).getPublisher()).isEqualTo(publisher));
	}

}
