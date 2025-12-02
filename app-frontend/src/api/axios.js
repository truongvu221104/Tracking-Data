import axios from "axios";
import { message, notification } from "antd";

let isRedirecting401 = false;

function parseApiError(err) {
  const res = err?.response;
  if (!res) return { code: "network_error", message: err?.message || "Mất kết nối", status: 0 };

  let body = res.data;
  if (typeof body === "string") { try { body = JSON.parse(body); } catch {} }
  const payload = (body && typeof body === "object") ? body : {};

  return {
    code: payload.error || "server_error",
    message: payload.message || err?.message || "Có lỗi xảy ra",
    details: payload.details ?? null,
    requestId: payload.requestId ?? null,
    status: payload.status || res.status || 0,
  };
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 15000,
  withCredentials: false,
});

function isAuthApi(url = "") {
  try {
    const u = new URL(url, api.defaults.baseURL);
    return u.pathname.startsWith("/auth");
  } catch {
    return url.startsWith("/auth");
  }
}

api.interceptors.request.use((config) => {
  if (!isAuthApi(config.url)) {
    const t = localStorage.getItem("accessToken");
    if (t) config.headers.Authorization = `Bearer ${t}`;
  }
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (error) => {
    const parsed = parseApiError(error);

    if (parsed.status === 401 && !isAuthApi(error?.config?.url)) {
      if (!isRedirecting401) {
        isRedirecting401 = true;
        message.warning("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
        localStorage.removeItem("accessToken");
        sessionStorage.setItem("postLoginRedirect", window.location.pathname + window.location.search);
        window.location.replace("/login");
      }
      error.parsed = parsed;
      return Promise.reject(error);
    }

    if (parsed.status === 403) {
      message.error("Bạn không có quyền thực hiện thao tác này.");
      error.parsed = parsed;
      return Promise.reject(error);
    }

    if (parsed.code === "validation_failed") {
      error.parsed = parsed;
      return Promise.reject(error);
    }

    if (parsed.status >= 500 || parsed.code === "server_error") {
      const desc = parsed.requestId ? `${parsed.message}\nRequestId: ${parsed.requestId}` : parsed.message;
      notification.error({ message: "Lỗi hệ thống", description: desc, duration: 5 });
      error.parsed = parsed;
      return Promise.reject(error);
    }

    message.error(parsed.message || "Có lỗi xảy ra");
    error.parsed = parsed;
    return Promise.reject(error);
  }
  
);

export default api;
