<template>
  <nav id="sidebar" class="sticky top-0 h-full w-2/12 p-4 flex flex-col bg-base-100 shadow-md">
    <!-- Brand -->
    <div class="text-center flex gap-2 items-center mb-6 uppercase">
      <svg
        width="49"
        height="40"
        viewBox="0 0 49 40"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M24.9727 0L29.687 3.92858L25.6129 7.32367C25.2421 7.63271 24.7034 7.63271 24.3325 7.32367L20.2585 3.92858L24.9727 0Z"
          fill="#00067F"
        ></path>
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M24.3325 11.3545C24.7034 11.6635 25.2421 11.6635 25.6129 11.3545L32.1054 5.94397L35.8399 9.05606L27.5335 15.9782C26.0501 17.2143 23.8954 17.2143 22.412 15.9782L14.1055 9.05606L17.84 5.94397L24.3325 11.3545Z"
          fill="#00067F"
        ></path>
        <path
          d="M24.9727 40L20.2585 36.0714L24.3325 32.6763C24.7034 32.3673 25.2421 32.3673 25.6129 32.6763L29.687 36.0714L24.9727 40Z"
          fill="#00067F"
        ></path>
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M25.6129 28.6455C25.2421 28.3365 24.7034 28.3365 24.3325 28.6455L17.84 34.056L14.1055 30.9439L22.412 24.0218C23.8954 22.7857 26.0501 22.7857 27.5335 24.0218L35.8399 30.9439L32.1054 34.056L25.6129 28.6455Z"
          fill="#00067F"
        ></path>
        <path
          d="M48.9727 20L44.2584 16.0714L40.466 19.2318C39.9862 19.6316 39.9862 20.3684 40.466 20.7682L44.2584 23.9286L48.9727 20Z"
          fill="#00067F"
        ></path>
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M35.6291 20.7682C35.1493 20.3684 35.1493 19.6316 35.6291 19.2318L41.8399 14.056L38.1054 10.9439L30.9257 16.9271C29.0067 18.5263 29.0067 21.4737 30.9257 23.0729L38.1054 29.0561L41.8399 25.944L35.6291 20.7682Z"
          fill="#00067F"
        ></path>
        <path
          d="M0.972656 20.0001L5.68692 23.9287L9.47933 20.7683C9.95909 20.3685 9.95909 19.6317 9.47933 19.2319L5.68692 16.0715L0.972656 20.0001Z"
          fill="#00067F"
        ></path>
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M14.3162 19.2319C14.796 19.6317 14.796 20.3685 14.3163 20.7683L8.10538 25.9441L11.8399 29.0562L19.0196 23.073C20.9386 21.4738 20.9386 18.5264 19.0196 16.9272L11.8399 10.944L8.10538 14.0561L14.3162 19.2319Z"
          fill="#00067F"
        ></path>
      </svg>
      <h1 class="text-2xl font-semibold">
        Genesis
        <span class="text-primary">App</span>
      </h1>
    </div>

    <!-- Navigation -->
    <ul class="menu flex-col flex flex-nowrap w-full overflow-y-auto">
      <li v-for="section in navigations" :key="section.sectionName" class="mb-2">
        <span class="menu-title font-semibold text-neutral uppercase opacity-30">
          <font-awesome-icon
            v-if="section.icon"
            :icon="section.icon"
            class="mr-2"
          ></font-awesome-icon>
          {{ section.sectionName }}
        </span>
        <GenesisNavList :items="section.navChilds" />
      </li>
    </ul>

    <!-- Footer part -->
    <div class="mt-auto pt-4">
      <UserNavProfile name="John Doe" />
    </div>
  </nav>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useNavigationsStore } from '@/stores/useNavigationsStore'
import { storeToRefs } from 'pinia'
import UserNavProfile from '../user/UserNavProfile.vue'
import GenesisNavList from './GenesisNavList.vue'

export default defineComponent({
  name: 'GenesisSidebarComponent',
  components: { UserNavProfile, GenesisNavList },
  setup() {
    const navigationsStore = useNavigationsStore()
    const { navigations } = storeToRefs(navigationsStore)
    return { navigations }
  },
})
</script>

<style scoped>
#sidebar {
  position: fixed; /* sticky ne peut pas dépasser le parent scrollable */
}
</style>
