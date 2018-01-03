# 基于redis的mq实现

## 接口

#### 入队
```
/enqueue?msg=boy
```
#### 出队
```
/dequeue
```

### 其中logqueue可以根据不同业务使用不同的队列名
