import axios from 'axios'
import { API_BASE_URL } from './ApiConfig'

/** Base URL for the Ingredient API. */
const REST_API_BASE_URL = API_BASE_URL + '/api/ingredients'

/** POST Ingredient - adds an ingredient with its initial inventory amount. */
export const createIngredient = (ingredient) => axios.post(REST_API_BASE_URL, ingredient)
