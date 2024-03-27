package org.example;


import jakarta.persistence.Tuple;
import org.example.domain.cte.CustomerEntity;
import org.example.domain.cte.ProfileEntity;
import org.example.domain.cte.ProfileStatsEntity;
import org.example.domain.derived.CallEntity;
import org.example.domain.derived.PersonEntity;
import org.example.domain.derived.PhoneEntity;
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

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class DerivedEntityTest {
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
			CallEntity.class, PersonEntity.class, PhoneEntity.class
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
	void test() {
		sessionFactory.inTransaction(session -> {
			List<Tuple> calls = session.createQuery(
					"""
      					select d.owner, d.payed 
						from (
						  select p.person as owner, c.payment is not null as payed 
						  from CallEntity c 
						  left join c.phone p 
						  ) d
						  """,
					Tuple.class)

				.getResultList();
		});
	}

	@Test
	void testDerived() {
		sessionFactory.inTransaction(session -> {
			List<Tuple> calls = session.createQuery(
					"""
      					select d.owner, d.payed 
						from (
						  select per as owner, c.payment is not null as payed 
						  from CallEntity c 
						  left join c.phone p
						  left join p.person per 
						  ) d
						  """,
					Tuple.class)

				.getResultList();
		});
	}

	@Test
	void testDerivedJoinNative() {
		sessionFactory.inTransaction(session -> {
			session.createNativeQuery("""				
				select
				            p.owner_name,
				            c.payment is not null
				        from
				            CallEntity c
				        left join
				            PhoneEntity phone
				                on phone.id=c.phone_id
				        left join (select per.name as owner_name, per.id from PersonEntity per) as p on phone.person_id = p.id        							
				""").getResultList();
		});
	}

	@Test
	void testDerivedJoinLateral() {
		sessionFactory.inTransaction(session -> {
			session.createQuery(
					"""						  
						select person.owner_name, c.payment is not null
						  from CallEntity c
						  left join c.phone ph
						  left join lateral (
						      select p.name as owner_name from PersonEntity p where ph.personId = p.id
						  ) as person
						  """)
				.getResultList();
		});

	}

	@Test
	void testDerivedJoinNoLateral() {
		sessionFactory.inTransaction(session -> {
			session.createQuery(
					"""						  
						select person.owner_name, c.payment is not null
						  from CallEntity c
						  left join c.phone ph
						  left join  (
						      select p.name as owner_name from PersonEntity p where ph.personId = p.id
						  ) as person
						  """)
				.getResultList();
		});

	}



}
