export const fileToArray = async (file: File): Promise<number[]> => {
    const buffer = await file.arrayBuffer()
    return Array.from(new Uint8Array(buffer))
}

export const bytesToUrl = (bytes: number[], mimeType = "image/png"): string => {
    const blob = new Blob([new Uint8Array(bytes)], { type: mimeType });
    return URL.createObjectURL(blob);
};

export const base64ToUrl = (
    base64: string,
    mimeType = "image/png"
): string => {
    return `data:${mimeType};base64,${base64}`;
};