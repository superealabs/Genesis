from pathlib import Path
import re,json
root=Path(__file__).resolve().parents[2]
out=root/'benchmarks/template-engine/resources/benchmark'
source=(root/'genesis-core/src/main/resources/data_genesis/templates/Template 1/ListViewTemplate.templ').read_text()
model={key:'' for key in re.findall(r'\$\{(\w+)\}',source)}
model.update({
'className':'Product','isView':False,'viewAnnotations':'<!-- Reconstructed benchmark fixture: Product -->',
'createLink':'/products/create','filterLink':'action="/products/filter"',
'exportLink':'action="/products/export"','deleteLink':'action="/products/delete"',
'detailsLink':'/products/details','updateLink':'/products/edit',
'deleteDataTagHelper':'data-id="$[thymeleafDollar]{product.id}"',
'orderSortsTagHelper':'<span th:text="$[thymeleafDollar]{sortOrder}"></span>',
'pageSizeChangeLink':"'/products?pageSize=' + this.value",
'pageSizeTagHelper':'th:selected="$[thymeleafDollar]{pageSize == ${this}}"',
'inputRadioTagHelper':'th:value="$[thymeleafDollar]{searchCriteria.${this.name}}"',
'inputDateTagHelper':'name="${this.name}" th:value="$[thymeleafDollar]{searchCriteria.${this.name}}"',
'inputTagHelper':'name="${this.name}" th:value="$[thymeleafDollar]{searchCriteria.${this.name}}"',
'selectTagHelper':'name="${this.name}" id="${this.name}"',
'pageSizeValue':'20','currentSortValue':'name,asc',
'antiForgeryTokenTagHelper':'<input type="hidden" name="_csrf" th:value="$[thymeleafDollar]{_csrf.token}" />',
'blockLoopStatementStart':'<th:block th:each="product : $[thymeleafDollar]{products}">',
'blockLoopStatementEnd':'</th:block>',
'dataValue':'<span th:text="$[thymeleafDollar]{product.${this.name}}"></span>',
'dataForeignValue':'<span th:text="$[thymeleafDollar]{product.${this.name}.name}"></span>',
'dataRawValue':'product.${this.name}',
'sortLink':'href="/products?sort=${this.name}"',
'activeSortAscCondition':'sort-asc-${this.name}',
'activeSortDescCondition':'sort-desc-${this.name}',
'previousPageLink':'/products?page=0','nextPageLink':'/products?page=2',
'previousClassCondition':'','nextClassCondition':'',
'pagesListLoop':'<a href="/products?page=1">2</a>',
'onGoingPageLink':'/products','onGoingPagesLoop':'<option value="1">2</option>',
'totalElementsTagHelper':'42',
'scriptSection':'<script src="/js/products-list.js"></script>',
'pageSizesList':[10,20,50,100],
})
fields=[]
for name,typ,col in [('id','Long','bigint'),('name','String','varchar'),('price','BigDecimal','decimal'),('active','Boolean','boolean'),('createdAt','LocalDateTime','timestamp'),('category','Category','bigint'),('payload','String','json'),('attributes','String','jsonb'),('document','String','xml'),('attachment','byte[]','bytea')]:
 fields.append(dict(name=name,type=typ,columnType=col,isPrimaryKey=name=='id',isForeignKey=name=='category'))
model.pop('this',None)
model['fields']=fields
model['fieldsFK']=[fields[5]]
model['inputsFilter']=[dict(name='active',type='radio'),dict(name='createdAt',type='datetime-local'),dict(name='name',type='text'),dict(name='price',type='number')]
(out/'spring-list-model.json').write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n')
# Convertir le source complet ; les fragments imbriqués deviennent des expressions
# FreeMarker natives, sans appel à Genesis ni HTML prérendu.
def expr(s):
 s=s.replace('this.','item.')
 s=re.sub(r'majStart\(([^)]+)\)',r'\1?cap_first',s)
 return s

def interpolate(m):
 key=m.group(1)
 if key == 'this':
  return '[=item]'
 if key in model and isinstance(model[key],str) and '${' in model[key]:
  return re.sub(r'\$\{([^}]+)\}',interpolate,model[key]).replace('$[thymeleafDollar]','$')
 if key in model and isinstance(model[key],str) and '$[thymeleafDollar]' in model[key]:
  return '[='+expr(key)+"?replace('$[thymeleafDollar]', '$')]"
 return '[='+expr(key)+']'

def cond(s):
 s=s.replace('this.','item.')
 match=re.fullmatch(r'(.+?)\s*=\s*(.+)',s)
 if match:
  a,b=match.groups()
  return a+' == '+(b if b in ('true','false') else json.dumps(b))
 return s
ftl=re.sub(r'\$\{([^}]+)\}',interpolate,source)
ftl=re.sub(r'\{\{#each (.*?)\}\}',r'<#list \1 as item>',ftl).replace('{{/each}}','</#list>')
ftl=re.sub(r'\{\{#if (.*?)\}\}',lambda m:'<#if '+cond(m[1])+'>',ftl)
ftl=re.sub(r'\{\{elseIf (.*?)\}\}',lambda m:'<#elseif '+cond(m[1])+'>',ftl)
ftl=ftl.replace('{{else}}','<#else>').replace('{{/if}}','</#if>').replace('{{removeLine}}','')
# Conserver la syntaxe d’exécution littérale, distincte de la syntaxe FreeMarker [=...].
# Les fragments du modèle gèrent l’échappement du dollar Thymeleaf ci-dessus.
(out/'spring-list.ftl').write_text(ftl)
print('Reconstructed',len(fields),'fields,',len(ftl),'FreeMarker source characters')
