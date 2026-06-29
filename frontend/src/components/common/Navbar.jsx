import { useNavigate } from 'react-router-dom'
import useAuthStore from '../../store/authStore'

export default function Navbar({ dark = false }) {
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)

  const onLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className={`fixed top-0 left-0 right-0 z-40 h-14 border-b transition-colors duration-200 ${
      dark 
        ? 'bg-[#0a0f1d]/80 border-white/10 text-white backdrop-blur-md' 
        : 'bg-white border-gray-200 shadow-sm text-gray-800'
    }`}>
      <div className="h-full max-w-6xl mx-auto px-4 flex items-center justify-between">
        <p className="font-black text-lg tracking-wider bg-gradient-to-r from-cyan-400 via-blue-500 to-indigo-400 bg-clip-text text-transparent uppercase">
          SplitSmart
        </p>
        <div className="flex items-center gap-3">
          <span className={`text-sm ${dark ? 'text-gray-300' : 'text-gray-700'}`}>{user?.name || 'User'}</span>
          <button
            type="button"
            onClick={onLogout}
            className={`text-sm px-3 py-1.5 rounded border transition cursor-pointer ${
              dark 
                ? 'border-white/15 hover:bg-white/10 text-white' 
                : 'border-gray-300 hover:bg-gray-50 text-gray-700'
            }`}
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  )
}


