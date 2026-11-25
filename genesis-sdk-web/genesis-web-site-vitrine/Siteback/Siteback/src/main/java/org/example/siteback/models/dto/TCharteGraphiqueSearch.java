package org.example.siteback.models.dto;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class TCharteGraphiqueSearch {

	private Integer id;

	private String file;
	private String version;
	private java.time.LocalDate dateAjout;
	private java.time.LocalDate dateAjoutMin;
	private java.time.LocalDate dateAjoutMax;

	
}
