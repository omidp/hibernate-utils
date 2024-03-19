package org.example.internal.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;


public final class CustomJacksonJsonFormatMapper implements FormatMapper {

	public static final String SHORT_NAME = "jackson";

	private final ObjectMapper objectMapper;

	public CustomJacksonJsonFormatMapper() {
		this(new ObjectMapper());
	}

	public CustomJacksonJsonFormatMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
		try {
			if (javaType instanceof JsonJavaType jsonJavaType){
				return objectMapper.readValue( charSequence.toString(), objectMapper.constructType(jsonJavaType.getPropertyType()));
			}
			return objectMapper.readValue( charSequence.toString(), objectMapper.constructType( javaType.getJavaType() ) );
		}
		catch (JsonProcessingException e) {
			throw new IllegalArgumentException( "Could not deserialize string to java type: " + javaType, e );
		}
	}

	@Override
	public <T> String toString(T value, JavaType<T> javaType, WrapperOptions wrapperOptions) {
		try {
			return objectMapper.writerFor( objectMapper.constructType( javaType.getJavaType() ) )
					.writeValueAsString( value );
		}
		catch (JsonProcessingException e) {
			throw new IllegalArgumentException( "Could not serialize object of java type: " + javaType, e );
		}
	}
}