import { Room, RoomEvent, VideoPresets, DisconnectReason } from 'livekit-client';
import { icons } from '../icons.js';
import { api } from '../api.js';
import { showToast } from '../main.js';

let currentRoom = null;
let isIntentionalDisconnect = false;

// ===========================
// CLEANUP ON PAGE UNLOAD
// ===========================
function cleanupRoom() {
  if (currentRoom) {
    isIntentionalDisconnect = true;
    try {
      // Detach all tracks first
      currentRoom.localParticipant?.trackPublications?.forEach((pub) => {
        if (pub.track) {
          pub.track.detach();
          pub.track.stop();
        }
      });
      currentRoom.remoteParticipants?.forEach((p) => {
        p.trackPublications?.forEach((pub) => {
          if (pub.track) {
            pub.track.detach();
          }
        });
      });
      currentRoom.disconnect(true);
    } catch (e) {
      console.warn('Room cleanup error:', e);
    }
    currentRoom = null;
  }
}

// Register global cleanup handlers
window.addEventListener('beforeunload', cleanupRoom);
window.addEventListener('pagehide', cleanupRoom);

// ===========================
// START CALL
// ===========================
export async function startCallFlow(calleeId) {
  // Clean up any existing room first
  cleanupRoom();
  
  try {
    const res = await api.startCall(calleeId);
    if (!res.room_name || !res.token) {
      throw new Error('Không nhận được LiveKit token từ server.');
    }
    
    const url = `ws://${window.location.hostname}:7880`;
    await openVideoCallModal(url, res.token, res.room_name);

  } catch(err) {
    showToast('Lỗi khi gọi video: ' + err.message, 'error');
  }
}

// ===========================
// JOIN CALL
// ===========================
export async function joinCallFlow(roomName) {
  // Clean up any existing room first
  cleanupRoom();
  
  try {
    const res = await api.joinCall(roomName);
    if (!res.token) {
      throw new Error('Không nhận được LiveKit token từ server.');
    }
    const url = `ws://${window.location.hostname}:7880`;
    await openVideoCallModal(url, res.token, roomName);
  } catch(err) {
    showToast('Lỗi khi tham gia gọi video: ' + err.message, 'error');
  }
}

// ===========================
// VIDEO CALL MODAL
// ===========================
async function openVideoCallModal(url, token, roomName) {
  isIntentionalDisconnect = false;

  let modalContainer = document.getElementById('modal-root');
  if(!modalContainer) {
    modalContainer = document.createElement('div');
    modalContainer.id = 'modal-root';
    document.body.appendChild(modalContainer);
  }

  modalContainer.innerHTML = `
    <div class="video-call-overlay active" id="video-call-modal">
        <!-- Header -->
        <div style="position:absolute; top:24px; left:24px; z-index:10;">
           <div style="color: white; font-weight: bold; font-size:18px;">Phòng: ${roomName}</div>
           <div id="video-call-status" style="color: #4ade80; font-size: 14px;">Đang kết nối...</div>
        </div>

        <!-- Video Grid -->
        <div class="video-grid" id="video-grid" style="width:100%; height:100%; padding-bottom: 100px;">
           <!-- Local & Remote videos will be appended here -->
        </div>

        <!-- Controls -->
        <div class="video-controls" style="position:absolute; bottom: 32px; left:50%; transform: translateX(-50%); border-radius:32px;">
           <button class="video-control-btn active" id="btn-toggle-mic" title="Bật/Tắt Mic">
             ${icons.mic}
           </button>
           <button class="video-control-btn active" id="btn-toggle-cam" title="Bật/Tắt Camera">
             ${icons.video}
           </button>
           <button class="video-control-btn hangup" id="btn-end-call" title="Kết thúc">
             ${icons.x}
           </button>
        </div>
    </div>
  `;

  const btnMic = document.getElementById('btn-toggle-mic');
  const btnCam = document.getElementById('btn-toggle-cam');
  const btnEnd = document.getElementById('btn-end-call');
  const statusEl = document.getElementById('video-call-status');

  // Initialize Room with reconnect disabled after intentional leave
  const room = new Room({
    adaptiveStream: true,
    dynacast: true,
    videoCaptureDefaults: {
      resolution: VideoPresets.h720.resolution,
    },
    disconnectOnPageLeave: true,
    stopLocalTrackOnUnpublish: true,
  });
  currentRoom = room;

  // ---- Room Events ----
  room.on(RoomEvent.Connected, () => {
    if (statusEl) {
      statusEl.textContent = 'Đã kết nối';
      statusEl.style.color = '#4ade80';
    }
  });

  room.on(RoomEvent.Reconnecting, () => {
    if (isIntentionalDisconnect) {
      // Don't reconnect if we intentionally disconnected
      try { room.disconnect(true); } catch(e) {}
      return;
    }
    if (statusEl) {
      statusEl.textContent = 'Đang kết nối lại...';
      statusEl.style.color = '#f59e0b';
    }
  });

  room.on(RoomEvent.Reconnected, () => {
    if (statusEl) {
      statusEl.textContent = 'Đã kết nối lại';
      statusEl.style.color = '#4ade80';
    }
  });

  room.on(RoomEvent.Disconnected, (reason) => {
    console.log('Room disconnected:', reason);
    // Only close modal if not already closing
    if (!isIntentionalDisconnect) {
      showToast('Cuộc gọi đã kết thúc', 'info');
    }
    closeVideoModal();
  });

  room.on(RoomEvent.MediaDevicesError, (error) => {
    console.error('Media device error:', error);
    showToast('Lỗi thiết bị: ' + error.message, 'error');
  });

  room.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
    if (track.kind === 'video') {
      const element = track.attach();
      element.style.width = '100%';
      element.style.height = '100%';
      element.style.objectFit = 'cover';
      element.style.borderRadius = 'var(--radius-md)';
      element.dataset.sid = track.sid;

      const wrapper = document.createElement('div');
      wrapper.className = 'video-container';
      wrapper.id = 'video-wrap-' + track.sid;
      wrapper.appendChild(element);

      const label = document.createElement('div');
      label.className = 'video-label';
      label.textContent = participant.identity;
      wrapper.appendChild(label);

      document.getElementById('video-grid')?.appendChild(wrapper);
    } else if (track.kind === 'audio') {
      const element = track.attach();
      element.style.display = 'none';
      element.id = 'audio-' + track.sid;
      document.body.appendChild(element);
    }
  });

  room.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
    // Detach all elements for this track
    const elements = track.detach();
    elements.forEach(el => el.remove());
    // Remove containers
    const wrapper = document.getElementById('video-wrap-' + track.sid);
    if (wrapper) wrapper.remove();
    const audioEl = document.getElementById('audio-' + track.sid);
    if (audioEl) audioEl.remove();
  });

  room.on(RoomEvent.LocalTrackPublished, (publication, participant) => {
    const track = publication.track;
    if (track && track.kind === 'video') {
      const element = track.attach();
      element.style.width = '100%';
      element.style.height = '100%';
      element.style.objectFit = 'cover';
      element.style.borderRadius = 'var(--radius-md)';
      element.muted = true;

      const wrapper = document.createElement('div');
      wrapper.className = 'video-container';
      wrapper.id = 'local-video-wrap';
      wrapper.appendChild(element);

      const label = document.createElement('div');
      label.className = 'video-label';
      label.textContent = 'Bạn (Local)';
      wrapper.appendChild(label);

      document.getElementById('video-grid')?.appendChild(wrapper);
    }
  });

  room.on(RoomEvent.LocalTrackUnpublished, (publication) => {
    const track = publication.track;
    if (track) {
      const elements = track.detach();
      elements.forEach(el => el.remove());
    }
    const localWrap = document.getElementById('local-video-wrap');
    if (localWrap) localWrap.remove();
  });

  // ---- Connect ----
  try {
    statusEl.textContent = 'Đang kết nối đến phòng...';
    
    await room.connect(url, token, {
      peerConnectionTimeout: 30000,
      autoSubscribe: true,
    });
    
    statusEl.textContent = 'Đang mở camera...';
    
    // Try to publish camera and mic with graceful fallback
    try {
      await room.localParticipant.enableCameraAndMicrophone();
    } catch(mediaErr) {
      console.warn('Could not enable camera/mic:', mediaErr);
      statusEl.textContent = 'Đã kết nối (không có camera)';
      showToast('Không thể mở camera/mic. Kiểm tra quyền truy cập.', 'warning');
    }

    // ---- Control Buttons ----
    let isMicOn = true;
    let isCamOn = true;

    btnMic.addEventListener('click', async () => {
      isMicOn = !isMicOn;
      try {
        await room.localParticipant.setMicrophoneEnabled(isMicOn);
      } catch(e) { console.warn('Mic toggle error:', e); }
      btnMic.classList.toggle('active', isMicOn);
      btnMic.innerHTML = isMicOn ? icons.mic : (icons.micOff || icons.mic);
      btnMic.style.backgroundColor = isMicOn ? '' : 'var(--busy)';
    });

    btnCam.addEventListener('click', async () => {
      isCamOn = !isCamOn;
      try {
        await room.localParticipant.setCameraEnabled(isCamOn);
      } catch(e) { console.warn('Cam toggle error:', e); }
      btnCam.classList.toggle('active', isCamOn);
      btnCam.innerHTML = isCamOn ? icons.video : (icons.videoOff || icons.video);
      btnCam.style.backgroundColor = isCamOn ? '' : 'var(--busy)';
    });

    btnEnd.addEventListener('click', () => {
      isIntentionalDisconnect = true;
      cleanupRoom();
      closeVideoModal();
    });

  } catch(e) {
    console.error('Video call connection error:', e);
    if (statusEl) {
      statusEl.textContent = 'Lỗi kết nối: ' + (e.message || 'Không rõ');
      statusEl.style.color = 'var(--busy)';
    }
    showToast('Không thể kết nối vào phòng video: ' + (e.message || ''), 'error');
    
    // Allow closing even on error
    btnEnd.addEventListener('click', () => {
      cleanupRoom();
      closeVideoModal();
    });
  }
}

// ===========================
// CLOSE MODAL
// ===========================
function closeVideoModal() {
  // Clean up room if still exists
  if (currentRoom) {
    isIntentionalDisconnect = true;
    cleanupRoom();
  }
  
  // Remove any orphaned audio elements
  document.querySelectorAll('audio[id^="audio-"]').forEach(el => el.remove());
  
  const modalContainer = document.getElementById('modal-root');
  if (modalContainer) modalContainer.innerHTML = '';
}
