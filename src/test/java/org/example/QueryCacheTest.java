package org.example;

import org.example.domain.CategoryDetailEntity;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.BlobProxy;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class QueryCacheTest {
	@ClassRule
	private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
	private SessionFactory sessionFactory;

	@BeforeAll
	static void startDb() {
		postgreSQLContainer.start();
	}

	@BeforeEach
	public void setUp() {
		sessionFactory = HibernateUtil.setUp(postgreSQLContainer.getJdbcUrl(), postgreSQLContainer.getUsername(),
			postgreSQLContainer.getUsername(),
			props -> {
				props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
				props.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");
				props.put("hibernate.cache.region.factory_class", "jcache");
				props.put("hibernate.cache.use_query_cache", "true");
				props.put("hibernate.cache.use_second_level_cache", "true");
				props.put("hibernate.javax.cache.provider", "com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider");
			},
			CategoryDetailEntity.class
		);
	}

	@AfterEach
	public void tearDown() {
		sessionFactory.close();
	}

	@AfterAll
	static void stopDb() {
		postgreSQLContainer.stop();
	}

	@Test
	public void containerRunning() {
		assertThat(postgreSQLContainer.isRunning()).isTrue();
	}

	@Test
	void testInsertBook() {
		Long id = 100L;
		sessionFactory.inTransaction(session -> {
			try {
				session.persist(new CategoryDetailEntity(id, BlobProxy.generateProxy(getClass().getResourceAsStream("/DukeCheers.png").readAllBytes())));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		sessionFactory.inTransaction(session -> {
			executeQuery(session, id);
		});

		//This second execution is causing Blobs may not be accessed after serialization
		sessionFactory.inTransaction(session -> executeQuery(session, id));


	}

	private void executeQuery(Session session, Long id) {
		session.createSelectionQuery("from CategoryDetailEntity c where c.id = :cid", CategoryDetailEntity.class)
			.setParameter("cid", id)
			.setCacheable(true)
			.uniqueResultOptional()
			.ifPresent(categoryDetail -> {
				try (InputStream is = categoryDetail.getImage().getBinaryStream()) {
					System.out.println(is == null);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
	}

}
