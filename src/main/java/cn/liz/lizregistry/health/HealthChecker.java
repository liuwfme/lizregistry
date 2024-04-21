package cn.liz.lizregistry.health;

/**
 * 定时检查服务实例是否存活，从注册的 服务实例列表中 剔除死掉的实例
 */
public interface HealthChecker {
    void start();

    void stop();
}
