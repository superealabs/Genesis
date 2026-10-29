import { createApp } from 'vue';
import { createPinia } from 'pinia';
import './assets/CSS/main.css';
import App from './App.vue';
import { router } from './router';
import { appService } from './core/services/app.service';

// 1. Imports des Clés d'Injection (via les manifestes)
import { FRAMEWORK_SERVICE_KEY } from '@genesis-labs/web-core/features/frameworks/manifest';
import { FRONTEND_SERVICE_KEY } from '@genesis-labs/web-core/features/frontend/manifest';

// 2. Imports des Services Concrets VSC
import { frameworkServiceVsc } from '@/features/frameworks/services/framework.service';
import { frontendServiceVsc } from '@/features/frontend/services/frontend.service';

import { GENERATOR_SERVICE_KEY } from '@genesis-labs/web-core/features/generator/manifest';
import { generatorServiceVsc } from '@/features/generator/services/generator.service';

import { DATABASE_SERVICE_KEY } from '@genesis-labs/web-core/features/database/manifest';
import { databaseServiceVsc } from '@/features/database/services/database.service';

import { REPOSITORY_SERVICE_KEY } from '@genesis-labs/web-core/features/repository/manifest';
import { repositoryServiceVsc } from './features/repository/services/Repository.service.ts';

import { CONFIG_SERVICE_KEY } from '@genesis-labs/web-core/core/features/config/manifest';
import { configServiceVsc } from './core/features/config/services/config-storage.service';

import { RECENT_SERVICE_KEY } from '@genesis-labs/web-core/core/features/recent/manifest';
import { recentServiceVsc } from './core/features/recent/services/recent.service';
// Rajouter un manifest

console.log('[Main] Router importé:', router);  
console.log('[Main] Routes:', router.getRoutes());

const app = createApp(App);
const pinia = createPinia();

// 3. Fourniture de TOUS les services nécessaires au Core
app.provide(FRAMEWORK_SERVICE_KEY, frameworkServiceVsc);
app.provide(FRONTEND_SERVICE_KEY, frontendServiceVsc); // <-- C'était la ligne manquante !
app.provide(GENERATOR_SERVICE_KEY, generatorServiceVsc);
app.provide(DATABASE_SERVICE_KEY, databaseServiceVsc);
app.provide(REPOSITORY_SERVICE_KEY, repositoryServiceVsc);

app.provide(CONFIG_SERVICE_KEY, configServiceVsc);
app.provide(RECENT_SERVICE_KEY, recentServiceVsc);

// Rajouter le provide ici plus tard



app
    .use(pinia)
    .use(router)
    .mount('#app');

console.log('[Main] Router installé dans l\'app');

appService.init();