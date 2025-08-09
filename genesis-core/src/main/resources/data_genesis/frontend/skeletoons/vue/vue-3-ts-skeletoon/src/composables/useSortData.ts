import { SortFieldParameter } from "@/models/api/RequestModel";

export function useSortData() {
  const sortFieldsParameters: SortFieldParameter[] = [];
  sortFieldsParameters.push(new SortFieldParameter("id", "asc"));

  return {
    sortFieldsParameters,
  };
}
