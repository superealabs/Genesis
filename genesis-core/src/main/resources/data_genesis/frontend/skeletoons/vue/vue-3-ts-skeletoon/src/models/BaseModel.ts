// BaseModel.ts
import { EntitySearchField } from "@/models/EntityModel";

export abstract class BaseModel {
  protected static searchDaoMetadata: EntitySearchField[] = [];

  static getSearchDaoMetadata(): EntitySearchField[] {
    return this.searchDaoMetadata;
  }
}
