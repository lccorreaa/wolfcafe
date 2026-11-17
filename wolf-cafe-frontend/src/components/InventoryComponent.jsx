import { useEffect, useState } from 'react'
import { deleteItemById } from '../services/ItemService'
import { getInventory, updateInventory } from '../services/InventoryService'

/** Returns true for a non-negative integer that the backend can store.
 * Received a complete override on 11/4/26 to implement items rather than ingerdients.
 * Changes were mostly directed by Github Copilot scanning the wireframe for the inventory page and the UC11 requirements.
 * @param {string} value - The value to check.
 * @returns {boolean} - True if the value is a valid non-negative integer, false otherwise.
 */
function isValidUnits(value) {
    return /^\d+$/.test(value) &&
        Number.isSafeInteger(Number(value)) &&
        Number(value) <= 2147483647
}

/** Shows the current item inventory and lets staff add units or delete items. */
const InventoryComponent = () => {
    const [items, setItems] = useState([])
    const [additions, setAdditions] = useState({})
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    useEffect(() => {
        loadInventory()
    }, [])

    function loadInventory() {
        getInventory().then((response) => {
            setItems(response.data.items ?? [])
            setAdditions({})
        }).catch((requestError) => {
            console.error(requestError)
            setError('Unable to load inventory. Please try again.')
        })
    }

    function updateAddition(id, value) {
        setAdditions({ ...additions, [id]: value })
        setError('')
        setSuccess('')
    }

    async function submitInventory(event) {
        event.preventDefault()
        setError('')
        setSuccess('')

        const changedItems = items.filter(item => additions[item.id] !== undefined && additions[item.id] !== '')
        if (changedItems.some(item => !isValidUnits(additions[item.id]))) {
            setError('Quantity to add must be a non-negative integer.')
            return
        }
        if (changedItems.length === 0) {
            setError('Enter a quantity to add for at least one item.')
            return
        }

        const requestItems = changedItems.map(item => ({
            id: item.id,
            amount: Number(additions[item.id])
        }))

        try {
            const response = await updateInventory({ items: requestItems })
            setItems(response.data.items ?? [])
            setAdditions({})
            setSuccess('Inventory updated.')
        } catch (requestError) {
            console.error(requestError)
            setError(requestError.response?.data?.message ??
                'Unable to update inventory. Please try again.')
        }
    }

    async function deleteItem(item) {
        setError('')
        setSuccess('')

        try {
            await deleteItemById(item.id)
            setItems(items.filter(existingItem => existingItem.id !== item.id))
            setAdditions((current) => {
                const remaining = { ...current }
                delete remaining[item.id]
                return remaining
            })
            setSuccess(`Deleted ${item.name}.`)
        } catch (requestError) {
            console.error(requestError)
            setError(requestError.response?.data?.message ??
                'Could not delete the item. Refresh the inventory and try again.')
        }
    }

    return (
        <div className="container">
            <br /><br />
            <div className="row">
                <div className="card col-lg-10 offset-lg-1 col-xl-8 offset-xl-2">
                    <div className="card-header">
                        <h2 className="text-center">Inventory</h2>
                    </div>
                    <div className="card-body">
                        {error && <div className="alert alert-danger" role="alert">{error}</div>}
                        {success && <div className="alert alert-success" role="status">{success}</div>}

                        <form onSubmit={submitInventory}>
                            <div className="table-responsive">
                                <table className="table table-bordered align-middle">
                                    <thead className="table-light">
                                        <tr>
                                            <th scope="col">Item Name</th>
                                            <th scope="col">Price</th>
                                            <th scope="col">Description</th>
                                            <th scope="col">Current Amount</th>
                                            <th scope="col">Add</th>
                                            <th scope="col">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {items.length === 0 && (
                                            <tr>
                                                <td colSpan="6" className="text-center text-muted">
                                                    No items in inventory.
                                                </td>
                                            </tr>
                                        )}
                                        {items.map((item) => {
                                            const addition = additions[item.id] ?? ''
                                            const invalid = addition !== '' && !isValidUnits(addition)

                                            return (
                                                <tr key={item.id}>
                                                    <td>{item.name}</td>
                                                    <td>${Number(item.price).toFixed(2)}</td>
                                                    <td>{item.description || '—'}</td>
                                                    <td>{item.amount}</td>
                                                    <td>
                                                        <input
                                                            type="text"
                                                            inputMode="numeric"
                                                            className={`form-control${invalid ? ' is-invalid' : ''}`}
                                                            aria-label={`Quantity to add for ${item.name}`}
                                                            value={addition}
                                                            onChange={(event) => updateAddition(item.id, event.target.value)}
                                                        />
                                                        {invalid &&
                                                            <div className="invalid-feedback">
                                                                Enter a non-negative integer.
                                                            </div>}
                                                    </td>
                                                    <td>
                                                        <button
                                                            type="button"
                                                            className="btn btn-danger btn-sm"
                                                            onClick={() => deleteItem(item)}
                                                            aria-label={`Delete ${item.name}`}
                                                        >
                                                            Delete
                                                        </button>
                                                    </td>
                                                </tr>
                                            )
                                        })}
                                    </tbody>
                                </table>
                            </div>

                            <button
                                type="submit"
                                className="btn btn-dark w-100"
                                disabled={items.length === 0}
                            >
                                Submit
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default InventoryComponent
