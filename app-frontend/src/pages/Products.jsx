import { useEffect, useMemo, useState } from "react";
import { Button, Input, Space, Table, Popconfirm, message, Tag, Modal, Form, InputNumber, Select, Radio, Card } from "antd"; // NEW: Modal, Form, InputNumber, Select, Radio, Card
import api from "../api/axios";
import { useAuth } from "../auth/AuthContext";

const PRICING = { FIXED: "FIXED", COST_PLUS: "COST_PLUS" }; // NEW
const STATUS = { ACTIVE: "ACTIVE", INACTIVE: "INACTIVE" };  // NEW

const toNumber = (x) => {
  if (x === null || x === undefined) return 0;
  const n = Number(x);
  return Number.isNaN(n) ? 0 : n;
};

export default function Products() {
  const { profile } = useAuth(); // <-- lấy roles từ AuthContext
  const isAdmin = !!profile?.roles?.includes("ADMIN");

  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState([]);
  const [page, setPage] = useState({ current: 1, pageSize: 10, total: 0, sort: "id,desc" });

  // NEW: state cho modal cập nhật
  const [editOpen, setEditOpen] = useState(false);
  const [editRow, setEditRow] = useState(null);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const params = { page: page.current - 1, size: page.pageSize, q, sort: page.sort };
      const { data } = await api.get("/products", { params });
      const content = Array.isArray(data?.content) ? data.content : [];
      setRows(content);
      setPage((s) => ({ ...s, total: Number(data?.totalElements || content.length) }));
    } catch (e) {
      message.error(e?.response?.data?.message || "Không thể tải danh sách sản phẩm");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page.current, page.pageSize, q]);

  const del = async (id) => {
    try {
      await api.delete(`/products/${id}`);
      message.success("Đã xoá");
      load();
    } catch (e) {
      message.error(e?.response?.data?.message || "Xoá thất bại");
    }
  };

  // NEW: mở modal cập nhật
  const onOpenEdit = (record) => {
    setEditRow(record);
    // Prefill form (nếu BE trả keys khác, map lại tại đây)
    form.setFieldsValue({
      sku: record.sku,
      name: record.name,
      description: record.description,
      unit: record.unit,
      price: toNumber(record.price),
      listPrice: toNumber(record.listPrice),
      markupPercent: toNumber(record.markupPercent),
      minMarginPercent: toNumber(record.minMarginPercent),
      pricingMode: record.pricingMode || PRICING.FIXED,
      status: record.status || STATUS.ACTIVE,
      stockMin: toNumber(record.stockMin),
    });
    setEditOpen(true);
  };

  // NEW: submit cập nhật
  const onSubmitEdit = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        name: values.name?.trim(),
        description: values.description || null,
        unit: values.unit?.trim(),
        price: Number(values.price ?? 0),
        listPrice: Number(values.listPrice ?? 0),
        markupPercent: values.markupPercent !== undefined ? Number(values.markupPercent) : null,
        minMarginPercent: values.minMarginPercent !== undefined ? Number(values.minMarginPercent) : null,
        pricingMode: values.pricingMode,
        status: values.status,
        stockMin: Number(values.stockMin ?? 0),
        // Thường SKU không đổi khi update; nếu muốn cho phép đổi, thêm: sku: values.sku?.trim(),
      };

      setEditSubmitting(true);
      await api.put(`/products/${editRow.id}`, payload); // hoặc .patch tùy BE
      message.success("Đã cập nhật sản phẩm");
      setEditOpen(false);
      setEditRow(null);
      form.resetFields();
      load();
    } catch (e) {
      if (e?.errorFields) return; // lỗi validate form
      message.error(e?.response?.data?.message || "Cập nhật thất bại");
    } finally {
      setEditSubmitting(false);
    }
  };

  const columns = useMemo(() => {
    const base = [
      { title: "Mã", dataIndex: "sku", width: 160 },
      { title: "Tên", dataIndex: "name" },
      { title: "Đơn vị", dataIndex: "unit", width: 100 },
      { title: "Giá niêm yết", dataIndex: "listPrice", width: 140, render: (v) => toNumber(v).toLocaleString() },
    ];
    if (isAdmin) {
      base.push({
        title: "Hành động",
        width: 200, // rộng hơn để chứa 2 nút
        render: (_, r) => (
          <Space>
            <Button size="small" onClick={() => onOpenEdit(r)}>Cập nhật</Button> {/* NEW */}
            <Popconfirm title="Xoá?" onConfirm={() => del(r.id)}>
              <Button danger size="small">Xóa</Button>
            </Popconfirm>
          </Space>
        ),
      });
    }
    return base;
  }, [isAdmin]);

  return (
    <>
      <Space style={{ marginBottom: 12 }}>
        <Input
          placeholder="Nhập tên sản phẩm...."
          value={q}
          onChange={(e) => { setQ(e.target.value); setPage((s) => ({ ...s, current: 1 })); }}
          allowClear
        />
        <Button onClick={load} loading={loading}>Tải lại</Button>
      </Space>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={rows}
        loading={loading}
        pagination={{
          current: page.current,
          pageSize: page.pageSize,
          total: page.total,
          showSizeChanger: true,
          onChange: (c, ps) => setPage((s) => ({ ...s, current: c, pageSize: ps })),
        }}
      />

      {/* NEW: Modal cập nhật sản phẩm */}
      <Modal
        title={`Cập nhật sản phẩm${editRow ? ` — ${editRow.sku}` : ""}`}
        open={editOpen}
        onCancel={() => { setEditOpen(false); setEditRow(null); form.resetFields(); }}
        onOk={onSubmitEdit}
        confirmLoading={editSubmitting}
        okText="Lưu"
        cancelText="Hủy"
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            pricingMode: PRICING.FIXED,
            status: STATUS.ACTIVE,
            price: 0, listPrice: 0, markupPercent: 0, minMarginPercent: 0, stockMin: 0,
          }}
        >
          <Space wrap>
            <Form.Item label="Mã SKU" name="sku">
              <Input style={{ width: 200 }} disabled /> {/* thường không cho sửa SKU */}
            </Form.Item>
            <Form.Item
              label="Tên sản phẩm"
              name="name"
              rules={[{ required: true, message: "Nhập tên" }, { max: 200 }]}
            >
              <Input style={{ width: 260 }} />
            </Form.Item>
            <Form.Item label="Đơn vị" name="unit" rules={[{ required: true }]}>
              <Select style={{ width: 140 }} options={[
                { value: "cái", label: "cái" },
                { value: "hộp", label: "hộp" },
                { value: "kg", label: "kg" },
                { value: "chai", label: "chai" },
                { value: "thùng", label: "thùng" },
              ]}/>
            </Form.Item>
            <Form.Item label="Trạng thái" name="status" rules={[{ required: true }]}>
              <Select style={{ width: 160 }} options={[
                { value: STATUS.ACTIVE, label: "Đang bán" },
                { value: STATUS.INACTIVE, label: "Ngừng bán" },
              ]}/>
            </Form.Item>
          </Space>

          <Form.Item label="Mô tả" name="description">
            <Input.TextArea rows={3} placeholder="Mô tả ngắn..." />
          </Form.Item>

          <Card type="inner" size="small" title="Định giá">
            <Form.Item label="Phương thức định giá" name="pricingMode">
              <Radio.Group>
                <Radio value={PRICING.FIXED}>Giá cố định</Radio>
                <Radio value={PRICING.COST_PLUS}>Giá cost-plus (% lãi)</Radio>
              </Radio.Group>
            </Form.Item>

            <Space wrap>
              <Form.Item label="Giá cơ sở (price)" name="price" rules={[{ required: true }]}>
                <InputNumber min={0} style={{ width: 160 }}
                  formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")}
                  parser={(v) => v.replace(/,/g, "")}/>
              </Form.Item>

              <Form.Item label="Giá bán (listPrice)" name="listPrice" rules={[{ required: true }]}>
                <InputNumber min={0} style={{ width: 180 }}
                  formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")}
                  parser={(v) => v.replace(/,/g, "")}/>
              </Form.Item>

              <Form.Item label="% lãi (markupPercent)" name="markupPercent">
                <InputNumber min={0} max={1000} style={{ width: 140 }} />
              </Form.Item>

              <Form.Item label="Biên LN tối thiểu (%)" name="minMarginPercent">
                <InputNumber min={0} max={1000} style={{ width: 160 }} />
              </Form.Item>

              <Form.Item label="Tồn kho tối thiểu" name="stockMin" rules={[{ required: true }]}>
                <InputNumber min={0} style={{ width: 140 }} />
              </Form.Item>
            </Space>
          </Card>
        </Form>
      </Modal>
    </>
  );
}
