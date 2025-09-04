<template>
  <font-awesome-icon icon="fas fa-undo"></font-awesome-icon>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'ReloadIcon',
  components: { FontAwesomeIcon },
  setup() {
    return {}
  },
})
</script>
