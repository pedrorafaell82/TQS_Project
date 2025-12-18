import { Link, NavLink } from "react-router-dom";
import "./Navbar.css";

export default function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar-left">
        <Link to="/instruments" className="navbar-brand">
          SoundShop
        </Link>
      </div>

      <div className="navbar-links">
        <NavLink to="/instruments">Instruments</NavLink>
        <NavLink to="/my-bookings">My Bookings</NavLink>
        <NavLink to="/booking-history">History</NavLink>
        <NavLink to="/pending-requests">Pending</NavLink>
      </div>

      <div className="navbar-auth">
        <NavLink to="/login">Login</NavLink>
        <NavLink to="/register">Register</NavLink>
      </div>
    </nav>
  );
}