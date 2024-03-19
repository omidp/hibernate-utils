package org.example.internal.id;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.example.internal.id.CustomIdGenerator;

import java.io.Serializable;

@MappedSuperclass
public abstract class AbstractEntity<ID extends Serializable> {

	@Id
	@CustomIdGenerator
	protected ID id;

	public ID getId(){
		return id;
	}

}