package org.example.siteback.services.interfaces;

import org.example.siteback.models.TContent;
import org.example.siteback.models.dto.TContentSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;    
import java.util.List;


public interface TContentService {
    Page<TContent> getAllTContent(Pageable pageable);

    Page<TContent> getAllTContent(Pageable pageable, TContentSearch object);

    TContent getTContentById(Integer id);

    public String exportTContentToCSV(List<TContent> tContent);

    

    TContent createTContent(TContent tContent);

    TContent updateTContent(Integer id, TContent tContent);

    void deleteTContent(Integer id);
    

}
