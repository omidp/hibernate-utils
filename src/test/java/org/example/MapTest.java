package org.example;

import org.example.domain.subselect.CustomerEntity;
import org.example.domain.subselect.CustomerTransactionView;
import org.example.domain.subselect.TransactionEntity;
import org.example.internal.map.Category;
import org.example.internal.map.OrderItemEntity;
import org.example.internal.map.OrderItemRecord;
import org.example.internal.map.ProductAttribute;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class MapTest {
	@ClassRule
	private static MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer(DockerImageName.parse("public.ecr.aws/docker/library/mysql:8.0-oracle")
		.asCompatibleSubstituteFor("mysql"));
	private SessionFactory sessionFactory;
	private UUID id;

	@BeforeAll
	static void startDb() {
		MY_SQL_CONTAINER.start();
	}

	@BeforeEach
	public void setUp() {
		sessionFactory = HibernateUtil.setUp(MY_SQL_CONTAINER.getJdbcUrl(), MY_SQL_CONTAINER.getUsername(), MY_SQL_CONTAINER.getUsername(),
			OrderItemEntity.class
		);
		this.id = UUID.randomUUID();
		sessionFactory.inTransaction(session -> {
			var orderItem = new OrderItemEntity();
			orderItem.setId(this.id);
			orderItem.setPrice(BigDecimal.ONE);
			orderItem.setCategory(Category.VEGETABLES);
			orderItem.setAttributes(Map.of(ProductAttribute.WEIGHT, "200"));
			session.persist(orderItem);
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
			String hql = """
				select NEW org.example.internal.map.OrderItemRecord(oi.category, cast(sum(oi.price) as java.math.BigDecimal)) 
				from OrderItemEntity oi
				group by oi.category
				""";
			List<OrderItemRecord> resultList = session.createSelectionQuery(hql, OrderItemRecord.class).getResultList();
			System.out.println(resultList);
		});
	}


}
