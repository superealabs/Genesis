<template>
  <div class="flex items-center justify-between gap-2">
    <!-- Avatar + Nom -->
    <div class="flex items-center gap-2">
      <div
        class="flex items-center justify-center w-10 h-10 rounded-full bg-gray-300 text-gray-600 font-bold"
      >
        <UserIcon />
      </div>
      <p class="m-0 font-medium text-gray-800">{{ name }}</p>
    </div>

    <!-- Bouton Déconnexion -->
    <div>
      <GenesisButton class="btn-ghost border-0">
        <LogoutIcon />
      </GenesisButton>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue'
import GenesisButton from '../button/GenesisButton.vue'
import LogoutIcon from '../icons/LogoutIcon.vue'
import UserIcon from '../icons/UserIcon.vue'

export default defineComponent({
  name: 'UserNavProfile',
  components: { UserIcon, LogoutIcon, GenesisButton },
  props: {
    name: {
      required: true,
      type: String,
    },
  },
  setup(props) {
    const initials = computed(() => {
      if (!props.name) return ''
      return props.name
        .split(' ')
        .map((n) => n[0])
        .join('')
        .toUpperCase()
    })
    return { initials }
  },
})
</script>
