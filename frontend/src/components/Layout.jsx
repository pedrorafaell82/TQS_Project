import { Outlet, useLocation } from "react-router-dom";
import Navbar from "./Navbar";

export default function Layout() {
  const location = useLocation();

  const hideNavbarRoutes = ["/login", "/register"];
  const hideNavbar = hideNavbarRoutes.includes(location.pathname);

  return (
    <div className="app">
      {!hideNavbar && <Navbar />}
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
