package org.example;


import org.example.domain.cte.CustomerEntity;
import org.example.domain.cte.ProfileEntity;
import org.example.domain.cte.ProfileStatsEntity;
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

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class MultipleCteEntityTest {
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
			CustomerEntity.class, ProfileEntity.class, ProfileStatsEntity.class
		);

	}

	@AfterEach
	public void tearDown() {
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
	void testMultipleCteNative() {
		sessionFactory.inTransaction(session -> {
			session.createNativeQuery("""
				with data as (
				select u.name as name, p.id as profile_id from profile p
				left join customer u on p.customer_id = u.id
				), stats as (
					select profile_id, viewed from profile_stats
				)
				select d.name, d.profile_id, s.viewed from data d
				left join stats s on d.profile_id = s.profile_id
				""").getResultList();
		});
	}

	@Test
	void testMultipleCteHqlCrossJoin() {
		sessionFactory.inTransaction(session -> {
			session.createQuery("""
						with data as(
						select u.name as name, p.id as profile_id from ProfileEntity p
						left join p.customer u
						), stats as (
						 select ps.viewed as viewed, ps.profile.id as profile_id from ProfileStatsEntity ps
						)
						select d.name from data d
						, stats s where d.profile_id = s.profile_id
					""")
				.getResultList();
		});
	}

	//This is where Hibernate fails
	@Test
	void testMultipleCte() {
		sessionFactory.inTransaction(session -> {
			session.createQuery("""
						with data as(
						select u.name as name, p.id as profile_id from ProfileEntity p
						left join p.customer u
						), stats as (
						 select ps.viewed as viewed, ps.profile.id as profile_id from ProfileStatsEntity ps
						)
						select d.name from data d
						left join stats s on d.profile_id = s.profile_id
					""")
				.getResultList();

		});
	}


}
