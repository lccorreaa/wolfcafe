import React from 'react'
import { Link, useLocation } from 'react-router-dom'

/** Admin landing page (UC4) with links to the admin features: UC5, UC7, and UC6. */
const AdminMenuComponent = () => {
  const location = useLocation()

  return (
    <div className='container'>
        <br /><br />
        <div className='row'>
            <div className='col-md-6 offset-md-3'>
                <div className='card'>
                    <div className='card-header'>
                        <h2 className='text-center'>Admin Menu</h2>
                    </div>
                    <div className='card-body'>
                        {location.state?.success &&
                            <div className='alert alert-success' role='status'>
                                {location.state.success}
                            </div>}
                        <div className='list-group'>
                            <Link to='/edit-users' className='list-group-item list-group-item-action'>Edit Users</Link>
                            <Link to='/create-staff' className='list-group-item list-group-item-action'>Create Staff</Link>
                            <Link to='/set-tax-rate' className='list-group-item list-group-item-action'>Set Tax Rate</Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
  )
}

export default AdminMenuComponent
