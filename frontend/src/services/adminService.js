const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

export async function getPlatformOverview() {
  try {
    const response = await fetch(`${API_BASE_URL}/admin/overview`, {
      credentials: "include", 
    });

    if (!response.ok) {
      throw new Error("Failed to fetch platform overview");
    }

    return await response.json();
  } catch (error) {
    console.error("Error loading platform overview:", error);
    throw error;
  }
}
