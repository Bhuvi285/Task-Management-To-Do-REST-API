import axios, { type InternalAxiosRequestConfig } from "axios";
import { env } from "@/config/env";
import { normalizeApiError } from "@/lib/api/errors";
import { tokenStore } from "@/features/auth/tokenStore";
import type { LoginResponse } from "@/features/auth/types";

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const PUBLIC_AUTH_PATHS = [
  "/auth/login",
  "/auth/register",
  "/auth/refresh",
  "/auth/logout",
  "/auth/forgot-password",
  "/auth/reset-password",
];

function isPublicAuthRequest(url?: string): boolean {
  if (!url) return false;
  return PUBLIC_AUTH_PATHS.some((path) => url.includes(path));
}

export const apiClient = axios.create({
  baseURL: env.API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 15_000,
});

// Single shared refresh operation across concurrent 401 responses
let activeRefreshPromise: Promise<LoginResponse> | null = null;

export async function refreshAccessTokenSingleFlight(): Promise<LoginResponse> {
  if (activeRefreshPromise) {
    return activeRefreshPromise;
  }

  activeRefreshPromise = (async () => {
    try {
      // Cookie-based refresh: browser sends the HttpOnly refresh cookie automatically
      // when withCredentials is true. Bare axios avoids triggering apiClient interceptors.
      const response = await axios.post<LoginResponse>(
        `${env.API_BASE_URL}/auth/refresh`,
        undefined,
        {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
          timeout: 15_000,
        },
      );
      const data = response.data;
      tokenStore.setAccessToken(data.accessToken);
      return data;
    } catch (err) {
      tokenStore.notifyAuthCleared();
      throw normalizeApiError(err);
    } finally {
      activeRefreshPromise = null;
    }
  })();

  return activeRefreshPromise;
}

apiClient.interceptors.request.use((config) => {
  if (!isPublicAuthRequest(config.url)) {
    const accessToken = tokenStore.getAccessToken();
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    if (!axios.isAxiosError(error) || !error.config) {
      return Promise.reject(normalizeApiError(error));
    }

    const originalRequest = error.config as RetryableRequestConfig;
    const status = error.response?.status;

    if (
      status === 401 &&
      !originalRequest._retry &&
      !isPublicAuthRequest(originalRequest.url)
    ) {
      originalRequest._retry = true;
      try {
        const refreshed = await refreshAccessTokenSingleFlight();
        originalRequest.headers.Authorization = `Bearer ${refreshed.accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        return Promise.reject(normalizeApiError(refreshError));
      }
    }

    return Promise.reject(normalizeApiError(error));
  },
);
