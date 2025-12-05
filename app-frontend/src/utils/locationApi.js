const BASE_URL = "https://vapi.vnappmob.com/api/v2";

// Lấy danh sách tỉnh / thành
export async function fetchProvinces() {
  const res = await fetch(`${BASE_URL}/province/`);
  if (!res.ok) {
    throw new Error("Failed to fetch provinces");
  }
  const json = await res.json();
  // API trả { results: [ { province_id, province_name, ... } ] }
  return json.results || [];
}

// Lấy danh sách quận / huyện theo mã tỉnh
export async function fetchDistricts(provinceId) {
  const res = await fetch(`${BASE_URL}/province/district/${provinceId}`);
  if (!res.ok) {
    throw new Error("Failed to fetch districts");
  }
  const json = await res.json();
  return json.results || [];
}
