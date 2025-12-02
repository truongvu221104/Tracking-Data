import { useEffect, useState } from "react";
import { Input, Table } from "antd";
import api from "../api/axios";

export default function Customers() {
  const [q, setQ] = useState("");
  const [data, setData] = useState([]);
  const [page, setPage] = useState({ current:1, pageSize:10, total:0 });

  const load = async () => {
    const p = { page: page.current - 1, size: page.pageSize, q };
    const { data } = await api.get("/customers", { params: p });
    setData(data.content);
    setPage((s)=>({ ...s, total: data.totalElements }));
  };

  useEffect(()=>{ load(); /* eslint-disable-next-line */ }, [q, page.current, page.pageSize]);

  return (
    <>
      <Input placeholder="Tìm kiếm theo tên" value={q} onChange={(e)=>{ setQ(e.target.value); setPage(s=>({...s, current:1})); }} allowClear style={{ maxWidth: 260, marginBottom: 12 }}/>
      <Table rowKey="id" dataSource={data} columns={[
        { title: "Thứ tự", dataIndex: "id", width: 80 },
        { title: "Mã", dataIndex: "code" },
        { title: "Tên", dataIndex: "name" },
        { title: "Số điện thoại", dataIndex: "phone" },
        { title: "Email", dataIndex: "email" },
      ]} pagination={{
        current: page.current, pageSize: page.pageSize, total: page.total,
        showSizeChanger: true,
        onChange: (c, ps)=>setPage((s)=>({ ...s, current:c, pageSize:ps })),
      }}/>
    </>
  );
}