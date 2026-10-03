import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import { createLogoResolver } from '@genesis-labs/web-core/core/utls/logoResolver';

const files = import.meta.glob('../../../assets/LOGO/Databases/*.svg', {
  eager: true,
  query: '?url',
  import: 'default',
}) as Record<string, string>;

export const resolveDatabaseLogo = createLogoResolver<DatabaseEngineDto>({
  files,
  getName: (engine) => engine.name,
});