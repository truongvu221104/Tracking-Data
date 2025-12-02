import { createContext, useContext, useMemo, useState } from "react";

const AuthCtx = createContext(null);
export const useAuth = () => useContext(AuthCtx);

function parseJwt(token) {
  try {
    const base64 = token.split(".")[1];
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map(c => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export default function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("accessToken") || null);

  const profile = useMemo(() => {
    if (!token) return null;
    const payload = parseJwt(token) || {};
    return {
      username: payload.sub,
      roles: payload.roles || [],
      raw: payload,
    };
  }, [token]);

  const hasRole = (role) => !!profile?.roles?.includes(role);

  const value = { token, setToken, profile, hasRole };
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}
