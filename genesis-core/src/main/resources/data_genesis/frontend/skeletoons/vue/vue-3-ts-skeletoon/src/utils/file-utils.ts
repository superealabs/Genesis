const MIME_SIGNATURES: Array<{ mimeType: string; signature: number[]; offset?: number }> = [
  { mimeType: 'image/png', signature: [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a] },
  { mimeType: 'image/jpeg', signature: [0xff, 0xd8, 0xff] },
  { mimeType: 'image/gif', signature: [0x47, 0x49, 0x46, 0x38] },
  { mimeType: 'application/pdf', signature: [0x25, 0x50, 0x44, 0x46] },
  { mimeType: 'image/webp', signature: [0x52, 0x49, 0x46, 0x46], offset: 0 },
  { mimeType: 'image/bmp', signature: [0x42, 0x4d] },
  { mimeType: 'image/x-icon', signature: [0x00, 0x00, 0x01, 0x00] },
  { mimeType: 'image/tiff', signature: [0x49, 0x49, 0x2a, 0x00] },
  { mimeType: 'image/tiff', signature: [0x4d, 0x4d, 0x00, 0x2a] },
]

const MIME_EXTENSIONS: Record<string, string> = {
  'application/pdf': 'pdf',
  'application/octet-stream': 'bin',
  'image/bmp': 'bmp',
  'image/gif': 'gif',
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'image/tiff': 'tiff',
  'image/webp': 'webp',
  'image/x-icon': 'ico',
}

function decodeBase64(value: string): Uint8Array | null {
  try {
    const binary = atob(value.replace(/\s/g, ''))
    return Uint8Array.from(binary, (character) => character.charCodeAt(0))
  } catch {
    return null
  }
}

function getBytes(content: unknown): Uint8Array | null {
  if (content instanceof Uint8Array) return content
  if (content instanceof ArrayBuffer) return new Uint8Array(content)
  if (ArrayBuffer.isView(content)) {
    return new Uint8Array(content.buffer, content.byteOffset, content.byteLength)
  }
  if (typeof content !== 'string') return null

  const dataUrlMatch = content.match(/^data:[^,]*,(.*)$/s)
  const value = dataUrlMatch ? dataUrlMatch[1] : content
  const decoded = decodeBase64(value)
  return decoded ?? new TextEncoder().encode(value)
}

function toBase64(content: unknown): string {
  if (typeof content === 'string') {
    const dataUrlMatch = content.match(/^data:[^,]*;base64,(.*)$/s)
    return (dataUrlMatch ? dataUrlMatch[1] : content).replace(/\s/g, '')
  }

  const bytes = getBytes(content)
  if (!bytes) return ''

  let binary = ''
  const chunkSize = 0x8000
  for (let index = 0; index < bytes.length; index += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(index, index + chunkSize))
  }
  return btoa(binary)
}

export function detectMimeType(content: unknown): string {
  if (typeof content === 'string') {
    const dataUrlMimeType = content.match(/^data:([^;,]+)/)?.[1]
    if (dataUrlMimeType && dataUrlMimeType !== 'application/octet-stream') return dataUrlMimeType
  }
  if (content instanceof Blob && content.type) return content.type

  const bytes = getBytes(content)
  if (!bytes) return 'application/octet-stream'

  for (const { mimeType, signature } of MIME_SIGNATURES) {
    const matches = signature.every((byte, index) => bytes[index] === byte)
    if (!matches) continue
    if (mimeType !== 'image/webp' || String.fromCharCode(...bytes.slice(8, 12)) === 'WEBP') {
      return mimeType
    }
  }
  return 'application/octet-stream'
}

export function isImageContent(content: unknown): boolean {
  return detectMimeType(content).startsWith('image/')
}

export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      if (typeof reader.result !== 'string') {
        reject(new Error('Unable to read the selected file'))
        return
      }
      const commaIndex = reader.result.indexOf(',')
      resolve(commaIndex >= 0 ? reader.result.substring(commaIndex + 1) : reader.result)
    }
    reader.onerror = () => reject(reader.error ?? new Error('Unable to read the selected file'))
    reader.readAsDataURL(file)
  })
}

export function buildFileSource(content: unknown): string {
  if (typeof content === 'string' && content.startsWith('data:')) return content
  const base64 = toBase64(content)
  return base64 ? `data:${detectMimeType(content)};base64,${base64}` : ''
}

export function getGeneratedFileName(content: unknown, prefix = 'fichier'): string {
  const extension = MIME_EXTENSIONS[detectMimeType(content)] ?? 'bin'
  return `${prefix}.${extension}`
}

export function downloadFile(content: unknown, fileName?: string): void {
  if (content == null || content === '') return

  const link = document.createElement('a')
  link.href = content instanceof Blob ? URL.createObjectURL(content) : buildFileSource(content)
  link.download = fileName ?? getGeneratedFileName(content)
  document.body.appendChild(link)
  link.click()
  link.remove()

  if (content instanceof Blob) URL.revokeObjectURL(link.href)
}