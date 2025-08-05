// src/services/api.ts
import axios, { AxiosRequestConfig } from "axios";
import RestResponse from "../models/api/RestResponseModel";

export default function useRestApi<T>() {
  const instance = axios.create({
    baseURL: process.env.VUE_APP_API_URL || "http://localhost:8080",
    headers: {
      "Content-Type": "application/json",
    },
  });

  // Optional: Add interceptors here
  // instance.interceptors.request.use(...);
  // instance.interceptors.response.use(...);

  async function request(config: AxiosRequestConfig): Promise<RestResponse<T>> {
    try {
      const response = await instance.request(config);
      return RestResponse.fromJson<T>(response.data);
    } catch (error: any) {
      if (error.response) {
        return RestResponse.fromJson<T>(error.response.data);
      }
      throw error;
    }
  }

  return {
    get: (url: string, config?: AxiosRequestConfig) =>
      request({ ...config, method: "get", url }),
    post: (url: string, data?: any, config?: AxiosRequestConfig) =>
      request({ ...config, method: "post", url, data }),
    put: (url: string, data?: any, config?: AxiosRequestConfig) =>
      request({ ...config, method: "put", url, data }),
    delete: (url: string, config?: AxiosRequestConfig) =>
      request({ ...config, method: "delete", url }),
  };
}
