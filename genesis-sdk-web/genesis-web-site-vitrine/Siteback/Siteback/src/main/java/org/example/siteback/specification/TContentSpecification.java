package org.example.siteback.specification;

import org.example.siteback.models.TContent;
import org.example.siteback.models.dto.TContentSearch;
import org.example.siteback.utils.WebUtils;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class TContentSpecification {

    public static Specification<TContent> filter(TContentSearch object) {
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
            
            System.out.println("🔍 TEMPLATE DEBUG: name=route | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("route"), object.getRoute(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=fichier | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("fichier"), object.getFichier(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=ordre | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getOrdre()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("ordre"), object.getOrdre()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("ordre"), object.getOrdreMin(), object.getOrdreMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=navbaridTNavbar | columnType=[integer] | isText=false | isNumeric=true");

            
            if(object.getNavbaridTNavbar()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("navbaridTNavbar"), object.getNavbaridTNavbar()
                    )
                );
            }
            
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
