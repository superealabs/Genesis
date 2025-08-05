<template>
  <div class="row gap-1 align-items-center justify">
    <div class="row col-auto">
      <!-- FIELDS SEARCH INPUTS -->
      <div class="col">
        <GenesisInput placeholder="ID" v-model="model.id" type="number" />
      </div>
      <div class="col">
        <GenesisInput
          placeholder="Nom Département"
          v-model="model.nomDepartement"
          type="text"
        />
      </div>
      <div class="col">
        <GenesisInput
          placeholder="Code Département"
          v-model="model.codeDepartement"
          type="text"
        />
      </div>
    </div>

    <!-- SEARCH BUTTON CONTROLS -->
    <div class="col d-flex gap-2 justify-content-end mb-3">
      <GenesisButton
        icon="fa fa-close"
        type="reset"
        class="bg-white text-dark"
        @click="resetFilters"
      />
      <GenesisButton
        icon="fa fa-filter"
        label="Filter"
        type="submit"
        class="bg-white text-dark"
        @click="emitSearch"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import { useSearch } from "@/composables/useSearch";
import { Departements } from "@/models/DepartementsModel";
import GenesisInput from "@/core/form/GenesisInput.vue";
import GenesisButton from "@/core/button/GenesisButton.vue";

export default defineComponent({
  name: "DepartementSpecification",
  components: {
    GenesisInput,
    GenesisButton,
  },
  emits: ["search"],
  setup(_, { emit }) {
    const model = ref(new Departements());
    const { emitSearch } = useSearch(model, emit);
    const resetFilters = function () {
      model.value = new Departements();
    };
    return {
      model,
      emitSearch,
      resetFilters,
    };
  },
});
</script>
