package org.example.siteback.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;      
import org.springframework.data.jpa.domain.Specification;
import org.example.siteback.models.TCharteGraphique;
import org.example.siteback.models.dto.TCharteGraphiqueSearch;
import org.springframework.stereotype.Service;
import org.example.siteback.repositories.TCharteGraphiqueRepository;
import java.util.Optional;
import org.example.siteback.services.interfaces.TCharteGraphiqueService;
import org.example.siteback.specification.TCharteGraphiqueSpecification;
import org.example.siteback.exception.ResourceNotFoundException;
import org.example.siteback.exception.InternalServerErrorException;
import org.example.siteback.utils.ExportUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;


@Service
public class DefaultTCharteGraphiqueService implements TCharteGraphiqueService {
	private final TCharteGraphiqueRepository tCharteGraphiqueRepository;

	public DefaultTCharteGraphiqueService(TCharteGraphiqueRepository tCharteGraphiqueRepository) {
	   this.tCharteGraphiqueRepository = tCharteGraphiqueRepository;
	}

	@Override
	public String exportTCharteGraphiqueToCSV(List<TCharteGraphique> tCharteGraphique) {
	   return ExportUtils.generateCsv(tCharteGraphique);
	}  

	@Override
	public Page<TCharteGraphique> getAllTCharteGraphique(Pageable pageable) {
	    try {
	        return tCharteGraphiqueRepository.findAll(pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving t charte graphique", ex);
	    }
	}

	@Override
	public Page<TCharteGraphique> getAllTCharteGraphique(Pageable pageable, TCharteGraphiqueSearch object) {
	    try {
	        Specification<TCharteGraphique> spec=TCharteGraphiqueSpecification.filter(object);
	        return tCharteGraphiqueRepository.findAll(spec, pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving t charte graphique", ex);
	    }
	}

	@Override
	public TCharteGraphique getTCharteGraphiqueById(Integer id) {
	    Optional<TCharteGraphique> tCharteGraphique = tCharteGraphiqueRepository.findById(id);
	    if (tCharteGraphique.isPresent()) {
	        return tCharteGraphique.get();
	    } else {
	        throw new ResourceNotFoundException("TCharteGraphique not found with id : " + id);
	    }
	}

	@Override
	public TCharteGraphique createTCharteGraphique(TCharteGraphique tCharteGraphique) {
	    try {
	        return tCharteGraphiqueRepository.save(tCharteGraphique);
	    } catch (DataIntegrityViolationException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while creating t charte graphique", ex);
	    }
	}

	@Override
	public TCharteGraphique updateTCharteGraphique(Integer id, TCharteGraphique tCharteGraphique) {
	    Optional<TCharteGraphique> existingTCharteGraphique = tCharteGraphiqueRepository.findById(id);
	    if (existingTCharteGraphique.isPresent()) {
	        tCharteGraphique.setId(id);
	        try {
	            return tCharteGraphiqueRepository.save(tCharteGraphique);
	    } catch (DataIntegrityViolationException ex) {
	            throw ex;    
	    } catch (Exception ex) {
	            throw new InternalServerErrorException("Error while updating t charte graphique", ex);
	        }
	    } else {
	        throw new ResourceNotFoundException("TCharteGraphique not found with id : " + id);
	    }
	}

	@Override
	public void deleteTCharteGraphique(Integer id) {
	    try {
	        tCharteGraphiqueRepository.deleteById(id);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while deleting t charte graphique", ex);
	    }
	}



}
