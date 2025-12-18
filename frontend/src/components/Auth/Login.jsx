import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);

  const navigate = useNavigate();
  const { setUser } = useAuth();

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);

    const response = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      setError("Invalid email or password");
      return;
    }

    const user = await response.json();
    setUser(user);

    if (user.roles.includes("OWNER")) {
      navigate("/owner");
    } else {
      navigate("/instruments");
    }
  }

  return (
    <div className="auth-page">
      <h2>Login</h2>

      <form onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          required
        />

        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          required
        />

        {error && <p className="error">{error}</p>}

        <button type="submit">Login</button>
      </form>
    </div>
  );
}
