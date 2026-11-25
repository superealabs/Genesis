<script setup lang="ts">
// Équipe : Equipe.js › Getall() (Siteback : /tequipes)
import { EquipeService } from '~/services/Equipe'
const { data } = await useAsyncData('equipe', () => EquipeService.Getall(), { default: () => [] })
const initials = (m: { nom: string; prenom?: string }) =>
  `${m.nom?.[0] ?? ''}${m.prenom?.[0] ?? ''}`.toUpperCase()
</script>

<template>
  <div class="team">
    <article v-for="m in data" :key="m.id" class="card member">
      <img v-if="m.photo" class="avatar" :src="m.photo" :alt="`${m.nom} ${m.prenom ?? ''}`" style="object-fit:cover">
      <div v-else class="avatar">{{ initials(m) }}</div>
      <h4>{{ m.nom }} {{ m.prenom }}</h4><p>{{ m.poste || 'Contributeur·rice' }}</p>
      <div class="links">
        <a v-if="m.github" :href="m.github" target="_blank" rel="noopener" :aria-label="`GitHub de ${m.nom}`"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 0a12 12 0 0 0-3.8 23.4c.6.1.8-.3.8-.6v-2.2c-3.3.7-4-1.4-4-1.4-.6-1.4-1.4-1.8-1.4-1.8-1-.7.1-.7.1-.7 1.2.1 1.8 1.2 1.8 1.2 1 1.8 2.8 1.3 3.5 1 .1-.8.4-1.3.7-1.6-2.7-.3-5.5-1.3-5.5-5.9 0-1.3.5-2.4 1.2-3.2-.1-.3-.5-1.5.1-3.2 0 0 1-.3 3.3 1.2a11.5 11.5 0 0 1 6 0C17.3 4.7 18.3 5 18.3 5c.6 1.7.2 2.9.1 3.2.8.8 1.2 1.9 1.2 3.2 0 4.6-2.8 5.6-5.5 5.9.4.4.8 1.1.8 2.2v3.3c0 .3.2.7.8.6A12 12 0 0 0 12 0z" /></svg></a>
        <a v-if="m.linkedin" :href="m.linkedin" target="_blank" rel="noopener" :aria-label="`LinkedIn de ${m.nom}`"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4.98 3.5a2.5 2.5 0 1 1 0 5 2.5 2.5 0 0 1 0-5zM3 9h4v12H3zm7 0h3.8v1.7h.1c.5-1 1.8-2 3.7-2 4 0 4.7 2.6 4.7 6V21h-4v-5.6c0-1.3 0-3-1.9-3s-2.1 1.4-2.1 2.9V21h-4z" /></svg></a>
        <a v-if="m.email" :href="`mailto:${m.email}`" :aria-label="`Email de ${m.nom}`"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 5h18v14H3zm2 2v.5l7 4.5 7-4.5V7zm14 2.9-7 4.5-7-4.5V17h14z" /></svg></a>
      </div>
    </article>
  </div>
</template>
