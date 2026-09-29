import { buildFileSource } from '@/utils/file-utils'

export function getUrl(image?: unknown) {
  return buildFileSource(image)
}
