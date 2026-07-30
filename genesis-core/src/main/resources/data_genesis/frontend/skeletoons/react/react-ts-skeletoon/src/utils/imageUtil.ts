export const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            if (typeof reader.result !== "string") {
                reject(new Error("Le fichier n'a pas pu être converti en Base64"));
                return;
            }
            const commaIndex = reader.result.indexOf(",");
            if (commaIndex === -1) {
                reject(new Error("Le contenu Base64 généré est invalide"));
                return;
            }
            resolve(reader.result.substring(commaIndex + 1));
        };
        reader.onerror = () => {
            reject(reader.error ?? new Error("Erreur pendant la lecture du fichier"));
        };
        reader.readAsDataURL(file);
    });
};

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