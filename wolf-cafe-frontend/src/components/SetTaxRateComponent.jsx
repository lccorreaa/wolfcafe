import { useEffect, useState } from 'react'
import { getTaxRate, updateTaxRate } from '../services/TaxRateService'

/** Checks that the entered rate is a positive, finite number such as 8 or 8.25. */
function isValidRate(value) {
    const rate = Number(value)
    return /^[0-9]*\.?[0-9]+$/.test(value.trim()) && Number.isFinite(rate) && rate > 0
}

/**
 * Admin settings page (UC6): shows the current tax rate in an editable text box
 * and lets the admin save a new positive rate.  Reached from the header's
 * Settings tab or the Admin Menu's Set Tax Rate link.
 *
 * @author sajose
 */
const SetTaxRateComponent = () => {
    const [rate, setRate] = useState('')
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    useEffect(() => {
        getTaxRate().then((response) => {
            setRate(String(response.data.rate))
        }).catch((requestError) => {
            console.error(requestError)
            setError('Unable to load the tax rate.')
        })
    }, [])

    const invalidRate = rate !== '' && !isValidRate(rate)

    function clearMessages() {
        setError('')
        setSuccess('')
    }

    async function saveTaxRate(event) {
        event.preventDefault()
        clearMessages()

        if (!isValidRate(rate)) {
            setError('Tax rate must be a positive number.')
            return
        }

        try {
            const response = await updateTaxRate(Number(rate))
            setRate(String(response.data.rate))
            setSuccess('Tax rate saved.')
        } catch (requestError) {
            console.error(requestError)
            const serverMessage = requestError.response?.data?.message
            setError(serverMessage || 'Unable to save the tax rate.')
        }
    }

    return (
        <div className="container">
            <br /><br />
            <div className="row">
                <div className="col-md-6 offset-md-3">
                    <h4 className="text-start mb-3">Admin Settings</h4>
                    <div className="card p-0">
                        <div className="card-body py-3">
                            <h5 className="card-title text-start mb-2">Tax Rate</h5>

                            {success && <div className="alert alert-success mb-2" role="status">{success}</div>}

                            <form onSubmit={saveTaxRate} noValidate>
                                <div className="d-flex gap-2 align-items-start">
                                    <div className="flex-grow-1">
                                        <div className="input-group has-validation">
                                            <input
                                                type="text"
                                                inputMode="decimal"
                                                className={'form-control' + (invalidRate || error ? ' is-invalid' : '')}
                                                aria-label="Tax rate percent"
                                                value={rate}
                                                onChange={(event) => {
                                                    setRate(event.target.value)
                                                    clearMessages()
                                                }}
                                            />
                                            <span className="input-group-text">%</span>
                                            <div className="invalid-feedback" role="alert">
                                                {error || 'Tax rate must be a positive number.'}
                                            </div>
                                        </div>
                                    </div>
                                    <button type="submit" className="btn btn-dark text-nowrap">
                                        Save
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default SetTaxRateComponent
