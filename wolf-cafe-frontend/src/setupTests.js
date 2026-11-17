import '@testing-library/jest-dom/vitest'
import { afterEach } from 'vitest'

/**
 * Runs before every test file (see `test.setupFiles` in vite.config.js).
 * Adds the jest-dom matchers, such as toBeInTheDocument(), to expect(), and
 * clears the saved login after each test so no role or token carries over.
 */
afterEach(() => {
  sessionStorage.clear()
  localStorage.clear()
})
