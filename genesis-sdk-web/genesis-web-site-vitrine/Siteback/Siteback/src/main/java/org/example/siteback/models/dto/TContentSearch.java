package org.example.siteback.models.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.siteback.models.TNavbar;



@Getter @Setter
public class TContentSearch {

	private Integer id;

	private String nom;
	private String route;
	private String fichier;
	private Integer ordre;
	private Integer ordreMin;
	private Integer ordreMax;

	private TNavbar navbaridTNavbar;
	
	
}
