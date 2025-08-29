// stores/useDepartementsStore.ts
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useFreezeScreen = defineStore('freezeScreen', () => {
  const freezeState = ref(false)
  const freezeMessage = ref('')

  const isFreeze = computed(() => {
    return freezeState
  })

  const getMessage = computed(() => {
    return freezeMessage
  })

  const setFreezeMessage = (msg: string) => {
    freezeMessage.value = msg
  }

  const setFreeze = (state: boolean) => {
    freezeState.value = state
  }

  const freeze = (msg: string) => {
    setFreezeMessage(msg)
    setFreeze(true)
  }
  const unfreeze = () => {
    setFreezeMessage('')
    setFreeze(false)
  }
  const debug = (location: string) => {
    console.log(
      `[DEBUG ${location}] freezeState = ${freezeState.value}, freezeMessage = "${freezeMessage.value}"`,
    )
  }

  return {
    freezeState,
    freezeMessage,
    freeze,
    unfreeze,
    debug,
    isFreeze,
    getMessage,
    setFreeze,
    setFreezeMessage,
  }
})
