import axios from "axios"
import { API_BASE_URL } from "./ApiConfig"

/** Base URL for the Tax Rate API - Correspond to methods in Backend's TaxRateController. */
const REST_API_BASE_URL = API_BASE_URL + "/api/tax"

/** GET Tax Rate - returns the current tax rate as a percent, e.g. { id, rate: 8.0 } */
export const getTaxRate = () => axios.get(REST_API_BASE_URL)

/** PUT Tax Rate - sets the tax rate (admin only) */
export const updateTaxRate = (rate) => axios.put(REST_API_BASE_URL, { rate })
