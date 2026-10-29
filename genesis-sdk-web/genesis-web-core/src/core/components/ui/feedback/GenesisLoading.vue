<!-- genesis-sdk-web/genesis-web-core/src/core/components/ui/feedback/GenesisLoading.vue -->
<template>
  <div class="flex flex-col items-center justify-center gap-6 p-8 w-full max-w-lg">
    
    <!-- GIF de chargement -->
    <img 
      :src="loadingGif" 
      alt="Loading animation" 
      class="w-80 h-80 object-contain rounded-lg drop-shadow-lg"
    />
    
    <!-- Barre de progression dynamique -->
    <div class="w-full h-2 bg-bg-light/20 rounded-full overflow-hidden backdrop-blur-sm border border-secondary/30">
      <div 
        class="h-full bg-primary rounded-full transition-all duration-300 ease-out shadow-[0_0_10px_rgba(var(--color-primary-rgb),0.5)]"
        :style="{ width: `${progress}%` }"
      ></div>
    </div>
    
    <!-- Textes dynamiques -->
    <div class="text-center space-y-2">
      <p class="text-xl font-semibold text-text tracking-wide">
        {{ currentMessage }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';

// Import du GIF depuis les assets
import loadingGif from '../../../../assets/GIF/loading-screen.gif';

// État de la progression (0 à 100)
const progress = ref(0);
let intervalId: ReturnType<typeof setInterval> | null = null;

// Mapping des messages en fonction du pourcentage (inspiré des étapes du wizard)
const stepMessages = [
  { threshold: 0,  text: "Initialisation de l'environnement de génération..." },
  { threshold: 15, text: "Analyse du framework et configuration du projet..." },
  { threshold: 30, text: "Configuration de la base de données et des connexions..." },
  { threshold: 45, text: "Génération de la structure des tables et des relations..." },
  { threshold: 60, text: "Création des modèles, services et contrôleurs backend..." },
  { threshold: 75, text: "Configuration de l'interface frontend et des layouts..." },
  { threshold: 85, text: "Initialisation du dépôt Git et des fichiers de configuration..." },
  { threshold: 90, text: "Finalisation des fichiers et préparation de l'ouverture..." },
  { threshold: 100, text: "Génération terminée avec succès !" }
];

// Computed pour sélectionner le message en fonction du pourcentage actuel
const currentMessage = computed(() => {
  // On parcourt le tableau à l'envers pour trouver le dernier seuil atteint
  for (let i = stepMessages.length - 1; i >= 0; i--) {
    if (progress.value >= stepMessages[i].threshold) {
      return stepMessages[i].text;
    }
  }
  return stepMessages[0].text;
});

onMounted(() => {
  progress.value = 0;
  
  // Simulation de la progression
  intervalId = setInterval(() => {
    if (progress.value < 90) {
      // On ralentit la progression à mesure qu'on approche de 90% pour plus de réalisme
      const increment = progress.value < 50 ? 1.5 : 0.3;
      progress.value = Math.min(90, progress.value + increment);
    }
  }, 100); // Mise à jour toutes les 100ms
});

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
});
</script>

<style scoped>
/* Fallback pour les navigateurs qui ne supportent pas les variables RGB Tailwind dans box-shadow */
.shadow-glow {
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.5); /* Remplace par ta couleur primaire si besoin */
}
</style>