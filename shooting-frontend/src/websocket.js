import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * WebSocket服务，用于实时通信
 */
class StompService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
    this.connectCallbacks = [];
  }

  /**
   * 连接到WebSocket服务器
   * @param {Function} onConnectedCallback - 连接成功后的回调函数
   */
  connect(onConnectedCallback) {
    // 如果已经连接，直接执行回调
    if (this.connected && this.client) {
      if (onConnectedCallback) {
        onConnectedCallback();
      }
      return;
    }

    // 如果有回调，添加到队列
    if (onConnectedCallback) {
      this.connectCallbacks.push(onConnectedCallback);
    }

    // 如果正在连接中，不重复连接
    if (this.client) {
      return;
    }

    // 获取token
    const token = localStorage.getItem('aimlab-token');
    
    // 创建新的STOMP客户端
    this.client = new Client({
      // 使用SockJS作为WebSocket传输，并传递token作为查询参数
      webSocketFactory: () => {
        const url = token ? `/ws?token=${encodeURIComponent(token)}` : '/ws';
        return new SockJS(url);
      },
      
      // 连接头（包含token）
      connectHeaders: token ? {
        'aimlab-token': token
      } : {},
      
      // 连接成功回调
      onConnect: () => {
        console.log('WebSocket连接成功');
        this.connected = true;
        
        // 重新订阅之前的主题
        this.subscriptions.forEach((callback, topic) => {
          this._subscribe(topic, callback);
        });
        
        // 执行所有连接回调
        this.connectCallbacks.forEach(callback => callback());
        this.connectCallbacks = [];
      },
      
      // 连接错误回调
      onStompError: (frame) => {
        console.warn('WebSocket连接错误（后端可能未启动）:', frame.headers.message || frame);
        this.connected = false;
      },
      
      // 连接断开回调
      onDisconnect: () => {
        console.log('WebSocket连接断开');
        this.connected = false;
      },
      
      // 自动重连
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    // 激活连接
    this.client.activate();
  }

  /**
   * 订阅主题
   * @param {string} topic - 要订阅的主题
   * @param {Function} callback - 收到消息时的回调函数
   * @returns {Object} 订阅对象
   */
  subscribe(topic, callback) {
    // 保存订阅信息
    this.subscriptions.set(topic, callback);
    
    // 如果已连接，立即订阅
    if (this.connected && this.client) {
      return this._subscribe(topic, callback);
    }
    
    // 否则连接后会自动订阅
    return null;
  }

  /**
   * 内部订阅方法
   * @private
   */
  _subscribe(topic, callback) {
    return this.client.subscribe(topic, (message) => {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (error) {
        console.error('解析WebSocket消息失败:', error);
        callback(message.body);
      }
    });
  }

  /**
   * 取消订阅主题
   * @param {string} topic - 要取消订阅的主题
   */
  unsubscribe(topic) {
    this.subscriptions.delete(topic);
  }

  /**
   * 发送消息到指定目的地
   * @param {string} destination - 目的地
   * @param {Object} body - 消息体
   */
  send(destination, body) {
    if (!this.connected || !this.client) {
      console.error('WebSocket未连接，无法发送消息');
      return;
    }
    
    this.client.publish({
      destination,
      body: JSON.stringify(body)
    });
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.connected = false;
    this.subscriptions.clear();
    this.connectCallbacks = [];
  }
}

// 创建单例实例
const stompService = new StompService();

export default stompService; 