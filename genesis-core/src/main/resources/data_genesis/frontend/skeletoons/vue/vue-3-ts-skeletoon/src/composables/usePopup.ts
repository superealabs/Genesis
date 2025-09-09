import { ref } from 'vue'

export function usePopup(initialVisible = false) {
  const visible = ref(initialVisible)

  function openPopup() {
    visible.value = true
  }

  function closePopup() {
    visible.value = false
  }

  function togglePopup() {
    visible.value = !visible.value
  }

  return {
    visible,
    openPopup,
    closePopup,
    togglePopup,
  }
}
