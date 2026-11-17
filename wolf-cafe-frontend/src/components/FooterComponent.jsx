import React from 'react'

const FooterComponent = () => {
  return (
    <footer className='footer mt-auto bg-dark text-white py-3'>
        <p className='text-center mb-0'>WolfCafe &copy; {new Date().getFullYear()}</p>
    </footer>
  )
}

export default FooterComponent