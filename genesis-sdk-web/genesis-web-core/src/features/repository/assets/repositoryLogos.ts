import type { RepositoryProviderMetadata } from '@genesis-labs/shared-types';
import { createLogoResolver } from '@genesis-labs/web-core/core/utls/logoResolver';

// Scanne tous les fichiers .svg dans le dossier LOGO/Repositories
const files = import.meta.glob('../../../assets/LOGO/Repositories/*.svg', {
  eager: true,
  query: '?url',
  import: 'default',
}) as Record<string, string>;

export const resolveRepositoryLogo = createLogoResolver<RepositoryProviderMetadata>({
  files,
  // Le nom affiché (ex: "GitHub")
  getName: (p) => p.name,
  // La "famille" ou catégorie. Ici, on utilise l'ID du provider (ex: 'github', 'gitlab') 
  // pour permettre un fallback si le nom exact ne correspond pas au nom du fichier.
  getFamily: (p) => p.id, 
});