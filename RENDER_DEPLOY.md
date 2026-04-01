# Hướng dẫn Deploy lên Render

## Thứ tự deploy (BẮT BUỘC)
1. eureka-server
2. user-service, order-service, payment-service, product-catalog-service, product-recommendation-service
3. api-gateway (cuối cùng)

## Cấu hình từng Web Service trên Render

### 1. eureka-server
- Root Directory: `eureka-server`
- Runtime: Docker
- Port: `8761`
- Không cần env đặc biệt

### 2. user-service
- Root Directory: `user-service`
- Runtime: Docker
- Port: `8811`
- Environment Variables:
  ```
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://<eureka-url>.onrender.com/eureka/
  EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
  ```

### 3. order-service
- Root Directory: `order-service`
- Runtime: Docker
- Port: `8813`
- Environment Variables: (giống user-service)

### 4. payment-service
- Root Directory: `payment-service`
- Runtime: Docker
- Port: `8819`
- Environment Variables: (giống user-service)

### 5. product-catalog-service
- Root Directory: `product-catalog-service`
- Runtime: Docker
- Port: `8810`
- Environment Variables: (giống user-service)

### 6. product-recommendation-service
- Root Directory: `product-recommendation-service`
- Runtime: Docker
- Port: `8812`
- Environment Variables: (giống user-service)

### 7. api-gateway (cuối cùng)
- Root Directory: `api-gateway`
- Runtime: Docker
- Port: `8080`
- Environment Variables:
  ```
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://<eureka-url>.onrender.com/eureka/
  EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
  SPRING_DATA_REDIS_HOST=<redis-host-from-render>
  SPRING_DATA_REDIS_PORT=6379
  ```
