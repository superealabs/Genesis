package org.example.siteback.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="t_equipe")
public class TEquipe  {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique = true, nullable = false)
	private Integer id;
	@Column(name="nom", unique = false, nullable = false)
	@NotNull(message = "Nom cannot be null")
	@Size(max = 100)
	private String nom;
	@Column(name="prenom", unique = false, nullable = true)
	@Size(max = 100)
	private String prenom;
	@Column(name="poste", unique = false, nullable = true)
	@Size(max = 100)
	private String poste;
	@Column(name="email", unique = true, nullable = true)
	@Size(max = 150)
	private String email;
	@Column(name="photo", unique = false, nullable = true)
	@Size(max = 255)
	private String photo;
	@Column(name="github", unique = false, nullable = true)
	@Size(max = 255)
	private String github;
	@Column(name="linkedin", unique = false, nullable = true)
	@Size(max = 255)
	private String linkedin;
	

}
