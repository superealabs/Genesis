<script setup lang="ts">
import illus from '~/assets/illustrations/database.png'
const { s, selectDatabase } = usePlayground()
</script>

<template>
  <div>
    <PgStepHeader title="Base de données" description="Choisissez la base pour laquelle votre script SQL est écrit. Aucune connexion n'est nécessaire." :illustration="illus" />
    <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <PgCard v-for="d in s.databases" :key="d.id" :title="d.name" :subtitle="`port ${d.port}`"
        :logo="logoOf(d.name)" :selected="s.database?.id === d.id"
        @select="selectDatabase(d)" />
    </div>
  </div>
</template>
