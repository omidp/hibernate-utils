package org.example.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.internal.json.CustomJacksonJsonFormatMapper;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import java.util.Properties;

public class HibernateUtil {

	public static SessionFactory setUp(String jdbcUrl, String username, String password, Class<?>... classes) {
		Properties props = new Properties();
		props.put("hibernate.connection.url", jdbcUrl);
		props.put("hibernate.connection.username", username);
		props.put("hibernate.connection.password", password);
		props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		props.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
		props.put("hibernate.show_sql", "true");
		props.put("hibernate.format_sql", "true");
		props.put("hibernate.highlight_sql", "true");
		props.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
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
					.addAnnotatedClasses(classes)
					.buildMetadata()
					.buildSessionFactory();
			return sessionFactory;
		} catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we
			// had trouble building the SessionFactory so destroy it manually.
			StandardServiceRegistryBuilder.destroy(registry);
			throw new RuntimeException(e);
		}
	}
}
