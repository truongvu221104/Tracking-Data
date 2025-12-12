import { useEffect } from "react";
import { Form, Input, Button, Card, Row, Col, App } from "antd";
import api from "../api/axios";
import AddressSection from "../components/address/AddressSection";

export default function CustomerProfile() {
  const { message } = App.useApp();
  const [form] = Form.useForm();

  // ====== Load hồ sơ khách hàng ======
  useEffect(() => {
    const loadProfile = async () => {
      try {
        const { data } = await api.get("/me/customer");
        if (data) {
          form.setFieldsValue({
            name: data.name ?? "",
            phone: data.phone ?? "",
            note: data.note ?? "",
          });
        }
      } catch (e) {
        console.error("Không load được hồ sơ khách hàng", e);
        const err =
          e?.parsed?.message ||
          e?.response?.data?.message ||
          e?.message ||
          "Không tải được hồ sơ khách hàng";
        message.error(err);
      }
    };

    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ====== Lưu hồ sơ ======
  const onFinish = async (values) => {
    try {
      const payload = {
        name: values.name?.trim(),
        phone: values.phone?.trim(),
        note: values.note?.trim() || null,
      };

      await api.post("/me/customer", payload);
      message.success("Cập nhật hồ sơ khách hàng thành công");
    } catch (e) {
      console.error("Cập nhật hồ sơ khách hàng lỗi", e);
      const err =
        e?.parsed?.message ||
        e?.response?.data?.message ||
        e?.message ||
        "Không cập nhật được hồ sơ khách hàng";
      message.error(err);
    }
  };

  return (
    <Row gutter={24} style={{ marginTop: 24 }}>
      <Col span={10}>
        <Card title="Thông tin khách hàng">
          <Form
            layout="vertical"
            form={form}
            onFinish={onFinish}
            autoComplete="off"
          >
            <Form.Item
              name="name"
              label="Họ và tên"
              rules={[{ required: true, message: "Vui lòng nhập họ và tên" }]}
            >
              <Input placeholder="Nhập họ và tên" />
            </Form.Item>

            <Form.Item
              name="phone"
              label="Số điện thoại"
              rules={[
                { required: true, message: "Vui lòng nhập số điện thoại" },
                {
                  pattern: /^[0-9]{9,11}$/,
                  message: "Số điện thoại không hợp lệ",
                },
              ]}
            >
              <Input placeholder="Ví dụ: 0989xxxxxx" />
            </Form.Item>

            <Form.Item name="note" label="Ghi chú">
              <Input.TextArea
                rows={3}
                placeholder="VD: Giao giờ hành chính"
              />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit">
                Lưu hồ sơ
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </Col>

      <Col span={14}>
        <AddressSection />
      </Col>
    </Row>
  );
}
