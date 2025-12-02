import { Button, Card, DatePicker, Form, Input, InputNumber, Select, Space, Table, message } from "antd";
import { useEffect, useState } from "react";
import dayjs from "dayjs";
import api from "../api/axios";

export default function SalesCreate() {
  const [products, setProducts] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => { loadRefs(); }, []);
  const loadRefs = async () => {
    const [p, c] = await Promise.all([
      api.get("/products", { params: { page:0, size:100 }}),
      api.get("/customers", { params: { page:0, size:100 }})
    ]);
    setProducts(p.data.content || []);
    setCustomers(c.data.content || []);
  };

  const addItem = () => setItems((s)=>[...s, { productId:null, qty:1, unitPrice:0, discountPercent:0 }]);
  const removeItem = (idx) => setItems(items.filter((_,i)=>i!==idx));
  const update = (idx, patch) => setItems((s)=> s.map((r,i)=> i===idx ? { ...r, ...patch } : r));

  const cols = [
    {
      title:"Product", dataIndex:"productId",
      render: (_,__,idx)=>(
        <Select style={{ width: 260 }} value={items[idx].productId}
          onChange={(v)=>update(idx,{productId:v})}
          options={products.map(p=>({ value:p.id, label:`${p.sku} - ${p.name}` }))}/>
      )
    },
    {
      title:"Qty", dataIndex:"qty", width:120,
      render: (_,__,idx)=>(
        <InputNumber min={1} value={items[idx].qty}
          onChange={(v)=>update(idx,{qty: v||1})}/>
      )
    },
    {
      title:"Unit Price", dataIndex:"unitPrice", width:160,
      render: (_,__,idx)=>(
        <InputNumber min={0} value={items[idx].unitPrice}
          onChange={(v)=>update(idx,{unitPrice: v||0})}/>
      )
    },
    {
      title:"Discount %", dataIndex:"discountPercent", width:140,
      render: (_,__,idx)=>(
        <InputNumber min={0} max={100} value={items[idx].discountPercent}
          onChange={(v)=>update(idx,{discountPercent: v||0})}/>
      )
    },
    {
      title:"Line Total",
      render: (_,__,idx)=>{
        const { qty, unitPrice, discountPercent } = items[idx];
        const base = (qty * unitPrice) || 0;
        const after = base - base * (discountPercent||0) / 100;
        return after.toFixed(2);
      }, width:160
    },
    { title:"", render:(_,__,idx)=><Button danger onClick={()=>removeItem(idx)}>Remove</Button>, width:120 }
  ];

  const onFinish = async (values) => {
    if (!values.customerId) { message.error("Chọn khách hàng"); return; }
    if (items.length===0) { message.error("Thêm ít nhất 1 dòng hàng"); return; }
    setLoading(true);
    try {
      const payload = {
        code: values.code,
        customerId: values.customerId,
        orderDate: values.orderDate ? values.orderDate.format("YYYY-MM-DD") : null,
        status: "CONFIRMED",
        items: items.map(i=>({
          productId: i.productId, qty: i.qty, unitPrice: i.unitPrice, discountPercent: i.discountPercent || 0
        })),
        discountAmount: values.discountAmount || 0,
        taxAmount: values.taxAmount || 0
      };
      const { data } = await api.post("/sales-orders", payload);
      message.success(`Tạo SO thành công (id=${data.id})`);
      form.resetFields(); setItems([]);
    } catch(e) {
      message.error(e?.response?.data?.message || "Lỗi tạo SO");
    } finally { setLoading(false); }
  };

  return (
    <Card title="Create Sales Order" extra={<Button onClick={addItem}>+ Add item</Button>}>
      <Form form={form} layout="vertical" onFinish={onFinish}>
        <Space wrap>
          <Form.Item name="code" label="Code" rules={[{ required:true }]}>
            <Input style={{ width:220 }}/>
          </Form.Item>
          <Form.Item name="customerId" label="Customer" rules={[{ required:true }]}>
            <Select style={{ width: 320 }}
              options={customers.map(c=>({ value:c.id, label:`${c.code} - ${c.name}` }))}/>
          </Form.Item>
          <Form.Item name="orderDate" label="Date" initialValue={dayjs()}>
            <DatePicker />
          </Form.Item>
          <Form.Item name="discountAmount" label="Discount"><InputNumber min={0}/></Form.Item>
          <Form.Item name="taxAmount" label="Tax"><InputNumber min={0}/></Form.Item>
        </Space>
        <Table rowKey={(_,i)=>i} columns={cols} dataSource={items} pagination={false} style={{ marginTop: 12 }}/>
        <Button type="primary" htmlType="submit" loading={loading} style={{ marginTop: 12 }}>
          Submit
        </Button>
      </Form>
    </Card>
  );
}
