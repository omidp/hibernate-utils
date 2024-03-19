package org.example;

import org.example.domain.id.LogEntity;
import org.example.domain.id.PhoneEntity;
import org.example.domain.id.UserEntity;
import org.example.internal.id.IdInterceptor;
import org.example.util.HibernateUtil;
import org.hibernate.SessionFactory;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class AbstractIdTest {
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
		sessionFactory = HibernateUtil.setUp(MY_SQL_CONTAINER.getJdbcUrl(), MY_SQL_CONTAINER.getUsername(), MY_SQL_CONTAINER.getUsername(),
			PhoneEntity.class, LogEntity.class, UserEntity.class
		);
		sessionFactory.inTransaction(session -> session.doWork(connection -> {
			connection.prepareStatement("""
						ALTER TABLE `phone`
				CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT FIRST,
				ADD INDEX `Índice 1` (`id`);
				""").executeUpdate();
		}));
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
	void testInsertLog() {
		var uuid = UUID.randomUUID();
		var log = new LogEntity("logbook");
		log.setId(uuid);
		var session = sessionFactory.withOptions().interceptor(IdInterceptor.INSTANCE).openSession();
		session.persist(log);
		assertThat(session.get(LogEntity.class, uuid)).isEqualTo(log);
	}

	@Test
	void insertPhone() {
		var phone = new PhoneEntity();
		phone.setName("myphone");
		sessionFactory.inTransaction(session -> session.persist(phone));
		assertThat(phone.getId()).isNotNull();
	}

	@Test
	void insertUser() {
		var user = new UserEntity("system");
		sessionFactory.inTransaction(session -> session.persist(user));
		assertThat(user.getId()).isNotNull();
	}

}
