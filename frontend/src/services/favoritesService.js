const API_BASE = "http://localhost:8080/api/favorites";

export async function addFavorite(id) {
  const response = await fetch(`${API_BASE}/${id}`, { method: "POST" });
  return response.ok;
}

export async function removeFavorite(id) {
  const response = await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
  return response.ok;
}

export async function getFavorites() {
  const response = await fetch(API_BASE);
  if (!response.ok) return [];
  return response.json();
}
