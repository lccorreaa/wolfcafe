import React, { useEffect, useState } from 'react'
import { listPendingOrders, fulfillOrder } from '../services/OrderService'

const ListOrdersComponent = () => {
    const [orders, setOrders] = useState([])
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(true)
    const [fulfillingId, setFulfillingId] = useState(null)

    useEffect(() => {
        loadPendingOrders()
    }, [])

    function loadPendingOrders() {
        setLoading(true)
        setError('')

        listPendingOrders()
            .then(response => {
                setOrders(response.data)
            })
            .catch(requestError => {
                console.error(requestError)
                setError('Unable to load pending orders.')
            })
            .finally(() => {
                setLoading(false)
            })
    }

    function handleFulfill(id) {
        setFulfillingId(id)
        setError('')

        fulfillOrder(id)
            .then(() => {
                loadPendingOrders()
            })
            .catch(requestError => {
                console.error(requestError)
                setError('Unable to fulfill this order.')
            })
            .finally(() => {
                setFulfillingId(null)
            })
    }

    return (
        <div className="container">
            <br /><br />
            <div className="row">
                <div className="card col-12 col-lg-8 offset-lg-2">
                    <h2 className="text-center mt-3">List of Orders</h2>

                    <div className="card-body">
                        {error && (
                            <div className="alert alert-danger" role="alert">
                                {error}
                            </div>
                        )}

                        <div className="row fw-bold border-bottom pb-2 mb-2">
                            <div className="col-4">Customer</div>
                            <div className="col-6">Items</div>
                            <div className="col-2 text-end">Action</div>
                        </div>

                        <div
                            className="border rounded p-2"
                            style={{
                                height: '60vh',
                                overflowY: 'auto',
                                backgroundColor: '#f8f9fa',
                            }}
                        >
                            {loading ? (
                                <p className="text-center mt-3">Loading orders...</p>
                            ) : orders.length === 0 ? (
                                <p className="text-center mt-3">No pending orders.</p>
                            ) : (
                                orders.map(order => (
                                    <div
                                        className="row align-items-center border-bottom py-3"
                                        key={order.id}
                                    >
                                        <div className="col-4">
                                            <strong>{order.customerName}</strong>
                                        </div>

                                        <div className="col-6">
                                            {(order.items ?? []).map((item, index) => (
                                                <div key={item.id ?? index}>
                                                    {item.amount}x {item.itemName}
                                                </div>
                                            ))}
                                        </div>

                                        <div className="col-2 text-end">
                                            <button
                                                className="btn btn-dark btn-sm"
                                                onClick={() => handleFulfill(order.id)}
                                                disabled={fulfillingId === order.id}
                                            >
                                                {fulfillingId === order.id
                                                    ? 'Fulfilling...'
                                                    : 'Fulfill'}
                                            </button>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default ListOrdersComponent