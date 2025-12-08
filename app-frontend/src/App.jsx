import { Layout, Menu, Button } from "antd";
import {
  createBrowserRouter,
  RouterProvider,
  Link,
  Outlet,
  Navigate,
  useLocation,
  useNavigate,
} from "react-router-dom";

import AuthProvider, { useAuth } from "./auth/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Products from "./pages/Products";
import Customers from "./pages/Customers";
import PurchaseCreate from "./pages/PurchaseCreate";
import SalesCreate from "./pages/SalesCreate";
import InventoryLedger from "./pages/InventoryLedger";
import OAuth2Callback from "./pages/OAuth2Callback";
import CustomerProfile from "./pages/CustomerProfile";
import ShopHome from "./pages/shop/ShopHome";
import ProductDetail from "./pages/shop/ProductDetail";


const { Header, Content } = Layout;

const AppShell = () => {
  const { hasRole, profile, setToken } = useAuth() || {};
  const location = useLocation();
  const navigate = useNavigate();

  const items = [
    { key: "/shop", label: <Link to="/shop">Cửa hàng</Link> },
    { key: "/profile", label: <Link to="/profile">Hồ sơ</Link> },
    ...(hasRole && hasRole("ADMIN")
      ? [
        { key: "/products", label: <Link to="/products">Quản lý sản phẩm</Link> },
        { key: "/customers", label: <Link to="/customers">Khách hàng</Link> },
        { key: "/purchase/create", label: <Link to="/purchase/create">Tạo sản phẩm mới</Link> },
        { key: "/sales/create", label: <Link to="/sales/create">Tạo đơn bán hàng</Link> },
        { key: "/inventory-ledger", label: <Link to="/inventory-ledger">Sổ kho</Link> },
      ]
      : []),
  ];

  const activeKey =
    items.find((i) => location.pathname.startsWith(i.key))?.key || "";

  const onLogout = () => {
    localStorage.removeItem("accessToken");
    setToken?.(null);
    navigate("/login", { replace: true });
  };

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Header style={{ display: "flex", alignItems: "center", gap: 16 }}>
        <div style={{ color: "#fff", fontWeight: 700 }}>Trường Vũ</div>
        <Menu
          theme="dark"
          mode="horizontal"
          selectable
          selectedKeys={[activeKey]}
          items={items}
        />
        <div style={{ marginLeft: "auto", color: "#fff" }}>
          {profile?.username}
        </div>
        <Button style={{ marginLeft: 12 }} onClick={onLogout}>
          Đăng xuất
        </Button>
      </Header>
      <Content style={{ padding: 24 }}>
        <Outlet />
      </Content>
    </Layout>
  );
};

const router = createBrowserRouter([
  { path: "/login", element: <Login /> },
  { path: "/oauth2/callback", element: <OAuth2Callback /> },
  { path: "/register", element: <Register /> },
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/shop" replace /> },
      { path: "shop", element: <ShopHome /> },
      { path: "shop/products/:id", element: <ProductDetail /> },
      { path: "products", element: <Products /> },
      { path: "profile", element: <CustomerProfile /> },
      { path: "customers", element: <Customers /> },
      { path: "purchase/create", element: <PurchaseCreate /> },
      { path: "sales/create", element: <SalesCreate /> },
      { path: "inventory-ledger", element: <InventoryLedger /> },
    ],
  },
  { path: "*", element: <Navigate to="/products" replace /> },
]);

export default function App() {
  return <RouterProvider router={router} />;
}
