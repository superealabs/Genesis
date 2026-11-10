const MIME_SIGNATURES: Array<{ mimeType: string; signature: number[]; offset?: number }> = [
  { mimeType: 'image/png', signature: [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a] },
  { mimeType: 'image/jpeg', signature: [0xff, 0xd8, 0xff] },
  { mimeType: 'image/gif', signature: [0x47, 0x49, 0x46, 0x38] },
  { mimeType: 'application/pdf', signature: [0x25, 0x50, 0x44, 0x46] },
  { mimeType: 'application/zip', signature: [0x50, 0x4b, 0x03, 0x04] },
  { mimeType: 'application/x-rar-compressed', signature: [0x52, 0x61, 0x72, 0x21, 0x1a, 0x07] },
  { mimeType: 'application/x-7z-compressed', signature: [0x37, 0x7a, 0xbc, 0xaf, 0x27, 0x1c] },
  { mimeType: 'application/gzip', signature: [0x1f, 0x8b] },
  { mimeType: 'image/webp', signature: [0x52, 0x49, 0x46, 0x46], offset: 0 },
  { mimeType: 'image/bmp', signature: [0x42, 0x4d] },
  { mimeType: 'image/x-icon', signature: [0x00, 0x00, 0x01, 0x00] },
  { mimeType: 'image/tiff', signature: [0x49, 0x49, 0x2a, 0x00] },
  { mimeType: 'image/tiff', signature: [0x4d, 0x4d, 0x00, 0x2a] },
]

const MIME_EXTENSIONS: Record<string, string> = {
  'application/pdf': 'pdf',
  'application/octet-stream': 'bin',
  'application/json': 'json',
  'application/msword': 'doc',
  'application/vnd.ms-excel': 'xls',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
  'application/vnd.oasis.opendocument.spreadsheet': 'ods',
  'application/vnd.oasis.opendocument.text': 'odt',
  'application/zip': 'zip',
  'application/x-7z-compressed': '7z',
  'application/x-tar': 'tar',
  'application/gzip': 'gz',
  'application/x-rar-compressed': 'rar',
  'application/xml': 'xml',
  'audio/mpeg': 'mp3',
  'audio/mp4': 'm4a',
  'audio/ogg': 'ogg',
  'audio/wav': 'wav',
  'image/bmp': 'bmp',
  'image/gif': 'gif',
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'image/tiff': 'tiff',
  'image/webp': 'webp',
  'image/x-icon': 'ico',
  'text/html': 'html',
  'text/csv': 'csv',
  'text/plain': 'txt',
  'text/xml': 'xml',
  'video/mp4': 'mp4',
  'video/quicktime': 'mov',
  'video/webm': 'webm',
}

const EXTENSION_MIME_TYPES: Record<string, string> = {
  csv: 'text/csv',
  doc: 'application/msword',
  docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  '7z': 'application/x-7z-compressed',
  gz: 'application/gzip',
  html: 'text/html',
  json: 'application/json',
  m4a: 'audio/mp4',
  mp3: 'audio/mpeg',
  mov: 'video/quicktime',
  mp4: 'video/mp4',
  ods: 'application/vnd.oasis.opendocument.spreadsheet',
  odt: 'application/vnd.oasis.opendocument.text',
  ogg: 'audio/ogg',
  pdf: 'application/pdf',
  rar: 'application/x-rar-compressed',
  txt: 'text/plain',
  xls: 'application/vnd.ms-excel',
  xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  tar: 'application/x-tar',
  wav: 'audio/wav',
  webm: 'video/webm',
  xml: 'text/xml',
  zip: 'application/zip',
}

type FileMetadata = { mimeType: string }

const FILE_METADATA = new Map<string, FileMetadata>()
let lastFileContent: string | undefined
let lastFileMetadata: FileMetadata | undefined

function getFileMetadataKey(content: string): string {
  let firstHash = 0x811c9dc5
  let secondHash = 0x9e3779b9
  for (let index = 0; index < content.length; index += 1) {
    const code = content.charCodeAt(index)
    firstHash = Math.imul(firstHash ^ code, 0x01000193)
    secondHash = Math.imul(secondHash ^ code, 0x85ebca6b)
  }
  return `${content.length}:${firstHash >>> 0}:${secondHash >>> 0}`
}

function getRememberedMetadata(content: string): FileMetadata | undefined {
  if (content === lastFileContent) return lastFileMetadata
  const metadata = FILE_METADATA.get(getFileMetadataKey(content))
  lastFileContent = content
  lastFileMetadata = metadata
  return metadata
}

function rememberFileMetadata(content: string, file: File): void {
  if (FILE_METADATA.size >= 64) {
    const oldestKey = FILE_METADATA.keys().next().value
    if (oldestKey) FILE_METADATA.delete(oldestKey)
  }
  const metadata = {
    mimeType: file.type || EXTENSION_MIME_TYPES[file.name.split('.').pop()?.toLowerCase() ?? ''] || 'application/octet-stream',
  }
  FILE_METADATA.set(getFileMetadataKey(content), metadata)
  lastFileContent = content
  lastFileMetadata = metadata
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

function containsAscii(bytes: Uint8Array, value: string): boolean {
  for (let index = 0; index <= bytes.length - value.length; index += 1) {
    let matches = true
    for (let characterIndex = 0; characterIndex < value.length; characterIndex += 1) {
      if (bytes[index + characterIndex] !== value.charCodeAt(characterIndex)) {
        matches = false
        break
      }
    }
    if (matches) return true
  }
  return false
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

export function detectMimeType(content: unknown, fileName?: string): string {
  if (typeof content === 'string') {
    const rememberedMimeType = getRememberedMetadata(content)?.mimeType
    if (rememberedMimeType && rememberedMimeType !== 'application/octet-stream') return rememberedMimeType
  }
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
  if (bytes[0] === 0x50 && bytes[1] === 0x4b) {
    if (containsAscii(bytes, 'word/')) return 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    if (containsAscii(bytes, 'xl/')) return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    return 'application/zip'
  }

  const extension = fileName?.split('.').pop()?.toLowerCase()
  return extension ? EXTENSION_MIME_TYPES[extension] ?? 'application/octet-stream' : 'application/octet-stream'
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
      const base64 = commaIndex >= 0 ? reader.result.substring(commaIndex + 1) : reader.result
      rememberFileMetadata(base64, file)
      resolve(base64)
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
  if (content && typeof content === 'object' && 'name' in content && typeof content.name === 'string') {
    return content.name
  }
  const extension = MIME_EXTENSIONS[detectMimeType(content, prefix)] ?? 'bin'
  return `${prefix.replace(/\.[^.]+$/, '')}.${extension}`
}

export function getFileTypeLabel(content: unknown, fileName: string): string {
  const extension = fileName.split('.').pop()?.toLowerCase() ?? ''
  if (extension === 'pdf') return 'PDF'
  if (['doc', 'docx', 'odt'].includes(extension)) return 'DOC'
  if (['xls', 'xlsx', 'ods'].includes(extension)) return 'XLS'
  if (extension === 'rar') return 'RAR'
  if (['zip', '7z', 'tar', 'gz'].includes(extension)) return 'ZIP'
  if (extension === 'csv') return 'CSV'
  if (extension === 'txt') return 'TXT'
  if (['json', 'xml'].includes(extension)) return extension.toUpperCase()

  const mimeType = detectMimeType(content, fileName)
  if (mimeType.startsWith('audio/')) return 'AUDIO'
  if (mimeType.startsWith('video/')) return 'VIDEO'
  if (!extension || extension === 'bin') return 'FILE'
  return extension.slice(0, 4).toUpperCase()
}

export function getFileSize(content: unknown): string {
  const byteLength = content instanceof Blob ? content.size : getBytes(content)?.byteLength
  if (byteLength == null) return ''
  if (byteLength < 1024) return `${byteLength} B`
  const units = ['KB', 'MB', 'GB']
  let size = byteLength / 1024
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex += 1
  }
  return `${size.toFixed(size >= 10 ? 0 : 1)} ${units[unitIndex]}`
}

export function downloadFile(content: unknown, fileName?: string): void {
  if (content == null || content === '') return

  const link = document.createElement('a')
  const objectUrl = content instanceof Blob ? URL.createObjectURL(content) : undefined
  link.href = objectUrl ?? buildFileSource(content)
  if (!link.href) return
  link.download = fileName ?? getGeneratedFileName(content)
  document.body.appendChild(link)
  link.click()
  link.remove()

  if (objectUrl) window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
}