// BaseModel.ts
import { useObjectUtils } from '@/composables/useObjectUtils'
import type { EntitySearchField } from '@/models/EntityModel'

export abstract class BaseModel {
  protected static searchDaoMetadata: EntitySearchField[] = []

  public static getSearchFieldByKey(key: string): EntitySearchField | undefined {
    const result = this.searchDaoMetadata.find((field) => field.key === key)
    return result
  }

  static getAllSearchFieldsMetadata(): EntitySearchField[] {
    return this.searchDaoMetadata
  }

  static getKey(): string {
    return 'id'
  }

  static getReferenceKey(): string {
    return 'label'
  }

  static createLabelSearchFilter(value: unknown): object {
    return { [this.getReferenceKey()]: value }
  }

  public getReferenceValue(): string {
    return useObjectUtils().getSecondValue(this)
  }

  public getKeyValue(): string {
    return useObjectUtils().getNValue(this, 0)
  }
}
