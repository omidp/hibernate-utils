package org.example;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.domain.root.InvoiceLineEntity;
import org.example.domain.root.OrderEntity;
import org.example.domain.root.OrderItemVO;
import org.example.domain.root.OrderLineEntity;
import org.example.domain.subselect.CustomerEntity;
import org.example.domain.subselect.CustomerTransactionView;
import org.example.domain.subselect.TransactionEntity;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.query.sqm.tree.from.SqmEntityJoin;
import org.hibernate.query.sqm.tree.from.SqmRoot;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;


@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class CriteriaRootTest {
	@ClassRule
	private static MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer(DockerImageName.parse("public.ecr.aws/docker/library/mysql:8.0-oracle")
		.asCompatibleSubstituteFor("mysql"));
	private SessionFactory sessionFactory;
	private UUID orderId;

	@BeforeAll
	static void startDb() {
		MY_SQL_CONTAINER.start();
	}

	@BeforeEach
	public void setUp() {
		sessionFactory = HibernateUtil.setUp(MY_SQL_CONTAINER.getJdbcUrl(), MY_SQL_CONTAINER.getUsername(), MY_SQL_CONTAINER.getUsername(),
			OrderEntity.class, OrderLineEntity.class, InvoiceLineEntity.class
		);
		sessionFactory.inTransaction(session -> {
			var order = new OrderEntity();
			order.setName("my order");
			session.persist(order);
			this.orderId = order.getId();
			var item = new OrderLineEntity();
			item.setOrderId(order.getId());
			item.setAmount(BigDecimal.ONE);
			session.persist(item);
			//
			var item2 = new OrderLineEntity();
			item2.setOrderId(order.getId());
			item2.setAmount(BigDecimal.TEN);
			session.persist(item2);
			//
			InvoiceLineEntity invoiceLineEntity = new InvoiceLineEntity();
			invoiceLineEntity.setOrderLineId(item.getId());
			invoiceLineEntity.setAmount(item.getAmount());
			session.persist(invoiceLineEntity);
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
			CriteriaFilterParam orderlineParam = new CriteriaFilterParam(OrderLineEntity.class.getName(), MatchOperator.AND);
			orderlineParam.addParam(new org.example.CriteriaRootTest.Operator.EqualOperator("orderId", this.orderId));
			orderlineParam.addParam(new org.example.CriteriaRootTest.Operator.EqualOperator("amount", BigDecimal.TEN));
			CriteriaFilterParam orderParam = new CriteriaFilterParam(OrderEntity.class.getName(), MatchOperator.AND);
			orderParam.addParam(new Operator.EqualOperator("name", "my order"));
			orderParam.addParam(new Operator.EqualOperator("id", randomUUID()));
			CriteriaFilterParam invoicelineParam = new CriteriaFilterParam(InvoiceLineEntity.class.getName(), MatchOperator.OR);
			invoicelineParam.addParam(new Operator.EqualOperator("orderLineId", randomUUID()));
			execute(
				session,
				List.of(
					orderlineParam, orderParam, invoicelineParam
				),
				MatchOperator.AND
			);
		});
	}

	private void execute(Session session, List<CriteriaFilterParam> filters, MatchOperator matchOperator) {
		Map<String, List<CriteriaFilterParam>> filterMap = filters.stream().collect(Collectors.groupingBy(CriteriaFilterParam::getEntityName));
		HibernateCriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		String hql = """
			select NEW org.example.domain.root.OrderItemVO(o.id, item.id, item.amount, o.name) from OrderLineEntity item 
			left join OrderEntity o on item.orderId = o.id
			left join InvoiceLineEntity i on item.id = i.orderLineId
			""";
		JpaCriteriaQuery<OrderItemVO> criteriaQuery = criteriaBuilder.createQuery(hql, OrderItemVO.class);
		SqmRoot<OrderLineEntity> root = (SqmRoot<OrderLineEntity>) criteriaQuery.getRootList().iterator().next();
		List<Predicate> roots = new ArrayList<>();
		List<CriteriaFilterParam> rootFilter = filterMap.getOrDefault(root.getEntityName(), Collections.emptyList());
		for (CriteriaFilterParam criteriaFilterParam : rootFilter) {
			List<Predicate> rootPredicates = new ArrayList<>();
			for (Operator operator : criteriaFilterParam.params) {
				switch (operator) {
					case Operator.EqualOperator eq:
						rootPredicates.add(criteriaBuilder.equal(root.get(eq.field), eq.value));
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + operator);
				}
			}
			if (criteriaFilterParam.operator == MatchOperator.OR) {
				roots.add(criteriaBuilder.or(rootPredicates.toArray(new Predicate[rootPredicates.size()])));
			} else {
				roots.addAll(rootPredicates);
			}
		}
		List<SqmEntityJoin> sqmJoins = ((SqmRoot) root).getSqmJoins();
		List<Predicate> joins = new ArrayList<>();
		for (SqmEntityJoin sqmJoin : sqmJoins) {
			List<CriteriaFilterParam> joinFilter = filterMap.getOrDefault(sqmJoin.getEntityName(), Collections.emptyList());
			for (CriteriaFilterParam criteriaFilterParam : joinFilter) {
				List<Predicate> joinPredicates = new ArrayList<>();
				for (Operator operator : criteriaFilterParam.params) {
					switch (operator) {
						case Operator.EqualOperator eq:
							joinPredicates.add(criteriaBuilder.equal(sqmJoin.get(eq.field), eq.value));
							break;
						default:
							throw new IllegalStateException("Unexpected value: " + operator);
					}
				}
				if (criteriaFilterParam.operator == MatchOperator.OR) {
					joins.add(criteriaBuilder.or(joinPredicates.toArray(new Predicate[joinPredicates.size()])));
				} else {
					joins.addAll(joinPredicates);
				}
			}
		}
		if (matchOperator == MatchOperator.OR) {
			criteriaQuery.where(criteriaBuilder.or(
				criteriaBuilder.and(roots.toArray(new Predicate[roots.size()])),
				criteriaBuilder.and(joins.toArray(new Predicate[joins.size()]))
			));
		} else {
			criteriaQuery.where(criteriaBuilder.and(roots.toArray(new Predicate[roots.size()])),
				criteriaBuilder.and(joins.toArray(new Predicate[joins.size()])));
		}


		List<OrderItemVO> resultList = session.createQuery(criteriaQuery).getResultList();
	}

	private void applyFilter(Map<String, List<CriteriaFilterParam>> filterMap, String entityName, Consumer<Operator> operatorConsumer) {
		List<CriteriaFilterParam> rootFilter = filterMap.getOrDefault(entityName, Collections.emptyList());
		for (CriteriaFilterParam criteriaFilterParam : rootFilter) {
			for (Operator param : criteriaFilterParam.params) {
				operatorConsumer.accept(param);
			}
		}
	}

	public static class CriteriaFilterParam {
		@Getter private final String entityName;
		@Getter private MatchOperator operator = MatchOperator.AND;
		private List<Operator> params = new ArrayList<>();

		public CriteriaFilterParam(String entityName) {
			this.entityName = entityName;
		}

		public CriteriaFilterParam(String entityName, MatchOperator operator) {
			this.entityName = entityName;
			this.operator = operator;
		}

		public void addParam(Operator param) {
			params.add(param);
		}
	}

	public sealed interface Operator {
		record EqualOperator(String field, Object value) implements Operator {}

		record LikeOperator(String field, Object value) implements Operator {}
	}

	public enum MatchOperator {
		AND, OR
	}

//	@Data
//	@AllArgsConstructor
//	public static class CriteriaParam {
//		private final String field;
//		private Operator operator;
//		private Object value;
//	}


//	public enum Operator {
//		EQ
//	}


	/**
	 * select
	 * ole1_0.id,
	 * ole1_0.amount,
	 * ole1_0.order_id
	 * from
	 * OrderLineEntity ole1_0
	 * left join
	 * OrderEntity oe1_0
	 * on ole1_0.order_id=oe1_0.id, OrderEntity oe2_0 // This is wrong
	 * where
	 * oe2_0.name=?
	 */
	@Test
	void testInvalidQuery() {
		sessionFactory.inTransaction(session -> {
			HibernateCriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			String hql = "select item from OrderLineEntity item left join OrderEntity o on item.orderId = o.id";
			JpaCriteriaQuery<OrderLineEntity> criteriaQuery = criteriaBuilder.createQuery(hql, OrderLineEntity.class);
			//shouldn't criteriaQuery.getRootList() return OrderLineEntity and OrderEntity
//			Root<OrderLineEntity> root = (Root<OrderLineEntity>) criteriaQuery.getRootList().iterator().next();
			JpaRoot<OrderEntity> itemRoot = criteriaQuery.from(OrderEntity.class);
			criteriaQuery.where(criteriaBuilder.equal(itemRoot.get("name"), "my order"));
			List<OrderLineEntity> resultList = session.createQuery(criteriaQuery).getResultList();
			System.out.println(resultList);
		});
	}


}
