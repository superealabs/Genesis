function extractBase64(content: unknown): string {
  if (typeof content === 'string') {
    const trimmedContent = content.trim();

    if (trimmedContent.startsWith('data:')) {
      const commaIndex = trimmedContent.indexOf(',');

      return commaIndex >= 0
        ? trimmedContent.substring(commaIndex + 1)
        : '';
    }

    return trimmedContent;
  }

  const bytes =
    content instanceof Uint8Array
      ? Array.from(content)
      : Array.isArray(content)
        ? content.map(Number)
        : [];

  if (bytes.length === 0) {
    return '';
  }

  let binary = '';

  for (let index = 0; index < bytes.length; index += 8192) {
    binary += String.fromCharCode(
      ...bytes.slice(index, index + 8192)
    );
  }

  return btoa(binary);
}

const MIME_EXTENSIONS: Record<string, string> = {
  'application/pdf': 'pdf', 'application/zip': 'zip',
  'application/x-rar-compressed': 'rar', 'application/x-7z-compressed': '7z',
  'application/gzip': 'gz', 'application/json': 'json', 'application/xml': 'xml',
  'application/msword': 'doc', 'application/vnd.ms-excel': 'xls',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
  'application/vnd.oasis.opendocument.text': 'odt', 'application/vnd.oasis.opendocument.spreadsheet': 'ods',
  'application/x-tar': 'tar',
  'text/plain': 'txt', 'text/csv': 'csv', 'image/png': 'png', 'image/jpeg': 'jpg',
  'image/gif': 'gif', 'image/webp': 'webp', 'image/bmp': 'bmp', 'image/tiff': 'tiff',
  'image/x-icon': 'ico', 'audio/mpeg': 'mp3', 'audio/ogg': 'ogg', 'audio/wav': 'wav',
  'video/mp4': 'mp4', 'video/webm': 'webm', 'video/quicktime': 'mov'
};

const EXTENSION_MIME_TYPES = Object.fromEntries(
  Object.entries(MIME_EXTENSIONS).map(([mime, extension]) => [extension, mime])
);
const FILE_MIME_TYPES = new Map<string, string>();

function mimeCacheKey(content: string): string {
  let hash = 0x811c9dc5;
  for (let index = 0; index < content.length; index += 1) {
    hash = Math.imul(hash ^ content.charCodeAt(index), 0x01000193);
  }
  return `${content.length}:${hash >>> 0}`;
}

function rememberFileMimeType(base64: string, file: File): void {
  const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
  const mimeType = file.type || EXTENSION_MIME_TYPES[extension];
  if (!mimeType) return;
  const key = mimeCacheKey(base64);
  if (FILE_MIME_TYPES.size >= 64) FILE_MIME_TYPES.delete(FILE_MIME_TYPES.keys().next().value!);
  FILE_MIME_TYPES.set(key, mimeType);
}

function extractBytes(content: unknown): number[] {
  if (content instanceof Uint8Array) {
    return Array.from(content);
  }

  if (Array.isArray(content)) {
    return content.map(Number);
  }

  const base64 = extractBase64(content);

  if (!base64) {
    return [];
  }

  try {
    const cleanedBase64 = base64.replace(/\s/g, '');

    const sample = cleanedBase64.substring(0, 128);
    const paddedSample = sample.padEnd(
      Math.ceil(sample.length / 4) * 4,
      '='
    );

    const binary = atob(paddedSample);

    return Array.from(binary).map(character =>
      character.charCodeAt(0)
    );
  } catch {
    return [];
  }
}

function extractMimeFromDataUrl(content: unknown): string | null {
  if (typeof content !== 'string') {
    return null;
  }

  const match = content.match(/^data:([^;,]+)[;,]/i);

  return match?.[1] ?? null;
}

export function detectMimeType(content: unknown): string {
  if (typeof content === 'string') {
    const rememberedMime = FILE_MIME_TYPES.get(mimeCacheKey(extractBase64(content)));
    if (rememberedMime) return rememberedMime;
  }
  const dataUrlMime = extractMimeFromDataUrl(content);

  if (dataUrlMime) {
    return dataUrlMime;
  }

  const bytes = extractBytes(content);

  // PNG
  if (
    bytes[0] === 0x89 &&
    bytes[1] === 0x50 &&
    bytes[2] === 0x4e &&
    bytes[3] === 0x47
  ) {
    return 'image/png';
  }

  // JPEG
  if (
    bytes[0] === 0xff &&
    bytes[1] === 0xd8 &&
    bytes[2] === 0xff
  ) {
    return 'image/jpeg';
  }

  // GIF
  if (
    bytes[0] === 0x47 &&
    bytes[1] === 0x49 &&
    bytes[2] === 0x46
  ) {
    return 'image/gif';
  }

  // PDF
  if (
    bytes[0] === 0x25 &&
    bytes[1] === 0x50 &&
    bytes[2] === 0x44 &&
    bytes[3] === 0x46
  ) {
    return 'application/pdf';
  }

  // BMP
  if (
    bytes[0] === 0x42 &&
    bytes[1] === 0x4d
  ) {
    return 'image/bmp';
  }

  // WEBP
  if (
    bytes[0] === 0x52 &&
    bytes[1] === 0x49 &&
    bytes[2] === 0x46 &&
    bytes[3] === 0x46 &&
    bytes[8] === 0x57 &&
    bytes[9] === 0x45 &&
    bytes[10] === 0x42 &&
    bytes[11] === 0x50
  ) {
    return 'image/webp';
  }

  // TIFF, ICO, ZIP, RAR, 7z et gzip
  if (bytes[0] === 0x49 && bytes[1] === 0x49 && bytes[2] === 0x2a && bytes[3] === 0x00) return 'image/tiff';
  if (bytes[0] === 0x4d && bytes[1] === 0x4d && bytes[2] === 0x00 && bytes[3] === 0x2a) return 'image/tiff';
  if (bytes[0] === 0x00 && bytes[1] === 0x00 && bytes[2] === 0x01 && bytes[3] === 0x00) return 'image/x-icon';
  if (bytes[0] === 0x50 && bytes[1] === 0x4b && bytes[2] === 0x03 && bytes[3] === 0x04) {
    return 'application/zip';
  }
  if (bytes[0] === 0x52 && bytes[1] === 0x61 && bytes[2] === 0x72 && bytes[3] === 0x21) return 'application/x-rar-compressed';
  if (bytes[0] === 0x37 && bytes[1] === 0x7a && bytes[2] === 0xbc && bytes[3] === 0xaf) return 'application/x-7z-compressed';
  if (bytes[0] === 0x1f && bytes[1] === 0x8b) return 'application/gzip';

  return 'application/octet-stream';
}

export function isImageContent(content: unknown): boolean {
  return detectMimeType(content).startsWith('image/');
}

export function buildFileSource(content: unknown): string {
  if (
    typeof content === 'string' &&
    content.startsWith('data:')
  ) {
    return content;
  }

  const base64 = extractBase64(content);

  if (!base64) {
    return '';
  }

  return `data:${detectMimeType(content)};base64,${base64}`;
}

export function getGeneratedFileName(content: unknown, prefix = 'fichier'): string {
  if (content && typeof content === 'object' && 'name' in content && typeof content.name === 'string') {
    return content.name;
  }
  const extensions: Record<string, string> = {
    'image/png': 'png',
    'image/jpeg': 'jpg',
    'image/gif': 'gif',
    'image/webp': 'webp',
    'image/bmp': 'bmp',
    'application/pdf': 'pdf'
  };

  const extension = extensions[detectMimeType(content)] ?? MIME_EXTENSIONS[detectMimeType(content)] ?? 'bin';

  return `${prefix.replace(/\.[^.]+$/, '')}.${extension}`;
}

export function getFileTypeLabel(content: unknown, fileName = getGeneratedFileName(content)): string {
  const extension = fileName.split('.').pop()?.toLowerCase() ?? '';
  if (!extension || extension === 'bin') return 'FILE';
  return extension.slice(0, 4).toUpperCase();
}

export function getFileSize(content: unknown): string {
  let bytes = 0;
  if (content instanceof Blob) bytes = content.size;
  else if (content instanceof Uint8Array) bytes = content.byteLength;
  else if (Array.isArray(content)) bytes = content.length;
  else if (typeof content === 'string') {
    try { bytes = atob(extractBase64(content)).length; } catch { return ''; }
  }
  if (bytes < 1024) return `${bytes} B`;
  const size = bytes / 1024;
  return `${size >= 1024 ? (size / 1024).toFixed(1) + ' MB' : size.toFixed(1) + ' KB'}`;
}

export function downloadFile(content: unknown, fileName?: string): void {
  if (content == null || content === '') return;

  const source = content instanceof Blob ? URL.createObjectURL(content) : buildFileSource(content);
  if (!source) return;

  const link = document.createElement('a');
  link.href = source;
  link.download = fileName ?? getGeneratedFileName(content);
  document.body.appendChild(link);
  link.click();
  link.remove();
  if (content instanceof Blob) window.setTimeout(() => URL.revokeObjectURL(source), 1000);
}

export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = () => {
      if (typeof reader.result !== 'string') {
        reject(new Error('Unable to read the selected file'));
        return;
      }

      const separatorIndex = reader.result.indexOf(',');
      const base64 = separatorIndex >= 0 ? reader.result.substring(separatorIndex + 1) : reader.result;
      rememberFileMimeType(base64, file);
      resolve(base64);
    };

    reader.onerror = () => {
      reject(
        reader.error ??
        new Error('An error occurred while reading the file')
      );
    };

    reader.readAsDataURL(file);
  });
}
