import { ref } from "vue";
import { PaginationData } from "@/models/api/PageResponseModel";
import { PaginationRequestParameter } from "@/models/api/RequestModel";

export function usePagination(defaultItemsPerPage?: number) {
  const page = ref(1);
  const totalPages = ref(1);
  const itemsPerPage = ref(defaultItemsPerPage ?? 10);

  function goToPage(p: number) {
    if (p >= 1 && p <= totalPages.value) {
      page.value = p;
    }
  }

  function setPagination(pagination: PaginationData) {
    page.value = pagination.number ? pagination.number + 1 : 1;
    totalPages.value = pagination.totalPages ?? 1;
    itemsPerPage.value = pagination.size ?? 10;
  }

  function getPaginationRequestParameter() {
    const paginationRequest = new PaginationRequestParameter();
    paginationRequest.setPage(page.value);
    paginationRequest.size = itemsPerPage.value;

    return paginationRequest;
  }

  function jsonPaginationData() {
    return `{ page:${page.value} , totalPages:${totalPages.value}, size:${itemsPerPage.value} }`;
  }

  return {
    page,
    totalPages,
    itemsPerPage,
    goToPage,
    setPagination,
    getPaginationRequestParameter,
    jsonPaginationData,
  };
}
