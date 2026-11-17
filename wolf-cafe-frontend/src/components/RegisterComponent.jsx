import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { registerAPICall } from '../services/AuthService'

/** AI was used throughout the file to incorporate validation checks and make sure the account creation is handled */

const initialForm = {
    name: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
}

const fields = [
    { name: 'name', label: 'Name', type: 'text', autoComplete: 'name' },
    { name: 'username', label: 'Username', type: 'text', autoComplete: 'username' },
    { name: 'email', label: 'Email', type: 'email', autoComplete: 'email' },
    {
        name: 'password',
        label: 'Password',
        type: 'password',
        autoComplete: 'new-password'
    },
    {
        name: 'confirmPassword',
        label: 'Confirm Password',
        type: 'password',
        autoComplete: 'new-password'
    }
]

const RegisterComponent = () => {
    const [form, setForm] = useState(initialForm)
    const [errorMessage, setErrorMessage] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const navigate = useNavigate()

    function handleChange(event) {
        const { name, value } = event.target
        setForm(previous => ({ ...previous, [name]: value }))
        setErrorMessage('')
    }

    async function handleRegistrationForm(event) {
        event.preventDefault()
        setErrorMessage('')

        if (!form.name.trim() || !form.username.trim() || !form.email.trim() || !form.password.trim()) {
            setErrorMessage('Name, username, email, and password are required.')
            return
        }

        if (form.password !== form.confirmPassword) {
            setErrorMessage('Passwords do not match.')
            return
        }

        setSubmitting(true)

        try {
            await registerAPICall({
                name: form.name.trim(),
                username: form.username.trim(),
                email: form.email.trim(),
                password: form.password
            })

            navigate('/login', {
                replace: true,
                state: {
                    successMessage: 'Account created successfully. Please log in.'
                }
            })
        } catch (error) {
            const message = error.response?.data?.message
            setErrorMessage(
                typeof message === 'string'
                    ? message
                    : 'Registration failed. Please try again.'
            )
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <div className='container py-4'>
            <div className='row'>
                <div className='col-md-6 offset-md-3'>
                    <div className='card'>
                        <div className='card-header'>
                            <h2 className='text-center'>Create Account</h2>
                        </div>

                        <div className='card-body'>
                            {errorMessage && (
                                <div className='alert alert-danger' role='alert'>
                                    {errorMessage}
                                </div>
                            )}

                            <form onSubmit={handleRegistrationForm}>
                                {fields.map(field => (
                                    <div className='mb-3' key={field.name}>
                                        <label
                                            htmlFor={field.name}
                                            className='form-label'
                                        >
                                            {field.label}
                                        </label>
                                        <input
                                            id={field.name}
                                            name={field.name}
                                            type={field.type}
                                            autoComplete={field.autoComplete}
                                            className='form-control'
                                            value={form[field.name]}
                                            onChange={handleChange}
                                            disabled={submitting}
                                            required
                                        />
                                    </div>
                                ))}

                                <button
                                    type='submit'
                                    className='btn btn-primary'
                                    disabled={submitting}
                                >
                                    {submitting ? 'Creating account…' : 'Submit'}
                                </button>

                                <Link to='/login' className='btn btn-link'>
                                    Back to Login
                                </Link>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default RegisterComponent