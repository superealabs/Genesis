import { createApp } from 'vue';
import { createPinia } from 'pinia';

// ✅ CORRECTION : Importe le App.vue LOCAL (celui qui est dans le même dossier), pas celui du core
import App from './App.vue';

// Import du router du core (si tu l'utilises)
import { router } from '@genesis-labs/core/router';

// Initialisation des services spécifiques à VSC (si ce n'est pas déjà fait dans App.vue)
import { appService } from './core/services/app.service';

const app = createApp(App);

app.use(createPinia());
app.use(router);

// Initialisation du service App au démarrage
appService.init();

app.mount('#app');