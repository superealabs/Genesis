<template>
  <input
    v-bind="$attrs"
    :value="modelValue"
    :placeholder="label"
    :id="inputId"
    @input="$emit('update:modelValue', $event.target.value)"
    class="form-control form-md mb-3"
  />
</template>

<script lang="ts" src="./GenesisInputComponent.ts"></script>
