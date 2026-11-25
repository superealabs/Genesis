<script setup lang="ts">
const { navbars, current, href } = useNavigation()
const { theme, apply } = useTheme()
const open = ref(false)
const route = useRoute()
watch(() => route.fullPath, () => { open.value = false })
</script>

<template>
  <header class="topbar">
    <div class="topbar__inner">
      <NuxtLink class="brand" to="/" aria-label="Genesis — accueil">
        <img src="/assets/img/genie-normal.svg" alt="">
        <span>Genesis</span>
      </NuxtLink>
      <nav id="main-nav" class="nav" :class="{ open }" aria-label="Navigation principale">
        <NuxtLink v-for="n in navbars" :key="n.id" :to="href(n)" :aria-current="current?.id === n.id ? 'page' : undefined">
          {{ n.nom }}
        </NuxtLink>
      </nav>
      <div class="topbar__actions">
        <div class="theme-switch" role="group" aria-label="Thème">
          <button type="button" :aria-pressed="theme === 'light'" @click="apply('light')"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zM11 1h2v3h-2zm0 19h2v3h-2zM1 11h3v2H1zm19 0h3v2h-3zM4.2 5.6l1.4-1.4 2.1 2.1-1.4 1.4zm12.1 12.1 1.4-1.4 2.1 2.1-1.4 1.4zM4.2 18.4l2.1-2.1 1.4 1.4-2.1 2.1zM16.3 6.3l2.1-2.1 1.4 1.4-2.1 2.1z" /></svg><span>Clair</span></button>
          <button type="button" :aria-pressed="theme === 'dark'" @click="apply('dark')"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12.3 22a10 10 0 0 1-2.6-19.7 1 1 0 0 1 1.2 1.3 7.5 7.5 0 0 0 9.5 9.5 1 1 0 0 1 1.3 1.2A10 10 0 0 1 12.3 22z" /></svg><span>Sombre</span></button>
        </div>
        <span class="topbar__divider" aria-hidden="true" />
        <a class="icon-btn icon-btn--plain" href="https://github.com/superealabs/Genesis" target="_blank" rel="noopener" aria-label="GitHub"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 0a12 12 0 0 0-3.8 23.4c.6.1.8-.3.8-.6v-2.2c-3.3.7-4-1.4-4-1.4-.6-1.4-1.4-1.8-1.4-1.8-1-.7.1-.7.1-.7 1.2.1 1.8 1.2 1.8 1.2 1 1.8 2.8 1.3 3.5 1 .1-.8.4-1.3.7-1.6-2.7-.3-5.5-1.3-5.5-5.9 0-1.3.5-2.4 1.2-3.2-.1-.3-.5-1.5.1-3.2 0 0 1-.3 3.3 1.2a11.5 11.5 0 0 1 6 0C17.3 4.7 18.3 5 18.3 5c.6 1.7.2 2.9.1 3.2.8.8 1.2 1.9 1.2 3.2 0 4.6-2.8 5.6-5.5 5.9.4.4.8 1.1.8 2.2v3.3c0 .3.2.7.8.6A12 12 0 0 0 12 0z" /></svg></a>
        <button class="icon-btn menu-btn" type="button" aria-controls="main-nav" :aria-expanded="open" aria-label="Menu" @click="open = !open"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 5h18v2H3zm0 6h18v2H3zm0 6h18v2H3z" /></svg></button>
      </div>
    </div>
  </header>
</template>
