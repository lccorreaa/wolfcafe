import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { hasAnyRole } from '../services/AuthService'
import { getAllItems, deleteItemById } from '../services/ItemService'


const ListItemsComponent = () => {

	const [items, setItems] = useState([])
	const [errorMessage, setErrorMessage] = useState('')

	const navigate = useNavigate()

	// Show only the actions the backend allows for the logged in user's role.
	// TODO - keep these in step with the @PreAuthorize roles in ItemController
	const canAddItems = hasAnyRole('ROLE_STAFF')
	const canEditItems = hasAnyRole('ROLE_ADMIN')

	useEffect(() => {
	    listItems()
	}, [])

	function listItems() {
	    getAllItems().then((response) => {
	        setItems(response.data)
	    }).catch(error => {
	        console.error(error)
	        setErrorMessage('Could not load the items.')
	    })
	}

	function addNewItem() {
		navigate('/add-item')
	}

	function updateItem(id) {
		navigate(`/update-item/${id}`)
	}

	function deleteItem(id) {
		deleteItemById(id).then(() => {
			listItems()
		}).catch(error => {
			console.error(error)
			setErrorMessage(error.response?.data?.message ?? 'Could not delete the item.')
		})
	}


	return (
		<div className='container'>
			<br /> <br />
		    <h2 className='text-center'>Items</h2>
			{
				errorMessage &&
				<div className='alert alert-danger' role='alert'>{errorMessage}</div>
			}
			{
				canAddItems &&
				<button className='btn btn-primary mb-2' onClick={addNewItem}>Add Item</button>
			}
			<div>
				<table className='table table-bordered table-striped'>
					<thead>
						<tr>
							<th scope='col'>Item Name</th>
							<th scope='col'>Description</th>
							<th scope='col'>Price</th>
							{ canEditItems && <th scope='col'>Actions</th> }
						</tr>
					</thead>
					<tbody>
						{
							items.map((item) =>
								<tr key={item.id}>
									<td>{item.name}</td>
									<td>{item.description}</td>
									<td>{item.price.toFixed(2)}</td>
									{
										canEditItems &&
										<td>
											<button className='btn btn-info' onClick={() => updateItem(item.id)}
												aria-label={`Update ${item.name}`}>Update</button>
											<button className='btn btn-danger' onClick={() => deleteItem(item.id)}
												aria-label={`Delete ${item.name}`} style={{marginLeft: "10px"}}>Delete</button>
										</td>
									}
								</tr>
							)
						}
					</tbody>
				</table>
			</div>
		</div>
	)
}

export default ListItemsComponent
