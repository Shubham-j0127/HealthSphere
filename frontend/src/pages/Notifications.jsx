import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'
import API_URL from '../config/api'
import './Notifications.css'

const Notifications = () => {
  const { user } = useAuth()
  const [notifications, setNotifications] = useState([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // 'all' or 'unread'

  useEffect(() => {
    loadNotifications()
    loadUnreadCount()
    const interval = setInterval(() => {
      loadNotifications()
      loadUnreadCount()
    }, 30000) // Refresh every 30 seconds

    return () => clearInterval(interval)
  }, [filter])

  const loadNotifications = async () => {
    try {
      setLoading(true)
      const response = await axios.get('${API_URL}/api/notifications')
      let filtered = response.data
      if (filter === 'unread') {
        filtered = filtered.filter(n => !n.read)
      }
      setNotifications(filtered)
    } catch (error) {
      console.error('Error loading notifications:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadUnreadCount = async () => {
    try {
      const response = await axios.get('${API_URL}/api/notifications/unread/count')
      setUnreadCount(response.data.count)
    } catch (error) {
      console.error('Error loading unread count:', error)
    }
  }

  const markAsRead = async (notificationId) => {
    try {
      await axios.put(`${API_URL}/api/notifications/${notificationId}/read`)
      loadNotifications()
      loadUnreadCount()
    } catch (error) {
      console.error('Error marking notification as read:', error)
    }
  }

  const markAllAsRead = async () => {
    try {
      await axios.put('${API_URL}/api/notifications/read-all')
      loadNotifications()
      loadUnreadCount()
    } catch (error) {
      console.error('Error marking all as read:', error)
    }
  }

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'APPOINTMENT_REMINDER':
      case 'APPOINTMENT_CONFIRMED':
      case 'APPOINTMENT_CANCELLED':
        return '📅'
      case 'PRESCRIPTION_CREATED':
        return '💊'
      case 'MEDICAL_RECORD_UPDATED':
        return '📋'
      default:
        return '🔔'
    }
  }

  const getNotificationColor = (type) => {
    switch (type) {
      case 'APPOINTMENT_CONFIRMED':
        return '#28a745'
      case 'APPOINTMENT_CANCELLED':
        return '#dc3545'
      case 'PRESCRIPTION_CREATED':
        return '#007bff'
      default:
        return '#667eea'
    }
  }

  if (loading && notifications.length === 0) {
    return <div className="loading">Loading notifications...</div>
  }

  return (
    <div className="notifications">
      <div className="page-header">
        <h1>Notifications</h1>
        <div className="header-actions">
          {unreadCount > 0 && (
            <button className="btn btn-secondary" onClick={markAllAsRead}>
              Mark All as Read
            </button>
          )}
        </div>
      </div>

      <div className="filter-tabs">
        <button
          className={`filter-tab ${filter === 'all' ? 'active' : ''}`}
          onClick={() => setFilter('all')}
        >
          All
        </button>
        <button
          className={`filter-tab ${filter === 'unread' ? 'active' : ''}`}
          onClick={() => setFilter('unread')}
        >
          Unread {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
        </button>
      </div>

      <div className="notifications-list">
        {notifications.length === 0 ? (
          <div className="card">
            <p>No notifications found.</p>
          </div>
        ) : (
          notifications.map((notification) => (
            <div
              key={notification.id}
              className={`card notification-card ${!notification.read ? 'unread' : ''}`}
              onClick={() => !notification.read && markAsRead(notification.id)}
            >
              <div className="notification-content">
                <div className="notification-icon" style={{ color: getNotificationColor(notification.type) }}>
                  {getNotificationIcon(notification.type)}
                </div>
                <div className="notification-details">
                  <div className="notification-header">
                    <h3>{notification.title}</h3>
                    {!notification.read && <span className="unread-dot"></span>}
                  </div>
                  <p className="notification-message">{notification.message}</p>
                  <p className="notification-time">
                    {new Date(notification.createdAt).toLocaleString()}
                  </p>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default Notifications

