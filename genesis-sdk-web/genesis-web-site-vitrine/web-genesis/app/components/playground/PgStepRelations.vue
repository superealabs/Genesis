<script setup lang="ts">
import GenesisSwitch from '@genesis-labs/web-core/core/components/ui/inputs/GenesisSwitch.vue'
import IconTrashAlt from '@genesis-labs/web-core/core/components/ui/icons/IconTrashAlt.vue'
import illus from '~/assets/illustrations/relation-summary.png'
const { s } = usePlayground()
</script>

<template>
  <div>
    <PgStepHeader title="Relations entre les tables" description="Les liens entre vos tables, trouvés à partir des clés étrangères du script." :illustration="illus" />
    <p v-if="!s.relations.length" class="rounded-lg bg-bg-light p-4 text-sm text-text-muted">
      Aucune clé étrangère entre les tables sélectionnées. Vous pouvez passer à l’étape suivante.
    </p>
    <div v-else class="overflow-hidden rounded-lg border border-bg-neutral-genesis">
      <table class="w-full text-sm">
        <thead class="bg-bg-light text-left text-xs uppercase tracking-wide text-text-muted">
          <tr><th class="p-3">Parent</th><th class="p-3">Enfant</th><th class="p-3">Obligatoire</th><th class="p-3">Formulaire enfant</th><th class="p-3" /></tr>
        </thead>
        <tbody>
          <tr v-for="(r, i) in s.relations" :key="`${r.parentTable}>${r.childTable}`" class="border-t border-bg-neutral-genesis">
            <td class="p-3 font-semibold">{{ r.parentTable }}</td>
            <td class="p-3">{{ r.childTable }}</td>
            <td class="p-3"><GenesisSwitch v-model="r.mandatory" size="sm" /></td>
            <td class="p-3"><GenesisSwitch v-model="r.hasForm" size="sm" /></td>
            <td class="p-3 text-right">
              <button type="button" class="rounded p-1 hover:bg-bg-light" title="Retirer la relation" @click="s.relations.splice(i, 1)">
                <IconTrashAlt color="var(--color-text-muted)" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
