<template>
  <font-awesome-icon icon="faa fa-circle-exclamation"></font-awesome-icon>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'WarningIcon',
  components: { FontAwesomeIcon },
  setup() {
    return {}
  },
})
</script>
