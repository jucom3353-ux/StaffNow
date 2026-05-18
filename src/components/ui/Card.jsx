import clsx from 'clsx'

export default function Card({ children, header, footer, padding = true, className }) {
  return (
    <div
      className={clsx(
        'bg-white rounded-xl border border-offwhite-200',
        'shadow-[0_1px_4px_0_rgba(27,43,72,0.06)]',
        className
      )}
    >
      {header && (
        <>
          <div className="px-5 py-3.5">{header}</div>
          <div className="border-t border-offwhite-200" />
        </>
      )}
      <div className={clsx(padding && 'p-5')}>{children}</div>
      {footer && (
        <>
          <div className="border-t border-offwhite-200" />
          <div className="px-5 py-3">{footer}</div>
        </>
      )}
    </div>
  )
}
