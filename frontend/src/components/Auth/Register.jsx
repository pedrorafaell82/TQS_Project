import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function RegisterPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);

    const response = await fetch("http://localhost:8080/api/users/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name,
        email,
        password
        // roles omitted → defaults to RENTER
      })
    });

    if (!response.ok) {
      setError("Registration failed");
      return;
    }

    navigate("/login");
  }

  return (
    <div className="auth-page">
      <h2>Create account</h2>

      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Name"
          value={name}
          onChange={e => setName(e.target.value)}
          required
        />

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

        <button type="submit">Register</button>
      </form>
    </div>
  );
}
