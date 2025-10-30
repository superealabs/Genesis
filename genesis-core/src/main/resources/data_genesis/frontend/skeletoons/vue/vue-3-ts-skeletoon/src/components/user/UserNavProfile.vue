<template>
  <div :class="dropDirections">
    <!-- Trigger -->
    <div
      tabindex="0"
      role="button"
      class="flex items-center gap-3 px-3 py-2 rounded-md nav-item transition-colors cursor-pointer"
    >
      <ProfileAvatar :username="name" />
      <div class="flex flex-col items-start">
        <p class="font-medium leading-tight">{{ name }}</p>
      </div>
    </div>

    <!-- Dropdown Menu -->
    <ul
      tabindex="0"
      class="menu dropdown-content mt-2 z-[1] p-2 shadow-md border border-base-100 bg-base-100 rounded-box w-60"
    >
      <li>
        <button @click="goToProfile" class="flex items-center justify-start gap-2">
          <ProfileIcon /> <span>My Profile</span>
        </button>
      </li>
      <li>
        <button @click="logout" class="flex items-center justify-start gap-2 text-red-500">
          <LogoutIcon /> <span>Logout</span>
        </button>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth.ts'
import LogoutIcon from '@/components/icons/LogoutIcon.vue'
import ProfileIcon from '@/components/icons/ProfileIcon.vue'
import ProfileAvatar from '@/components/user/ProfileAvatar.vue'

/* Props */
const props = defineProps<{
  name: string
  dropdownDirection?: {
    top?: boolean
    center?: boolean
    left?: boolean
    right?: boolean
    bottom?: boolean
  }
}>()

/* Composables */
const auth = useAuth()

/* Methods */
const logout = () => {
  auth.logout()
}
const goToProfile = () => {
  auth.goToProfileView()
}

const dropDirections = computed(() => {
  let directions = 'dropdown w-full'
  if (props.dropdownDirection?.center) {
    directions += ' dropdown-center'
  }

  if (props.dropdownDirection?.left) {
    directions += ' dropdown-left'
  }

  if (props.dropdownDirection?.right) {
    directions += ' dropdown-right'
  }

  if (props.dropdownDirection?.top) {
    directions += ' dropdown-top'
  }

  if (props.dropdownDirection?.bottom) {
    directions += ' dropdown-bottom'
  }
  return directions
})
</script>

<style scoped>
.dropdown [role='button']:focus {
  outline: none;
}
</style>
