import { type Ref, ref } from 'vue'

export type RowItem<T> = { _internalId: number; data: T }

let nextId = 1
const getNextId = () => nextId++

export function useTableForm<T extends object>(
  initialData: T[] = [],
  rowTemplate: (init: Partial<T>) => T,
) {
  if (initialData.length > 0) {
    nextId = initialData.length + 1
  }

  const initialRows: RowItem<T>[] = initialData.map((data, index) => ({
    data,
    _internalId: index + 1,
  })) as RowItem<T>[]

  const rows = ref<RowItem<T>[]>(initialRows) as Ref<RowItem<T>[]>

  const addRow = (qte = 1, defaultData: Partial<T> = {}) => {
    for (let i = 0; i < qte; i++) {
      const newRow = {
        data: rowTemplate(defaultData),
        _internalId: getNextId(),
      } as RowItem<T>
      rows.value.push(newRow)
    }
  }

  const removeRow = (id: number) => {
    if (rows.value.length > 1) {
      rows.value = rows.value.filter((row) => row._internalId !== id)
    } else {
      // Optionnel: Empêcher la suppression de la dernière ligne et la réinitialiser
      console.warn('Cannot remove the last row. Resetting fields instead.')
      const lastRow = rows.value[0]
      // Réinitialiser les champs de la dernière ligne
      Object.assign(lastRow, rowTemplate({}))
    }
  }

  const getTableData = (): T[] => {
    return rows.value
      .map((row) => {
        const { data } = row
        return data
      })
      .filter((data) => data !== null) as T[]
  }

  if (rows.value.length === 0) {
    addRow(1)
  }

  return {
    rows,
    addRow,
    removeRow,
    getTableData,
  }
}
