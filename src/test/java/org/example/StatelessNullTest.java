package org.example;

import org.example.domain.stateless.StatelessPaymentEntity;
import org.example.domain.stateless.StatelessUserEntity;
import org.example.domain.stateless.StatelessUserPayVO;
import org.example.domain.subselect.CustomerEntity;
import org.example.domain.subselect.CustomerTransactionView;
import org.example.domain.subselect.TransactionEntity;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class StatelessNullTest {
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
			StatelessUserEntity.class, StatelessPaymentEntity.class
		);
		sessionFactory.inTransaction(session -> {
			var user = new StatelessUserEntity();

			session.persist(user);
			var pay = new StatelessPaymentEntity();
			pay.setUser(user);
			pay.setAmount(BigDecimal.ONE);
			session.persist(pay);
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
		sessionFactory.inStatelessSession(statelessSession -> {
			String q = """
   			select new org.example.domain.stateless.StatelessUserPayVO(u, p) from StatelessPaymentEntity p left join p.user u
   			""";
			List<StatelessUserPayVO> resultList = statelessSession.createQuery(q, StatelessUserPayVO.class)
				.getResultList();
			System.out.println(resultList);
			System.out.println(resultList.iterator().next().user().getPayments());
		});
	}



}
