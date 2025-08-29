// stores/useDepartementsStore.ts
import { defineStore } from 'pinia'

export const usePaginationOptionsStore = defineStore('paginationOptions', {
  state: () => ({
    pagination: {
      size: 10,
      itemsPerPageOptions: [
        { label: '5', value: 5 },
        { label: '10', value: 10 },
        { label: '50', value: 50 },
        { label: '100', value: 100 },
      ],
    },
  }),
  actions: {},
})
