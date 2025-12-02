import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function RoleGuard({ roles = [], children }) {
  const { hasAnyRole } = useAuth();
  const location = useLocation();

  if (roles.length && !hasAnyRole(roles)) {
    // không đủ quyền → điều hướng tới trang Not Authorized
    return <Navigate to="/403" replace state={{ from: location }} />;
  }
  return children;
}
