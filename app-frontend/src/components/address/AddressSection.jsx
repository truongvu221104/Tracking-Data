// src/components/address/AddressSection.jsx
import { useEffect, useState } from "react";
import {
  Card,
  Table,
  Button,
  Form,
  Input,
  Checkbox,
  Space,
  Typography,
  Divider,
  Popconfirm,
  App,
} from "antd";
import api from "../../api/axios";
import LocationSelectors from "./LocationSelectors";
import MapPicker from "./MapPicker";

const { Text, Title } = Typography;

// Kho mặc định (origin) – align với backend
const WAREHOUSE = { lat: 21.0278, lng: 105.8342 };

// Haversine: khoảng cách km
function distanceKm(lat1, lon1, lat2, lon2) {
  if (
    lat1 == null ||
    lon1 == null ||
    lat2 == null ||
    lon2 == null
  )
    return null;

  const R = 6371;
  const toRad = (deg) => (deg * Math.PI) / 180;

  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) *
      Math.cos(toRad(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

// Rule demo phí ship – PORT sang BE khi làm thật
function calcShippingFee(d) {
  if (d == null) return null;
  if (d <= 5) return 20000;
  if (d <= 10) return 30000;
  if (d <= 30) return 40000;
  return 50000;
}

// Geocode huyện để map nhảy tới vùng đó
async function geocodeDistrict(provinceName, districtName) {
  const q = `${districtName}, ${provinceName}, Vietnam`;
  const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
    q
  )}`;

  const res = await fetch(url, {
    headers: { "Accept-Language": "vi" },
  });
  if (!res.ok) throw new Error("Geocode failed");

  const json = await res.json();
  if (!json || json.length === 0) return null;

  const { lat, lon } = json[0];
  return { lat: parseFloat(lat), lng: parseFloat(lon) };
}

export default function AddressSection() {
  const { message } = App.useApp(); // ✅ dùng context AntdApp

  const [addresses, setAddresses] = useState([]);
  const [loadingList, setLoadingList] = useState(false);
  const [form] = Form.useForm();
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);

  const [previewFee, setPreviewFee] = useState(null);
  const [previewDistance, setPreviewDistance] = useState(null);
  const [geocoding, setGeocoding] = useState(false);

  // ========== LOAD DANH SÁCH ĐỊA CHỈ ==========
  const loadAddresses = async () => {
    setLoadingList(true);
    try {
      const res = await api.get("/me/addresses");
      setAddresses(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      console.error(e);
      const err =
        e?.parsed?.message ||
        e?.response?.data?.message ||
        e?.message ||
        "Không tải được danh sách địa chỉ";
      message.error(err);
    } finally {
      setLoadingList(false);
    }
  };

  useEffect(() => {
    loadAddresses();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ========== TÍNH PHÍ SHIP DEMO ==========
  const recomputePreviewFee = (pos) => {
    let lat = null;
    let lng = null;

    if (pos && Array.isArray(pos)) {
      [lat, lng] = pos;
    } else {
      lat = form.getFieldValue("latitude");
      lng = form.getFieldValue("longitude");
    }

    if (lat != null && lng != null) {
      const d = distanceKm(WAREHOUSE.lat, WAREHOUSE.lng, lat, lng);
      setPreviewDistance(d);
      setPreviewFee(calcShippingFee(d));
    } else {
      setPreviewDistance(null);
      setPreviewFee(null);
    }
  };

  // ========== THÊM MỚI ==========
  const startCreate = () => {
    setEditingId(null);
    form.resetFields();
    setPreviewDistance(null);
    setPreviewFee(null);
    setShowForm(true);
  };

  // ========== EDIT ==========
  const handleEdit = (record) => {
    setEditingId(record.id);
    form.setFieldsValue({
      label: record.label,
      provinceCode: record.provinceCode,
      districtCode: record.districtCode,
      addressLine: record.addressLine,
      isDefault: record.isDefault,
      latitude: record.latitude,
      longitude: record.longitude,
    });

    if (record.latitude != null && record.longitude != null) {
      const p = [record.latitude, record.longitude];
      recomputePreviewFee(p);
    } else {
      recomputePreviewFee();
    }

    setShowForm(true);
  };

  // ========== XOÁ ==========
  const handleDelete = async (record) => {
    if (record.isDefault) {
      message.warning(
        "Không thể xoá địa chỉ mặc định. Hãy đặt địa chỉ khác làm mặc định trước."
      );
      return;
    }

    try {
      await api.delete(`/me/addresses/${record.id}`);
      message.success("Đã xoá địa chỉ");
      await loadAddresses();
    } catch (e) {
      console.error(e);
      const err =
        e?.parsed?.message ||
        e?.response?.data?.message ||
        e?.message ||
        "Không xoá được địa chỉ";
      message.error(err);
    }
  };

  // ========== CHỌN QUẬN/HUYỆN (để geocode) ==========
  const handleDistrictSelected = async ({
    provinceName,
    districtName,
  }) => {
    setGeocoding(true);
    try {
      const result = await geocodeDistrict(provinceName, districtName);
      if (result) {
        form.setFieldsValue({
          latitude: result.lat,
          longitude: result.lng,
        });
        recomputePreviewFee([result.lat, result.lng]);
      } else {
        message.warning("Không tìm được toạ độ cho huyện này");
      }
    } catch (e) {
      console.error(e);
      message.error("Lỗi khi định vị huyện trên bản đồ");
    } finally {
      setGeocoding(false);
    }
  };

  // ========== SUBMIT FORM ==========
  const onFinish = async (values) => {
    const payload = {
      label: values.label || "",
      countryCode: "VN",
      provinceCode: values.provinceCode,
      districtCode: values.districtCode,
      addressLine: values.addressLine,
      isDefault: values.isDefault || false,
      latitude: values.latitude ?? null,
      longitude: values.longitude ?? null,
    };

    try {
      if (editingId) {
        await api.put(`/me/addresses/${editingId}`, payload);
        message.success("Cập nhật địa chỉ thành công");
      } else {
        await api.post("/me/addresses", payload);
        message.success("Thêm địa chỉ thành công");
      }
      form.resetFields();
      setEditingId(null);
      setPreviewDistance(null);
      setPreviewFee(null);
      setShowForm(false);
      await loadAddresses();
    } catch (e) {
      console.error(e);
      const err =
        e?.parsed?.message ||
        e?.response?.data?.message ||
        e?.message ||
        "Không lưu được địa chỉ";
      message.error(err);
    }
  };

  const handleCancelEdit = () => {
    form.resetFields();
    setEditingId(null);
    setPreviewDistance(null);
    setPreviewFee(null);
    setShowForm(false);
  };

  const columns = [
    {
      title: "Nhãn",
      dataIndex: "label",
      key: "label",
      render: (v) => v || "Địa chỉ",
    },
    {
      title: "Địa chỉ",
      dataIndex: "addressLine",
      key: "addressLine",
    },
    {
      title: "Mặc định",
      dataIndex: "isDefault",
      key: "isDefault",
      render: (v) => (v ? "✔" : ""),
    },
    {
      title: "Hành động",
      key: "actions",
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => handleEdit(record)}>
            Sửa
          </Button>
          <Popconfirm
            title="Xoá địa chỉ này?"
            onConfirm={() => handleDelete(record)}
          >
            <Button size="small" danger>
              Xoá
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ display: "flex", gap: 16, alignItems: "flex-start" }}>
      {/* Card 1: danh sách địa chỉ + nút Thêm mới ở dưới */}
      <Card
        title="Địa chỉ đã lưu"
        style={{ flex: 1, minWidth: 350 }}
        loading={loadingList}
      >
        {addresses.length === 0 ? (
          <Text type="secondary">
            Chưa có địa chỉ nào. Bấm nút bên dưới để thêm địa chỉ mới.
          </Text>
        ) : (
          <Table
            rowKey="id"
            dataSource={addresses}
            columns={columns}
            pagination={false}
            size="small"
          />
        )}

        <div style={{ marginTop: 16, textAlign: "right" }}>
          <Button type="primary" onClick={startCreate}>
            Thêm địa chỉ mới
          </Button>
        </div>
      </Card>

      {/* Card 2: Form thêm / sửa – chỉ hiện khi showForm = true */}
      {showForm && (
        <Card
          title={editingId ? "Chỉnh sửa địa chỉ" : "Thêm địa chỉ mới"}
          style={{ flex: 1.2, minWidth: 400 }}
          extra={
            <Button type="link" onClick={handleCancelEdit}>
              Đóng
            </Button>
          }
        >
          <Form
            layout="vertical"
            form={form}
            onFinish={onFinish}
            autoComplete="off"
            onValuesChange={(changed) => {
              if (
                Object.prototype.hasOwnProperty.call(changed, "latitude") ||
                Object.prototype.hasOwnProperty.call(changed, "longitude")
              ) {
                recomputePreviewFee();
              }
            }}
          >
            <Title level={5}>Bản đồ vị trí</Title>
            <Text type="secondary">
              Chọn tỉnh / huyện để map nhảy tới khu vực đó, sau đó click vào vị
              trí chính xác trên bản đồ.
            </Text>
            <Divider style={{ margin: "8px 0 16px" }} />

            {/* Map trên cùng */}
            <MapPicker
              form={form}
              onPositionChange={(pos) => {
                recomputePreviewFee(pos);
              }}
            />

            {/* Tỉnh / Huyện */}
            <LocationSelectors
              form={form}
              onDistrictSelected={handleDistrictSelected}
            />

            {/* Địa chỉ + nhãn */}
            <Form.Item
              name="addressLine"
              label="Địa chỉ chi tiết"
              rules={[{ required: true, message: "Vui lòng nhập địa chỉ" }]}
            >
              <Input.TextArea
                rows={2}
                placeholder="Ví dụ: Số 10, ngõ X, đường Y..."
              />
            </Form.Item>

            <Form.Item name="label" label="Nhãn địa chỉ">
              <Input placeholder="Ví dụ: Nhà riêng, Cơ quan..." />
            </Form.Item>

            {previewFee != null && previewDistance != null && (
              <div style={{ marginBottom: 16 }}>
                <Text strong>Khoảng cách tới kho: </Text>
                <Text>
                  {previewDistance.toFixed(1)} km –{" "}
                  <Text strong>
                    Phí ship ước tính: {previewFee.toLocaleString()} đ
                  </Text>
                </Text>
              </div>
            )}
            {geocoding && (
              <Text type="secondary">Đang định vị huyện trên bản đồ...</Text>
            )}

            <Form.Item name="isDefault" valuePropName="checked">
              <Checkbox>Đặt làm địa chỉ mặc định</Checkbox>
            </Form.Item>

            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit">
                  {editingId ? "Cập nhật địa chỉ" : "Thêm địa chỉ"}
                </Button>
                <Button onClick={handleCancelEdit}>Huỷ</Button>
              </Space>
            </Form.Item>
          </Form>
        </Card>
      )}
    </div>
  );
}
