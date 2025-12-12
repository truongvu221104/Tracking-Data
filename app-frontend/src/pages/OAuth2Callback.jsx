import { useEffect, useRef } from "react"; 
import { useLocation, useNavigate } from "react-router-dom";
import { App, Spin } from "antd";
import { useAuth } from "../auth/AuthContext";

export default function OAuth2Callback() {
  const { message } = App.useApp();
  const { setToken } = useAuth();
  const navigate = useNavigate();
  const { search } = useLocation();

  const handledRef = useRef(false);

  useEffect(() => {
     if (handledRef.current) return;
    handledRef.current = true;

    const params = new URLSearchParams(search);
    const token = params.get("token");

    if (token) {
      setToken(token);
      localStorage.setItem("accessToken", token);
      message.success("Đăng nhập thành công");
      navigate("/shop", { replace: true });
    } else {
      message.error("Không nhận được token từ server");
      navigate("/login", { replace: true });
    }
  }, [search, setToken, message, navigate]);

  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: 120 }}>
      <Spin tip="Đang xử lý đăng nhập..." />
    </div>
  );
}
