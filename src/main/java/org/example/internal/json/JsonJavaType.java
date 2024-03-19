package org.example.internal.json;

import org.example.internal.util.ReflectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.annotations.common.reflection.java.JavaXMember;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.usertype.DynamicParameterizedType;

import java.lang.reflect.Method;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static org.hibernate.usertype.DynamicParameterizedType.PARAMETER_TYPE;

public class JsonJavaType extends AbstractClassJavaType<Object> {

	private Type propertyType;
	private Class propertyClass;
	private static List<Class> validatedTypes = new ArrayList<>();


	protected JsonJavaType() {
		super(Object.class);
	}

	@Override
	public <X> X unwrap(Object value, Class<X> type, WrapperOptions options) {
		if (value == null) {
			return null;
		}

		throw unknownUnwrap(type);
	}

	@Override
	public <X> Object wrap(X value, WrapperOptions options) {
		if (value == null) {
			return null;
		}
		return options.getSessionFactory().getFastSessionServices().getJsonFormatMapper().toString(value, this, options);
	}


	@Override
	public JdbcType getRecommendedJdbcType(JdbcTypeIndicators context) {
		return context.getJdbcType(Types.VARCHAR);
	}

	public void setParameterValues(Properties parameters) {
		final XProperty xProperty = (XProperty) parameters.get(DynamicParameterizedType.XPROPERTY);
		Type type = null;
		if(xProperty instanceof JavaXMember) {
			type = ((JavaXMember) xProperty).getJavaType();
		} else {
			Object parameterType = parameters.get(PARAMETER_TYPE);
			if(parameterType instanceof DynamicParameterizedType.ParameterType) {
				type = ((DynamicParameterizedType.ParameterType) parameterType).getReturnedClass();
			} else if(parameterType instanceof String) {
				type = ReflectionUtils.getClass((String) parameterType);
			}
		}
		if(type == null) {
			throw new HibernateException("Could not resolve property type!");
		}
		setPropertyClass(type);
	}

	private void setPropertyClass(Type type) {
		this.propertyType = type;
		if (type instanceof ParameterizedType) {
			type = ((ParameterizedType) type).getRawType();
		} else if (type instanceof TypeVariable) {
			type = ((TypeVariable) type).getGenericDeclaration().getClass();
		}
		this.propertyClass = (Class) type;
		validatePropertyType();
	}

	private void validatePropertyType() {
		if(Collection.class.isAssignableFrom(propertyClass)) {
			if (propertyType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) propertyType;

				for(Class genericType : ReflectionUtils.getGenericTypes(parameterizedType)) {
					if(validatedTypes.contains(genericType)) {
						continue;
					}
					validatedTypes.add(genericType);
					Method equalsMethod = ReflectionUtils.getMethodOrNull(genericType, "equals", Object.class);
					Method hashCodeMethod = ReflectionUtils.getMethodOrNull(genericType, "hashCode");

					if(equalsMethod == null ||
						hashCodeMethod == null ||
						Object.class.equals(equalsMethod.getDeclaringClass()) ||
						Object.class.equals(hashCodeMethod.getDeclaringClass())) {
					}
				}
			}
		}
	}

	public Type getPropertyType(){
		return propertyType;
	}

}
