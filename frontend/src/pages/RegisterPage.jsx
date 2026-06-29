import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { zodResolver } from '@hookform/resolvers/zod'
import useAuthStore from '../store/authStore'
import Button from '../components/common/Button'
import Input from '../components/common/Input'

const schema = z
  .object({
    name: z.string().min(2, 'Name required'),
    phone: z.string().regex(/^[0-9]{10}$/, 'Phone must be 10 digits'),
    email: z.string().email().optional().or(z.literal('')),
    password: z.string().min(6, 'Minimum 6 characters'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  })

export default function RegisterPage() {
  const navigate = useNavigate()
  const registerUser = useAuthStore((s) => s.register)
  const loading = useAuthStore((s) => s.loading)
  const clearError = useAuthStore((s) => s.clearError)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: '',
      phone: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  })

  const onSubmit = async (values) => {
    clearError()
    const { confirmPassword, ...payload } = values
    void confirmPassword
    const ok = await registerUser(payload)
    if (ok) {
      navigate('/dashboard')
      return
    }
    toast.error(useAuthStore.getState().error || 'Registration failed')
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
          <Input label="Name" placeholder="Enter name" register={register('name')} error={errors.name} />
          <Input label="Phone" placeholder="10 digit number" register={register('phone')} error={errors.phone} />
          <Input
            label="Email (optional)"
            placeholder="Enter email"
            register={register('email')}
            error={errors.email}
          />
          <Input
            label="Password"
            type="password"
            placeholder="Enter password"
            register={register('password')}
            error={errors.password}
          />
          <Input
            label="Confirm Password"
            type="password"
            placeholder="Re-enter password"
            register={register('confirmPassword')}
            error={errors.confirmPassword}
          />
          <Button type="submit" fullWidth loading={loading}>
            Create account
          </Button>
        </form>
        <p className="text-sm text-gray-400 text-center">
          Already have an account?{' '}
          <Link to="/login" className="text-primary-400 hover:underline font-semibold">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}

