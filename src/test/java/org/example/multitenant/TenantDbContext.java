package org.example.multitenant;

import java.util.Deque;
import java.util.LinkedList;

public class TenantDbContext {
	private static ThreadLocal<Deque<String>> currentTenantDbId = new ThreadLocal<>();

	public static String getTenantDb() {
		if (currentTenantDbId.get() == null) {
			throw new IllegalStateException("Tenant not set");
		}
		String dbId = currentTenantDbId.get().peek();
		if (dbId == null) {
			throw new IllegalStateException("Tenant not set");
		}
		return dbId;
	}

	public static void pushTenantDb(String dbId) {
		Deque<String> dbIdStack = currentTenantDbId.get();
		if (dbIdStack == null) {
			dbIdStack = new LinkedList<>();
			currentTenantDbId.set(dbIdStack);
		}
		dbIdStack.push(dbId);
	}

	public static void popTenantDb() {
		Deque<String> dbIdStack = currentTenantDbId.get();
		if (dbIdStack == null) {
			throw new IllegalStateException("TenantDb not found");
		}
		dbIdStack.pop();
	}

	public static void reset() {
		currentTenantDbId.remove();
	}
}
