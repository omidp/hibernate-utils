package org.example;

import org.example.domain.id.LogEntity;
import org.example.domain.id.PhoneEntity;
import org.example.domain.id.UserEntity;
import org.example.domain.subselect.CustomerEntity;
import org.example.domain.subselect.CustomerTransactionView;
import org.example.domain.subselect.TransactionEntity;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class SubSelectTest {
	@ClassRule
	private static MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer(DockerImageName.parse("public.ecr.aws/docker/library/mysql:8.0-oracle")
		.asCompatibleSubstituteFor("mysql"));
	private SessionFactory sessionFactory;
	private UUID customerId;

	@BeforeAll
	static void startDb() {
		MY_SQL_CONTAINER.start();
	}

	@BeforeEach
	public void setUp() {
		sessionFactory = HibernateUtil.setUp(MY_SQL_CONTAINER.getJdbcUrl(), MY_SQL_CONTAINER.getUsername(), MY_SQL_CONTAINER.getUsername(),
			CustomerEntity.class, TransactionEntity.class, CustomerTransactionView.class
		);
		sessionFactory.inTransaction(session -> {
			var customer = new CustomerEntity(UUID.randomUUID(), "customer1");
			this.customerId = customer.getId();
			var transaction = new TransactionEntity(BigDecimal.TEN, customer);
			session.persist(customer);
			session.persist(transaction);
			session.flush();
		});
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
	void test() {
		sessionFactory.inTransaction(session -> {
			CustomerTransactionView customerTransactionView = session.get(CustomerTransactionView.class, this.customerId);
			assertThat(customerTransactionView.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
		});
	}



}
