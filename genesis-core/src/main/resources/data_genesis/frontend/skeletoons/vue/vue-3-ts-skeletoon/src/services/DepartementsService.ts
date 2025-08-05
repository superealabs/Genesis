// src/services/departement.service.ts
import useRestApi from "../composables/useRestApi";
import type { IPageResponse } from "../models/api/PageResponseModel";
import type { Departements } from "../models/DepartementsModel";

const restApi = useRestApi<IPageResponse<Departements>>();
const BASE_URL = "/departements";

export async function getAll() {
  const response = await restApi.get(BASE_URL);
  return {
    data: response.returnCode === 1 ? response.data?.content || [] : [],
    error: response.returnCode !== 1 ? response.message : null,
  };
}

export async function getById(id: string) {
  const response = await restApi.get(`${BASE_URL}/${id}`);
  return {
    data: response.returnCode === 1 ? response.data : null,
    error: response.returnCode !== 1 ? response.message : null,
  };
}

export async function create(data: Departements) {
  const response = await restApi.post(BASE_URL, data);
  return {
    data: response.returnCode === 1 ? response.data : null,
    error: response.returnCode !== 1 ? response.message : null,
  };
}

export async function update(id: string, data: Departements) {
  const response = await restApi.put(`${BASE_URL}/${id}`, data);
  return {
    data: response.returnCode === 1 ? response.data : null,
    error: response.returnCode !== 1 ? response.message : null,
  };
}

export async function remove(id: string) {
  const response = await restApi.delete(`${BASE_URL}/${id}`);
  return {
    success: response.returnCode === 1,
    error: response.returnCode !== 1 ? response.message : null,
  };
}
