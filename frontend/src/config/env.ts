const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || "http://localhost:8080/api/v1";

export const env = {
  API_BASE_URL: apiBaseUrl,
  BACKEND_ORIGIN: new URL(apiBaseUrl, window.location.origin).origin,
} as const;
