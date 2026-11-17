import React, { useState } from 'react'
import { loginAPICall, saveLoggedInUser, storeToken, isAdminUser } from '../services/AuthService'
import { Link, useLocation, useNavigate } from 'react-router-dom'

/** AI was used for the link and location additions */

const LoginComponent = () => {

    const [usernameOrEmail, setUsernameOrEmail] = useState('')
    const [password, setPassword] = useState('')
    const [errorMessage, setErrorMessage] = useState('')

    const navigator = useNavigate()
    const location = useLocation()
    const successMessage = location.state?.successMessage

    function handleLoginForm(e) {
        e.preventDefault()

        loginAPICall(usernameOrEmail, password).then((response) => {
            const token = 'Bearer ' + response.data.accessToken
            const role = response.data.role

            storeToken(token)
            saveLoggedInUser(usernameOrEmail, role)

            // The admin starts at the Admin Menu (UC4); everyone else at the items page
            navigator(isAdminUser() ? '/admin' : '/items')

            // Reload so the header picks up the logged-in user
            window.location.reload(false)
        }).catch(error => {
            console.error(error)
            setErrorMessage('Login failed. Check your username or email and password.')
        })
    }


  return (
    <div className='container'>
        <br /><br />
        <div className='row'>
            <div className='col-md-6 offset-md-3 offset-md-3'>
                <div className='card'>
                    <div className='card-header'>
                        <h2 className='text-center'>Login Form</h2>
                    </div>
                    <div className='card-body'>
                        {successMessage && (
                            <div className='alert alert-success' role='status'>
                                {successMessage}
                            </div>
                        )}

                        {
                            errorMessage &&
                            <div className='alert alert-danger' role='alert'>{errorMessage}</div>
                        }
                        <form onSubmit={handleLoginForm}>
                            <div className='row mb-3'>
                                <label htmlFor='usernameOrEmail' className='col-md-3 control-label'>Username</label>
                                <div className='col-md-9'>
                                    <input
                                        type='text'
                                        id='usernameOrEmail'
                                        name='usernameOrEmail'
                                        className='form-control'
                                        placeholder='Enter username or email'
                                        autoComplete='username'
                                        value={usernameOrEmail}
                                        onChange={(e) => setUsernameOrEmail(e.target.value)}
                                    >
                                    </input>
                                </div>
                            </div>

                            <div className='row mb-3'>
                                <label htmlFor='password' className='col-md-3 control-label'>Password</label>
                                <div className='col-md-9'>
                                    <input
                                        type='password'
                                        id='password'
                                        name='password'
                                        className='form-control'
                                        placeholder='Enter password'
                                        autoComplete='current-password'
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                    >
                                    </input>
                                </div>
                            </div>

                            <div className='form-group mb-3'>
                                <button type='submit' className='btn btn-primary'>Submit</button>
                            </div>
                        </form>
                        <p className='mt-3 mb-0'>
                            Need an account? <Link to='/register'>Create Account</Link>
                        </p>

                    </div>
                </div>
            </div>
        </div>
    </div>
  )
}

export default LoginComponent
