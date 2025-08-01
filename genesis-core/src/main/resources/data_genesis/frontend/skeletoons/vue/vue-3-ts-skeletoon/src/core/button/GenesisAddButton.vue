<template>
  <GenesisButton icon="fa fa-plus">
    <span v-if="label">{{ label }}</span>
    <span v-else><slot /></span>
  </GenesisButton>
</template>

<script lang="ts" src="./GenesisAddButtonComponent.ts"></script>
