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
import OAuth2Callback from "./pages/OAuth2Callback";

import CustomerProfile from "./pages/CustomerProfile";
import ShopHome from "./pages/shop/ShopHome";
import ProductDetail from "./pages/shop/ProductDetail";
import Cart from "./pages/Cart";
import Checkout from "./pages/Checkout";
import MyOrders from "./pages/MyOrders";

import AdminOrders from "./pages/AdminOrders";
import AdminProducts from "./pages/AdminProducts";
import AdminProductForm from "./pages/AdminProductForm";
import AdminStockIn from "./pages/AdminStockIn";
import SupportChat from "./pages/SupportChat";

import AdminCustomers from "./pages/AdminCustomers";

const { Header, Content } = Layout;

// Helper tìm active key ổn định hơn
const getActiveKey = (items, pathname) => {
  const sorted = [...items].sort((a, b) => b.key.length - a.key.length);
  return sorted.find((i) => pathname.startsWith(i.key))?.key || "";
};

const AppShell = () => {
  const { hasRole, profile, setToken } = useAuth() || {};
  const location = useLocation();
  const navigate = useNavigate();

  const isAdmin = !!hasRole?.("ADMIN");
  const isAdminArea = location.pathname.startsWith("/admin");

  const customerItems = [
    { key: "/shop", label: <Link to="/shop">Cửa hàng</Link> },
    { key: "/profile", label: <Link to="/profile">Hồ sơ</Link> },
    { key: "/cart", label: <Link to="/cart">Giỏ hàng</Link> },
    { key: "/orders", label: <Link to="/orders">Đơn hàng</Link> },
    { key: "/support", label: <Link to="/support">Hỗ trợ</Link> },

    ...(isAdmin
      ? [{ key: "/admin", label: <Link to="/admin/orders">Khu quản trị</Link> }]
      : []),
  ];
  const adminItems = [
    { key: "/admin/orders", label: <Link to="/admin/orders">QL Đơn hàng</Link> },
    { key: "/admin/products", label: <Link to="/admin/products">QL Sản phẩm</Link> },
    { key: "/admin/customers", label: <Link to="/admin/customers">Khách hàng</Link> },
    { key: "/admin/stock-in", label: <Link to="/admin/stock-in">Nhập kho</Link> },
    { key: "/shop", label: <Link to="/shop">Về cửa hàng</Link> },
  ];

  const items = isAdmin && isAdminArea ? adminItems : customerItems;

  const activeKey = getActiveKey(items, location.pathname);

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

// ===== Router =====
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
      { path: "profile", element: <CustomerProfile /> },
      { path: "cart", element: <Cart /> },
      { path: "checkout", element: <Checkout /> },
      { path: "orders", element: <MyOrders /> },
      { path: "support", element: <SupportChat /> },
      {
        element: (
          <ProtectedRoute roles={["ADMIN"]}>
            <Outlet />
          </ProtectedRoute>
        ),
        children: [
          { path: "admin", element: <Navigate to="/admin/orders" replace /> },

          { path: "admin/orders", element: <AdminOrders /> },

          { path: "admin/products", element: <AdminProducts /> },
          { path: "admin/products/new", element: <AdminProductForm /> },
          { path: "admin/products/:id", element: <AdminProductForm /> },
          { path: "admin/customers", element: <AdminCustomers /> },
          { path: "admin/stock-in", element: <AdminStockIn /> },
        ],
      },
    ],
  },

  { path: "*", element: <Navigate to="/shop" replace /> },
]);

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}
