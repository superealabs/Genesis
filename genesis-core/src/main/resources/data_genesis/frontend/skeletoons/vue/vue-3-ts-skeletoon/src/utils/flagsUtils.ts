export const flags = {
  fr: 'FR',
  en: 'GB',
}

export function getFlag(lang: string) {
  const flagsObject = Object(flags)
  const result = flagsObject[lang]
  if (!result) {
    return ''
  }
  return result
}

export function getFlagUrl(lang: string) {
  const flag = getFlag(lang)
  return `https://flagsapi.com/${flag}/shiny/32.png`
}
