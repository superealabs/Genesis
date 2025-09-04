<template>
  <font-awesome-icon icon="fa-solid fa-plus"></font-awesome-icon>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'PlusIcon',
  components: { FontAwesomeIcon },
  setup() {
    return {}
  },
})
</script>
