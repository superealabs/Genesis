import type { Framework } from '@genesis-labs/shared-types';
import { createLogoResolver } from '@genesis-labs/web-core/core/utls/logoResolver';

const files = import.meta.glob('../../../assets/LOGO/Frameworks/*.svg', {
  eager: true,
  query: '?url',
  import: 'default',
}) as Record<string, string>;

export const resolveFrameworkLogo = createLogoResolver<Framework>({
  files,
  getName: (fw) => fw.name,
  getFamily: (fw) => fw.coreFramework,
});