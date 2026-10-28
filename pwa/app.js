// Kshetrajna Progressive Web App Core Engine
document.addEventListener('DOMContentLoaded', () => {
  // State
  let currentStream = null;
  let useFacingMode = 'environment';
  let currentLocation = null;
  let capturedBlob = null;
  let db = null;

  // DOM Elements
  const video = document.getElementById('camera-video');
  const hudTime = document.getElementById('hud-time');
  const hudCoord = document.getElementById('hud-coord');
  const hudAccuracy = document.getElementById('hud-accuracy');
  const shutterBtn = document.getElementById('shutter-btn');
  const switchCamBtn = document.getElementById('switch-cam-btn');
  const journalBtn = document.getElementById('journal-btn');
  const reviewModal = document.getElementById('review-modal');
  const journalModal = document.getElementById('journal-modal');
  const closeReviewBtn = document.getElementById('close-review-btn');
  const closeJournalBtn = document.getElementById('close-journal-btn');
  const saveBtn = document.getElementById('save-photo-btn');
  const shareBtn = document.getElementById('share-photo-btn');
  const previewCanvas = document.getElementById('preview-canvas');
  const fieldNoteInput = document.getElementById('field-note-input');
  const journalList = document.getElementById('journal-list');

  // IndexedDB Setup for local-only offline persistence
  const initDB = () => {
    const request = indexedDB.open('KshetrajnaPwaDB', 1);
    request.onupgradeneeded = (e) => {
      const database = e.target.result;
      if (!database.objectStoreNames.contains('photos')) {
        database.createObjectStore('photos', { keyPath: 'id', autoIncrement: true });
      }
    };
    request.onsuccess = (e) => {
      db = e.target.result;
      updateJournalCount();
    };
  };

  // Clock Update
  const updateClock = () => {
    const now = new Date();
    hudTime.textContent = now.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }) + ', ' + now.toLocaleTimeString();
  };
  setInterval(updateClock, 1000);
  updateClock();

  // Geolocation Service
  const initGeolocation = () => {
    if (!navigator.geolocation) {
      hudCoord.textContent = 'GPS NOT SUPPORTED';
      return;
    }

    navigator.geolocation.watchPosition(
      (pos) => {
        const { latitude, longitude, accuracy } = pos.coords;
        currentLocation = {
          lat: latitude,
          lng: longitude,
          accuracy: accuracy,
          timestamp: pos.timestamp
        };

        const latDir = latitude >= 0 ? 'N' : 'S';
        const lngDir = longitude >= 0 ? 'E' : 'W';
        hudCoord.textContent = `${Math.abs(latitude).toFixed(5)}° ${latDir}, ${Math.abs(longitude).toFixed(5)}° ${lngDir}`;
        hudAccuracy.textContent = `±${Math.round(accuracy)}m`;
      },
      (err) => {
        console.warn('Geolocation warning:', err.message);
        hudCoord.textContent = 'GPS ACQUIRING / OFFLINE';
        hudAccuracy.textContent = '±--m';
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 5000
      }
    );
  };

  // Camera Management
  const startCamera = async () => {
    if (currentStream) {
      currentStream.getTracks().forEach(track => track.stop());
    }

    try {
      const constraints = {
        video: {
          facingMode: { ideal: useFacingMode },
          width: { ideal: 1920 },
          height: { ideal: 1080 }
        },
        audio: false
      };

      currentStream = await navigator.mediaDevices.getUserMedia(constraints);
      video.srcObject = currentStream;
      await video.play();
    } catch (err) {
      console.error('Camera initialization error:', err);
      alert('Camera access is required for field documentation. Please grant permission in your browser.');
    }
  };

  switchCamBtn.addEventListener('click', () => {
    useFacingMode = useFacingMode === 'environment' ? 'user' : 'environment';
    startCamera();
  });

  // Capture & Stamp Pipeline
  const captureAndStamp = () => {
    const vWidth = video.videoWidth || 1280;
    const vHeight = video.videoHeight || 720;

    previewCanvas.width = vWidth;
    previewCanvas.height = vHeight;
    const ctx = previewCanvas.getContext('2d');

    // Draw video frame
    ctx.drawImage(video, 0, 0, vWidth, vHeight);

    // Apply Kshetrajna Stamp Overlay
    renderStampOverlay(ctx, vWidth, vHeight, fieldNoteInput.value.trim());

    // Show Review Modal
    reviewModal.classList.add('active');
  };

  const renderStampOverlay = (ctx, width, height, note) => {
    const baseScale = Math.max(width, height) / 1200;
    const bannerPadding = Math.max(16, 20 * baseScale);
    const brandFontSize = Math.max(14, 20 * baseScale);
    const detailFontSize = Math.max(10, 14 * baseScale);
    const lineHeight = detailFontSize * 1.45;

    const lines = [];
    lines.push('SOURCE: Kshetrajna Field Capture (PWA)');
    lines.push('TIME: ' + new Date().toLocaleString());

    if (currentLocation) {
      const latDir = currentLocation.lat >= 0 ? 'N' : 'S';
      const lngDir = currentLocation.lng >= 0 ? 'E' : 'W';
      lines.push(`LOC: ${Math.abs(currentLocation.lat).toFixed(5)}° ${latDir}, ${Math.abs(currentLocation.lng).toFixed(5)}° ${lngDir} (±${Math.round(currentLocation.accuracy)}m)`);
    } else {
      lines.push('LOC: Not recorded (satellite signal unavailable)');
    }

    if (note) {
      lines.push('NOTE: ' + note);
    }

    const bannerHeight = (bannerPadding * 2) + brandFontSize + (lines.length * lineHeight) + (10 * baseScale);
    const bannerTop = height - bannerHeight;

    // Translucent dark forest green background
    ctx.fillStyle = 'rgba(11, 43, 32, 0.92)';
    ctx.fillRect(0, bannerTop, width, bannerHeight);

    // Gold accent divider line
    ctx.strokeStyle = '#D4A574';
    ctx.lineWidth = Math.max(2, 2 * baseScale);
    ctx.beginPath();
    ctx.moveTo(0, bannerTop);
    ctx.lineTo(width, bannerTop);
    ctx.stroke();

    // Brand Wordmark
    ctx.fillStyle = '#F8F5ED';
    ctx.font = `bold ${brandFontSize}px serif`;
    let curY = bannerTop + bannerPadding + brandFontSize * 0.8;
    ctx.fillText('KSHETRAJNA  |  क्षेत्रज्ञ', bannerPadding, curY);

    // Tagline
    ctx.fillStyle = '#D4A574';
    ctx.font = `bold ${detailFontSize * 0.9}px sans-serif`;
    ctx.fillText('• CAPTURE WHAT MATTERS.', bannerPadding + ctx.measureText('KSHETRAJNA  |  क्षेत्रज्ञ  ').width, curY);

    // Metadata lines
    ctx.fillStyle = '#FAF8F3';
    ctx.font = `${detailFontSize}px monospace`;
    lines.forEach((line) => {
      curY += lineHeight;
      ctx.fillText(line, bannerPadding, curY);
    });
  };

  // Re-render stamp when field note updates
  fieldNoteInput.addEventListener('input', () => {
    const vWidth = video.videoWidth || 1280;
    const vHeight = video.videoHeight || 720;
    const ctx = previewCanvas.getContext('2d');
    ctx.drawImage(video, 0, 0, vWidth, vHeight);
    renderStampOverlay(ctx, vWidth, vHeight, fieldNoteInput.value.trim());
  });

  shutterBtn.addEventListener('click', captureAndStamp);

  closeReviewBtn.addEventListener('click', () => {
    reviewModal.classList.remove('active');
    fieldNoteInput.value = '';
  });

  // Save to Journal (IndexedDB)
  saveBtn.addEventListener('click', () => {
    previewCanvas.toBlob((blob) => {
      if (!blob || !db) return;

      const tx = db.transaction('photos', 'readwrite');
      const store = tx.objectStore('photos');
      const photoItem = {
        blob: blob,
        timestamp: Date.now(),
        location: currentLocation,
        note: fieldNoteInput.value.trim()
      };

      store.add(photoItem);
      tx.oncomplete = () => {
        reviewModal.classList.remove('active');
        fieldNoteInput.value = '';
        updateJournalCount();
        alert('Photo saved to local Field Journal!');
      };
    }, 'image/jpeg', 0.92);
  });

  // Share / Export Photo
  shareBtn.addEventListener('click', () => {
    previewCanvas.toBlob(async (blob) => {
      if (!blob) return;

      const file = new File([blob], `Kshetrajna_${Date.now()}.jpg`, { type: 'image/jpeg' });
      if (navigator.canShare && navigator.canShare({ files: [file] })) {
        try {
          await navigator.share({
            files: [file],
            title: 'Kshetrajna Verified Photo',
            text: 'Captured and verified with Kshetrajna.'
          });
        } catch (e) {
          console.log('Share dismissed:', e);
        }
      } else {
        // Fallback: direct download
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Kshetrajna_${Date.now()}.jpg`;
        a.click();
        URL.revokeObjectURL(url);
      }
    }, 'image/jpeg', 0.92);
  });

  // Open Journal Modal
  journalBtn.addEventListener('click', () => {
    renderJournal();
    journalModal.classList.add('active');
  });

  closeJournalBtn.addEventListener('click', () => {
    journalModal.classList.remove('active');
  });

  const updateJournalCount = () => {
    if (!db) return;
    const tx = db.transaction('photos', 'readonly');
    const store = tx.objectStore('photos');
    const countReq = store.count();
    countReq.onsuccess = () => {
      journalBtn.title = `Field Journal (${countReq.result} photos)`;
    };
  };

  const renderJournal = () => {
    if (!db) return;
    journalList.innerHTML = '';
    const tx = db.transaction('photos', 'readonly');
    const store = tx.objectStore('photos');
    const req = store.getAll();

    req.onsuccess = () => {
      const photos = req.result.reverse();
      if (photos.length === 0) {
        journalList.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: var(--muted-sage); padding: 40px 0;">No photographs recorded yet.</p>';
        return;
      }

      photos.forEach(item => {
        const card = document.createElement('div');
        card.className = 'journal-card';
        const img = document.createElement('img');
        const url = URL.createObjectURL(item.blob);
        img.src = url;

        const info = document.createElement('div');
        info.className = 'journal-info';
        info.innerHTML = `
          <strong>${new Date(item.timestamp).toLocaleDateString()}</strong>
          <span style="color: var(--warm-gold);">${item.note ? item.note.substring(0, 24) : 'Field record'}</span>
        `;

        card.appendChild(img);
        card.appendChild(info);
        journalList.appendChild(card);
      });
    };
  };

  // Register PWA Service Worker
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('./sw.js').then((reg) => {
      console.log('Kshetrajna PWA Service Worker active:', reg.scope);
    }).catch((err) => {
      console.warn('Service Worker registration skipped:', err);
    });
  }

  // Initialize Core Services
  initDB();
  initGeolocation();
  startCamera();
});
