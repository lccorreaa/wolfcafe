import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createStaffAPICall } from '../services/AuthService'

/**
 * Creates a staff account through the UC7 admin workflow.
 *
 * @author Lylan Bui (labui)
 */
const CreateStaffComponent = () => {
    const [name, setName] = useState('')
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [fieldErrors, setFieldErrors] = useState({})
    const navigate = useNavigate()

    const nameValid = name.trim() !== ''
    const usernameValid = username.trim().length > 3
    const passwordValid = password.length >= 8 && /\d/.test(password)
    const formValid = nameValid && usernameValid && passwordValid

    function clearError(field) {
        setError('')
        setFieldErrors((current) => ({ ...current, [field]: '' }))
    }

    async function submitStaff(event) {
        event.preventDefault()
        setError('')
        setFieldErrors({})
        if (!formValid) {
            setFieldErrors({
                name: nameValid ? '' : 'Name is required.',
                username: usernameValid ? '' : 'Username must be more than 3 characters.',
                password: passwordValid ? '' : 'Password must be at least 8 characters and contain a number.'
            })
            return
        }

        try {
            const response = await createStaffAPICall({
                name: name.trim(),
                username: username.trim(),
                password
            })
            navigate('/admin', {
                state: { success: `Staff account ${response.data.username} was created.` }
            })
        } catch (requestError) {
            console.error(requestError)
            const message = requestError.response?.data?.message ?? 'Unable to create the staff account.'
            if (message.toLowerCase().includes('username')) {
                setFieldErrors({ username: message })
            } else {
                setError(message)
            }
        }
    }

    return (
        <div className='container'>
            <br /><br />
            <div className='row'>
                <div className='col-md-8 offset-md-2 col-lg-6 offset-lg-3'>
                    <div className='card'>
                        <div className='card-header'>
                            <h2 className='text-center'>Create Staff</h2>
                        </div>
                        <div className='card-body'>
                            {error && <div className='alert alert-danger' role='alert'>{error}</div>}
                            <form onSubmit={submitStaff} noValidate>
                                <div className='mb-3'>
                                    <label htmlFor='staff-name' className='form-label'>Name</label>
                                    <input id='staff-name' className={`form-control${fieldErrors.name ? ' is-invalid' : ''}`}
                                        value={name} onChange={(event) => {
                                            setName(event.target.value)
                                            clearError('name')
                                        }} required />
                                    {fieldErrors.name && <div className='invalid-feedback'>{fieldErrors.name}</div>}
                                </div>
                                <div className='mb-3'>
                                    <label htmlFor='staff-username' className='form-label'>Username</label>
                                    <input id='staff-username' autoComplete='username'
                                        className={`form-control${fieldErrors.username || (username && !usernameValid) ? ' is-invalid' : ''}`}
                                        value={username} onChange={(event) => {
                                            setUsername(event.target.value)
                                            clearError('username')
                                        }} required />
                                    {(fieldErrors.username || (username && !usernameValid)) &&
                                        <div className='invalid-feedback'>
                                            {fieldErrors.username || 'Username must be more than 3 characters.'}
                                        </div>}
                                </div>
                                <div className='mb-3'>
                                    <label htmlFor='staff-password' className='form-label'>Password</label>
                                    <input id='staff-password' type='password' autoComplete='new-password'
                                        className={`form-control${fieldErrors.password || (password && !passwordValid) ? ' is-invalid' : ''}`}
                                        value={password} onChange={(event) => {
                                            setPassword(event.target.value)
                                            clearError('password')
                                        }} required />
                                    {(fieldErrors.password || (password && !passwordValid)) &&
                                        <div className='invalid-feedback'>
                                            {fieldErrors.password || 'Use at least 8 characters and include a number.'}
                                        </div>}
                                </div>
                                <div className='d-flex gap-2'>
                                    <button type='submit' className='btn btn-dark' disabled={!formValid}>
                                        Submit
                                    </button>
                                    <Link to='/admin' className='btn btn-outline-secondary'>Cancel</Link>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default CreateStaffComponent
