// Logo d'après le nom d'un framework / d'une base (fichiers de public/assets/logos)
const LOGOS: [RegExp, string][] = [
  [/asp\.?net.*mvc|\.net.*mvc/i, 'aspnet'], [/asp|\.net/i, 'aspnet-core'], [/spring/i, 'spring'],
  [/django/i, 'django'], [/laravel/i, 'laravel'], [/nest/i, 'nestjs'], [/express/i, 'express'],
  [/react/i, 'react'], [/angular/i, 'angular'], [/vue/i, 'vuejs'],
  [/mysql/i, 'mysql'], [/postgre/i, 'postgresql'], [/oracle/i, 'oracle'], [/sql ?server/i, 'sql-server'], [/mongo/i, 'mongodb']
]
export function logoOf(name = '') {
  const hit = LOGOS.find(([re]) => re.test(name))
  return hit ? `/assets/logos/${hit[1]}.svg` : '/assets/img/genie-normal.svg'
}
