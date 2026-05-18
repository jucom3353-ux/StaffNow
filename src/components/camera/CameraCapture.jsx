import { useRef, useState, useEffect } from 'react'
import { Camera, X, RotateCcw, Check, AlertCircle } from 'lucide-react'

export default function CameraCapture({ onCapture, onClose }) {
  const videoRef  = useRef(null)
  const canvasRef = useRef(null)
  const streamRef = useRef(null)

  const [phase, setPhase]           = useState('loading') // 'loading' | 'preview' | 'captured' | 'error'
  const [capturedUrl, setCapturedUrl] = useState(null)
  const [errorMsg, setErrorMsg]     = useState('')

  useEffect(() => {
    startCamera()
    return () => stopStream()
  }, [])

  async function startCamera() {
    setPhase('loading')
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' }, width: { ideal: 1280 }, height: { ideal: 960 } },
        audio: false,
      })
      streamRef.current = stream
      if (videoRef.current) {
        videoRef.current.srcObject = stream
        videoRef.current.onloadedmetadata = () => setPhase('preview')
      }
    } catch (err) {
      setErrorMsg(
        err.name === 'NotAllowedError'   ? '카메라 권한을 허용해주세요.' :
        err.name === 'NotFoundError'     ? '카메라를 찾을 수 없습니다.' :
        err.name === 'NotSupportedError' ? '이 브라우저는 카메라를 지원하지 않습니다.' :
                                           '카메라를 시작할 수 없습니다.'
      )
      setPhase('error')
    }
  }

  function stopStream() {
    streamRef.current?.getTracks().forEach(t => t.stop())
    streamRef.current = null
  }

  function capture() {
    const video  = videoRef.current
    const canvas = canvasRef.current
    if (!video || !canvas) return
    canvas.width  = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0)
    const url = canvas.toDataURL('image/jpeg', 0.85)
    stopStream()
    setCapturedUrl(url)
    setPhase('captured')
  }

  function retake() {
    setCapturedUrl(null)
    startCamera()
  }

  function handleClose() {
    stopStream()
    onClose()
  }

  return (
    <div className="fixed inset-0 z-[200] bg-black flex flex-col">
      {/* 헤더 */}
      <div className="flex items-center justify-between px-4 py-3 bg-black/80 shrink-0">
        <p className="text-white font-semibold text-sm">
          {phase === 'captured' ? '사진 확인' : '카메라'}
        </p>
        <button
          onClick={handleClose}
          className="w-8 h-8 flex items-center justify-center rounded-full bg-white/10 text-white"
        >
          <X size={16} />
        </button>
      </div>

      {/* 뷰파인더 */}
      <div className="flex-1 relative overflow-hidden bg-black">
        <video
          ref={videoRef}
          autoPlay
          playsInline
          muted
          className={`absolute inset-0 w-full h-full object-cover ${phase !== 'preview' ? 'hidden' : ''}`}
        />

        {phase === 'captured' && capturedUrl && (
          <img src={capturedUrl} alt="촬영된 사진" className="absolute inset-0 w-full h-full object-cover" />
        )}

        {phase === 'loading' && (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-3">
            <div className="w-10 h-10 border-4 border-white/20 border-t-white rounded-full animate-spin" />
            <p className="text-white/60 text-sm">카메라 시작 중...</p>
          </div>
        )}

        {phase === 'error' && (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-4 px-8 text-center">
            <div className="w-14 h-14 rounded-full bg-red-500/20 flex items-center justify-center">
              <AlertCircle size={28} className="text-red-400" />
            </div>
            <p className="text-white font-semibold">{errorMsg}</p>
            <p className="text-white/50 text-sm leading-relaxed">
              브라우저 설정에서 카메라 권한을 허용한 후 다시 시도해주세요.
            </p>
            <button
              onClick={startCamera}
              className="px-5 py-2.5 bg-white/10 rounded-xl text-white text-sm font-semibold"
            >
              다시 시도
            </button>
          </div>
        )}

        {/* 촬영 가이드 프레임 */}
        {phase === 'preview' && (
          <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
            <div className="border-2 border-white/40 rounded-2xl w-64 h-48" />
            <p className="text-white/60 text-xs mt-3">프레임 안에 현장을 담아주세요</p>
          </div>
        )}
      </div>

      {/* 하단 컨트롤 */}
      <div className="bg-black/80 shrink-0 px-6 py-8">
        {phase === 'preview' && (
          <div className="flex items-center justify-center">
            <button
              onClick={capture}
              className="w-16 h-16 rounded-full bg-white flex items-center justify-center shadow-lg active:scale-95 transition-transform"
            >
              <Camera size={24} className="text-black" />
            </button>
          </div>
        )}

        {phase === 'captured' && (
          <div className="flex items-center justify-center gap-12">
            <button onClick={retake} className="flex flex-col items-center gap-2">
              <div className="w-12 h-12 rounded-full bg-white/10 flex items-center justify-center">
                <RotateCcw size={20} className="text-white" />
              </div>
              <span className="text-white/70 text-xs">다시 찍기</span>
            </button>

            <button onClick={() => onCapture(capturedUrl)} className="flex flex-col items-center gap-2">
              <div className="w-16 h-16 rounded-full bg-orange flex items-center justify-center shadow-lg active:scale-95 transition-transform">
                <Check size={26} className="text-white" />
              </div>
              <span className="text-white text-xs font-semibold">사용하기</span>
            </button>
          </div>
        )}

        {phase === 'error' && (
          <div className="flex justify-center">
            <button
              onClick={handleClose}
              className="px-6 py-2.5 bg-white/10 rounded-xl text-white text-sm font-semibold"
            >
              닫기
            </button>
          </div>
        )}
      </div>

      <canvas ref={canvasRef} className="hidden" />
    </div>
  )
}
