package org.example.siteback.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="t_navbar")
public class TNavbar  {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique = true, nullable = false)
	private Integer id;
	@Column(name="nom", unique = true, nullable = false)
	@NotNull(message = "Nom cannot be null")
	@Size(max = 100)
	private String nom;
	@Column(name="ordre", unique = false, nullable = false)
	@NotNull(message = "Ordre cannot be null")
	private Integer ordre;
	

}
