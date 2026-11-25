package org.example.siteback.services.interfaces;

import org.example.siteback.models.TCharteGraphique;
import org.example.siteback.models.dto.TCharteGraphiqueSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;    
import java.util.List;


public interface TCharteGraphiqueService {
    Page<TCharteGraphique> getAllTCharteGraphique(Pageable pageable);

    Page<TCharteGraphique> getAllTCharteGraphique(Pageable pageable, TCharteGraphiqueSearch object);

    TCharteGraphique getTCharteGraphiqueById(Integer id);

    public String exportTCharteGraphiqueToCSV(List<TCharteGraphique> tCharteGraphique);

    

    TCharteGraphique createTCharteGraphique(TCharteGraphique tCharteGraphique);

    TCharteGraphique updateTCharteGraphique(Integer id, TCharteGraphique tCharteGraphique);

    void deleteTCharteGraphique(Integer id);
    

}
