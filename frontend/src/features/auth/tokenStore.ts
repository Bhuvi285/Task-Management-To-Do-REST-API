/**
 * Token storage strategy:
 * - Access token is kept strictly in memory (never written to localStorage or sessionStorage).
 * - Refresh token is managed exclusively by the backend via an HttpOnly, SameSite=Lax cookie
 *   scoped to /api/v1/auth, so JavaScript never reads or stores the refresh token.
 */

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

  clear(): void {
    inMemoryAccessToken = null;
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
