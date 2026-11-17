import './App.css'
import {BrowserRouter, Routes, Route, Navigate, Outlet} from 'react-router-dom'
import HeaderComponent from './components/HeaderComponent'
import FooterComponent from './components/FooterComponent'
import ListItemsComponent from './components/ListItemsComponent'
import ItemComponent from './components/ItemComponent'
import RegisterComponent from './components/RegisterComponent'
import LoginComponent from './components/LoginComponent'
import ListOrdersComponent from './components/ListOrdersComponent.jsx'
import RecipeFormComponent from './components/RecipeFormComponent'
import EditRecipeWSComponent from './components/EditRecipeWSComponent'
import InventoryComponent from './components/InventoryComponent'
import AddIngredientComponent from './components/AddIngredientComponent'
import MakeRecipeComponent from './components/MakeRecipeComponent'
import AdminMenuComponent from './components/AdminMenuComponent'
import SetTaxRateComponent from './components/SetTaxRateComponent'
import EditUsersComponent from './components/EditUsersComponent'
import CreateStaffComponent from './components/CreateStaffComponent'
import { isUserLoggedIn, isAdminUser, hasAnyRole } from './services/AuthService'

/**
 * Shows the nested routes only to a logged in user, and sends anyone else to the
 * login page.  <Outlet /> is where React Router draws the matching nested route.
 */
function AuthenticatedRoute() {
  if (isUserLoggedIn()) {
    return <Outlet />
  }
  return <Navigate to='/' />
}

/**
 * Shows the nested routes only to the admin.  A logged in user who is not the admin
 * is sent to the items page, and anyone else to the login page.  This only hides
 * pages; the backend still checks the role on every request.
 */
function AdminRoute() {
  if (isAdminUser()) {
    return <Outlet />
  }
  if (isUserLoggedIn()) {
    return <Navigate to='/items' />
  }
  return <Navigate to='/' />
}

/** Shows staff-owned pages only to staff users. Added by Copilot to make Staff Use Cases work */
function StaffRoute() {
  if (hasAnyRole('ROLE_STAFF')) {
    return <Outlet />
  }
  if (isUserLoggedIn()) {
    return <Navigate to='/items' />
  }
  return <Navigate to='/' />
}

function App() {
	// Used AI to link these components into the wolf cafe frontend.
  return (
    <div className='app-layout'>
      <BrowserRouter>
	  <HeaderComponent />
	  <Routes>
	  	<Route path='/' element={<LoginComponent />}></Route>
		<Route path='/register' element={<RegisterComponent />}></Route>
		<Route path='/login' element={<LoginComponent />}></Route>
		{/* Routes nested here require a logged in user */}
		<Route element={<AuthenticatedRoute />}>
			<Route path='/items' element={<ListItemsComponent />}></Route>
			<Route path='/update-item/:id' element={<ItemComponent />}></Route>
			<Route path='/orders' element={<ListOrdersComponent />}></Route>
			<Route path='/add-recipe' element={<RecipeFormComponent />}></Route>
			<Route path='/edit-recipe/:incomingName' element={<RecipeFormComponent />}></Route>
			<Route path='/edit-recipe-ws/:incomingName' element={<EditRecipeWSComponent />}></Route>
			<Route path='/inventory' element={<InventoryComponent />}></Route>
			<Route path='/add-ingredient' element={<AddIngredientComponent />}></Route>
			<Route path='/make-recipe' element={<MakeRecipeComponent />}></Route>
		</Route>
		{/* Routes nested here require the admin */}
		<Route element={<AdminRoute />}>
			<Route path='/admin' element={<AdminMenuComponent />}></Route>
			<Route path='/set-tax-rate' element={<SetTaxRateComponent />}></Route>
			<Route path='/edit-users' element={<EditUsersComponent />}></Route>
			<Route path='/edit-users/:id' element={<EditUsersComponent />}></Route>
			<Route path='/create-staff' element={<CreateStaffComponent />}></Route>
		</Route>
		{/* UC10 create-item flow is owned by Staff */}
		<Route element={<StaffRoute />}>
			<Route path='/add-item' element={<ItemComponent />}></Route>
		</Route>
	  </Routes>
	  <FooterComponent />
	  </BrowserRouter>
    </div>
  )
}

export default App
