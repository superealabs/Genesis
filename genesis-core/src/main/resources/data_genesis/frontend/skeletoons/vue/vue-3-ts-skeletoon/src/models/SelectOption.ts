import { useObjectUtils } from "@/composables/useObjectUtils";
import { toRaw } from "vue";

export interface SelectOption {
  label: string | number | undefined;
  value: string | number | undefined;
}

export const createOptionsLoader = (serviceCall: () => any) => async () => {
  const { data } = await serviceCall();
  return extractSelectOptionsFromRawsData(data);
};

export function extractSelectOptionsFromOjectsData(objects: object[]) {
  return extractSelectOptionsFromRawsData(toRaw(objects));
}
export function extractSelectOptionsFromRawsData(
  rawData: object[]
): SelectOption[] {
  const options: SelectOption[] = [];
  const { getNValue } = useObjectUtils();
  rawData.forEach((raw) => {
    options.push({
      value: getNValue(raw as object, 0),
      label: getNValue(raw as object, 1),
    });
  });
  return options;
}
