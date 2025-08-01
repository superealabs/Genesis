<template>
  <button v-bind="$attrs" :class="btnClassNames" @click="onClick">
    <i v-if="icon" :class="icon" />
    <span class="mx-2" v-if="label">{{ label }}</span>
    <span v-else><slot></slot></span>
  </button>
</template>

<script lang="ts" src="./GenesisButtonComponent.ts"></script>
