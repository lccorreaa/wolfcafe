import React from 'react'
import { NavLink } from 'react-router-dom'
import { useNavigate } from 'react-router-dom'
import { isUserLoggedIn, isAdminUser, logout } from '../services/AuthService'

const HeaderComponent = () => {

	const isAuth = isUserLoggedIn()
	const isAdmin = isAdminUser()

    const navigator = useNavigate()

	function handleLogout() {
	    logout()
	    navigator('/login')
	}

  return (
    <div>
        <header>
            <nav className='navbar navbar-expand-md navbar-dark bg-dark px-3'>
                <div>
                    <NavLink to='/' className='navbar-brand'>
                        {isAdmin ? 'WolfCafe Admin' : 'WolfCafe'}
                    </NavLink>
                </div>
                <div className='collapse navbar-collapse'>
					<ul className='navbar-nav'>
					{
						isAuth &&
						<li className='nav-item'>
							<NavLink to='/items' className='nav-link'>Items</NavLink>
						</li>
					}
					{
						isAuth &&
						<li className='nav-item'>
							<NavLink to='/orders' className='nav-link'>Orders</NavLink>
						</li>
					}
					{
						isAuth &&
						<li className='nav-item'>
							<NavLink to='/inventory' className='nav-link'>Inventory</NavLink>
						</li>
					}
					{
						isAuth &&
						<li className='nav-item'>
							<NavLink to='/add-ingredient' className='nav-link'>Add Ingredient</NavLink>
						</li>
					}
					{
						isAuth &&
						<li className='nav-item'>
							<NavLink to='/make-recipe' className='nav-link'>Make Recipe</NavLink>
						</li>
					}
					</ul>
				</div>
				<ul className='navbar-nav'>
                    {
                        !isAuth &&
                        <li className='nav-item'>
                            <NavLink to='/register' className='nav-link'>Register</NavLink>
                        </li>
                    }
                    {
                        !isAuth &&
                        <li className='nav-item'>
                            <NavLink to='/login' className='nav-link'>Login</NavLink>
                        </li>
                    }
                    {
                        isAdmin &&
                        <li className='nav-item'>
                            <NavLink to='/set-tax-rate' className='nav-link'>Settings</NavLink>
                        </li>
                    }
                    {
                        isAuth &&
                        <li className='nav-item d-flex align-items-center ms-2'>
                            <NavLink to='/login' className='btn btn-secondary btn-sm border-0' onClick={handleLogout}>Logout</NavLink>
                        </li>
                    }
                </ul>
            </nav>
        </header>
    </div>
  )
}

export default HeaderComponent
