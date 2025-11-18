import { type Ref, ref } from 'vue'

export type RowItem<T, Q> = { _internalId: number; data: T; rowValue: Q }

let nextId = 1
const getNextId = () => nextId++

export function useTableForm<T extends object, Q extends object>(initialData: T[] = []) {
  if (initialData.length > 0) {
    nextId = initialData.length + 1
  }

  const initialRows: RowItem<T, Q>[] = initialData.map((data, index) => ({
    data,
    _internalId: index + 1,
    rowValue: {},
  })) as RowItem<T, Q>[]

  const rows = ref<RowItem<T, Q>[]>(initialRows) as Ref<RowItem<T, Q>[]>

  const addRow = (qte = 1, defaultData: Partial<T> = {}) => {
    for (let i = 0; i < qte; i++) {
      const newRow = {
        data: defaultData,
        _internalId: getNextId(),
        rowValue: {},
      } as RowItem<T, Q>
      rows.value.push(newRow)
    }
  }

  const removeRow = (id: number) => {
    if (rows.value.length > 0) {
      rows.value = rows.value.filter((row) => row._internalId !== id)
    } else {
      console.warn('Cannot remove from empty rows')
    }
  }

  const getTableData = (): Q[] => {
    return rows.value
      .map((row) => {
        const { rowValue } = row
        return rowValue
      })
      .filter((data) => data !== null) as Q[]
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
