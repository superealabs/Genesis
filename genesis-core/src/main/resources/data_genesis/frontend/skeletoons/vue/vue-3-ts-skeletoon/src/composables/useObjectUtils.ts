export function useObjectUtils() {
  const getSecondValue = (obj: object) => {
    return getNValue(obj, 1)
  }

  const getNValue = (obj: object, n: number) => {
    if (!obj) return ''
    return Object.values(obj)[n] ?? ''
  }

  const getNKey = (obj: object, n: number) => {
    if (!obj) return ''
    return Object.keys(obj)[n] ?? ''
  }

  return { getSecondValue, getNValue, getNKey }
}
