package org.example.siteback.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.siteback.models.TContent;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import org.example.siteback.models.TNavbar;



public interface TContentRepository extends JpaRepository<TContent, Integer>, JpaSpecificationExecutor<TContent> {

List<TContent> findByNavbaridTNavbar(TNavbar navbaridTNavbar);



}
