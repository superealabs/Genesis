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

export function getGeneratedFileName(content: unknown): string {
  const extensions: Record<string, string> = {
    'image/png': 'png',
    'image/jpeg': 'jpg',
    'image/gif': 'gif',
    'image/webp': 'webp',
    'image/bmp': 'bmp',
    'application/pdf': 'pdf'
  };

  const extension =
    extensions[detectMimeType(content)] ?? 'bin';

  return `fichier.${extension}`;
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

      resolve(
        separatorIndex >= 0
          ? reader.result.substring(separatorIndex + 1)
          : reader.result
      );
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
