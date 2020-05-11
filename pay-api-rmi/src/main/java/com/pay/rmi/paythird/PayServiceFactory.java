package com.pay.rmi.paythird;


import com.pay.rmi.common.exception.RException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PayServiceFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public PayService getService(String channelNo) {
        PayService service;
        try {
            service = applicationContext.getBean(channelNo, PayService.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new RException("平台反馈：暂不支持该通道");
        }
        return service;
    }


    /*
    public PayService getService(String channelNo, String script) {
        if (StringUtils.isNotBlank(channelNo) && StringUtils.isNotBlank(script)) {
            //获取script的hashCode
            String scriptCode = String.valueOf(script.hashCode());
            //获取上一次变动的hashCode
            String preScriptCode = channelMap.get(channelNo);
            //判断代码是否有变动
            if (!scriptCode.equals(preScriptCode)) {
                //动态加载器

                GroovyClassLoader loader = new GroovyClassLoader();
                //编译
                Class<PayService> clazz = null;
                try {
                    clazz = loader.parseClass(script);
                } catch (CompilationFailedException e) {
                    e.printStackTrace();
                    logger.error("通道：{}，动态代码编译错误", channelNo);
                    throw new RException("通道编译错误");
                }
                //创建springbean构造器
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                //获取bean实例
                BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
                //获取bean工厂
                DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
                //依赖注入
                beanFactory.applyBeanPostProcessorsAfterInitialization(beanDefinition, channelNo);
                //注册bean
                beanFactory.registerBeanDefinition(channelNo, beanDefinition);

                //加入代码变动记录
                channelMap.put(channelNo, scriptCode);
            }
        }
        return getService(channelNo);
    }*/
}
