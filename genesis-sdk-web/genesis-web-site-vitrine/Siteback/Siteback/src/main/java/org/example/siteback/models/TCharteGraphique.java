package org.example.siteback.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="t_charte_graphique")
public class TCharteGraphique  {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique = true, nullable = false)
	private Integer id;
	@Column(name="file", unique = false, nullable = false)
	@NotNull(message = "File cannot be null")
	@Size(max = 255)
	private String file;
	@Column(name="version", unique = false, nullable = true)
	@Size(max = 20)
	private String version;
	@Column(name="date_ajout", unique = false, nullable = true)
	private java.time.LocalDate dateAjout;
	

}
