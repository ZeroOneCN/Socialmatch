/**
 * 显示toast提示
 * @param {string} message 提示信息
 * @param {number} duration 显示时长（毫秒）
 */
export function showToast(message, duration = 2000) {
  // 检查是否已存在toast元素
  let toastEl = document.getElementById('global-toast');
  
  if (!toastEl) {
    // 创建toast元素
    toastEl = document.createElement('div');
    toastEl.id = 'global-toast';
    toastEl.style.position = 'fixed';
    toastEl.style.top = '50%';
    toastEl.style.left = '50%';
    toastEl.style.transform = 'translate(-50%, -50%)';
    toastEl.style.backgroundColor = 'rgba(0, 0, 0, 0.7)';
    toastEl.style.color = '#fff';
    toastEl.style.padding = '10px 15px';
    toastEl.style.borderRadius = '4px';
    toastEl.style.fontSize = '14px';
    toastEl.style.maxWidth = '80%';
    toastEl.style.textAlign = 'center';
    toastEl.style.zIndex = '10000';
    document.body.appendChild(toastEl);
  }
  
  // 设置提示信息
  toastEl.textContent = message;
  toastEl.style.display = 'block';
  
  // 定时关闭
  clearTimeout(toastEl.timer);
  toastEl.timer = setTimeout(() => {
    toastEl.style.display = 'none';
  }, duration);
} 