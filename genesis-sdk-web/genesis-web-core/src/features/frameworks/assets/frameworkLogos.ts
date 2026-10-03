import type { Framework } from '@genesis-labs/shared-types';

// Résolu par le bundler : fonctionne même quand web-core est utilisé comme librairie
const files = import.meta.glob('../../../assets/LOGO/Frameworks/*.svg', {
  eager: true,
  query: '?url',
  import: 'default',
}) as Record<string, string>;

const file = (name: string): string | undefined =>
  files[`../../../assets/LOGO/Frameworks/${name}.svg`];

// "ASP.NET" / "ASP .Net" / "asp.net" -> "aspnet"
const normalize = (s: string) => s.toLowerCase().replace(/[^a-z0-9]/g, '');

// Logo par famille (coreFramework)
const BY_FAMILY: Record<string, string | undefined> = {
    aspnet: file('aspnet'),
    spring: file('spring'),
    django: file('django'),
    express: file('express'),
    laravel: file('laravel'),
};

// Exceptions : un framework précis qui a son propre logo
const BY_NAME: Record<string, string | undefined> = {
  aspnetcore: file('aspnet-core'),
};

export function resolveFrameworkLogo(fw: Framework): string | null {
  return (
    BY_NAME[normalize(fw.name)] ??
    BY_FAMILY[normalize(fw.coreFramework)] ??
    null
  );
}