<template>
  <font-awesome-icon icon="fa-solid fa-filter"></font-awesome-icon>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'FilterIcon',
  components: { FontAwesomeIcon },
  setup() {
    return {}
  },
})
</script>
