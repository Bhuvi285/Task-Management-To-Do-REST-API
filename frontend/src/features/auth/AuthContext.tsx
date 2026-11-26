import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { useQueryClient } from "@tanstack/react-query";
import { authService } from "@/features/auth/api/authService";
import { extractEmailFromJwt, tokenStore } from "@/features/auth/tokenStore";
import type { AuthUser, LoginRequest } from "@/features/auth/types";

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isBootstrapping: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  clearSession: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isBootstrapping, setIsBootstrapping] = useState<boolean>(true);

  const clearSession = useCallback(() => {
    tokenStore.clear();
    setUser(null);
    queryClient.clear();
  }, [queryClient]);

  useEffect(() => {
    const unsubscribe = tokenStore.onAuthCleared(() => {
      setUser(null);
      queryClient.clear();
    });
    return unsubscribe;
  }, [queryClient]);

  useEffect(() => {
    let active = true;

    // Establish session from the HttpOnly refresh cookie (supports OAuth2 redirect & page refresh)
    authService
      .refresh()
      .then((tokens) => {
        if (!active) return;
        const email = extractEmailFromJwt(tokens.accessToken);
        setUser(email ? { email } : { email: "Authenticated User" });
      })
      .catch(() => {
        if (!active) return;
        clearSession();
      })
      .finally(() => {
        if (active) {
          setIsBootstrapping(false);
        }
      });

    return () => {
      active = false;
    };
  }, [clearSession]);

  const login = useCallback(async (credentials: LoginRequest) => {
    const tokens = await authService.login(credentials);
    tokenStore.setAccessToken(tokens.accessToken);
    const email = extractEmailFromJwt(tokens.accessToken) ?? credentials.email.trim();
    setUser({ email });
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // Even if server revocation fails (e.g., already expired), always clear local session
    } finally {
      clearSession();
    }
  }, [clearSession]);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: Boolean(user),
        isBootstrapping,
        login,
        logout,
        clearSession,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
