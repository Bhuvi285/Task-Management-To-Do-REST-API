/**
 * Token storage strategy:
 * - Access token is kept strictly in memory (never written to localStorage/sessionStorage).
 * - Refresh token is stored in sessionStorage ONLY as an explicitly documented interim
 *   compromise for compatibility with the backend's JSON { refreshToken } contract.
 *   It is cleared on logout or refresh failure.
 */

const REFRESH_TOKEN_KEY = "taskflow_refresh_token";

let inMemoryAccessToken: string | null = null;
type AuthClearedListener = () => void;
const authClearedListeners = new Set<AuthClearedListener>();

export const tokenStore = {
  getAccessToken(): string | null {
    return inMemoryAccessToken;
  },

  setAccessToken(token: string | null): void {
    inMemoryAccessToken = token;
  },

  getRefreshToken(): string | null {
    try {
      return window.sessionStorage.getItem(REFRESH_TOKEN_KEY);
    } catch {
      return null;
    }
  },

  setRefreshToken(token: string | null): void {
    try {
      if (token) {
        window.sessionStorage.setItem(REFRESH_TOKEN_KEY, token);
      } else {
        window.sessionStorage.removeItem(REFRESH_TOKEN_KEY);
      }
    } catch {
      // Ignore storage errors in restricted environments
    }
  },

  setTokens(accessToken: string, refreshToken: string): void {
    inMemoryAccessToken = accessToken;
    this.setRefreshToken(refreshToken);
  },

  clear(): void {
    inMemoryAccessToken = null;
    this.setRefreshToken(null);
  },

  onAuthCleared(listener: AuthClearedListener): () => void {
    authClearedListeners.add(listener);
    return () => {
      authClearedListeners.delete(listener);
    };
  },

  notifyAuthCleared(): void {
    this.clear();
    authClearedListeners.forEach((listener) => listener());
  },
};

/**
 * Extracts the JWT subject (email) for UI display only.
 * Not used as a security boundary; backend validates the JWT signature on every request.
 */
export function extractEmailFromJwt(token: string | null): string | null {
  if (!token) return null;
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
    const payload = JSON.parse(atob(padded)) as { sub?: unknown };
    return typeof payload.sub === "string" && payload.sub.trim() ? payload.sub : null;
  } catch {
    return null;
  }
}
