import { useEffect, useState } from 'react'
import { getItemById, saveItem, updateItem } from '../services/ItemService'
import { useNavigate, useParams } from 'react-router-dom'

/**
 * This component is used to create and edit items.
 * Most of the code is the same as what was provided, but on 11/4/26 edits were
 * made to follow UC10 requirements. Github Copilot was used to help with these edits
 * scanning the wireframe for the item creation page and the UC10 requirements.
 * @returns {React.JSX.Element} The item component
 * @constructor
 */
const ItemComponent = () => {

    const [name, setName] = useState('')
    const [description, setDescription] = useState('')
    const [price, setPrice] = useState('')
    const [amount, setAmount] = useState('')
    const [errorMessage, setErrorMessage] = useState('')
    const { id } = useParams()

    const navigate = useNavigate()
    const isEdit = Boolean(id)

    useEffect(() => {
        if(id) {
            getItemById(id).then((response) => {
                setName(response.data.name)
                setDescription(response.data.description)
                setPrice(String(response.data.price))
                setAmount(String(response.data.amount))
            }).catch(error => {
                console.error(error)
                setErrorMessage('Could not load the item.')
            })
        }
    }, [id])

    function saveOrUpdateItem(e) {
        e.preventDefault()
        if (!formValid) return

        const item = {
            name: name.trim(),
            description: description.trim(),
            price: Number(price),
            amount: Number(amount)
        }

        const request = id ? updateItem(id, item) : saveItem(item)
        request.then(() => {
            navigate('/items')
        }).catch(error => {
            console.error(error)
            // The backend explains the problem, e.g. "Item name is required."
            setErrorMessage(error.response?.data?.message ?? 'Could not save the item.')
        })
    }

    const priceValid = price !== '' && Number.isFinite(Number(price)) && Number(price) >= 0
    const amountValid = amount !== '' && /^\d+$/.test(amount)
    const formValid = name.trim() !== '' && priceValid && amountValid

  return (
    <div className='container'>
        <br /> <br />
        <div className='row'>
            <div className='card col-md-6 offset-md-3'>
                <div className='card-header'>
                    <h2 className='text-center'>{isEdit ? 'Update Item' : 'Create Menu Item'}</h2>
                </div>

                <div className='card-body'>
                    {
                        errorMessage &&
                        <div className='alert alert-danger' role='alert'>{errorMessage}</div>
                    }
                    <form onSubmit={saveOrUpdateItem}>
                        <div className='form-group mb-2'>
                            <label htmlFor='name' className='form-label'>Item Name:</label>
                            <input
                                type='text'
                                id='name'
                                className='form-control'
                                placeholder='Enter item name'
                                name='name'
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                required
                            >
                            </input>
                        </div>

                        <div className='form-group mb-2'>
                            <label htmlFor='description' className='form-label'>Item Description:</label>
                            <textarea
                                id='description'
                                className='form-control'
                                placeholder='Enter item description (optional)'
                                name='description'
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                rows='3'
                            />
                        </div>

                        <div className='form-group mb-2'>
                            <label htmlFor='price' className='form-label'>Item Price:</label>
                            <input
                                type='text'
                                id='price'
                                inputMode='decimal'
                                className={`form-control ${price !== '' && !priceValid ? 'is-invalid' : ''}`}
                                placeholder='Enter item price'
                                name='price'
                                value={price}
                                onChange={(e) => setPrice(e.target.value)}
                                required
                            />
                            {price !== '' && !priceValid &&
                                <div className='invalid-feedback'>Price must be a non-negative number.</div>}
                        </div>

                        {!isEdit &&
                            <div className='form-group mb-3'>
                                <label htmlFor='amount' className='form-label'>Initial Quantity:</label>
                                <input
                                    type='text'
                                    id='amount'
                                    inputMode='numeric'
                                    className={`form-control ${amount !== '' && !amountValid ? 'is-invalid' : ''}`}
                                    placeholder='Enter initial quantity'
                                    name='amount'
                                    value={amount}
                                    onChange={(e) => setAmount(e.target.value)}
                                    required
                                />
                                {amount !== '' && !amountValid &&
                                    <div className='invalid-feedback'>Quantity must be a non-negative integer.</div>}
                            </div>
                        }

                        <button type='submit' className='btn btn-dark w-100' disabled={!formValid}>Submit</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
  )
}

export default ItemComponent
