// src/pages/Login.jsx
import { Button, Card, Form, Input, Typography, App, Divider } from "antd";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";           // baseURL = http://localhost:8080/api
import { useAuth } from "../auth/AuthContext";
import { useState } from "react";
import googleLogo from "../assets/google.svg";
import fbLogo from "../assets/facebook.svg";

export default function Login() {
  const { message } = App.useApp();
  const { setToken } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values) => {
    try {
      setLoading(true);
      // gọi BE: POST http://localhost:8080/api/auth/login
      const { data } = await api.post("/auth/login", values);
      setToken(data.accessToken);
      localStorage.setItem("accessToken", data.accessToken);
      message.success("Đăng nhập thành công");
      navigate("/products", { replace: true });
    } catch (e) {
      const status = e?.response?.status;
      const resMsg = e?.response?.data?.message;

      const BAD_CREDENTIALS_MSG = "Sai tên đăng nhập hoặc mật khẩu";

      if (status === 401 || status === 400) {
        message.error(BAD_CREDENTIALS_MSG);
        form.setFields([
          { name: "username", errors: [BAD_CREDENTIALS_MSG] },
          { name: "password", errors: [BAD_CREDENTIALS_MSG] },
        ]);
      } else {
        message.error(resMsg || "Đăng nhập thất bại");
      }
    } finally {
      setLoading(false);
    }
  };

  // 👉 bắt đầu flow OAuth2 Google
  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorize/google";
  };

  const handleFacebookLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorize/facebook";
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: 120 }}>
      <Card title="Đăng nhập" style={{ width: 380 }}>
        <Form
          layout="vertical"
          form={form}
          onFinish={onFinish}
          validateTrigger={["onBlur", "onSubmit"]}
        >
          <Form.Item
            name="username"
            label="Tên đăng nhập"
            normalize={(v) => (typeof v === "string" ? v.trim() : v)}
            rules={[
              { required: true, message: "Vui lòng nhập tên đăng nhập" },
              { whitespace: true, message: "Tên đăng nhập không được chỉ là khoảng trắng" },
              { min: 3, message: "Tên đăng nhập tối thiểu 3 ký tự" },
              { max: 32, message: "Tên đăng nhập tối đa 32 ký tự" },
              { pattern: /^[a-zA-Z0-9._-]+$/, message: "Chỉ cho phép chữ, số và . _ -" },
            ]}
            hasFeedback
            tooltip="Chỉ cho phép chữ, số và . _ -"
          >
            <Input autoFocus allowClear autoComplete="username" placeholder="Nhập tên đăng nhập" />
          </Form.Item>

          <Form.Item
            name="password"
            label="Mật khẩu"
            rules={[
              { required: true, message: "Vui lòng nhập mật khẩu" },
              { min: 6, message: "Mật khẩu tối thiểu 6 ký tự" },
              { max: 64, message: "Mật khẩu tối đa 64 ký tự" },
            ]}
            hasFeedback
          >
            <Input.Password autoComplete="current-password" placeholder="Nhập mật khẩu" />
          </Form.Item>

          <Button type="primary" htmlType="submit" block loading={loading} disabled={loading}>
            Đăng nhập
          </Button>
        </Form>

        <Divider plain>hoặc</Divider>

        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {/* Google */}
          <Button
            block
            onClick={handleGoogleLogin}
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 8,
              backgroundColor: "#fff",
              borderColor: "#d9d9d9",
            }}
          >
            <img
              src={googleLogo}
              alt="Google"
              style={{ width: 18, height: 18 }}
            />
            <span>Đăng nhập bằng Google</span>
          </Button>

          {/* Facebook */}
          <Button
            block
            onClick={handleFacebookLogin}
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 8,
              backgroundColor: "#fff",
              borderColor: "#d9d9d9",
            }}
          >
            <img
              src={fbLogo}
              alt="Facebook"
              style={{ width: 18, height: 18 }}
            />
            <span>Đăng nhập bằng Facebook</span>
          </Button>
        </div>

        <Typography.Paragraph type="secondary" style={{ marginTop: 12 }}>
          Chưa có tài khoản? <Link to="/register">Đăng ký</Link>
        </Typography.Paragraph>
      </Card>
    </div>
  );
}
