package org.example.siteback.specification;

import org.example.siteback.models.TCharteGraphique;
import org.example.siteback.models.dto.TCharteGraphiqueSearch;
import org.example.siteback.utils.WebUtils;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class TCharteGraphiqueSpecification {

    public static Specification<TCharteGraphique> filter(TCharteGraphiqueSearch object) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            System.out.println("🔍 TEMPLATE DEBUG: name=id | columnType=[serial] | isText=false | isNumeric=true");

            
            if(object.getId()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("id"), object.getId()
                    )
                );
            }
            
            System.out.println("🔍 TEMPLATE DEBUG: name=file | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("file"), object.getFile(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=version | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("version"), object.getVersion(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=dateAjout | columnType=[date] | isText=false | isNumeric=false");

            
            if(object.getDateAjout()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("dateAjout"), object.getDateAjout()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("dateAjout"), object.getDateAjoutMin(), object.getDateAjoutMax(), predicates);
            
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
