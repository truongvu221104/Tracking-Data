import { Button, Card, Form, Input, Typography, App } from "antd";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useState } from "react";

export default function Register() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { message } = App.useApp();       
  const [loading, setLoading] = useState(false);

  const onFinish = async (values) => {
    try {
      setLoading(true);
      const payload = {
        username: values.username,
        email: values.email,
        password: values.password,
        confirmPassword: values.confirm,   
      };
      await api.post("/auth/register", payload);

      message.success("Đăng ký thành công! Vui lòng kiểm tra email để xác thực.");
      navigate("/login", { replace: true });
    } catch (e) {
      const res = e?.response?.data;

      if (res?.error === "validation_failed" && res?.details) {
        const fields = Object.entries(res.details).map(([name, msg]) => ({
          name,
          errors: [String(msg)],
        }));
        form.setFields(fields);
        message.error("Dữ liệu không hợp lệ");
      } else {
        message.error(res?.message || "Đăng ký thất bại");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: 80 }}>
      <Card title="Tạo tài khoản" style={{ width: 420 }}>
        <Form layout="vertical" form={form} onFinish={onFinish}>
          <Form.Item
            name="username" label="Username"
            rules={[
              { required: true, message: "Vui lòng nhập username" },
              { min: 3, message: "Tối thiểu 3 ký tự" },
              { max: 64, message: "Tối đa 64 ký tự" },
            ]}
          >
            <Input autoFocus autoComplete="username" />
          </Form.Item>

          <Form.Item
            name="email" label="Email"
            rules={[
              { required: true, message: "Vui lòng nhập email" },
              { type: "email", message: "Email không hợp lệ" },
              { max: 128, message: "Tối đa 128 ký tự" },
            ]}
          >
            <Input autoComplete="email" />
          </Form.Item>

          <Form.Item
            name="password" label="Mật khẩu" hasFeedback
            rules={[
              { required: true, message: "Vui lòng nhập mật khẩu" },
              { min: 6, message: "Tối thiểu 6 ký tự" },
              { max: 64, message: "Tối đa 64 ký tự" },
            ]}
          >
            <Input.Password autoComplete="new-password" />
          </Form.Item>

          <Form.Item
            name="confirm" label="Xác nhận mật khẩu" dependencies={["password"]} hasFeedback
            rules={[
              { required: true, message: "Vui lòng nhập lại mật khẩu" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("password") === value) return Promise.resolve();
                  return Promise.reject(new Error("Mật khẩu nhập lại không khớp"));
                },
              }),
            ]}
          >
            <Input.Password autoComplete="new-password" />
          </Form.Item>

          <Button type="primary" htmlType="submit" block loading={loading} disabled={loading}>
            Đăng ký
          </Button>
        </Form>

        <Typography.Paragraph style={{ marginTop: 16 }}>
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </Typography.Paragraph>
      </Card>
    </div>
  );
}
