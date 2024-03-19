package org.example.internal.id;

import java.io.Serializable;

public interface TransientEntity<ID extends Serializable> {

	void setId(ID id);

}
