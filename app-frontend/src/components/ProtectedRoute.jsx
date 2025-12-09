// ProtectedRoute.jsx
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, hasRole } = useAuth() || {};
  const location = useLocation();

  const tokenInStorage = localStorage.getItem("accessToken");
  const okAuth = isAuthenticated || !!tokenInStorage;

  if (!okAuth) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (roles?.length) {
    const okRole = roles.some((r) => hasRole?.(r));
    if (!okRole) return <Navigate to="/shop" replace />;
  }

  return children;
}
