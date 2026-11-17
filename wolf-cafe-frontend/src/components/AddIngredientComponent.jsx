import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import { createIngredient } from '../services/IngredientService'

/** Form to add an ingredient and its initial amount to inventory. */
const AddIngredientComponent = () => {
    const [name, setName] = useState('')
    const [amount, setAmount] = useState('')
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    async function addIngredient(event) {
        event.preventDefault()
        setError('')
        setSuccess('')

        const units = Number(amount)
        if (!name.trim()) {
            setError('Ingredient name is required.')
            return
        }
        if (!/^[0-9]+$/.test(amount) || !Number.isSafeInteger(units) || units <= 0 || units > 2147483647) {
            setError('Units must be a positive integer.')
            return
        }

        try {
            await createIngredient({ name: name.trim(), amount: units })
            setName('')
            setAmount('')
            setSuccess('Ingredient added.')
        } catch (requestError) {
            console.error(requestError)
            const serverMessage = requestError.response?.data?.message
            setError(serverMessage || (requestError.response?.status === 400
                ? 'An ingredient with this name already exists.'
                : 'Unable to add ingredient. Please try again.'))
        }
    }

    return (
        <div className="container">
            <br /><br />
            <div className="row">
                <div className="card col-md-4 offset-md-4">
                    <h2 className="text-center mt-3">Inventory</h2>
                    <div className="card-body">
                        <ul className="nav nav-tabs">
                            <li className="nav-item me-1">
                                <NavLink className="nav-link border text-body bg-body-secondary" to="/inventory">
                                    Update
                                </NavLink>
                            </li>
                            <li className="nav-item">
                                <NavLink className="nav-link active border text-body" to="/add-ingredient">
                                    Add
                                </NavLink>
                            </li>
                        </ul>

                        <div className="border border-top-0 rounded-bottom p-3">
                            {error && <div className="alert alert-danger mb-2" role="alert">{error}</div>}
                            {success && <div className="alert alert-success mb-2" role="status">{success}</div>}

                            <form onSubmit={addIngredient}>
                                <div className="mb-2">
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder="Name"
                                        aria-label="Ingredient name"
                                        value={name}
                                        onChange={(event) => {
                                            setName(event.target.value)
                                            setError('')
                                            setSuccess('')
                                        }}
                                        required
                                    />
                                </div>
                                <div className="input-group mb-2">
                                    <input
                                        type="number"
                                        className="form-control"
                                        placeholder="Quantity"
                                        aria-label="Initial quantity"
                                        min="1"
                                        max="2147483647"
                                        step="1"
                                        value={amount}
                                        onChange={(event) => {
                                            setAmount(event.target.value)
                                            setError('')
                                            setSuccess('')
                                        }}
                                        required
                                    />
                                    <span className="input-group-text">units</span>
                                </div>
                                <button type="submit" className="btn btn-dark w-100">
                                    Add
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default AddIngredientComponent
