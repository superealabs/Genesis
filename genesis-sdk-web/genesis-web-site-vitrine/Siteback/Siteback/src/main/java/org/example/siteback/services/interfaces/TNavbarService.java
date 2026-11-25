package org.example.siteback.services.interfaces;

import org.example.siteback.models.TNavbar;
import org.example.siteback.models.dto.TNavbarSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;    
import java.util.List;


public interface TNavbarService {
    Page<TNavbar> getAllTNavbar(Pageable pageable);

    Page<TNavbar> getAllTNavbar(Pageable pageable, TNavbarSearch object);

    TNavbar getTNavbarById(Integer id);

    public String exportTNavbarToCSV(List<TNavbar> tNavbar);

    

    TNavbar createTNavbar(TNavbar tNavbar);

    TNavbar updateTNavbar(Integer id, TNavbar tNavbar);

    void deleteTNavbar(Integer id);
    

}
