/**
 * Déclenche le téléchargement d'un fichier dans le navigateur à partir d'un objet Blob.
 * * @param blob Les données binaires (Blob) représentant le contenu du fichier.
 * @param filename Le nom souhaité pour le fichier téléchargé (ex: 'data.csv').
 */
export function triggerFileDownload(blob: Blob, filename: string): void {
  // 1. Crée une URL temporaire pour le Blob
  const urlObject = window.URL.createObjectURL(blob)

  // 2. Crée un tag <a> temporaire pour déclencher le téléchargement
  const link = document.createElement('a')
  link.href = urlObject
  link.setAttribute('download', filename)

  // 3. Ajoute au corps, clique, et supprime pour lancer le téléchargement
  document.body.appendChild(link)
  link.click()
  link.remove()

  // 4. Libère la mémoire de l'objet URL
  window.URL.revokeObjectURL(urlObject)
}

/**
 * Tente d'extraire le nom du fichier à partir de l'en-tête Content-Disposition.
 * * @param contentDisposition La valeur de l'en-tête Content-Disposition (peut être null).
 * @param defaultFilename Le nom de fichier par défaut à utiliser en cas d'échec d'extraction.
 * @returns Le nom du fichier extrait ou par défaut.
 */
export function getFilenameFromHeaders(contentDisposition: string | null | undefined, defaultFilename: string): string {
  if (contentDisposition) {
    // Expression régulière pour extraire le nom du fichier : filename="value" ou filename=value
    const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/i);
    if (filenameMatch && filenameMatch[1]) {
      return filenameMatch[1];
    }
  }
  return defaultFilename;
}
