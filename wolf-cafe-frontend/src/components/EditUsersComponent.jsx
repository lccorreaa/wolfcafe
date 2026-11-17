import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { deleteUserAPICall, getUsersAPICall, updateUserAPICall } from '../services/AuthService'

/**
 * Lists staff and customer accounts and supports editing or deleting them for UC5.
 *
 * @author Lylan Bui (labui)
 */
const EditUsersComponent = () => {
    const [users, setUsers] = useState([])
    const [form, setForm] = useState({ name: '', email: '', phone: '' })
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const { id } = useParams()
    const location = useLocation()
    const navigate = useNavigate()

    useEffect(() => {
        let active = true
        getUsersAPICall().then((response) => {
            if (!active) return
            const userList = response.data ?? []
            setUsers(userList)
            if (id) {
                const user = userList.find((candidate) => String(candidate.id) === id)
                if (!user) {
                    setError('That user could not be found. The user may have been deleted.')
                } else {
                    setForm({
                        name: user.name ?? '',
                        email: user.email ?? '',
                        phone: user.phone ?? ''
                    })
                }
            }
        }).catch((requestError) => {
            console.error(requestError)
            if (active) setError(requestError.response?.data?.message ?? 'Unable to load users.')
        }).finally(() => {
            if (active) setLoading(false)
        })
        return () => { active = false }
    }, [id])

    useEffect(() => {
        if (location.state?.success) setSuccess(location.state.success)
        if (location.state?.error) setError(location.state.error)
    }, [location.state])

    function updateField(event) {
        setForm({ ...form, [event.target.name]: event.target.value })
        setError('')
        setSuccess('')
    }

    const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())
    const phoneValid = /^\d{3}-\d{3}-\d{4}$/.test(form.phone.trim())
    const formValid = form.name.trim() !== '' && emailValid && phoneValid

    async function saveUser(event) {
        event.preventDefault()
        setError('')
        setSuccess('')
        if (!formValid) {
            setError('Enter a name, valid email, and phone in 000-000-0000 format.')
            return
        }

        try {
            await updateUserAPICall(id, {
                name: form.name.trim(),
                email: form.email.trim(),
                phone: form.phone.trim()
            })
            navigate('/edit-users', { state: { success: 'User information saved.' } })
        } catch (requestError) {
            console.error(requestError)
            if (requestError.response?.status === 404) {
                navigate('/edit-users', {
                    state: { error: 'That user could not be found. The user may have been deleted.' }
                })
            } else {
                setError(requestError.response?.data?.message ?? 'Unable to save user information.')
            }
        }
    }

    async function deleteUser(user) {
        if (!window.confirm(`Delete ${user.name} (${user.username})?`)) return
        setError('')
        setSuccess('')
        try {
            await deleteUserAPICall(user.id)
            setUsers((currentUsers) => currentUsers.filter((existing) => existing.id !== user.id))
            setSuccess(`${user.name} was deleted.`)
        } catch (requestError) {
            console.error(requestError)
            setError(requestError.response?.data?.message ?? 'Unable to delete this user.')
        }
    }

    function renderUserTable(role, title) {
        const roleUsers = users.filter((user) => user.role === role)
        return (
            <section className='mb-4' aria-labelledby={`${role}-heading`}>
                <h3 id={`${role}-heading`} className='h5'>{title}</h3>
                <div className='table-responsive'>
                    <table className='table table-bordered table-striped align-middle'>
                        <thead className='table-light'>
                            <tr>
                                <th scope='col'>Name</th>
                                <th scope='col'>Username</th>
                                <th scope='col'>Email</th>
                                <th scope='col'>Phone</th>
                                <th scope='col'>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {roleUsers.length === 0 && (
                                <tr><td colSpan='5' className='text-center text-muted'>
                                    No {title.toLowerCase()} accounts.
                                </td></tr>
                            )}
                            {roleUsers.map((user) => (
                                <tr key={user.id}>
                                    <td>{user.name}</td>
                                    <td>{user.username}</td>
                                    <td>{user.email || '—'}</td>
                                    <td>{user.phone || '—'}</td>
                                    <td className='text-nowrap'>
                                        <Link className='btn btn-dark btn-sm me-2'
                                            to={`/edit-users/${user.id}`}>
                                            Edit
                                        </Link>
                                        <button type='button' className='btn btn-danger btn-sm'
                                            onClick={() => deleteUser(user)}
                                            aria-label={`Delete ${user.name}`}>
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>
        )
    }

    if (id) {
        const user = users.find((candidate) => String(candidate.id) === id)
        return (
            <div className='container'>
                <br /><br />
                <div className='row'>
                    <div className='col-md-8 offset-md-2 col-lg-6 offset-lg-3'>
                        <div className='card'>
                            <div className='card-header'>
                                <h2 className='text-center'>Edit User</h2>
                            </div>
                            <div className='card-body'>
                                {error && <div className='alert alert-danger' role='alert'>{error}</div>}
                                {loading && <p role='status'>Loading user...</p>}
                                {!loading && user && (
                                    <>
                                        <p className='text-muted'>Username: {user.username} · Role: {
                                            user.role === 'ROLE_STAFF' ? 'Staff' : 'Customer'
                                        }</p>
                                        <form onSubmit={saveUser} noValidate>
                                            <div className='mb-3'>
                                                <label htmlFor='name' className='form-label'>Name</label>
                                                <input id='name' name='name' className='form-control'
                                                    value={form.name} onChange={updateField} required />
                                            </div>
                                            <div className='mb-3'>
                                                <label htmlFor='email' className='form-label'>Email</label>
                                                <input id='email' name='email' type='email'
                                                    className={`form-control${form.email && !emailValid ? ' is-invalid' : ''}`}
                                                    value={form.email} onChange={updateField} required />
                                                {form.email && !emailValid &&
                                                    <div className='invalid-feedback'>Enter a valid email address.</div>}
                                            </div>
                                            <div className='mb-3'>
                                                <label htmlFor='phone' className='form-label'>Phone</label>
                                                <input id='phone' name='phone' type='tel'
                                                    placeholder='828-888-8282'
                                                    className={`form-control${form.phone && !phoneValid ? ' is-invalid' : ''}`}
                                                    value={form.phone} onChange={updateField} required />
                                                {form.phone && !phoneValid &&
                                                    <div className='invalid-feedback'>Use the format 000-000-0000.</div>}
                                            </div>
                                            <div className='d-flex gap-2'>
                                                <button type='submit' className='btn btn-dark'
                                                    disabled={!formValid}>Submit</button>
                                                <Link to='/edit-users' className='btn btn-outline-secondary'>Cancel</Link>
                                            </div>
                                        </form>
                                    </>
                                )}
                                {!loading && !user &&
                                    <Link to='/edit-users' className='btn btn-dark'>Back to users</Link>}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }

    return (
        <div className='container'>
            <br /><br />
            <div className='row'>
                <div className='col-lg-10 offset-lg-1'>
                    <div className='card'>
                        <div className='card-header'>
                            <h2 className='text-center'>User &amp; Staff Management</h2>
                        </div>
                        <div className='card-body'>
                            {error && <div className='alert alert-danger' role='alert'>{error}</div>}
                            {success && <div className='alert alert-success' role='status'>{success}</div>}
                            {loading && <p role='status'>Loading users...</p>}
                            {!loading && !error && (
                                <>
                                    {renderUserTable('ROLE_CUSTOMER', 'Customers')}
                                    {renderUserTable('ROLE_STAFF', 'Staff')}
                                </>
                            )}
                            <Link to='/admin' className='btn btn-outline-secondary'>Back to Admin Menu</Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default EditUsersComponent
