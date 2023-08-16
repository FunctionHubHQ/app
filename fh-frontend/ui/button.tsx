import clsx from 'clsx';

export default function Button({
  kind = 'default',
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & {
  kind?: 'default' | 'error' | 'blue';
}) {
  return (
    <button
      className={clsx('rounded-lg px-3 py-1 text-sm font-medium', {
        'bg-gray-700 text-gray-100 hover:bg-gray-500 hover:text-white':
          kind === 'default',
        'bg-vercel-pink text-red-50 hover:bg-pink-600 hover:text-white':
          kind === 'error',
        'bg-vercel-blue text-white hover:bg-vercel-blue/90 hover:text-white':
            kind === 'blue',
      })}
      {...props}
    />
  );
}
"bg-vercel-blue hover:bg-vercel-blue/90 relative w-full items-center space-x-2 rounded-lg px-3  py-1 text-sm font-medium text-white disabled:text-white/70"
