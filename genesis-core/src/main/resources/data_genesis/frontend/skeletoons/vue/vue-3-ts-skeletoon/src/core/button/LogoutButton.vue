<template>
  <GenesisButton class="btn-ghost border-0" @click="logout">
    <LogoutIcon />
  </GenesisButton>
</template>
<script setup lang="ts">
import LogoutIcon from '@/core/icons/LogoutIcon.vue'
import GenesisButton from '@/core/button/GenesisButton.vue'
import { useAuth } from '@/composables/useAuth.ts'

const auth = useAuth()
const logout = () => {
  auth.goToLoginView()
}
</script>
