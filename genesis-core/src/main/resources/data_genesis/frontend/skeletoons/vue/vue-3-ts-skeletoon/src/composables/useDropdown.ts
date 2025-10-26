import { ref, onMounted, onBeforeUnmount, type Ref } from 'vue'

/**
 * Manages the visibility and click-outside logic for a generic dropdown component.
 *
 * @param triggerRef Ref for the element that toggles the dropdown (e.g., input field).
 * @param contentRef Ref for the dropdown content element itself.
 * @returns An object containing the visibility state and control methods.
 */
export function useDropdown(
  triggerRef: Ref<HTMLElement | null>,
  contentRef: Ref<HTMLElement | null>,
) {
  const showDropdown = ref(false)

  /**
   * Closes the dropdown.
   */
  const hideDropdown = () => {
    showDropdown.value = false
  }

  /**
   * Opens the dropdown.
   */
  const openDropdown = () => {
    showDropdown.value = true
  }

  /**
   * Toggles the dropdown visibility.
   */
  const toggleDropdown = () => {
    if (showDropdown.value) {
      hideDropdown()
    } else {
      openDropdown()
    }
  }

  /**
   * Global click handler to close the dropdown if the click is outside
   * both the trigger and the dropdown content.
   */
  const handleClickOutside = (event: MouseEvent) => {
    const target = event.target as Node
    // Check if the click occurred within the trigger or the dropdown content
    if (contentRef.value?.contains(target) || triggerRef.value?.contains(target)) {
      return
    }
    hideDropdown()
  }

  onMounted(() => {
    document.addEventListener('click', handleClickOutside)
  })

  onBeforeUnmount(() => {
    document.removeEventListener('click', handleClickOutside)
  })

  return {
    showDropdown,
    openDropdown,
    hideDropdown,
    toggleDropdown,
  }
}
