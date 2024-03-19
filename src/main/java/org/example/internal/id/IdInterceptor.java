package org.example.internal.id;

import org.hibernate.Interceptor;

import java.io.Serializable;
import org.hibernate.type.Type;

public final class IdInterceptor implements Interceptor, Serializable {

	public static final Interceptor INSTANCE = new IdInterceptor();

	private IdInterceptor() {
	}

	@Override
	public Boolean isTransient(Object entity) {
		if(entity instanceof TransientEntity){
			return true;
		}
		return null;
	}

	@Override
	public boolean onLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		return false;
	}

	@Override
	public boolean onFlushDirty(
			Object entity,
			Object id,
			Object[] currentState,
			Object[] previousState,
			String[] propertyNames,
			Type[] types) {
		return false;
	}

	@Override
	public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		return false;
	}

	@Override
	public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
	}

	@Override
	public int[] findDirty(
			Object entity,
			Object id,
			Object[] currentState,
			Object[] previousState,
			String[] propertyNames,
			Type[] types) {
		return null;
	}

	@Override
	public Object getEntity(String entityName, Object id) {
		return null;
	}

}