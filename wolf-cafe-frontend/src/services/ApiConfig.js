/**
 * Base URL for the WolfCafe backend REST API.
 *
 * Defaults to the backend's development port.  To point the frontend at a
 * different backend, create a `.env.local` file in `wolf-cafe-frontend`
 * containing, for example:
 *
 *     VITE_API_BASE_URL=http://localhost:9090
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080"
