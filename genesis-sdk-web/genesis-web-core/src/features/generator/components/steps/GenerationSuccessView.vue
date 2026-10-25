<!-- genesis-sdk-web/genesis-web-core/src/features/generator/components/steps/GenerationSuccessView.vue -->
<template>
  <div class="flex flex-col items-center justify-center h-full p-8 text-center space-y-6">
    
    <!-- Icône de succès animée -->
    <div class="w-24 h-24 rounded-full bg-green-500/10 flex items-center justify-center animate-in fade-in zoom-in duration-500">
      <svg xmlns="http://www.w3.org/2000/svg" class="w-12 h-12 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
      </svg>
    </div>

    <div class="space-y-2">
      <h2 class="text-2xl font-bold text-text">Génération réussie !</h2>
      <p class="text-text-muted max-w-md">
        Votre projet a été généré avec succès et est prêt à être utilisé.
      </p>
    </div>

    <!-- Zone d'actions spécifiques à l'environnement -->
    <div class="flex flex-col items-center gap-3 w-full max-w-sm mt-6">
      <slot name="actions">
        <!-- Fallback de sécurité : si aucun environnement ne fournit de slot, on affiche au moins un bouton pour sortir -->
        <GenesisButton variant="secondary" size="lg" fill-width @click="emit('close')">
          Fermer
        </GenesisButton>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';

const emit = defineEmits<{
  close: [];
}>();
</script>