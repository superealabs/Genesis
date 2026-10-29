// genesis-sdk-web/genesis-web-core/src/core/utls/logoResolver.ts

export const normalizeLogoKey = (s: string) => s.toLowerCase().replace(/[^a-z0-9]/g, '');

/**
 * Découpe un nom en mots normalisés (l'ordre n'a pas d'importance).
 * "Spring Boot - Eureka Server" -> ['spring', 'boot', 'eureka', 'server']
 * "ASP.NET Core"                -> ['aspnet', 'core']   (le point ne coupe pas un mot)
 */
const tokenizeLogoName = (s: string): string[] =>
  s.split(/[\s\-_/]+/).map(normalizeLogoKey).filter(Boolean);

interface LogoEntry {
  url: string;
  /** Mots du nom de fichier : "spring-eureka.svg" -> ['spring', 'eureka'] */
  tokens: string[];
  /** Précision du logo = nombre de caractères couverts (plus long = plus spécifique) */
  weight: number;
}

interface LogoResolverConfig<T> {
  /** Résultat de import.meta.glob(..., { eager: true, query: '?url', import: 'default' }) */
  files: Record<string, string>;
  /** Nom de l'élément, comparé au nom de fichier */
  getName: (item: T) => string | null | undefined;
  /** Famille de repli (ex. coreFramework), optionnelle */
  getFamily?: (item: T) => string | null | undefined;
  /** Alias : clé qui ne correspond à aucun fichier -> nom du fichier visé */
  aliases?: Record<string, string>;
}

export function createLogoResolver<T>({ files, getName, getFamily, aliases = {} }: LogoResolverConfig<T>) {
  // "…/Frameworks/aspnet-core.svg" -> clé "aspnetcore"
  const index: Record<string, string> = {};
  const entries: LogoEntry[] = [];

  for (const [path, url] of Object.entries(files)) {
    const base = path.split('/').pop()!.replace(/\.[^.]+$/, '');
    index[normalizeLogoKey(base)] = url;

    const tokens = tokenizeLogoName(base);
    if (tokens.length > 0) entries.push({ url, tokens, weight: tokens.join('').length });
  }

  const alias: Record<string, string> = {};
  for (const [from, to] of Object.entries(aliases)) {
    alias[normalizeLogoKey(from)] = normalizeLogoKey(to);
  }

  /** Correspondance exacte (clé complète normalisée, alias compris) */
  const lookup = (raw?: string | null): string | undefined => {
    if (!raw) return undefined;
    const key = normalizeLogoKey(raw);
    return index[alias[key] ?? key];
  };

  /**
   * Logo le plus proche : tous les mots du fichier doivent se trouver dans le nom,
   * et le fichier qui en couvre le plus l'emporte (en cas d'égalité : le premier).
   * Ex. avec spring.svg et spring-eureka.svg :
   *   "Spring - MVC"               -> spring
   *   "Spring Boot - Eureka Server" -> spring-eureka
   */
  const closest = (raw?: string | null): string | undefined => {
    if (!raw) return undefined;
    const words = new Set(tokenizeLogoName(raw));
    let best: LogoEntry | undefined;
    for (const entry of entries) {
      if (!entry.tokens.every(t => words.has(t))) continue;
      if (!best || entry.weight > best.weight) best = entry;
    }
    return best?.url;
  };

  // Priorité : nom exact, puis logo le plus proche du nom, puis famille, sinon null
  // (GenesisCard affiche les initiales)
  return (item: T): string | null => {
    const name = getName(item);
    return lookup(name) ?? closest(name) ?? lookup(getFamily?.(item)) ?? null;
  };
}