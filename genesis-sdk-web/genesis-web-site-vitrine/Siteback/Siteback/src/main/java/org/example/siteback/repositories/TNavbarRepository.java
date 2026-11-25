package org.example.siteback.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.siteback.models.TNavbar;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TNavbarRepository extends JpaRepository<TNavbar, Integer>, JpaSpecificationExecutor<TNavbar> {

}
