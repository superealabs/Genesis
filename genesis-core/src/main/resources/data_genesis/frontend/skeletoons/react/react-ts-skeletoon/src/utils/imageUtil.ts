import { buildFileSource } from './file-utils';

export * from './file-utils';

/** @deprecated Use buildFileSource. Kept for existing generated projects. */
export const bytesToUrl = (bytes: number[], mimeType = 'image/png'): string => {
    const source = buildFileSource(Uint8Array.from(bytes));
    return source ? source.replace(/^data:[^;]+/, `data:${mimeType}`) : '';
};

/** @deprecated Use buildFileSource. Kept for existing generated projects. */
export const base64ToUrl = (base64: string, mimeType = 'image/png'): string =>
    base64.startsWith('data:') ? base64 : `data:${mimeType};base64,${base64}`;
