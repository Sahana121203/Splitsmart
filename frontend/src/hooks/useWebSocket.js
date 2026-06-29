import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export default function useWebSocket(
  tripId,
  onKittyUpdate,
  onExpenseEvent,
  onStatusChange,
  onEditEvent,
  onSettlementEvent
) {
  const clientRef = useRef(null)

  useEffect(() => {
    if (!tripId) return
    const token = localStorage.getItem('access_token')
    if (!token) return

    const client = new Client({
      webSocketFactory: () =>
        new SockJS('http://localhost:8080/ws?token=' + token),
      reconnectDelay: 5000,
      debug: () => {},
      onConnect: () => {
        client.subscribe(`/topic/trips/${tripId}/kitty`, (msg) => {
          if (onKittyUpdate) onKittyUpdate(JSON.parse(msg.body))
        })
        client.subscribe(`/topic/trips/${tripId}/expenses`, (msg) => {
          if (onExpenseEvent) onExpenseEvent(JSON.parse(msg.body))
        })
        client.subscribe(`/topic/trips/${tripId}/status`, (msg) => {
          if (onStatusChange) onStatusChange(JSON.parse(msg.body))
        })
        client.subscribe(`/topic/trips/${tripId}/edits`, (msg) => {
          if (onEditEvent) onEditEvent(JSON.parse(msg.body))
        })
        client.subscribe(`/topic/trips/${tripId}/settlement`, (msg) => {
          if (onSettlementEvent) onSettlementEvent(JSON.parse(msg.body))
        })
      },
      onStompError: (frame) => {
        console.warn('STOMP error:', frame)
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected')
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate()
        clientRef.current = null
      }
    }
  }, [tripId])
}
