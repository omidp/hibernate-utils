package org.example.multitenant;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Stoppable;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiTenant extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl
	implements ServiceRegistryAwareService, Stoppable {

	private Map<String, DataSource> dataSourceMap;
	private String tenantDb;

	@Override
	protected DataSource selectAnyDataSource() {
		return selectDataSource(tenantDb);
	}

	@Override
	protected DataSource selectDataSource(String tenantIdentifier) {
		DataSource dataSource = dataSourceMap().get(tenantIdentifier);
		return dataSource;
	}

	@Override
	public void injectServices(ServiceRegistryImplementor serviceRegistry) {
		Map<String, Object> settings = serviceRegistry.getService(ConfigurationService.class)
			.getSettings();
		var custom_tenant_id = settings.get("custom_tenant_id");
		String jdbcUrl = (String) settings.get(AvailableSettings.URL);
		if (jdbcUrl == null) {
			jdbcUrl = (String) settings.get(AvailableSettings.JAKARTA_JDBC_URL);
		}
		if (jdbcUrl == null) {
			throw new HibernateException("Could not find ds-tenantIdentifier");
		}
		DataSource ds = (DataSource) settings.get(AvailableSettings.DATASOURCE);
		//jdbc:mysql://localhost:3306/tenantdb-43
		tenantDb = jdbcUrl.substring(jdbcUrl.lastIndexOf("/") + 1);
		dataSourceMap().put(tenantDb, ds);

	}

	@Override
	public void stop() {
		if (dataSourceMap != null) {
			dataSourceMap.clear();
			dataSourceMap = null;
		}
	}

	private Map<String, DataSource> dataSourceMap() {
		if (dataSourceMap == null) {
			dataSourceMap = new ConcurrentHashMap<>();
		}
		return dataSourceMap;
	}
}
