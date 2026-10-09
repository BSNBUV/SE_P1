import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Login } from './Login'

describe('Login', () => {
  it('renders seeded demo credentials and submits the form', async () => {
    render(<Login onLogin={vi.fn()} />)

    expect(screen.getByText('Astra Command')).toBeInTheDocument()
    expect(screen.getByDisplayValue('operator')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: /login/i }))
  })
})
