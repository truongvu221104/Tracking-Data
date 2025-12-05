import { useEffect, useMemo, useState } from "react";
import { Button, Input, InputNumber, Space, Table, Tag, message } from "antd";
import api from "../api/axios";
import { useAuth } from "../auth/AuthContext";

const toNumber = (x) => {
  if (x === null || x === undefined) return 0;
  const n = Number(x);
  return Number.isNaN(n) ? 0 : n;
};

const fmtNum = (x) => {
  const n = Number(x);
  return Number.isFinite(n) ? n.toLocaleString() : "-";
};

const fmtDateTime = (x) => {
  if (!x) return "-";
  const d = new Date(x);
  if (isNaN(d.getTime())) return "-";
  // hiển thị dd/MM/yyyy HH:mm
  const pad = (v) => String(v).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

export default function InventoryLedger() {
  const { profile } = useAuth();
  const isAdmin = !!profile?.roles?.includes("ADMIN");

  const [productId, setProductId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState([]);
  const [page, setPage] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
    sort: "id,desc",
  });

  const load = async () => {
    setLoading(true);
    try {
      const params = {
        page: page.current - 1,
        size: page.pageSize,
        sort: page.sort,
      };
      if (productId) params.productId = productId;

      const { data } = await api.get("/inventory-ledger", { params });
      const content = Array.isArray(data?.content) ? data.content : [];
      setRows(content);
      setPage((s) => ({
        ...s,
        total: Number(data?.totalElements || content.length),
      }));
    } catch (e) {
      message.error(
        e?.response?.data?.message || "Không thể tải sổ kho (inventory ledger)"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page.current, page.pageSize, page.sort]);

  const columns = useMemo(() => {
    const base = [
      { title: "ID", dataIndex: "id", width: 90, sorter: true },
      {
        title: "SKU",
        width: 140,
        render: (_, r) => r?.product?.sku || "-",
      },
      {
        title: "Tên sản phẩm",
        render: (_, r) => r?.product?.name || "-",
      },
      {
        title: "Nhập (qtyIn)",
        width: 140,
        align: "right",
        render: (_, r) => fmtNum(r?.qtyIn),
      },
      {
        title: "Xuất (qtyOut)",
        width: 140,
        align: "right",
        render: (_, r) => fmtNum(r?.qtyOut),
      },
      {
        title: "Tồn sau giao dịch (balance)",
        width: 180,
        align: "right",
        render: (_, r) => fmtNum(r?.balance ?? r?.onHand ?? r?.stockBalance),
      },
      {
        title: "Thời điểm",
        dataIndex: "createdAt",
        width: 180,
        render: (v) => fmtDateTime(v),
        sorter: true,
      },
      {
        title: "Tham chiếu",
        width: 160,
        render: (_, r) => r?.refNo || r?.reference || "-",
      },
    ];
    return base;
  }, []);

  const onChangeTable = (pagination, _filters, sorter) => {
    let sort = page.sort; // fallback
    if (Array.isArray(sorter) && sorter.length) {
      // AntD multi-sort: pick the first
      const s0 = sorter[0];
      if (s0.field) sort = `${s0.field},${s0.order === "ascend" ? "asc" : "desc"}`;
    } else if (sorter && sorter.field) {
      sort = `${sorter.field},${sorter.order === "ascend" ? "asc" : "desc"}`;
    }
    setPage((s) => ({
      ...s,
      current: pagination.current,
      pageSize: pagination.pageSize,
      sort,
    }));
  };

  return (
    <>
      <Space style={{ marginBottom: 12 }} wrap>
        <span>Product ID:</span>
        <InputNumber
          style={{ width: 160 }}
          min={1}
          placeholder="VD: 123"
          value={productId}
          onChange={(v) => setProductId(v || null)}
        />
        <Button
          onClick={() => {
            setPage((s) => ({ ...s, current: 1 })); // quay về trang 1
            load();
          }}
          loading={loading}
        >
          Tìm kiếm
        </Button>
        <Button
          onClick={() => {
            setProductId(null);
            setPage((s) => ({ ...s, current: 1 }));
            load();
          }}
          disabled={loading}
        >
          Xóa lọc
        </Button>
        {!isAdmin && (
          <Tag color="orange">
            Lưu ý: API này yêu cầu quyền ADMIN (FE chỉ xem được nếu BE cho phép)
          </Tag>
        )}
      </Space>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={rows}
        loading={loading}
        onChange={onChangeTable}
        pagination={{
          current: page.current,
          pageSize: page.pageSize,
          total: page.total,
          showSizeChanger: true,
          onChange: (c, ps) =>
            setPage((s) => ({ ...s, current: c, pageSize: ps })),
        }}
      />
    </>
  );
}
