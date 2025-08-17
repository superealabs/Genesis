import { SelectOption } from "./SelectOption";

// SearchModel.ts
export interface EntitySearchField {
  key: string;
  label: string;
  type: string;
  sortable: boolean;
  searchKey?: string | number;
  options?: SelectOption[];
  optionsLoader?: () => Promise<SelectOption[]>;
}
