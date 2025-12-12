import { useEffect, useState } from "react";
import { Form, Select, App } from "antd";
import { fetchProvinces, fetchDistricts } from "../../utils/locationApi";

const { Option } = Select;

export default function LocationSelectors({ form, onDistrictSelected }) {
  const { message } = App.useApp();

  const [provinces, setProvinces] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [loadingProvince, setLoadingProvince] = useState(false);
  const [loadingDistrict, setLoadingDistrict] = useState(false);
  const [selectedProvince, setSelectedProvince] = useState(null);

  const provinceCode = Form.useWatch("provinceCode", form);
  const districtCode = Form.useWatch("districtCode", form);

  useEffect(() => {
    const loadProvinces = async () => {
      setLoadingProvince(true);
      try {
        const data = await fetchProvinces();
        setProvinces(data);
      } catch (e) {
        console.error(e);
        message.error("Không tải được danh sách tỉnh/thành");
      } finally {
        setLoadingProvince(false);
      }
    };
    loadProvinces();
  }, []);

  const handleProvinceChange = (provinceId) => {
    form.setFieldsValue({ provinceCode: provinceId, districtCode: undefined });
  };

  const handleDistrictChange = (districtId) => {
    form.setFieldsValue({ districtCode: districtId });

    if (!districtId || !selectedProvince) return;

    const district =
      districts.find((d) => d.district_id === districtId) || null;

    if (district && onDistrictSelected) {
      onDistrictSelected({
        provinceId: selectedProvince.province_id,
        provinceName: selectedProvince.province_name,
        districtId: district.district_id,
        districtName: district.district_name,
      });
    }
  };

  // 🧠 Khi provinceCode trên form thay đổi (ví dụ: do setFieldsValue khi edit)
  useEffect(() => {
    if (!provinceCode) {
      setSelectedProvince(null);
      setDistricts([]);
      return;
    }

    if (!provinces.length) return; // đợi provinces load xong

    const province =
      provinces.find((p) => p.province_id === provinceCode) || null;
    setSelectedProvince(province);

    if (!province) return;

    const loadDistricts = async () => {
      setLoadingDistrict(true);
      try {
        const data = await fetchDistricts(provinceCode);
        setDistricts(data);
      } catch (e) {
        console.error(e);
        message.error("Không tải được danh sách quận/huyện");
      } finally {
        setLoadingDistrict(false);
      }
    };

    loadDistricts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [provinceCode, provinces]);

  return (
    <>
      <Form.Item
        name="provinceCode"
        label="Tỉnh / Thành phố"
        rules={[{ required: true, message: "Vui lòng chọn tỉnh/thành" }]}
      >
        <Select
          placeholder="Chọn tỉnh/thành"
          onChange={handleProvinceChange}
          loading={loadingProvince}
          allowClear
          showSearch
          optionFilterProp="children"
        >
          {provinces.map((p) => (
            <Option key={p.province_id} value={p.province_id}>
              {p.province_name}
            </Option>
          ))}
        </Select>
      </Form.Item>

      <Form.Item
        name="districtCode"
        label="Quận / Huyện"
        rules={[{ required: true, message: "Vui lòng chọn quận/huyện" }]}
      >
        <Select
          placeholder="Chọn quận/huyện"
          loading={loadingDistrict}
          allowClear
          showSearch
          optionFilterProp="children"
          onChange={handleDistrictChange}
        >
          {districts.map((d) => (
            <Option key={d.district_id} value={d.district_id}>
              {d.district_name}
            </Option>
          ))}
        </Select>
      </Form.Item>
    </>
  );
}
