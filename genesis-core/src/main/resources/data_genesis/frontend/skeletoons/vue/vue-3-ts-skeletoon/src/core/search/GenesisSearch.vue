<template>
  <div class="d-flex flex-wrap align-items-center gap-2 py-2">
    <!-- Filtres actifs -->
    <div class="d-flex flex-wrap align-items-center gap-2">
      <span class="fw-bold me-2">Filtres :</span>

      <template v-if="activeFields.length">
        <div
          v-for="field in activeFields"
          :key="field.key"
          class="d-flex align-items-center shadow-sm rounded-2 ps-2 bg-white"
        >
          <span class="me-1 text-nowrap">{{ field.label }}:</span>
          <GenesisInput
            :placeholder="field.label"
            v-model="searchModel[field.key]"
            :type="field.type"
            class="border-0 bg-transparent form-control form-control-md"
            @update:model-value="onValueChnage"
          />
          <GenesisButton
            icon="bi bi-x"
            @click="removeFilter(field.key)"
            class="btn-white border-0 text-danger"
            title="Supprimer ce filtre"
          />
        </div>
      </template>

      <span v-else class="text-muted fst-italic">No filter</span>
    </div>

    <!-- Dropdown ajout de filtre -->
    <div class="dropdown">
      <GenesisButton
        icon="bi bi-plus"
        class="btn-light border-0 shadow-sm"
        type="button"
        data-bs-toggle="dropdown"
        aria-expanded="false"
        title="Ajouter un filtre"
      />

      <ul class="dropdown-menu border-0 bg-white shadow-sm">
        <li v-for="field in availableFields" :key="field.key">
          <button
            class="dropdown-item"
            :disabled="activeFieldKeys.includes(field.key)"
            @click="onFilterSelected(field.key)"
          >
            {{ field.label }}
          </button>
        </li>
      </ul>
    </div>

    <!-- Bouton Apply -->
    <GenesisButton
      icon="bi bi-funnel-fill me-2"
      label="Apply"
      @click="emitSearch"
      class="btn-secondary border-0 shadow-sm"
    />

    <!-- Reset -->
    <GenesisButton
      v-if="activeFields.length"
      icon="bi bi-arrow-counterclockwise"
      class="btn-outline-secondary text-danger"
      @click="clearAllFilters"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { useSearch } from "@/composables/useSearch";
import GenesisInput from "@/core/form/GenesisInput.vue";
import GenesisButton from "@/core/button/GenesisButton.vue";
import { EntityListField } from "@/models/EntityModel";

export default defineComponent({
  name: "GenesisSearch",
  components: { GenesisInput, GenesisButton },
  props: {
    initialModel: {
      type: Object as PropType<Record<string, any>>,
      required: true,
    },
    searchFields: {
      type: Array as PropType<EntityListField[]>,
      required: true,
    },
    defaultActive: { type: Array as PropType<string[]>, default: () => [] },
  },
  emits: ["update:filter", "search"], // renamed from "search" to match your intention
  setup(props, { emit }) {
    const {
      searchModel,
      activeFieldKeys,
      selectedFieldToAdd,
      activeFields,
      availableFields,
      activateField,
      desactivateField,
      resetFilters,
      getFiltersValues,
    } = useSearch(props.initialModel, props.searchFields, emit);

    if (props.defaultActive.length) {
      activeFieldKeys.value.push(...props.defaultActive);
    }

    const onFilterSelected = (key: string) => {
      selectedFieldToAdd.value = key;
      activateField();
      updateFilters();
    };

    const updateFilters = () => {
      emit("update:filter", getFiltersValues());
    };

    const removeFilter = (key: string) => {
      desactivateField(key);
      updateFilters;
      emitSearch();
    };

    const onValueChnage = () => {
      // updateFilters();
      // emitSearch();
    };

    const clearAllFilters = () => {
      resetFilters();
      updateFilters();
      emitSearch();
    };

    const emitSearch = (e?: Event) => {
      updateFilters();
      emit("search", e);
    };

    return {
      searchModel,
      activeFields,
      availableFields,
      selectedFieldToAdd,
      activeFieldKeys,
      onFilterSelected,
      removeFilter,
      clearAllFilters,
      updateFilters,
      resetFilters,
      emitSearch,
      onValueChnage,
    };
  },
});
</script>
