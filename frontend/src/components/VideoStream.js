import React, { useEffect, useRef, useState } from "react";
import Hls from "hls.js";

function VideoStream({ cctvUrl, isAnalyzing }) {
  const videoRef = useRef();
  const hlsRef = useRef();
  const [isPlaying, setIsPlaying] = useState(false);
  const [error, setError] = useState(null);
  const [isFullscreen, setIsFullscreen] = useState(false);

  useEffect(() => {
    if (!cctvUrl) {
      setError(null);
      return;
    }

    const video = videoRef.current;
    
    // 기존 HLS 인스턴스 정리
    if (hlsRef.current) {
      hlsRef.current.destroy();
      hlsRef.current = null;
    }

    try {
      if (Hls.isSupported()) {
        const hls = new Hls({
          enableWorker: true,
          lowLatencyMode: true,
          backBufferLength: 90,
        });

        hls.loadSource(cctvUrl);
        hls.attachMedia(video);

        hls.on(Hls.Events.MANIFEST_PARSED, () => {
          video.play().then(() => {
            setIsPlaying(true);
            setError(null);
          }).catch((err) => {
            console.error("재생 실패:", err);
            setError("영상 재생에 실패했습니다.");
          });
        });

        hls.on(Hls.Events.ERROR, (event, data) => {
          console.error("HLS 오류:", data);
          if (data.fatal) {
            switch (data.type) {
              case Hls.ErrorTypes.NETWORK_ERROR:
                setError("네트워크 오류가 발생했습니다.");
                hls.startLoad();
                break;
              case Hls.ErrorTypes.MEDIA_ERROR:
                setError("미디어 오류가 발생했습니다.");
                hls.recoverMediaError();
                break;
              default:
                setError("스트리밍 오류가 발생했습니다.");
                hls.destroy();
                break;
            }
          }
        });

        hlsRef.current = hls;
      } else if (video.canPlayType("application/vnd.apple.mpegurl")) {
        // Safari 네이티브 HLS 지원
        video.src = cctvUrl;
        video.addEventListener("loadedmetadata", () => {
          video.play().then(() => {
            setIsPlaying(true);
            setError(null);
          });
        });
      } else {
        setError("HLS를 지원하지 않는 브라우저입니다.");
      }
    } catch (err) {
      console.error("스트리밍 초기화 실패:", err);
      setError("영상 로딩 중 오류가 발생했습니다.");
    }

    return () => {
      if (hlsRef.current) {
        hlsRef.current.destroy();
      }
    };
  }, [cctvUrl]);

  // 전체화면 토글
  const toggleFullscreen = () => {
    const video = videoRef.current;
    if (!document.fullscreenElement) {
      video.requestFullscreen().then(() => {
        setIsFullscreen(true);
      });
    } else {
      document.exitFullscreen().then(() => {
        setIsFullscreen(false);
      });
    }
  };

  // 재생/일시정지
  const togglePlayPause = () => {
    const video = videoRef.current;
    if (video.paused) {
      video.play();
      setIsPlaying(true);
    } else {
      video.pause();
      setIsPlaying(false);
    }
  };

  // 스크린샷 캡처
  const captureScreenshot = () => {
    const video = videoRef.current;
    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext("2d");
    ctx.drawImage(video, 0, 0);
    
    canvas.toBlob((blob) => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `cctv-${new Date().getTime()}.png`;
      a.click();
      URL.revokeObjectURL(url);
    });
  };

  if (!cctvUrl) {
    return (
      <div className="video-stream placeholder">
        <div className="placeholder-content">
          <span className="placeholder-icon">📹</span>
          <p>CCTV를 선택하면 영상이 표시됩니다</p>
        </div>
      </div>
    );
  }

  return (
    <div className="video-stream">
      <div className="video-header">
        <h3>📡 실시간 영상</h3>
        {isAnalyzing && (
          <span className="analyzing-badge">
            🔄 분석 중...
          </span>
        )}
      </div>

      <div className="video-container">
        <video
          ref={videoRef}
          controls={false}
          autoPlay
          muted
          playsInline
          className="video-player"
        />

        {error && (
          <div className="video-error">
            <span>⚠️</span>
            <p>{error}</p>
            <button onClick={() => window.location.reload()}>새로고침</button>
          </div>
        )}

        {/* 커스텀 컨트롤 */}
        <div className="video-controls">
          <button onClick={togglePlayPause} className="control-btn">
            {isPlaying ? "⏸️" : "▶️"}
          </button>
          <button onClick={captureScreenshot} className="control-btn">
            📸 캡처
          </button>
          <button onClick={toggleFullscreen} className="control-btn">
            {isFullscreen ? "🗙" : "⛶"} 전체화면
          </button>
        </div>

        {/* 분석 오버레이 */}
        {isAnalyzing && (
          <div className="analysis-overlay">
            <div className="pulse-ring"></div>
            <span>AI 분석 중</span>
          </div>
        )}
      </div>

      {/* 영상 정보 */}
      <div className="video-info">
        <div className="info-item">
          <span className="info-label">상태:</span>
          <span className={`info-value ${isPlaying ? "live" : "paused"}`}>
            {isPlaying ? "🔴 LIVE" : "⏸️ 일시정지"}
          </span>
        </div>
        <div className="info-item">
          <span className="info-label">화질:</span>
          <span className="info-value">
            {videoRef.current ? `${videoRef.current.videoWidth}x${videoRef.current.videoHeight}` : "로딩 중..."}
          </span>
        </div>
      </div>
    </div>
  );
}

export default VideoStream;