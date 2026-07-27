import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE } from '../api/client';

// Subscribes to /topic/buses (see WebSocketConfig + BusLocationIngestService
// on the backend) and keeps a live vehicleId -> latest event map, so any
// screen can show positions pushed in real time instead of polling.
export default function useBusSocket({ enabled = true } = {}) {
  const [positions, setPositions] = useState({});
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

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
            setPositions((prev) => ({ ...prev, [event.vehicleId]: event }));
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

    return () => {
      stompClient.deactivate();
    };
  }, [enabled]);

  return { positions, connected };
}
