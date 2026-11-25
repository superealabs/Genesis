package org.example.siteback.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.siteback.models.TEquipe;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TEquipeRepository extends JpaRepository<TEquipe, Integer>, JpaSpecificationExecutor<TEquipe> {

}
