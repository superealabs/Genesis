package org.example.siteback.specification;

import org.example.siteback.models.TNavbar;
import org.example.siteback.models.dto.TNavbarSearch;
import org.example.siteback.utils.WebUtils;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class TNavbarSpecification {

    public static Specification<TNavbar> filter(TNavbarSearch object) {
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
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nom | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("nom"), object.getNom(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=ordre | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getOrdre()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("ordre"), object.getOrdre()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("ordre"), object.getOrdreMin(), object.getOrdreMax(), predicates);
            
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
