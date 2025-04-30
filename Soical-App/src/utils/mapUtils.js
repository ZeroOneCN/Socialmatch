// 高德地图工具类

// 高德地图API配置
const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || '';
const AMAP_SECURITY_CODE = import.meta.env.VITE_AMAP_SECURITY_CODE || '';

// 检查API Key是否配置
const isAPIConfigured = AMAP_KEY && AMAP_KEY.length > 0;

/**
 * 异步加载高德地图SDK
 * @returns {Promise} 加载完成的Promise
 */
export function loadAMapScript() {
  return new Promise((resolve, reject) => {
    // 如果已加载过，直接返回
    if (window.AMap) {
      resolve(window.AMap);
      return;
    }
    
    // 检查API Key是否配置
    if (!isAPIConfigured) {
      console.error('高德地图API密钥未配置');
      reject(new Error('高德地图API密钥未配置'));
      return;
    }

    // 设置安全密钥
    if (AMAP_SECURITY_CODE) {
      window._AMapSecurityConfig = {
        securityJsCode: AMAP_SECURITY_CODE
      };
    }

    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.async = true;
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}&callback=initAMap`;

    // 超时处理
    const timeout = setTimeout(() => {
      reject(new Error('高德地图脚本加载超时'));
      delete window.initAMap;
    }, 10000); // 10秒超时

    window.initAMap = () => {
      clearTimeout(timeout);
      resolve(window.AMap);
      delete window.initAMap;
    };

    script.onerror = () => {
      clearTimeout(timeout);
      reject(new Error('高德地图脚本加载失败'));
      delete window.initAMap;
    };

    document.head.appendChild(script);
  });
}

/**
 * 根据经纬度获取位置信息
 * @param {number} latitude 纬度
 * @param {number} longitude 经度
 * @returns {Promise<Object>} 位置信息
 */
export async function getLocationFromCoords(latitude, longitude) {
  // 验证经纬度
  if (!latitude || !longitude) {
    throw new Error('无效的经纬度');
  }

  // 验证坐标是否在中国区域内（粗略判断）
  // 中国大陆经度范围约为73°E至135°E，纬度范围约为18°N至53°N
  const isChinaMainland = (
    longitude >= 73 && longitude <= 135 &&
    latitude >= 18 && latitude <= 53
  );

  // 如果不在中国区域，直接返回默认位置
  if (!isChinaMainland) {
    console.warn('坐标点不在中国区域内，使用默认位置', { latitude, longitude });
    return {
      city: '北京',
      address: '北京',
      province: '北京',
      district: '',
      township: ''
    };
  }

  try {
    // 检查API Key是否配置
    if (!isAPIConfigured) {
      throw new Error('高德地图API密钥未配置');
    }
    
    // 加载高德地图API
    const AMap = await loadAMapScript();
    
    return new Promise((resolve, reject) => {
      // 使用AMap.plugin加载逆地理编码插件
      AMap.plugin('AMap.Geocoder', () => {
        const geocoder = new AMap.Geocoder();
        
        // 调用逆地理编码接口
        geocoder.getAddress([longitude, latitude], (status, result) => {
          if (status === 'complete' && result.info === 'OK') {
            const address = result.regeocode.formattedAddress;
            const addressComponent = result.regeocode.addressComponent;
            
            // 返回位置信息对象
            resolve({
              address,
              province: addressComponent.province || '',
              city: addressComponent.city || addressComponent.province || '',
              district: addressComponent.district || '',
              township: addressComponent.township || '',
            });
          } else {
            console.error('逆地理编码失败:', status, result);
            // 遇到no_data错误，返回默认位置而不是抛出错误
            if (status === 'no_data') {
              console.warn('该坐标点没有对应的地址信息，使用默认位置');
              resolve({
                city: '北京',
                address: '北京',
                province: '北京',
                district: '',
                township: ''
              });
            } else {
              reject(new Error('获取地址信息失败'));
            }
          }
        });
      });
    });
  } catch (error) {
    console.error('地理位置转换失败:', error);
    throw error;
  }
}

/**
 * 使用HTML5 Geolocation API获取当前位置
 * @returns {Promise<{latitude: number, longitude: number}>} 经纬度信息
 */
export function getCurrentPosition() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持地理位置服务'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        });
      },
      (error) => {
        let errorMessage = '获取位置失败';
        switch (error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = '用户拒绝了位置请求';
            break;
          case error.POSITION_UNAVAILABLE:
            errorMessage = '位置信息不可用';
            break;
          case error.TIMEOUT:
            errorMessage = '获取位置请求超时';
            break;
        }
        reject(new Error(errorMessage));
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  });
}

/**
 * 使用高德地图IP定位API获取当前位置
 * 当浏览器定位失败时的备选方案
 * @returns {Promise<{latitude: number, longitude: number, city: string}>} 位置信息
 */
export async function getPositionByIP() {
  try {
    // 检查API Key是否配置
    if (!isAPIConfigured) {
      throw new Error('高德地图API密钥未配置');
    }
    
    // 加载高德地图API
    const AMap = await loadAMapScript();
    
    return new Promise((resolve, reject) => {
      // 使用高德地图citySearch插件进行IP定位
      AMap.plugin('AMap.CitySearch', () => {
        const citySearch = new AMap.CitySearch();
        citySearch.getLocalCity((status, result) => {
          if (status === 'complete' && result.info === 'OK') {
            // 成功获取城市信息
            const city = result.city;
            const center = result.rectangle.split(';')[0].split(',');
            
            if (center.length === 2) {
              resolve({
                latitude: parseFloat(center[1]),
                longitude: parseFloat(center[0]),
                city: city
              });
            } else {
              // 如果解析坐标失败，使用默认位置
              resolve({
                latitude: 39.9042,
                longitude: 116.4074,
                city: '北京'
              });
            }
          } else {
            reject(new Error('IP定位失败'));
          }
        });
      });
    });
  } catch (error) {
    console.error('IP定位失败:', error);
    throw error;
  }
}

// 备用位置 - 默认使用北京坐标
const FALLBACK_LOCATIONS = [
  { latitude: 39.9042, longitude: 116.4074, city: '北京' }, // 北京
  { latitude: 31.2304, longitude: 121.4737, city: '上海' }, // 上海
  { latitude: 23.1291, longitude: 113.2644, city: '广州' }, // 广州
  { latitude: 30.5728, longitude: 104.0668, city: '成都' }, // 成都
  { latitude: 22.5431, longitude: 114.0579, city: '深圳' }  // 深圳
];

/**
 * 获取当前位置并转换为地址
 * @returns {Promise<Object>} 位置信息对象
 */
export async function getCurrentLocationInfo() {
  try {
    // 检查API Key是否配置，如果未配置直接使用备用位置
    if (!isAPIConfigured) {
      console.warn('高德地图API密钥未配置，使用备用位置');
      const fallback = FALLBACK_LOCATIONS[Math.floor(Math.random() * FALLBACK_LOCATIONS.length)];
      return {
        city: fallback.city,
        address: fallback.city,
        province: '',
        district: '',
        township: ''
      };
    }
    
    // 尝试使用浏览器定位API获取位置
    let position;
    try {
      position = await getCurrentPosition();
      console.log('成功获取浏览器位置坐标:', position);
    } catch (browserPosError) {
      console.warn('浏览器定位失败，尝试IP定位', browserPosError);
      
      // 尝试使用IP定位作为备选方案
      try {
        position = await getPositionByIP();
        console.log('成功获取IP定位坐标:', position);
        
        // 如果IP定位直接返回了城市信息，可以直接使用
        if (position.city) {
          return {
            city: position.city,
            address: position.city,
            province: '',
            district: '',
            township: ''
          };
        }
      } catch (ipPosError) {
        console.warn('IP定位也失败，使用备用位置', ipPosError);
        // 两种定位方式都失败，使用备用位置
        const fallback = FALLBACK_LOCATIONS[Math.floor(Math.random() * FALLBACK_LOCATIONS.length)];
        return {
          city: fallback.city,
          address: fallback.city,
          province: '',
          district: '',
          township: ''
        };
      }
    }
    
    // 获取成功，转换为地址信息
    if (position && position.latitude && position.longitude) {
      try {
        const locationInfo = await getLocationFromCoords(position.latitude, position.longitude);
        console.log('成功获取地址信息:', locationInfo);
        return locationInfo;
      } catch (geoError) {
        console.error('获取地址信息失败:', geoError);
        
        // 如果IP定位返回了城市信息，但逆地理编码失败，使用IP定位的城市信息
        if (position.city) {
          return {
            city: position.city,
            address: position.city,
            province: '',
            district: '',
            township: ''
          };
        }
        
        // 否则使用备用位置
        const fallback = FALLBACK_LOCATIONS[Math.floor(Math.random() * FALLBACK_LOCATIONS.length)];
        return {
          city: fallback.city,
          address: fallback.city,
          province: '',
          district: '',
          township: ''
        };
      }
    } else {
      throw new Error('获取位置坐标无效');
    }
  } catch (error) {
    console.error('获取当前位置信息失败:', error);
    // 始终返回一个结果，不抛出错误
    const fallback = FALLBACK_LOCATIONS[0];
    return {
      city: fallback.city,
      address: fallback.city,
      province: '',
      district: '',
      township: ''
    };
  }
} 