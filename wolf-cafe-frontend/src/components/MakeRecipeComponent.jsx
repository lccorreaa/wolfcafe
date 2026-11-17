import { useEffect, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { listRecipes } from '../services/RecipesService'
import { makeRecipe } from '../services/MakeRecipeService'

/** Largest payment the backend can read as an Integer. */
const MAX_PAYMENT = 2147483647

/** True for a non-negative whole number the backend will accept as a payment. */
function isValidPayment(text) {
    return /^[0-9]+$/.test(text) && Number(text) <= MAX_PAYMENT
}

/** True if the text is a number at all, so there is money to hand back. */
function isNumeric(text) {
    return text !== '' && Number.isFinite(Number(text))
}

/** UC6 Order Beverage: pick a beverage, pay for it, and receive change. */
const MakeRecipeComponent = () => {
    const [recipes, setRecipes] = useState([])
    const [recipeName, setRecipeName] = useState('')
    const [payment, setPayment] = useState('')
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [ordering, setOrdering] = useState(false)

    useEffect(() => {
        listRecipes().then((response) => {
            setRecipes(response.data)
        }).catch((requestError) => {
            console.error(requestError)
            setError('Unable to load beverages. Please try again.')
        })
    }, [])

    const canOrder = recipeName !== '' && !ordering

    async function orderBeverage(event) {
        event.preventDefault()
        setError('')
        setSuccess('')

        if (!canOrder) {
            return
        }

        const paid = payment.trim()

        // [Invalid Payment]
        if (!isValidPayment(paid)) {
            setError(isNumeric(paid)
                ? `Payment must be a whole number of 0 or more. Your payment of ${paid} has been returned.`
                : 'Payment must be a whole number.')
            return
        }

        setOrdering(true)
        try {
            const response = await makeRecipe(recipeName, Number(paid))
            setPayment('')
            setSuccess(`Your ${recipeName} is ready. Your change is ${response.data}.`)
        } catch (requestError) {
            console.error(requestError)
            const status = requestError.response?.status
            const returned = `Your payment of ${paid} has been returned.`
            if (status === 409) {
                // [Insufficient Payment]
                const price = recipes.find((recipe) => recipe.name === recipeName)?.price
                setError(`Insufficient payment: ${recipeName} costs ${price}. ${returned}`)
            } else if (status === 400 && Number.isInteger(requestError.response.data)) {
                // [Insufficient Ingredients] - the backend sends the payment back
                setError(`Not enough ingredients in inventory to make ${recipeName}. ${returned}`)
            } else if (status === 400) {
                setError(`Payment must be a whole number of 0 or more. ${returned}`)
            } else if (status === 404) {
                setError(`${recipeName} is no longer available. ${returned}`)
            } else {
                setError(`Unable to order the beverage. ${returned}`)
            }
        } finally {
            setOrdering(false)
        }
    }

    return (
        <div className="container">
            <br /><br />
            <div className="row">
                <div className="card col-md-4 offset-md-4">
                    <h2 className="text-center mt-3">Order Beverage</h2>
                    <div className="card-body">
                        {error && <div className="alert alert-danger mb-2" role="alert">{error}</div>}
                        {success && <div className="alert alert-success mb-2" role="status">{success}</div>}

                        <form onSubmit={orderBeverage}>
                            <select
                                className="form-select mb-2"
                                aria-label="Beverage"
                                value={recipeName}
                                onChange={(event) => {
                                    setRecipeName(event.target.value)
                                    setError('')
                                    setSuccess('')
                                }}
                            >
                                <option value="" disabled>
                                    {recipes.length === 0 ? 'No beverages available' : 'Beverage'}
                                </option>
                                {recipes.map((recipe) => (
                                    <option key={recipe.id} value={recipe.name}>
                                        {recipe.name} - {recipe.price}
                                    </option>
                                ))}
                            </select>

                            <input
                                type="text"
                                inputMode="numeric"
                                className="form-control mb-2"
                                placeholder="Amount Paid"
                                aria-label="Amount paid"
                                value={payment}
                                onChange={(event) => {
                                    setPayment(event.target.value)
                                    setError('')
                                    setSuccess('')
                                }}
                            />

                            <button
                                type="submit"
                                className={'btn w-100 ' + (canOrder ? 'btn-dark' : 'btn-secondary')}
                                disabled={!canOrder}
                            >
                                Order
                            </button>
                        </form>

                        <div className="text-center mt-3">
                            <NavLink to="/">Return to Main Menu</NavLink>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default MakeRecipeComponent
