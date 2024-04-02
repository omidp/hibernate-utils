package org.example.multitenant;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.internal.json.CustomJacksonJsonFormatMapper;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;

public class TestMain {


	public static void main(String[] args) {
		String jdbcUrl = "jdbc:mysql://localhost:3306/webui-tenant-43";
		String username = "root";
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword("");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		DataSource ds = new HikariDataSource(config);
		Properties props = new Properties();
		props.put(AvailableSettings.DATASOURCE, ds);
		props.put("hibernate.connection.url", jdbcUrl);
		props.put("hibernate.connection.username", username);
		props.put("hibernate.connection.password", "");
		props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		props.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
		props.put("hibernate.show_sql", "true");
		props.put("hibernate.format_sql", "true");
		props.put("hibernate.highlight_sql", "true");
		props.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
		//Multi tenant conf
		props.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, MultiTenant.class);
		props.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, ThreadCurrentTenantIdentifierResolver.class);
		props.put("custom_tenant_id", "SOME_TENANT_ID");
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
		props.put(AvailableSettings.JSON_FORMAT_MAPPER, new CustomJacksonJsonFormatMapper(objectMapper));
		final StandardServiceRegistry registry =
			new StandardServiceRegistryBuilder()
				.applySettings(props)
				.build();
		try {
			var sessionFactory =
				new MetadataSources(registry)
					.addAnnotatedClasses(SimpleEntity.class)
					.buildMetadata()
					.buildSessionFactory();
			doWithSessionFactory(sessionFactory);
		} catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we
			// had trouble building the SessionFactory so destroy it manually.
			StandardServiceRegistryBuilder.destroy(registry);
			throw new RuntimeException(e);
		}

	}

	private static void doWithSessionFactory(SessionFactory sessionFactory) {
		var id = UUID.randomUUID();
		try {
			TenantDbContext.pushTenantDb("webui-tenant-43");
			sessionFactory.inTransaction(session -> session.persist(new SimpleEntity(id, "simple")));
			sessionFactory.inTransaction(session -> session.get(SimpleEntity.class, id));
		} finally {
			TenantDbContext.popTenantDb();
		}

	}

}
