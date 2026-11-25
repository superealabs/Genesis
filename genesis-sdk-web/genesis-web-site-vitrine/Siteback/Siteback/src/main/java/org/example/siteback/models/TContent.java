package org.example.siteback.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="t_content")
public class TContent  {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique = true, nullable = false)
	private Integer id;
	@Column(name="nom", unique = false, nullable = false)
	@NotNull(message = "Nom cannot be null")
	@Size(max = 150)
	private String nom;
	@Column(name="route", unique = true, nullable = false)
	@NotNull(message = "Route cannot be null")
	@Size(max = 255)
	private String route;
	@Column(name="fichier", unique = false, nullable = true)
	@Size(max = 255)
	private String fichier;
	@Column(name="ordre", unique = false, nullable = false)
	@NotNull(message = "Ordre cannot be null")
	private Integer ordre;
	@ManyToOne
	@JoinColumn(name="navbar_id")
	@NotNull(message = "NavbarId cannot be null")
	private TNavbar navbaridTNavbar;
	

}
