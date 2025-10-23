import React, { useEffect, useRef } from "react";
import Hls from "hls.js";

function VideoStream({ cctvUrl }) {
  const videoRef = useRef();

  useEffect(() => {
    if (cctvUrl && Hls.isSupported()) {
      const hls = new Hls();
      hls.loadSource(cctvUrl);
      hls.attachMedia(videoRef.current);
    }
  }, [cctvUrl]);

  return (
    <div>
      <h3>실시간 CCTV</h3>
      <video ref={videoRef} controls autoPlay muted width="600" height="400" />
    </div>
  );
}

export default VideoStream;