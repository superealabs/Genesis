package org.example.siteback.services.interfaces;

import org.example.siteback.models.TEquipe;
import org.example.siteback.models.dto.TEquipeSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;    
import java.util.List;


public interface TEquipeService {
    Page<TEquipe> getAllTEquipe(Pageable pageable);

    Page<TEquipe> getAllTEquipe(Pageable pageable, TEquipeSearch object);

    TEquipe getTEquipeById(Integer id);

    public String exportTEquipeToCSV(List<TEquipe> tEquipe);

    

    TEquipe createTEquipe(TEquipe tEquipe);

    TEquipe updateTEquipe(Integer id, TEquipe tEquipe);

    void deleteTEquipe(Integer id);
    

}
