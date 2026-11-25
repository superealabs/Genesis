package org.example.siteback.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;      
import org.springframework.data.jpa.domain.Specification;
import org.example.siteback.models.TContent;
import org.example.siteback.models.dto.TContentSearch;
import org.springframework.stereotype.Service;
import org.example.siteback.repositories.TContentRepository;
import java.util.Optional;
import org.example.siteback.services.interfaces.TContentService;
import org.example.siteback.specification.TContentSpecification;
import org.example.siteback.exception.ResourceNotFoundException;
import org.example.siteback.exception.InternalServerErrorException;
import org.example.siteback.utils.ExportUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;


@Service
public class DefaultTContentService implements TContentService {
	private final TContentRepository tContentRepository;

	public DefaultTContentService(TContentRepository tContentRepository) {
	   this.tContentRepository = tContentRepository;
	}

	@Override
	public String exportTContentToCSV(List<TContent> tContent) {
	   return ExportUtils.generateCsv(tContent);
	}  

	@Override
	public Page<TContent> getAllTContent(Pageable pageable) {
	    try {
	        return tContentRepository.findAll(pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving t content", ex);
	    }
	}

	@Override
	public Page<TContent> getAllTContent(Pageable pageable, TContentSearch object) {
	    try {
	        Specification<TContent> spec=TContentSpecification.filter(object);
	        return tContentRepository.findAll(spec, pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving t content", ex);
	    }
	}

	@Override
	public TContent getTContentById(Integer id) {
	    Optional<TContent> tContent = tContentRepository.findById(id);
	    if (tContent.isPresent()) {
	        return tContent.get();
	    } else {
	        throw new ResourceNotFoundException("TContent not found with id : " + id);
	    }
	}

	@Override
	public TContent createTContent(TContent tContent) {
	    try {
	        return tContentRepository.save(tContent);
	    } catch (DataIntegrityViolationException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while creating t content", ex);
	    }
	}

	@Override
	public TContent updateTContent(Integer id, TContent tContent) {
	    Optional<TContent> existingTContent = tContentRepository.findById(id);
	    if (existingTContent.isPresent()) {
	        tContent.setId(id);
	        try {
	            return tContentRepository.save(tContent);
	    } catch (DataIntegrityViolationException ex) {
	            throw ex;    
	    } catch (Exception ex) {
	            throw new InternalServerErrorException("Error while updating t content", ex);
	        }
	    } else {
	        throw new ResourceNotFoundException("TContent not found with id : " + id);
	    }
	}

	@Override
	public void deleteTContent(Integer id) {
	    try {
	        tContentRepository.deleteById(id);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while deleting t content", ex);
	    }
	}



}
