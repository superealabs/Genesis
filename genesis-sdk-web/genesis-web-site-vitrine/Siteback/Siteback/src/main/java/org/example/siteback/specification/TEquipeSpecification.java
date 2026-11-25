package org.example.siteback.specification;

import org.example.siteback.models.TEquipe;
import org.example.siteback.models.dto.TEquipeSearch;
import org.example.siteback.utils.WebUtils;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class TEquipeSpecification {

    public static Specification<TEquipe> filter(TEquipeSearch object) {
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
            
            System.out.println("🔍 TEMPLATE DEBUG: name=prenom | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("prenom"), object.getPrenom(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=poste | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("poste"), object.getPoste(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=email | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("email"), object.getEmail(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=photo | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("photo"), object.getPhoto(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=github | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("github"), object.getGithub(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=linkedin | columnType=[varchar] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("linkedin"), object.getLinkedin(), predicates);
            
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
