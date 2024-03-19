package org.example.internal.json;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JsonType implements UserType<Object>, DynamicParameterizedType {

	private final JdbcType jdbcType;
	private JsonJavaType javaType;

	public JsonType() {
		this.jdbcType = JsonJdbcType.INSTANCE;
	}

	@Override
	public int getSqlType() {
		return jdbcType.getJdbcTypeCode();
	}

	@Override
	public Class<Object> returnedClass() {
		return Object.class;
	}

	@Override
	public boolean equals(Object x, Object y) {
		return javaType.areEqual(x, y);
	}

	@Override
	public int hashCode(Object x) {
		return javaType.extractHashCode(x);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
		return jdbcType.getExtractor(javaType).extract(rs, position, session);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws SQLException {
		jdbcType.getBinder(javaType).bind(st, value, index, session);
	}

	@Override
	public Object deepCopy(Object value) {
		return javaType.getMutabilityPlan().deepCopy(value);
	}

	@Override
	public boolean isMutable() {
		return javaType.getMutabilityPlan().isMutable();
	}

	@Override
	public Serializable disassemble(Object value) {
		return javaType.getMutabilityPlan().disassemble(value, null);
	}

	@Override
	public Object assemble(Serializable cached, Object owner) {
		return javaType.getMutabilityPlan().assemble(cached, null);
	}

	@Override
	public void setParameterValues(Properties parameters) {
		this.javaType = new JsonJavaType();
		javaType.setParameterValues(parameters);
	}
}
