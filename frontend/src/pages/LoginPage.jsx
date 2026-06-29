import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { zodResolver } from '@hookform/resolvers/zod'
import useAuthStore from '../store/authStore'
import Button from '../components/common/Button'
import Input from '../components/common/Input'

const schema = z.object({
  phoneOrEmail: z.string().min(1, 'Phone or email is required'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
})

export default function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)
  const loading = useAuthStore((s) => s.loading)
  const clearError = useAuthStore((s) => s.clearError)
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { phoneOrEmail: '', password: '' },
  })

  const onSubmit = async (values) => {
    clearError()
    const ok = await login(values.phoneOrEmail, values.password)
    if (ok) {
      navigate('/dashboard')
      return
    }
    toast.error(useAuthStore.getState().error || 'Login failed')
  }

  return (
    <div className="min-h-screen bg-[#0a0f1d] flex items-center justify-center p-4 relative overflow-hidden">
      {/* Background glow effects */}
      <div className="absolute top-1/4 left-1/4 w-72 h-72 bg-primary-600/10 rounded-full blur-3xl -z-10 pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-72 h-72 bg-purple-600/10 rounded-full blur-3xl -z-10 pointer-events-none" />

      <div className="w-full max-w-md glass-card p-8 space-y-6 relative z-10">
        <div className="text-center space-y-2">
          <h1 className="text-heading text-primary-400">SplitSmart</h1>
          <p className="text-body font-medium">Smart travel expense splitting</p>
        </div>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <Input
            label="Phone or Email"
            placeholder="Enter phone or email"
            register={register('phoneOrEmail')}
            error={errors.phoneOrEmail}
          />
          <Input
            label="Password"
            type="password"
            placeholder="Enter password"
            register={register('password')}
            error={errors.password}
          />
          <Button type="submit" fullWidth loading={loading}>
            Sign in
          </Button>
        </form>
        <p className="text-sm text-gray-400 text-center">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary-400 hover:underline font-semibold">
            Register
          </Link>
        </p>
      </div>
    </div>
  )
}

