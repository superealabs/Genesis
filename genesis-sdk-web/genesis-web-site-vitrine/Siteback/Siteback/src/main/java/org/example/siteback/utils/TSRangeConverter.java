package org.example.siteback.utils;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TSRangeConverter implements AttributeConverter<LocalDateTime[], String>{
  @Override
  public String convertToDatabaseColumn(LocalDateTime[] attribute) {
    String range="[\"%s\",\"%s\"]";
    return String.format(range, attribute[0].toString().replace("T", " "), attribute[1].toString().replace("T", " "));
  }

  @Override
  public LocalDateTime[] convertToEntityAttribute(String dbData) {
    String[] range=dbData.substring(1, dbData.length()-1).split(",");
    LocalDateTime[] ranges={
      LocalDateTime.parse(range[0].replace(" ", "T").substring(1, range[0].length()-1)),
      LocalDateTime.parse(range[1].replace(" ", "T").substring(1, range[1].length()-1))
    };
    return ranges;
  }
}
