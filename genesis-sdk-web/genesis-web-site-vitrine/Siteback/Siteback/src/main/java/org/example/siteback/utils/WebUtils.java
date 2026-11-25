package org.example.siteback.utils;

import org.springframework.data.domain.Sort;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

public class WebUtils {
    public static final String ASC = "asc";
    public static final String DESC = "desc";
    public static final String SORT_CRITERIA_DELIMITER = ";";
    public static final String SORT_PARTS_DELIMITER = ",";

    public static Sort createSortObject(String sortParam) {
        try {
            String[] sortCriteria = sortParam.split(SORT_CRITERIA_DELIMITER);

            List<Sort.Order> orders = Arrays.stream(sortCriteria)
                    .map(criteria -> {
                        String[] parts = criteria.split(SORT_PARTS_DELIMITER);
                        if (parts.length != 2) {
                            throw new IllegalArgumentException(
                                "Each sort criteria must have exactly 2 parts: property and direction"
                            );
                        }

                        String property = parts[0].trim();
                        String direction = parts[1].trim().toLowerCase();

                        if (!direction.equals(ASC) && !direction.equals(DESC)) {
                            throw new IllegalArgumentException(
                                "Sort direction must be either '"+ASC+"' or '"+DESC+"'"
                            );
                        }

                        return new Sort.Order(
                                  Sort.Direction.fromString(direction),
                                  property
                        );
                    })
                    .collect(Collectors.toList());

            return Sort.by(orders);

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    public static void addLikeIfPresent(jakarta.persistence.criteria.CriteriaBuilder cb,
                                 jakarta.persistence.criteria.Path<String> field,
                                 String value,
                                 List<Predicate> predicates) {
          if (value != null && !value.isBlank()) {
              predicates.add(cb.like(cb.lower(field), "%" + value.toLowerCase() + "%"));
          }
    }

    public static <T extends Comparable<? super T>> void addInterval(
      jakarta.persistence.criteria.CriteriaBuilder cb,
      jakarta.persistence.criteria.Path<T> field,
      T min, T max,
      List<Predicate> predicates) {
      
      if (min != null) {
        predicates.add(cb.greaterThanOrEqualTo(field, min));
      }
      if (max != null) {
        predicates.add(cb.lessThanOrEqualTo(field, max));
      }
    }
}
