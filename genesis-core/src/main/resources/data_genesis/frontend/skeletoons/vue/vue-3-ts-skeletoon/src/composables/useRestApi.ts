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

  const GET = (url: string, config?: AxiosRequestConfig) => {
    return request({ ...config, method: "get", url });
  };

  const POST = (url: string, data?: any, config?: AxiosRequestConfig) => {
    return request({ ...config, method: "post", url, data });
  };

  const PUT = (url: string, data?: any, config?: AxiosRequestConfig) => {
    return request({ ...config, method: "put", url, data });
  };

  const DELETE = (url: string, config?: AxiosRequestConfig) => {
    return request({ ...config, method: "delete", url });
  };

  return {
    GET,
    POST,
    PUT,
    DELETE,
  };
}
