package org.example.siteback.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;      
import org.springframework.data.jpa.domain.Specification;
import org.example.siteback.models.TNavbar;
import org.example.siteback.models.dto.TNavbarSearch;
import org.springframework.stereotype.Service;
import org.example.siteback.repositories.TNavbarRepository;
import java.util.Optional;
import org.example.siteback.services.interfaces.TNavbarService;
import org.example.siteback.specification.TNavbarSpecification;
import org.example.siteback.exception.ResourceNotFoundException;
import org.example.siteback.exception.InternalServerErrorException;
import org.example.siteback.utils.ExportUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;


@Service
public class DefaultTNavbarService implements TNavbarService {
	private final TNavbarRepository tNavbarRepository;

	public DefaultTNavbarService(TNavbarRepository tNavbarRepository) {
	   this.tNavbarRepository = tNavbarRepository;
	}

	@Override
	public String exportTNavbarToCSV(List<TNavbar> tNavbar) {
	   return ExportUtils.generateCsv(tNavbar);
	}  

	@Override
	public Page<TNavbar> getAllTNavbar(Pageable pageable) {
	    try {
	        return tNavbarRepository.findAll(pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving t navbar", ex);
	    }
	}

	@Override
	public Page<TNavbar> getAllTNavbar(Pageable pageable, TNavbarSearch object) {
	    try {
	        Specification<TNavbar> spec=TNavbarSpecification.filter(object);
	        return tNavbarRepository.findAll(spec, pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving t navbar", ex);
	    }
	}

	@Override
	public TNavbar getTNavbarById(Integer id) {
	    Optional<TNavbar> tNavbar = tNavbarRepository.findById(id);
	    if (tNavbar.isPresent()) {
	        return tNavbar.get();
	    } else {
	        throw new ResourceNotFoundException("TNavbar not found with id : " + id);
	    }
	}

	@Override
	public TNavbar createTNavbar(TNavbar tNavbar) {
	    try {
	        return tNavbarRepository.save(tNavbar);
	    } catch (DataIntegrityViolationException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while creating t navbar", ex);
	    }
	}

	@Override
	public TNavbar updateTNavbar(Integer id, TNavbar tNavbar) {
	    Optional<TNavbar> existingTNavbar = tNavbarRepository.findById(id);
	    if (existingTNavbar.isPresent()) {
	        tNavbar.setId(id);
	        try {
	            return tNavbarRepository.save(tNavbar);
	    } catch (DataIntegrityViolationException ex) {
	            throw ex;    
	    } catch (Exception ex) {
	            throw new InternalServerErrorException("Error while updating t navbar", ex);
	        }
	    } else {
	        throw new ResourceNotFoundException("TNavbar not found with id : " + id);
	    }
	}

	@Override
	public void deleteTNavbar(Integer id) {
	    try {
	        tNavbarRepository.deleteById(id);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while deleting t navbar", ex);
	    }
	}



}
