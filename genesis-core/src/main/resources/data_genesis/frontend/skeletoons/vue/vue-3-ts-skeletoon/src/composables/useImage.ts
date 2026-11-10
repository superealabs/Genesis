import { onUnmounted, ref, watch } from 'vue'
import { buildFileSource, downloadFile as saveFile } from '@/utils/file-utils'

export function getUrl(image?: unknown) {
  return buildFileSource(image)
}

export function useImagePreview() {
  const previewSource = ref<string | null>(null)
  const previewAlt = ref('')
  const downloadedFile = ref<string | null>(null)
  let feedbackTimeout: ReturnType<typeof setTimeout> | undefined

  const closeImagePreview = () => {
    previewSource.value = null
  }
  const handleKeydown = (event: KeyboardEvent) => {
    if (event.key === 'Escape') closeImagePreview()
  }

  watch(previewSource, (source: string | null) => {
    if (source) window.addEventListener('keydown', handleKeydown)
    else window.removeEventListener('keydown', handleKeydown)
  })
  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
    if (feedbackTimeout) clearTimeout(feedbackTimeout)
  })

  const openImagePreview = (source: string, alt: string) => {
    previewSource.value = source
    previewAlt.value = alt
  }

  const downloadAsset = (content: unknown, fileName: string) => {
    saveFile(content, fileName)
    downloadedFile.value = fileName
    if (feedbackTimeout) clearTimeout(feedbackTimeout)
    feedbackTimeout = setTimeout(() => {
      downloadedFile.value = null
      feedbackTimeout = undefined
    }, 1600)
  }

  return { previewSource, previewAlt, downloadedFile, openImagePreview, closeImagePreview, downloadAsset }
}
