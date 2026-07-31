import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE } from '../api/client';

// Subscribes to /topic/buses and batches incoming live position updates
// at a 1-second interval using useRef + React hooks to prevent high-frequency re-render lag.
export default function useBusSocket({ enabled = true } = {}) {
  const [positions, setPositions] = useState({});
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);
  const bufferRef = useRef({});
  const isDirtyRef = useRef(false);

  useEffect(() => {
    if (!enabled) return undefined;

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(WS_BASE),
      reconnectDelay: 4000,
      onConnect: () => {
        setConnected(true);
        stompClient.subscribe('/topic/buses', (message) => {
          try {
            const event = JSON.parse(message.body);
            if (event?.vehicleId) {
              bufferRef.current[event.vehicleId] = event;
              isDirtyRef.current = true;
            }
          } catch {
            // ignore malformed frame
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
    });

    clientRef.current = stompClient;
    stompClient.activate();

    // Flush buffered position updates to React state once per second
    const flushInterval = setInterval(() => {
      if (isDirtyRef.current) {
        setPositions((prev) => ({ ...prev, ...bufferRef.current }));
        isDirtyRef.current = false;
      }
    }, 1000);

    return () => {
      clearInterval(flushInterval);
      stompClient.deactivate();
    };
  }, [enabled]);

  return { positions, connected };
}
