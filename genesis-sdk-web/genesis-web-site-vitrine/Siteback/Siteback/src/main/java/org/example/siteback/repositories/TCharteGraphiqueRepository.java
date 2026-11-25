package org.example.siteback.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.siteback.models.TCharteGraphique;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TCharteGraphiqueRepository extends JpaRepository<TCharteGraphique, Integer>, JpaSpecificationExecutor<TCharteGraphique> {

}
