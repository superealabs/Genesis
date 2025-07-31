<template>
  <div class="row gap-1 align-items-center justify">
    <div class="row col-auto">
      <div v-for="(value, attr) in filter" :key="attr" class="col">
        <GenesisInput
          :label="attr"
          v-model="filter[attr]"
          :type="filterTypes[attr] || 'text'"
        />
      </div>
    </div>
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

<script lang="ts" src="./GenesisSearchComponent.ts"></script>
