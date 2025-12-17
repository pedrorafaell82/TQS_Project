/**
 * User Story: SOUN-23 - User Management
 * Admin can view and manage platform users.
 */
import React, { useEffect, useMemo, useState } from "react";
import { getAllUsers, setUserActive } from "../../services/adminUsersService";
import "./UserManagement.css";

export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await getAllUsers();
      setUsers(data);
      setError(null);
    } catch (e) {
      console.error(e);
      setError("Failed to load users (check backend endpoint / permissions).");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const filteredUsers = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return users;

    return users.filter((u) => {
      const name = (u.name || "").toLowerCase();
      const email = (u.email || "").toLowerCase();
      const role = (u.role || "").toLowerCase();
      return name.includes(q) || email.includes(q) || role.includes(q);
    });
  }, [users, query]);

  const handleToggleActive = async (user) => {
    try {
      await setUserActive(user.id, !user.active);
      setUsers((prev) =>
        prev.map((u) => (u.id === user.id ? { ...u, active: !u.active } : u))
      );
    } catch (e) {
      console.error(e);
      alert("Could not update user status.");
    }
  };

  if (loading) return <div className="loading">Loading users...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="user-mgmt">
      <div className="user-mgmt-header">
        <h2>Admin — User Management</h2>
        <button className="btn" onClick={loadUsers}>Refresh</button>
      </div>

      <div className="user-mgmt-filters">
        <input
          type="text"
          placeholder="Search by name, email or role..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <span className="count">
          Showing {filteredUsers.length} of {users.length}
        </span>
      </div>

      <div className="table-wrap">
        <table className="users-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th style={{ width: "170px" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map((u) => (
              <tr key={u.id} className={!u.active ? "inactive-row" : ""}>
                <td>{u.name || "-"}</td>
                <td>{u.email || "-"}</td>
                <td>{u.role || "-"}</td>
                <td>
                  <span className={u.active ? "badge active" : "badge inactive"}>
                    {u.active ? "Active" : "Inactive"}
                  </span>
                </td>
                <td>
                  <button
                    className="btn secondary"
                    onClick={() => handleToggleActive(u)}
                  >
                    {u.active ? "Deactivate" : "Activate"}
                  </button>
                </td>
              </tr>
            ))}

            {filteredUsers.length === 0 && (
              <tr>
                <td colSpan="5" style={{ textAlign: "center", padding: "1rem" }}>
                  No users found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
