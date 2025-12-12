import { useEffect, useState } from "react";
import { Form, Input } from "antd";
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from "react-leaflet";
import L from "leaflet";

import "leaflet/dist/leaflet.css";

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

const DEFAULT_CENTER = [21.0278, 105.8342]; 

function ClickHandler({ onChange }) {
  useMapEvents({
    click(e) {
      onChange(e.latlng);
    },
  });
  return null;
}

function Recenter({ position }) {
  const map = useMap();
  useEffect(() => {
    if (!position) return;
    map.setView(position, map.getZoom());
  }, [position, map]);
  return null;
}

export default function MapPicker({ form, onPositionChange }) {
  const [position, setPosition] = useState(DEFAULT_CENTER);

  const lat = Form.useWatch("latitude", form);
  const lng = Form.useWatch("longitude", form);

  useEffect(() => {
    if (
      lat === undefined ||
      lat === null ||
      lng === undefined ||
      lng === null
    ) {
      return;
    }
    const p = [lat, lng];
    setPosition(p);
    if (typeof onPositionChange === "function") onPositionChange(p);
  }, [lat, lng, onPositionChange]);

  const handleChange = (latlng) => {
    const { lat, lng } = latlng;
    const p = [lat, lng];
    setPosition(p);
    form.setFieldsValue({ latitude: lat, longitude: lng });
    if (typeof onPositionChange === "function") onPositionChange(p);
  };

  return (
    <>
      <div style={{ height: 300, marginBottom: 8 }}>
        <MapContainer
          center={position}
          zoom={13}
          style={{ height: "100%", width: "100%" }}
        >
          <TileLayer
            attribution="&copy; OpenStreetMap"
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <ClickHandler onChange={handleChange} />
          <Recenter position={position} />
          <Marker position={position} />
        </MapContainer>
      </div>

      <div style={{ display: "flex", gap: 8 }}>
        <Form.Item label="Vĩ độ (lat)" name="latitude" style={{ flex: 1 }}>
          <Input readOnly />
        </Form.Item>
        <Form.Item label="Kinh độ (lng)" name="longitude" style={{ flex: 1 }}>
          <Input readOnly />
        </Form.Item>
      </div>
    </>
  );
}
