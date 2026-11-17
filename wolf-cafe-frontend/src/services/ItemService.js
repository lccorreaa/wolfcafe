import axios from 'axios'
import { API_BASE_URL } from './ApiConfig'

/** Base URL for the Item API - Correspond to methods in Backend's Item Controller. */
const BASE_REST_API_URL = API_BASE_URL + '/api/items'

/** POST Item - creates a new item */
export const saveItem = (item) => axios.post(BASE_REST_API_URL, item)

/** GET Item - gets a single item by id */
export const getItemById = (id) => axios.get(BASE_REST_API_URL + '/' + id)

/** GET Items - lists all items */
export const getAllItems = () => axios.get(BASE_REST_API_URL)

/** PUT Item - updates the item with the given id */
export const updateItem = (id, item) => axios.put(BASE_REST_API_URL + '/' + id, item)

/** DELETE Item - deletes the item with the given id */
export const deleteItemById = (id) => axios.delete(BASE_REST_API_URL + '/' + id)
