<script setup lang="ts">
const { navbars } = useNavigation()
</script>

<template>
  <footer class="footer">
    <div class="footer__inner">
      <div>
        <NuxtLink class="brand" to="/"><img src="/assets/img/genie-normal.svg" alt=""><span>Genesis</span></NuxtLink>
        <p class="footer__desc">Genesis crée un projet complet (backend, frontend et sécurité) à partir de votre base de données. Gratuit et open-source.</p>
      </div>
      <div v-for="n in navbars" :key="n.id">
        <h5>{{ n.nom }}</h5>
        <NuxtLink v-for="c in n.contents" :key="c.id" :to="c.route">{{ c.nom }}</NuxtLink>
      </div>
    </div>
    <div class="footer__bottom">
      <span>© {{ new Date().getFullYear() }} Genesis. Projet open-source sous licence MIT.</span>
      <a href="https://github.com/superealabs/Genesis" target="_blank" rel="noopener">github.com/superealabs/Genesis</a>
    </div>
  </footer>
</template>
