// src/components/ProductForm.jsx
import React, { useEffect, useMemo } from "react";
import {
  App as AntdApp,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Radio,
  Select,
  Space,
} from "antd";
import api from "../api/axios";

const PRICING = { FIXED: "FIXED", COST_PLUS: "COST_PLUS" };
const STATUS = { ACTIVE: "ACTIVE", INACTIVE: "INACTIVE" };

const toNumber = (x, def = 0) => {
  const n = Number(x);
  return Number.isFinite(n) ? n : def;
};

export default function ProductForm({
  mode = "create",              // "create" | "edit"
  record = null,                // bản ghi cần sửa (khi edit)
  onSuccess = () => {},         // callback khi submit OK
  onCancel = null,              // optional: callback khi nhấn Hủy (dùng trong modal)
  submitText,                   // override text nút lưu
  hideChrome = false,           // nếu true: ẩn Card wrapper, chỉ render Form (dùng trong Modal)
}) {
  const [form] = Form.useForm();

  // AntD v5: ưu tiên message từ App context, fallback legacy
  let ctxMessage;
  try {
    const { message } = AntdApp.useApp();
    ctxMessage = message;
  } catch {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const { default: legacyMessage } = require("antd/es/message");
    ctxMessage = legacyMessage;
  }

  // Watch fields
  const watchMode = Form.useWatch("pricingMode", form) || PRICING.FIXED;
  const watchPrice = Form.useWatch("price", form) || 0;
  const watchMarkup = Form.useWatch("markupPercent", form) || 0;
  const watchMinMargin = Form.useWatch("minMarginPercent", form) || 0;
  const watchListPrice = Form.useWatch("listPrice", form) || 0;

  // Tính listPrice khi COST_PLUS
  const computedListPrice = useMemo(() => {
    if (watchMode === PRICING.COST_PLUS) {
      const p = toNumber(watchPrice, 0);
      const m = toNumber(watchMarkup, 0);
      return p * (1 + m / 100);
    }
    return watchListPrice;
  }, [watchMode, watchPrice, watchMarkup, watchListPrice]);

  // Khi COST_PLUS thì auto đổ listPrice
  useEffect(() => {
    if (watchMode === PRICING.COST_PLUS) {
      form.setFieldsValue({
        listPrice: Number.isFinite(computedListPrice)
          ? Number(computedListPrice.toFixed(2))
          : 0,
      });
    }
  }, [computedListPrice, watchMode, form]);

  // Cảnh báo margin (chỉ hiển thị, không chặn submit — chặn đã có validator cross-field)
  const marginWarning = useMemo(() => {
    const cost = toNumber(watchPrice, 0);
    const sell =
      watchMode === PRICING.COST_PLUS
        ? toNumber(computedListPrice, 0)
        : toNumber(watchListPrice, 0);
    const minMargin = toNumber(watchMinMargin, 0);

    if (cost > 0 && sell > 0 && minMargin > 0) {
      const marginPercent = ((sell - cost) / cost) * 100;
      if (marginPercent + 1e-9 < minMargin) {
        return `Biên lợi nhuận hiện tại ~ ${marginPercent.toFixed(
          2
        )}% < mức tối thiểu ${minMargin}%`;
      }
    }
    return null;
  }, [watchPrice, watchListPrice, computedListPrice, watchMode, watchMinMargin]);

  // Nạp initial khi edit
  useEffect(() => {
    if (mode === "edit" && record) {
      form.setFieldsValue({
        sku: record.sku,
        name: record.name,
        description: record.description,
        unit: record.unit,
        price: toNumber(record.price, 0),
        listPrice: toNumber(record.listPrice, 0),
        markupPercent: toNumber(record.markupPercent, 0),
        minMarginPercent: toNumber(record.minMarginPercent, 0),
        pricingMode: record.pricingMode || PRICING.FIXED,
        status: record.status || STATUS.ACTIVE,
        stockMin: toNumber(record.stockMin, 0),
      });
    } else if (mode === "create") {
      form.setFieldsValue({
        pricingMode: PRICING.FIXED,
        status: STATUS.ACTIVE,
        unit: "cái",
        price: 0,
        listPrice: 0,
        markupPercent: 0,
        minMarginPercent: 0,
        stockMin: 0,
      });
    }
  }, [mode, record, form]);

  const buildPayload = (values) => {
    const m = values.pricingMode;
    return {
      sku: values.sku?.trim(),
      name: values.name?.trim(),
      description: values.description || null,
      unit: values.unit?.trim(),
      price: toNumber(values.price),
      listPrice:
        m === PRICING.COST_PLUS
          ? Number((computedListPrice ?? 0).toFixed(2))
          : toNumber(values.listPrice),
      markupPercent:
        m === PRICING.COST_PLUS
          ? toNumber(values.markupPercent, 0)
          : values.markupPercent !== undefined
          ? toNumber(values.markupPercent)
          : null,
      minMarginPercent:
        values.minMarginPercent !== undefined
          ? toNumber(values.minMarginPercent)
          : null,
      pricingMode: m,
      status: values.status,
      stockMin: toNumber(values.stockMin),
    };
  };

  const submit = async (values) => {
    const payload = buildPayload(values);
    try {
      if (mode === "edit" && record?.id) {
        await api.put(`/products/${record.id}`, payload);
        ctxMessage.success("Đã cập nhật sản phẩm");
      } else {
        const { data } = await api.post("/products", payload);
        ctxMessage.success(`Đã tạo sản phẩm (ID = ${data.id})`);
        // reset về default
        form.resetFields();
        form.setFieldsValue({
          pricingMode: PRICING.FIXED,
          status: STATUS.ACTIVE,
          unit: "cái",
          price: 0,
          listPrice: 0,
          markupPercent: 0,
          minMarginPercent: 0,
          stockMin: 0,
        });
      }
      onSuccess();
    } catch (e) {
      const apiMsg = e?.response?.data?.message || (mode === "edit" ? "Cập nhật thất bại" : "Lỗi tạo sản phẩm");
      const fieldErrors = e?.response?.data?.errors;
      if (Array.isArray(fieldErrors) && fieldErrors.length) {
        form.setFields(
          fieldErrors.map((err) => ({
            name: err.field,
            errors: [err.message || "Không hợp lệ"],
          }))
        );
      }
      ctxMessage.error(apiMsg);
    }
  };

  const onFinishFailed = (info) => {
    const first = info?.errorFields?.[0]?.name;
    if (first) form.scrollToField(first, { behavior: "smooth", block: "center" });
    ctxMessage.error("Vui lòng kiểm tra các trường bị lỗi.");
  };

  const innerForm = (
    <Form
      form={form}
      layout="vertical"
      onFinish={submit}
      onFinishFailed={onFinishFailed}
      initialValues={{
        pricingMode: PRICING.FIXED,
        status: STATUS.ACTIVE,
        unit: "cái",
        price: 0,
        listPrice: 0,
        markupPercent: 0,
        minMarginPercent: 0,
        stockMin: 0,
      }}
    >
      <Space wrap>
        <Form.Item
          label="Mã SKU"
          name="sku"
          rules={[
            { required: mode === "create", message: "Vui lòng nhập SKU" },
            { max: 64, message: "Tối đa 64 ký tự" },
          ]}
        >
          <Input placeholder="VD: SP-001" style={{ width: 220 }} disabled={mode === "edit"} />
        </Form.Item>

        <Form.Item
          label="Tên sản phẩm"
          name="name"
          rules={[
            { required: true, message: "Vui lòng nhập tên" },
            { max: 200, message: "Tối đa 200 ký tự" },
          ]}
        >
          <Input placeholder="VD: Bánh quy bơ" style={{ width: 260 }} />
        </Form.Item>

        <Form.Item
          label="Đơn vị tính"
          name="unit"
          rules={[{ required: true, message: "Nhập đơn vị tính" }]}
        >
          <Select
            style={{ width: 160 }}
            options={[
              { value: "cái", label: "cái" },
              { value: "hộp", label: "hộp" },
              { value: "kg", label: "kg" },
              { value: "chai", label: "chai" },
              { value: "thùng", label: "thùng" },
            ]}
          />
        </Form.Item>

        <Form.Item label="Trạng thái" name="status" rules={[{ required: true }]}>
          <Select
            style={{ width: 160 }}
            options={[
              { value: STATUS.ACTIVE, label: "Đang bán" },
              { value: STATUS.INACTIVE, label: "Ngừng bán" },
            ]}
          />
        </Form.Item>
      </Space>

      <Form.Item label="Mô tả" name="description">
        <Input.TextArea rows={3} placeholder="Mô tả ngắn..." />
      </Form.Item>

      <Card type="inner" title="Định giá">
        <Form.Item label="Phương thức định giá" name="pricingMode">
          <Radio.Group>
            <Radio value={PRICING.FIXED}>Giá cố định</Radio>
            <Radio value={PRICING.COST_PLUS}>Giá cost-plus (% lãi)</Radio>
          </Radio.Group>
        </Form.Item>

        <Space wrap>
          <Form.Item
            label="Giá cơ sở (price)"
            name="price"
            rules={[
              { required: true, message: "Vui lòng nhập giá cơ sở" },
              () => ({
                validator: (_, value) => {
                  const n = toNumber(value, NaN);
                  if (!Number.isFinite(n) || n < 0) {
                    return Promise.reject("Giá cơ sở phải ≥ 0");
                  }
                  return Promise.resolve();
                },
              }),
            ]}
          >
            <InputNumber
              min={0}
              style={{ width: 180 }}
              placeholder="0.00"
              formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")}
              parser={(v) => String(v ?? "").replace(/,/g, "")}
            />
          </Form.Item>

          <Form.Item
            label="Giá bán hiện hành (listPrice)"
            name="listPrice"
            dependencies={["pricingMode", "price", "minMarginPercent"]}
            rules={[
              ({ getFieldValue }) => ({
                validator: (_, value) => {
                  const modeV = getFieldValue("pricingMode");
                  const priceV = toNumber(getFieldValue("price"));
                  const minMargin = toNumber(getFieldValue("minMarginPercent"), 0);

                  if (modeV === PRICING.FIXED) {
                    if (value === undefined || value === null || value === "") {
                      return Promise.reject("Vui lòng nhập giá bán");
                    }
                    const sell = toNumber(value);
                    if (sell < 0) return Promise.reject("Giá bán không được âm");

                    if (priceV > 0 && minMargin > 0) {
                      const margin = ((sell - priceV) / priceV) * 100;
                      if (margin + 1e-9 < minMargin) {
                        return Promise.reject(
                          `Biên LN ${margin.toFixed(2)}% < tối thiểu ${minMargin}%`
                        );
                      }
                    }
                  }
                  return Promise.resolve();
                },
              }),
            ]}
          >
            <InputNumber
              min={0}
              disabled={watchMode === PRICING.COST_PLUS}
              style={{ width: 200 }}
              placeholder={watchMode === PRICING.COST_PLUS ? "Tự tính" : "0.00"}
              formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")}
              parser={(v) => String(v ?? "").replace(/,/g, "")}
            />
          </Form.Item>

          <Form.Item
            label="% lãi (markupPercent)"
            name="markupPercent"
            dependencies={["pricingMode"]}
            rules={[
              ({ getFieldValue }) => ({
                validator: (_, value) => {
                  const modeV = getFieldValue("pricingMode");
                  if (modeV === PRICING.COST_PLUS) {
                    if (value === undefined || value === null || value === "")
                      return Promise.reject("Vui lòng nhập % lãi");
                    const m = toNumber(value);
                    if (m < 0) return Promise.reject("% lãi không được âm");
                  }
                  return Promise.resolve();
                },
              }),
            ]}
          >
            <InputNumber
              min={0}
              max={1000}
              style={{ width: 160 }}
              placeholder={watchMode === PRICING.FIXED ? "Không bắt buộc" : "VD: 20"}
              disabled={watchMode === PRICING.FIXED}
            />
          </Form.Item>

          <Form.Item label="Biên LN tối thiểu (%)" name="minMarginPercent">
            <InputNumber min={0} max={1000} style={{ width: 180 }} />
          </Form.Item>

          <Form.Item
            label="Tồn kho tối thiểu"
            name="stockMin"
            rules={[
              { required: true, message: "Nhập tồn tối thiểu" },
              () => ({
                validator: (_, value) => {
                  const n = toNumber(value, NaN);
                  if (!Number.isFinite(n) || n < 0) return Promise.reject("Phải ≥ 0");
                  if (!Number.isInteger(n)) return Promise.reject("Phải là số nguyên");
                  return Promise.resolve();
                },
              }),
            ]}
          >
            <InputNumber min={0} style={{ width: 160 }} />
          </Form.Item>
        </Space>

        {marginWarning && (
          <div style={{ color: "#d46b08", marginTop: 4 }}>⚠️ {marginWarning}</div>
        )}
      </Card>

      <Space style={{ marginTop: 12 }}>
        {onCancel && (
          <Button onClick={onCancel}>
            Hủy
          </Button>
        )}
        <Button type="primary" onClick={() => form.submit()}>
          {submitText || (mode === "edit" ? "Lưu thay đổi" : "Lưu sản phẩm")}
        </Button>
      </Space>
    </Form>
  );

  if (hideChrome) return innerForm;

  return (
    <Card
      title={mode === "edit" ? "Cập nhật sản phẩm" : "Thêm Sản phẩm"}
      extra={
        <Space>
          <Button onClick={() => form.resetFields()}>Làm mới</Button>
          <Button type="primary" onClick={() => form.submit()} style={{ minWidth: 120 }}>
            {submitText || (mode === "edit" ? "Lưu thay đổi" : "Lưu sản phẩm")}
          </Button>
        </Space>
      }
    >
      {innerForm}
    </Card>
  );
}
