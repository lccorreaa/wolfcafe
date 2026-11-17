import axios from 'axios'
import { API_BASE_URL } from './ApiConfig'

/** Base URL for the Auth API - Correspond to methods in Backend's Auth Controller. */
const AUTH_REST_API_BASE_URL = API_BASE_URL + '/api/auth'

/**
 * Sends the login token, when there is one, in the Authorization header of every
 * request this application makes with axios.  The backend answers requests to
 * secured endpoints without it with 401 Unauthorized.
 */
axios.interceptors.request.use(function (config) {
  const token = getToken()
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

/** POST Register - registers a new customer */
export const registerAPICall = (registerObj) => axios.post(AUTH_REST_API_BASE_URL + '/register', registerObj)

/** POST Login - logs in and returns a login token and the user's role */
export const loginAPICall = (usernameOrEmail, password) => axios.post(AUTH_REST_API_BASE_URL + '/login', { usernameOrEmail, password })

/** GET users - lists staff and customers for an administrator */
export const getUsersAPICall = () => axios.get(AUTH_REST_API_BASE_URL + '/users')

/** PUT user - updates the editable profile fields of a staff/customer account */
export const updateUserAPICall = (id, user) => axios.put(AUTH_REST_API_BASE_URL + '/user/' + id, user)

/** DELETE user - removes a staff/customer account */
export const deleteUserAPICall = (id) => axios.delete(AUTH_REST_API_BASE_URL + '/user/' + id)

/** POST staff - creates a staff account (admin only) */
export const createStaffAPICall = (staff) => axios.post(AUTH_REST_API_BASE_URL + '/staff', staff)

/** Saves the login token, including its "Bearer " prefix */
export const storeToken = (token) => localStorage.setItem('token', token)

/** Returns the saved login token, or null if nobody is logged in */
export const getToken = () => localStorage.getItem('token')

/** Saves the logged in user's username and role for this browser tab */
export const saveLoggedInUser = (username, role) => {
    sessionStorage.setItem('authenticatedUser', username)
    sessionStorage.setItem('role', role)
}

/** Returns true if a user is logged in */
export const isUserLoggedIn = () => sessionStorage.getItem('authenticatedUser') != null

/** Returns the logged in user's username */
export const getLoggedInUser = () => sessionStorage.getItem('authenticatedUser')

/** Returns the logged in user's role, such as 'ROLE_STAFF', or null */
export const getRole = () => sessionStorage.getItem('role')

/**
 * Returns true if the logged in user has one of the given roles.  Use this to
 * decide what to show; the backend still checks every request, so hiding a
 * button is a convenience for the user, not a security control.
 * @param {...string} roles role names, such as 'ROLE_ADMIN', 'ROLE_STAFF'
 */
export const hasAnyRole = (...roles) => roles.includes(getRole())

/** Returns true if the logged in user is the admin */
export const isAdminUser = () => hasAnyRole('ROLE_ADMIN')

/** Logs out the current user */
export const logout = () => {
    localStorage.clear()
    sessionStorage.clear()
}
