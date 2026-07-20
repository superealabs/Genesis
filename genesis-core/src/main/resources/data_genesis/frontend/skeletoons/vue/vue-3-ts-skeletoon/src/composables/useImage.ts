export function getUrl(image?: Uint8Array) {
  if (!image) return ''

  return `data:image/png;base64,${image}`
}
