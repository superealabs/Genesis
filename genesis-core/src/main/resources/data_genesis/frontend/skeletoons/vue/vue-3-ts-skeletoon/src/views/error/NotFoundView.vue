<template>
  <div class="min-h-screen flex items-center justify-center bg-base-200">
    <div class="card bg-base-100 shadow-xl w-full max-w-md">
      <div class="card-body items-center text-center">
        <!-- Icône 404 -->
        <div class="text-8xl font-bold text-error mb-4">404</div>

        <!-- Message -->
        <h2 class="card-title text-2xl mb-2">{{ $t('error.404.title') }}</h2>
        <p class="text-base-content/70 mb-6">{{ $t('error.404.description') }}</p>

        <!-- Boutons d'action -->
        <div class="card-actions flex-col sm:flex-row gap-3 w-full">
          <GenesisButton @click="goBack" class="btn-outline btn-secondary flex-1">
            <LeftArrowIcon />
            {{ $t('button.back') }}
          </GenesisButton>

          <GenesisButton @click="goHome" class="btn-primary flex-1">
            {{ $t('navbar.home') }}
          </GenesisButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import GenesisButton from '@/components/button/GenesisButton.vue'
import LeftArrowIcon from '@/components/icons/LeftArrowIcon.vue'

const router = useRouter()

const goBack = () => {
  // Retourner à la page précédente ou à l'accueil si pas d'historique
  if (window.history.length > 1) {
    router.go(-2)
  } else {
    router.push('/home')
  }
}

const goHome = () => {
  router.push('/home')
}
</script>
