export const normalizeLogoKey = (s: string) => s.toLowerCase().replace(/[^a-z0-9]/g, '');

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
  for (const [path, url] of Object.entries(files)) {
    const base = path.split('/').pop()!.replace(/\.[^.]+$/, '');
    index[normalizeLogoKey(base)] = url;
  }

  const alias: Record<string, string> = {};
  for (const [from, to] of Object.entries(aliases)) {
    alias[normalizeLogoKey(from)] = normalizeLogoKey(to);
  }

  const lookup = (raw?: string | null): string | undefined => {
    if (!raw) return undefined;
    const key = normalizeLogoKey(raw);
    return index[alias[key] ?? key];
  };

  // Priorité : nom exact, puis famille, sinon null (GenesisCard affiche les initiales)
  return (item: T): string | null =>
    lookup(getName(item)) ?? lookup(getFamily?.(item)) ?? null;
}