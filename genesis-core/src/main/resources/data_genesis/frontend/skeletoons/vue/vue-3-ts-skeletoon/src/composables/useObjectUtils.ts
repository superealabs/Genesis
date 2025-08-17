export function useObjectUtils() {
  const getSecondValue = (obj: object | undefined) => {
    return getNValue(obj, 1);
  };

  const getNValue = (obj: object | undefined, n: number) => {
    if (!obj) return "";
    return Object.values(obj)[n] ?? "";
  };

  return { getSecondValue, getNValue };
}
