package org.example.internal.id;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.OnExecutionGenerator;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;
import org.hibernate.id.factory.spi.StandardGenerator;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.type.descriptor.java.UUIDJavaType;

import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.UUID;

public class CustomIdentifierGenerator implements IdentifierGenerator, OnExecutionGenerator, StandardGenerator {

	private UUIDJavaType.ValueTransformer valueTransformer;
	private IdentityGenerator identityGenerator;
	private boolean generatedOnExecution = true;
	private String entityName;

	public CustomIdentifierGenerator(CustomIdGenerator config, Member member,
									 CustomIdGeneratorCreationContext context) {
		this.identityGenerator = new IdentityGenerator();
		String returnedClassName = context.getProperty().getReturnedClassName();
		this.entityName = context.getRootClass().getEntityName();
		if (returnedClassName.equals(UUID.class.getName())) {
			valueTransformer = UUIDJavaType.PassThroughTransformer.INSTANCE;
			this.generatedOnExecution = false;
		}
	}


	@Override
	public boolean referenceColumnsInSql(Dialect dialect) {
		return this.identityGenerator.referenceColumnsInSql(dialect);
	}

	@Override
	public boolean writePropertyValue() {
		return this.identityGenerator.writePropertyValue();
	}

	@Override
	public String[] getReferencedColumnValues(Dialect dialect) {
		return this.identityGenerator.getReferencedColumnValues(dialect);
	}

	@Override
	public EnumSet<EventType> getEventTypes() {
		return EventTypeSets.INSERT_ONLY;
	}

	@Override
	public InsertGeneratedIdentifierDelegate getGeneratedIdentifierDelegate(PostInsertIdentityPersister persister) {
		return this.identityGenerator.getGeneratedIdentifierDelegate(persister);
	}

	@Override
	public Object generate(SharedSessionContractImplementor session, Object object) {
		final Object id = session.getEntityPersister(entityName, object).getIdentifier(object, session);
		if (id != null) {
			return id;
		}
		return valueTransformer.transform(UUID.randomUUID());
	}

	@Override
	public boolean generatedOnExecution() {
		return generatedOnExecution;
	}

}