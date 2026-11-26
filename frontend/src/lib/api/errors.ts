import axios, { AxiosError } from "axios";
import type { ApiErrorResponse } from "@/types/api";

export class AppApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly details: Record<string, string>;
  readonly isNetworkError: boolean;

  constructor(options: {
    message: string;
    status: number;
    code: string;
    details?: Record<string, string>;
    isNetworkError?: boolean;
  }) {
    super(options.message);
    this.name = "AppApiError";
    this.status = options.status;
    this.code = options.code;
    this.details = options.details ?? {};
    this.isNetworkError = options.isNetworkError ?? false;
  }
}

function isApiErrorResponse(data: unknown): data is ApiErrorResponse {
  return (
    typeof data === "object" &&
    data !== null &&
    "status" in data &&
    "message" in data &&
    typeof (data as ApiErrorResponse).message === "string"
  );
}

export function normalizeApiError(error: unknown): AppApiError {
  if (error instanceof AppApiError) {
    return error;
  }

  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<unknown>;

    if (!axiosError.response) {
      return new AppApiError({
        message: "Unable to reach the server. Please check your connection and try again.",
        status: 0,
        code: "NETWORK_ERROR",
        isNetworkError: true,
      });
    }

    const { status, data } = axiosError.response;
    const serverBody = isApiErrorResponse(data) ? data : null;
    const details = serverBody?.details ?? {};

    switch (status) {
      case 400:
        return new AppApiError({
          message: serverBody?.message || "Please check your input and try again.",
          status,
          code: "VALIDATION_ERROR",
          details,
        });
      case 401:
        return new AppApiError({
          message: serverBody?.message || "Your session has expired. Please sign in again.",
          status,
          code: "UNAUTHORIZED",
          details,
        });
      case 403:
        return new AppApiError({
          message: serverBody?.message || "You are not authorized to perform this action.",
          status,
          code: "FORBIDDEN",
          details,
        });
      case 404:
        return new AppApiError({
          message: serverBody?.message || "The requested resource was not found.",
          status,
          code: "NOT_FOUND",
          details,
        });
      case 429:
        return new AppApiError({
          message: serverBody?.message || "Too many requests. Please wait a moment and try again.",
          status,
          code: "RATE_LIMITED",
          details,
        });
      default:
        return new AppApiError({
          message:
            status >= 500
              ? "A server error occurred. Please try again later."
              : serverBody?.message || "An unexpected error occurred.",
          status,
          code: status >= 500 ? "SERVER_ERROR" : "HTTP_ERROR",
          details,
        });
    }
  }

  if (error instanceof Error) {
    return new AppApiError({
      message: error.message,
      status: 0,
      code: "UNKNOWN_ERROR",
    });
  }

  return new AppApiError({
    message: "An unexpected error occurred.",
    status: 0,
    code: "UNKNOWN_ERROR",
  });
}
