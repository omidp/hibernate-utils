package org.example.domain.json;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.example.internal.json.JsonType;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "book")
@Data
public class BookEntity {

	@Id
	private UUID id;

	@Column(name = "name")
	private String name;

	@Type(JsonType.class)
	private List<Author> authors = new ArrayList<>();

	@Type(JsonType.class)
	private Publisher publisher;

}
