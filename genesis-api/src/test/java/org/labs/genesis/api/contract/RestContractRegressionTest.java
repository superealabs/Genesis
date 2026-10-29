package org.labs.genesis.api.contract;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RestContractRegressionTest {
 @Autowired ApplicationContext context;
 static ObjectMapper json=new ObjectMapper();
 static Object shape(JavaType type,int depth,Set<String> ancestors){
  Class<?> raw=type.getRawClass();String name=raw.getName();
  if(depth>7)return "depth-limit";
  if(raw.isArray()||Collection.class.isAssignableFrom(raw))return Map.of("array",shape(type.getContentType()==null?json.constructType(Object.class):type.getContentType(),depth+1,ancestors));
  if(Map.class.isAssignableFrom(raw))return Map.of("map",shape(type.getContentType()==null?json.constructType(Object.class):type.getContentType(),depth+1,ancestors));
  if(raw==org.springframework.http.ResponseEntity.class)return Map.of("responseEntity",shape(type.containedType(0),depth+1,ancestors));
  if(raw.isEnum())return Map.of("enum",Arrays.stream(raw.getEnumConstants()).map(Object::toString).toList());
  if(raw.isPrimitive()||name.startsWith("java.")||name.startsWith("org.springframework."))return name;
  if(ancestors.contains(name))return "recursive";
  var seen=new HashSet<>(ancestors);seen.add(name);var props=new TreeMap<String,Object>();
  for(var p:json.getSerializationConfig().introspect(type).findProperties())if(p.couldSerialize())props.put(p.getName(),shape(p.getPrimaryType(),depth+1,seen));
  var inputs=new TreeMap<String,Object>();for(var p:json.getDeserializationConfig().introspect(type).findProperties())if(p.couldDeserialize())inputs.put(p.getName(),shape(p.getPrimaryType(),depth+1,seen));
  return Map.of("outputProperties",props,"inputProperties",inputs);
 }

 @Test
 void preservesAllMappingsParametersAndJsonWireShapes() throws Exception {
   var mapping=context.getBean(RequestMappingHandlerMapping.class);var routes=new ArrayList<Map<String,Object>>();
   for(var entry:mapping.getHandlerMethods().entrySet()){
    var handler=entry.getValue();if(!handler.getBeanType().getName().startsWith("org.labs.genesis.api."))continue;
    var info=entry.getKey();var method=handler.getMethod();
    for(var path:info.getPatternValues())for(var verb:info.getMethodsCondition().getMethods()){
     var row=new LinkedHashMap<String,Object>();row.put("path",path);row.put("http",verb.name());row.put("javaClass",handler.getBeanType().getName());row.put("javaMethod",method.getName());row.put("javaReturnType",method.getGenericReturnType().getTypeName());row.put("responseShape",shape(json.constructType(method.getGenericReturnType()),0,Set.of()));row.put("consumes",info.getConsumesCondition().getConsumableMediaTypes().stream().map(Object::toString).sorted().toList());row.put("produces",info.getProducesCondition().getProducibleMediaTypes().stream().map(Object::toString).sorted().toList());row.put("conditions",info.getParamsCondition().toString());
     var params=new ArrayList<Object>();for(var p:method.getParameters())params.add(Map.of("annotations",Arrays.stream(p.getAnnotations()).map(Object::toString).sorted().toList(),"javaType",p.getParameterizedType().getTypeName(),"shape",shape(json.constructType(p.getParameterizedType()),0,Set.of())));row.put("parameters",params);routes.add(row);
    }
   }
   routes.sort(Comparator.comparing(r->r.get("http")+" "+r.get("path")));

  JsonNode actual=json.valueToTree(routes);
  JsonNode expected;
  try(var stream=getClass().getResourceAsStream("/contracts/rest-contracts-before.json")) {
   expected=json.readTree(stream).get("routes");
  }
  normalize(actual); normalize(expected);
  assertThat(actual).isEqualTo(expected);
 }
 private void normalize(JsonNode routes) {
  for(JsonNode row:routes) {
   var object=(com.fasterxml.jackson.databind.node.ObjectNode)row;
   object.remove(List.of("javaClass","javaMethod","javaReturnType"));
   for(JsonNode p:row.get("parameters"))((com.fasterxml.jackson.databind.node.ObjectNode)p).remove("javaType");
  }
 }
}
