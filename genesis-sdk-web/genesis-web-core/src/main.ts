import { createApp } from 'vue';
import { createPinia } from 'pinia';
import '@/assets/CSS/main.css';
import App from '@/App.vue'; // App.vue de prévisualisation du core

const app = createApp(App);
const pinia = createPinia();

app
    .use(pinia)
    // Pas de router ici, c'est le projet hôte (VSC ou Web) qui le gère
    .mount('#app');