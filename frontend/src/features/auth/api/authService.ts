import { apiClient, refreshAccessTokenSingleFlight } from "@/lib/api/client";
import type {
  AuthMessageResponse,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RegistrationRequest,
  ResetPasswordRequest,
} from "@/features/auth/types";

export const authService = {
  async register(payload: RegistrationRequest): Promise<AuthMessageResponse> {
    const { data } = await apiClient.post<AuthMessageResponse>("/auth/register", payload);
    return data;
  },

  async login(payload: LoginRequest): Promise<LoginResponse> {
    const { data } = await apiClient.post<LoginResponse>("/auth/login", payload);
    return data;
  },

  async refresh(): Promise<LoginResponse> {
    return refreshAccessTokenSingleFlight();
  },

  async logout(payload: RefreshTokenRequest): Promise<void> {
    await apiClient.post("/auth/logout", payload);
  },

  async forgotPassword(payload: ForgotPasswordRequest): Promise<AuthMessageResponse> {
    const { data } = await apiClient.post<AuthMessageResponse>(
      "/auth/forgot-password",
      payload,
    );
    return data;
  },

  async resetPassword(payload: ResetPasswordRequest): Promise<AuthMessageResponse> {
    const { data } = await apiClient.post<AuthMessageResponse>(
      "/auth/reset-password",
      payload,
    );
    return data;
  },

  async changePassword(payload: ChangePasswordRequest): Promise<AuthMessageResponse> {
    const { data } = await apiClient.post<AuthMessageResponse>(
      "/auth/change-password",
      payload,
    );
    return data;
  },
};
