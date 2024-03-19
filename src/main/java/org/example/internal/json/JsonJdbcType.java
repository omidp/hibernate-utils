package org.example.internal.json;

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;
import org.hibernate.type.descriptor.jdbc.BasicExtractor;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JsonJdbcType implements JdbcType {

	public static final JsonJdbcType INSTANCE = new JsonJdbcType();

	@Override
	public int getJdbcTypeCode() {
		return Types.VARCHAR;
	}

	@Override
	public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
		return new BasicBinder<X>(javaType, this) {
			@Override
			protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
				String jsonValue = (String) getJavaType().wrap(value, options);
				st.setString(index, jsonValue);
			}

			@Override
			protected void doBind(CallableStatement st, X value, String name, WrapperOptions options) throws SQLException {
				String jsonValue = (String) getJavaType().wrap(value, options);
				st.setString(name, jsonValue);
			}
		};
	}


	@Override
	public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
		return new BasicExtractor<X>(javaType, this) {
			@Override
			protected X doExtract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
				return fromString(rs.getString(paramIndex), (JsonJavaType) getJavaType(), options);
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
				return fromString(statement.getString(index), (JsonJavaType) getJavaType(), options);
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
				return fromString(statement.getString(name), (JsonJavaType) getJavaType(), options);
			}

		};
	}

	protected <X> X fromString(String string, JsonJavaType javaType, WrapperOptions options) throws SQLException {
		if (string == null) {
			return null;
		}

		return (X) options.getSessionFactory().getFastSessionServices().getJsonFormatMapper().fromString(
			string,
			javaType,
			options
		);
	}

}
