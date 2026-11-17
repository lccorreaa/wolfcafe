import axios from "axios"
import { API_BASE_URL } from "./ApiConfig"

/** Base URL for the Recipe API - Correspond to methods in Backend's Recipe Controller. */
const REST_API_BASE_URL = API_BASE_URL + "/api/orders"

/** GET Recipes - lists all recipes */
export const listRecipes = () => axios.get(REST_API_BASE_URL)

/** POST Recipe - creates a new recipe */
export const createRecipe = (recipe) => axios.post(REST_API_BASE_URL, recipe)

/** GET Recipe - gets a single recipe by name */
export const getRecipe = (name) => axios.get(REST_API_BASE_URL + "/" + encodeURIComponent(name))

/** PUT Recipe - updates the recipe with the given id */
export const updateRecipe = (id, recipe) => axios.put(REST_API_BASE_URL + "/" + id, recipe)

/** DELETE Recipe - deletes the recipe with the given id */
export const deleteRecipe = (id) => axios.delete(REST_API_BASE_URL + "/" + id)