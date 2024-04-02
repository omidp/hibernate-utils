package org.example.multitenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class ThreadCurrentTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

	@Override
	public String resolveCurrentTenantIdentifier() {
		return TenantDbContext.getTenantDb();
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}
}
