package org.example;

import org.example.internal.map.Category;
import org.example.internal.map.OrderEntity;
import org.example.internal.map.OrderItemEntity;
import org.example.internal.map.OrderItemRecord;
import org.example.internal.map.ProductAttribute;
import org.example.util.HibernateUtil;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.context.internal.ManagedSessionContext;
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
public class SessionContextTest {
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
			props -> props.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, ManagedSessionContext.class.getName()),
			OrderItemEntity.class, OrderEntity.class
		);
		this.id = UUID.randomUUID();
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
			session.persist(new OrderEntity(UUID.randomUUID(), "test"));
			if(!ManagedSessionContext.hasBind(sessionFactory)){
				ManagedSessionContext.bind(session);
			}
			insertItem();
			ManagedSessionContext.unbind(sessionFactory);
		});
	}


	private void insertItem(){
		var orderItem = new OrderItemEntity();
		orderItem.setId(this.id);
		orderItem.setPrice(BigDecimal.ONE);
		orderItem.setCategory(Category.VEGETABLES);
		orderItem.setAttributes(Map.of(ProductAttribute.WEIGHT, "200"));
		sessionFactory.getCurrentSession().persist(orderItem);
	}


}
