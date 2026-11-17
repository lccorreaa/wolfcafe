import axios from 'axios'

const ORDERS_API_BASE_URL = 'http://localhost:8080/api/orders'

export const listPendingOrders = () => axios.get(ORDERS_API_BASE_URL)

export const fulfillOrder = (id) =>
    axios.put(`${ORDERS_API_BASE_URL}/${id}/fulfill`)