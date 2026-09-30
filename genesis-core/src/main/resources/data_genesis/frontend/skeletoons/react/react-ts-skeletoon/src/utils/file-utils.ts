const MIME_SIGNATURES: Array<{ mime: string; bytes: number[] }> = [
    { mime: 'image/png', bytes: [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a] },
    { mime: 'image/jpeg', bytes: [0xff, 0xd8, 0xff] },
    { mime: 'image/gif', bytes: [0x47, 0x49, 0x46, 0x38] },
    { mime: 'application/pdf', bytes: [0x25, 0x50, 0x44, 0x46] },
    { mime: 'image/bmp', bytes: [0x42, 0x4d] },
    { mime: 'image/tiff', bytes: [0x49, 0x49, 0x2a, 0x00] },
    { mime: 'image/x-icon', bytes: [0x00, 0x00, 0x01, 0x00] },
];

const EXTENSIONS: Record<string, string> = {
    'image/png': 'png', 'image/jpeg': 'jpg', 'image/gif': 'gif', 'image/webp': 'webp',
    'image/bmp': 'bmp', 'image/tiff': 'tiff', 'image/x-icon': 'ico', 'application/pdf': 'pdf',
    'application/zip': 'zip', 'application/x-rar-compressed': 'rar', 'application/x-7z-compressed': '7z',
    'application/gzip': 'gz', 'application/json': 'json', 'application/xml': 'xml', 'text/plain': 'txt',
    'text/csv': 'csv', 'application/msword': 'doc', 'application/vnd.ms-excel': 'xls',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
    'application/vnd.oasis.opendocument.text': 'odt', 'application/vnd.oasis.opendocument.spreadsheet': 'ods',
};

const EXTENSION_MIMES = Object.fromEntries(Object.entries(EXTENSIONS).map(([mime, ext]) => [ext, mime]));
const fileMetadata = new Map<string, { mime: string; name: string }>();

function contentKey(content: string): string {
    let hash = 0x811c9dc5;
    for (let index = 0; index < content.length; index += 1) hash = Math.imul(hash ^ content.charCodeAt(index), 0x01000193);
    return `${content.length}:${hash >>> 0}`;
}

function bytesOf(content: unknown): Uint8Array | null {
    if (content instanceof Uint8Array) return content;
    if (content instanceof ArrayBuffer) return new Uint8Array(content);
    if (ArrayBuffer.isView(content)) return new Uint8Array(content.buffer, content.byteOffset, content.byteLength);
    if (Array.isArray(content)) return Uint8Array.from(content.map(Number));
    if (typeof content !== 'string') return null;
    const value = content.match(/^data:[^,]*,(.*)$/s)?.[1] ?? content;
    try {
        const binary = atob(value.replace(/\s/g, ''));
        return Uint8Array.from(binary, (character) => character.charCodeAt(0));
    } catch {
        return null;
    }
}

function containsAscii(bytes: Uint8Array, value: string): boolean {
    const signature = new TextEncoder().encode(value);
    for (let index = 0; index <= bytes.length - signature.length; index += 1) {
        if (signature.every((byte, offset) => bytes[index + offset] === byte)) return true;
    }
    return false;
}

function toBase64(content: unknown): string {
    if (typeof content === 'string') {
        const value = content.match(/^data:[^,]*,(.*)$/s)?.[1] ?? content;
        return value.replace(/\s/g, '');
    }
    const bytes = bytesOf(content);
    if (!bytes) return '';
    let binary = '';
    for (let index = 0; index < bytes.length; index += 0x8000) {
        binary += String.fromCharCode(...bytes.subarray(index, index + 0x8000));
    }
    return btoa(binary);
}

export function detectMimeType(content: unknown): string {
    if (typeof content === 'string') {
        const remembered = fileMetadata.get(contentKey(toBase64(content)))?.mime;
        if (remembered && remembered !== 'application/octet-stream') return remembered;
        const dataUrlMime = content.match(/^data:([^;,]+)/)?.[1];
        if (dataUrlMime && dataUrlMime !== 'application/octet-stream') return dataUrlMime;
    }
    if (content instanceof Blob && content.type) return content.type;
    const bytes = bytesOf(content);
    if (!bytes) return 'application/octet-stream';
    for (const { mime, bytes: signature } of MIME_SIGNATURES) {
        if (signature.every((byte, index) => bytes[index] === byte)) return mime;
    }
    if (String.fromCharCode(...bytes.subarray(0, 4)) === 'RIFF' && String.fromCharCode(...bytes.subarray(8, 12)) === 'WEBP') return 'image/webp';
    if (bytes[0] === 0x50 && bytes[1] === 0x4b) {
        if (containsAscii(bytes, 'word/')) return 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
        if (containsAscii(bytes, 'xl/')) return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
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

export function fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            if (typeof reader.result !== 'string') return reject(new Error('Unable to read the selected file'));
            const base64 = reader.result.substring(reader.result.indexOf(',') + 1);
            const mime = (file.type && file.type !== 'application/octet-stream' ? file.type : undefined)
                || EXTENSION_MIMES[file.name.split('.').pop()?.toLowerCase() ?? '']
                || 'application/octet-stream';
            if (fileMetadata.size >= 64) fileMetadata.delete(fileMetadata.keys().next().value!);
            fileMetadata.set(contentKey(base64), { mime, name: file.name });
            resolve(base64);
        };
        reader.onerror = () => reject(reader.error ?? new Error('Unable to read the selected file'));
        reader.readAsDataURL(file);
    });
}

export function buildFileSource(content: unknown): string {
    if (typeof content === 'string' && content.startsWith('data:')) return content;
    const base64 = toBase64(content);
    return base64 ? `data:${detectMimeType(content)};base64,${base64}` : '';
}

export function getGeneratedFileName(content: unknown, prefix = 'fichier'): string {
    const metadata = typeof content === 'string' ? fileMetadata.get(contentKey(toBase64(content))) : undefined;
    if (metadata?.name) return metadata.name;
    if (content && typeof content === 'object' && 'name' in content && typeof content.name === 'string') return content.name;
    const mime = detectMimeType(content);
    return `${prefix.replace(/\.[^.]+$/, '')}.${EXTENSIONS[mime] ?? 'bin'}`;
}

export function getFileTypeLabel(content: unknown, fileName = getGeneratedFileName(content)): string {
    const extension = fileName.split('.').pop()?.toLowerCase() ?? '';
    if (!extension || extension === 'bin') return 'FILE';
    if (['doc', 'docx', 'odt'].includes(extension)) return 'DOC';
    if (['xls', 'xlsx', 'ods'].includes(extension)) return 'XLS';
    if (['zip', '7z', 'tar', 'gz', 'rar'].includes(extension)) return 'ZIP';
    return extension.slice(0, 4).toUpperCase();
}

export function getFileSize(content: unknown): string {
    const size = content instanceof Blob ? content.size : bytesOf(content)?.byteLength;
    if (size == null) return '';
    if (size < 1024) return `${size} B`;
    const kb = size / 1024;
    return kb < 1024 ? `${kb.toFixed(1)} KB` : `${(kb / 1024).toFixed(1)} MB`;
}

export function downloadFile(content: unknown, fileName?: string): void {
    if (content == null || content === '') return;
    const objectUrl = content instanceof Blob ? URL.createObjectURL(content) : undefined;
    const href = objectUrl ?? buildFileSource(content);
    if (!href) return;
    const link = document.createElement('a');
    link.href = href;
    link.download = fileName ?? getGeneratedFileName(content);
    document.body.appendChild(link);
    link.click();
    link.remove();
    if (objectUrl) window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
}
