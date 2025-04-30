import request from '../utils/request';

/**
 * 上传图片文件
 * 
 * @param {File} file 要上传的图片文件
 * @returns {Promise<string>} 返回上传后的图片URL
 */
export function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(response => {
    // 假设服务器返回格式为 { code: 0, data: '图片URL', message: '成功' }
    if (response && response.data) {
      return response.data;
    }
    throw new Error('上传失败');
  });
}

/**
 * 上传文件
 * 
 * @param {File} file 要上传的文件
 * @param {string} type 文件类型，如 'document', 'image', 'video'
 * @returns {Promise<string>} 返回上传后的文件URL
 */
export function uploadFile(file, type = 'file') {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('type', type);
  
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(response => {
    if (response && response.data) {
      return response.data;
    }
    throw new Error('上传失败');
  });
} 