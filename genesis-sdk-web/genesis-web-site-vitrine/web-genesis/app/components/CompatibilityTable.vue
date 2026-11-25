<script setup lang="ts">
// Compatibilité par framework et base de données, lue dans genesis-api.
// Si l'API n'est pas lancée, le contenu statique (slot) est affiché.
const { data, error, pending } = useGenesisCatalog()
const backends = computed(() => (data.value?.frameworks ?? []).filter(f => f.isProd))
const frontNames = computed(() => (data.value?.frontends ?? []).filter(f => f.type === 'spa').map(f => f.coreFramework || f.name).join(' · '))
</script>

<template>
  <slot v-if="error || pending || !data" />
  <template v-else>
    <div class="table-wrap"><table>
      <thead><tr><th>Framework</th><th v-for="d in data.databases" :key="d.id">{{ d.name }}</th><th>Frontend</th></tr></thead>
      <tbody>
        <tr v-for="f in backends" :key="f.id">
          <td><b>{{ f.name }}</b></td>
          <td v-for="d in data.databases" :key="d.id" :class="f.useDB ? 'ok' : ''">{{ f.useDB ? '✓' : '—' }}</td>
          <td>{{ f.type === 'MVC' ? 'Vues intégrées' : (f.useFrontendApp ? frontNames : '—') }}</td>
        </tr>
      </tbody>
    </table></div>
    <div class="tiles">
      <div v-for="d in data.databases" :key="d.id" class="tile">
        <img :src="logoOf(d.name)" alt="" loading="lazy">
        <div><b>{{ d.name }}</b><small>port {{ d.port }}</small></div>
      </div>
    </div>
  </template>
</template>
