import { createApp } from 'vue';
import { createPinia } from 'pinia';
import './assets/CSS/main.css';
import App from './App.vue';
import { router } from './router';
import { appService } from './core/services/app.service';

// ✅ 1. Imports des Clés d'Injection (via les manifestes)
import { FRAMEWORK_SERVICE_KEY } from '@genesis-labs/core/features/frameworks/manifest';
import { FRONTEND_SERVICE_KEY } from '@genesis-labs/core/features/frontend/manifest';

// ✅ 2. Imports des Services Concrets VSC
import { frameworkServiceVsc } from './features/frameworks/services/framework.service';
import { frontendServiceVsc } from './features/frontend/services/frontend.service';

console.log('[Main] Router importé:', router);  
console.log('[Main] Routes:', router.getRoutes());

const app = createApp(App);
const pinia = createPinia();

// ✅ 3. Fourniture de TOUS les services nécessaires au Core
app.provide(FRAMEWORK_SERVICE_KEY, frameworkServiceVsc);
app.provide(FRONTEND_SERVICE_KEY, frontendServiceVsc); // <-- C'était la ligne manquante !

app
    .use(pinia)
    .use(router)
    .mount('#app');

console.log('[Main] Router installé dans l\'app');

appService.init();