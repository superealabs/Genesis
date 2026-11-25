<script setup lang="ts">
// Liste des frameworks (backend, frontend) et des bases, lue dans genesis-api.
const { data, error, pending } = useGenesisCatalog()
const lang = (id: number) => data.value?.languages.find(l => l.id === id)?.name ?? '—'
</script>

<template>
  <slot v-if="error || pending || !data" />
  <template v-else>
    <h2 id="fw-0">Backend</h2>
    <div class="table-wrap"><table>
      <thead><tr><th>Nom</th><th>Langage</th><th>Type</th><th>Statut</th></tr></thead>
      <tbody>
        <tr v-for="f in data.frameworks" :key="f.id">
          <td><div style="display:flex;align-items:center;gap:10px"><span class="logo-chip"><img :src="logoOf(f.name)" alt="" loading="lazy"></span><b>{{ f.name }}</b></div></td>
          <td>{{ lang(f.languageId) }}</td>
          <td>{{ f.type }}</td>
          <td>
            <span v-if="f.isProd" class="badge"><span class="dot" />Disponible</span>
            <span v-else class="badge badge--accent"><span class="dot" />En cours</span>
          </td>
        </tr>
      </tbody>
    </table></div>

    <h2 id="fw-1">Frontend</h2>
    <div class="table-wrap"><table>
      <thead><tr><th>Nom</th><th>Port par défaut</th><th>Statut</th></tr></thead>
      <tbody>
        <tr v-for="f in data.frontends.filter(x => x.type === 'spa')" :key="f.id">
          <td><div style="display:flex;align-items:center;gap:10px"><span class="logo-chip"><img :src="logoOf(f.name)" alt="" loading="lazy"></span><b>{{ f.name }}</b></div></td>
          <td>{{ f.defaultPort }}</td>
          <td><span class="badge"><span class="dot" />Disponible</span></td>
        </tr>
      </tbody>
    </table></div>

    <h2 id="fw-2">Bases de données</h2>
    <div class="table-wrap"><table>
      <thead><tr><th>Nom</th><th>Port par défaut</th><th>Statut</th></tr></thead>
      <tbody>
        <tr v-for="d in data.databases" :key="d.id">
          <td><div style="display:flex;align-items:center;gap:10px"><span class="logo-chip"><img :src="logoOf(d.name)" alt="" loading="lazy"></span><b>{{ d.name }}</b></div></td>
          <td>{{ d.port }}</td>
          <td><span class="badge"><span class="dot" />Disponible</span></td>
        </tr>
      </tbody>
    </table></div>
  </template>
</template>
