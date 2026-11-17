import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import AdminMenuComponent from './AdminMenuComponent'

describe('AdminMenuComponent (UC4)', () => {
  it('shows links to Edit Users, Create Staff, and Set Tax Rate', () => {
    render(
      <MemoryRouter>
        <AdminMenuComponent />
      </MemoryRouter>
    )

    expect(screen.getByRole('link', { name: 'Edit Users' })).toHaveAttribute('href', '/edit-users')
    expect(screen.getByRole('link', { name: 'Create Staff' })).toHaveAttribute('href', '/create-staff')
    expect(screen.getByRole('link', { name: 'Set Tax Rate' })).toHaveAttribute('href', '/set-tax-rate')
  })
})
