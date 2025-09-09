<template>
  <font-awesome-icon icon="fa-solid fa-trash"></font-awesome-icon>
</template>

<script lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'TrashIcon',
  components: { FontAwesomeIcon },
  setup() {
    return {}
  },
})
</script>
