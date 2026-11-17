import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createRecipe, getRecipe, updateRecipe } from '../services/RecipesService'
import { getInventory } from '../services/InventoryService'

const EMPTY_ROW = { name: "", amount: "" }

/** True for whole numbers of 1 or more. */
function isPositiveInt(value) {
    return /^[1-9]\d*$/.test(String(value).trim())
}

/** Quantity box has text in it, but not a valid quantity. */
function quantityInvalid(row) {
    return row.amount !== "" && !isPositiveInt(row.amount)
}

function rowComplete(row) {
    return row.name !== "" && isPositiveInt(row.amount)
}

/** Shared form for UC2 (Add Recipe, /add-recipe) and UC4 (Edit Recipe, /edit-recipe/:incomingName). */
const RecipeFormComponent = () => {

    const { incomingName } = useParams()
    const isEdit = Boolean(incomingName)
    const navigator = useNavigate()

    const [recipeId, setRecipeId] = useState(null)
    const [name, setName] = useState("")
    const [price, setPrice] = useState("")
    const [rows, setRows] = useState([{ ...EMPTY_ROW }])
    const [options, setOptions] = useState([])
    const [generalError, setGeneralError] = useState("")

    // Fill the ingredient dropdown from the inventory
    useEffect(() => {
        getInventory().then((response) => {
            const data = response.data
            const list = Array.isArray(data) ? data : (data.ingredients ?? [])
            setOptions(list.map(ingredient => ingredient.name))
        }).catch(error => {
            console.error(error)
            setGeneralError("Could not load the ingredient list.")
        })
    }, [])

    // Edit mode: load the recipe by name, keep its id for the PUT
    useEffect(() => {
        if (!incomingName) return
        getRecipe(incomingName).then((response) => {
            const recipe = response.data
            setRecipeId(recipe.id)
            setName(recipe.name)
            setPrice(String(recipe.price))
            const loaded = (recipe.ingredients ?? []).map(i => ({ name: i.name, amount: String(i.amount) }))
            setRows(loaded.length > 0 ? loaded : [{ ...EMPTY_ROW }])
        }).catch(error => {
            console.error(error)
            setGeneralError("Could not load that recipe.")
        })
    }, [incomingName])

    const formValid =
        name.trim() !== "" &&
        isPositiveInt(price) &&
        rows.length > 0 &&
        rows.every(rowComplete) &&
        (!isEdit || recipeId !== null)

    // Every ingredient name we know about, including ones already in this recipe
    const allOptions = Array.from(new Set([...options, ...rows.map(r => r.name).filter(Boolean)]))

    function optionsForRow(row) {
        // Hide ingredients already picked in other rows
        return allOptions.filter(option => option === row.name || !rows.some(r => r.name === option))
    }

    function updateRow(index, field, value) {
        setRows(rows.map((row, i) => i === index ? { ...row, [field]: value } : row))
    }

    function addRow() {
        setRows([...rows, { ...EMPTY_ROW }])
    }

    function removeRow(index) {
        setRows(rows.filter((_, i) => i !== index))
    }

    function saveRecipe(e) {
        e.preventDefault()
        if (!formValid) return
        setGeneralError("")

        const recipe = {
            name: name.trim(),
            price: parseInt(price, 10),
            ingredients: rows.map(row => ({ name: row.name, amount: parseInt(row.amount, 10) }))
        }

        const request = isEdit ? updateRecipe(recipeId, recipe) : createRecipe(recipe)

        request.then(() => {
            navigator('/recipes')
        }).catch(error => {
            console.error(error)
            const status = error.response?.status
            if (!error.response) {
                setGeneralError("Could not reach the server.")
            } else if (status === 409) {
                setGeneralError("A recipe with that name already exists.")
            } else if (status === 404) {
                setGeneralError("That recipe no longer exists.")
            } else if (status === 400) {
                setGeneralError(isEdit
                    ? "The server rejected these values."
                    : "The server rejected this recipe. The system holds at most 3 recipes, and all values must be valid.")
            } else {
                setGeneralError("Something went wrong saving the recipe.")
            }
        })
    }

    return (
        <div className="container">
            <br /><br />
            <div className="row">
                <div className="card col-md-6 offset-md-3">
                    <h2 className="text-center">{isEdit ? "Edit Recipe" : "Add Recipe"}</h2>

                    <div className="card-body">
                        {generalError && <div className="p-3 mb-2 bg-danger text-white">{generalError}</div>}

                        <form>
                            <div className="form-group mb-2">
                                <label className="form-label">Recipe Name</label>
                                <input
                                    type="text"
                                    placeholder="Enter Recipe Name"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    className="form-control"
                                />
                            </div>

                            <div className="form-group mb-3">
                                <label className="form-label">Recipe Price</label>
                                <input
                                    type="text"
                                    placeholder="Enter Recipe Price"
                                    value={price}
                                    onChange={(e) => setPrice(e.target.value)}
                                    className={`form-control ${price !== "" && !isPositiveInt(price) ? "is-invalid" : ""}`}
                                />
                                {price !== "" && !isPositiveInt(price) && <div className="invalid-feedback">Invalid Price</div>}
                            </div>

                            {rows.map((row, index) => (
                                <div className="form-group mb-3" key={index}>
                                    <select
                                        className="form-select mb-1"
                                        value={row.name}
                                        onChange={(e) => updateRow(index, "name", e.target.value)}
                                    >
                                        <option value="">Ingredient</option>
                                        {optionsForRow(row).map(option => (
                                            <option key={option} value={option}>{option}</option>
                                        ))}
                                    </select>
                                    <input
                                        type="text"
                                        placeholder="Quantity"
                                        value={row.amount}
                                        onChange={(e) => updateRow(index, "amount", e.target.value)}
                                        className={`form-control ${quantityInvalid(row) ? "is-invalid" : ""}`}
                                    />
                                    {quantityInvalid(row) && <div className="invalid-feedback">Invalid Units</div>}
                                    {rows.length > 1 && (
                                        <button type="button" className="btn btn-link text-danger p-0" onClick={() => removeRow(index)}>
                                            Remove
                                        </button>
                                    )}
                                </div>
                            ))}

                            <div className="text-center mb-3">
                                <button type="button" className="btn btn-link" onClick={addRow}>+ Add Ingredient</button>
                            </div>

                            <button className="btn btn-dark w-100" disabled={!formValid} onClick={saveRecipe}>Submit</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default RecipeFormComponent