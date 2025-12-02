export function parseApiError(err) {
  const res = err?.response?.data;
  if (!res) return { code: "network_error", message: err?.message || "Mất kết nối", status: 0 };
  return {
    code: res.error || "server_error",
    message: res.message || "Có lỗi xảy ra",
    details: res.details || null,
    requestId: res.requestId || null,
    status: res.status || err?.response?.status || 0,
  };
}
