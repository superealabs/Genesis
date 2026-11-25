package org.example.siteback.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;      
import org.springframework.data.jpa.domain.Specification;
import org.example.siteback.models.TEquipe;
import org.example.siteback.models.dto.TEquipeSearch;
import org.springframework.stereotype.Service;
import org.example.siteback.repositories.TEquipeRepository;
import java.util.Optional;
import org.example.siteback.services.interfaces.TEquipeService;
import org.example.siteback.specification.TEquipeSpecification;
import org.example.siteback.exception.ResourceNotFoundException;
import org.example.siteback.exception.InternalServerErrorException;
import org.example.siteback.utils.ExportUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;


@Service
public class DefaultTEquipeService implements TEquipeService {
	private final TEquipeRepository tEquipeRepository;

	public DefaultTEquipeService(TEquipeRepository tEquipeRepository) {
	   this.tEquipeRepository = tEquipeRepository;
	}

	@Override
	public String exportTEquipeToCSV(List<TEquipe> tEquipe) {
	   return ExportUtils.generateCsv(tEquipe);
	}  

	@Override
	public Page<TEquipe> getAllTEquipe(Pageable pageable) {
	    try {
	        return tEquipeRepository.findAll(pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving t equipe", ex);
	    }
	}

	@Override
	public Page<TEquipe> getAllTEquipe(Pageable pageable, TEquipeSearch object) {
	    try {
	        Specification<TEquipe> spec=TEquipeSpecification.filter(object);
	        return tEquipeRepository.findAll(spec, pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving t equipe", ex);
	    }
	}

	@Override
	public TEquipe getTEquipeById(Integer id) {
	    Optional<TEquipe> tEquipe = tEquipeRepository.findById(id);
	    if (tEquipe.isPresent()) {
	        return tEquipe.get();
	    } else {
	        throw new ResourceNotFoundException("TEquipe not found with id : " + id);
	    }
	}

	@Override
	public TEquipe createTEquipe(TEquipe tEquipe) {
	    try {
	        return tEquipeRepository.save(tEquipe);
	    } catch (DataIntegrityViolationException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while creating t equipe", ex);
	    }
	}

	@Override
	public TEquipe updateTEquipe(Integer id, TEquipe tEquipe) {
	    Optional<TEquipe> existingTEquipe = tEquipeRepository.findById(id);
	    if (existingTEquipe.isPresent()) {
	        tEquipe.setId(id);
	        try {
	            return tEquipeRepository.save(tEquipe);
	    } catch (DataIntegrityViolationException ex) {
	            throw ex;    
	    } catch (Exception ex) {
	            throw new InternalServerErrorException("Error while updating t equipe", ex);
	        }
	    } else {
	        throw new ResourceNotFoundException("TEquipe not found with id : " + id);
	    }
	}

	@Override
	public void deleteTEquipe(Integer id) {
	    try {
	        tEquipeRepository.deleteById(id);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while deleting t equipe", ex);
	    }
	}



}
