package com.nsu.aspect;
/*
  Date:2025/3/12
  Time:17:19
  @author llh 
 */

import com.nsu.annotation.AutoFill;
import com.nsu.constant.AutoFillConstant;
import com.nsu.context.BaseContext;
import com.nsu.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/*
 * 自定义切面类，实现自动填充功能
 */
@Aspect // 标识为切面类
@Component // 将切面类交给Spring容器管理
@Slf4j // 使用lombok提供的日志注解
public class AutoFillAspect {

    // 切入点,@Pointcut定义切入点，对哪些类和方法进行切入
    // execution(* com.sky.mapper.*.*(..))表示切入com.sky.mapper包下的所有类的所有方法，*表示任意返回值，..表示任意参数
    // 第一个*表示返回值，第二个*表示类名，第三个*表示方法名，(..)表示任意参数
    // @annotation(com.sky.annotation.AutoFill)表示切入带有@AutoFill注解的方法
    @Pointcut("execution(* com.nsu.mapper.*.*(..)) && @annotation(com.nsu.annotation.AutoFill)")
    public void AutoFillPointCut(){}

    @Before("AutoFillPointCut()")
    // 前置通知，方法执行前执行
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段的自动填充");
        // 获取被拦截方法的数据库操作类型
        // 方法签名是区分方法的一个重要标志，可以通过方法签名获取方法的相关信息
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature(); // 方法签名对象
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class); // 获取方法上的@AutoFill注解
        OperationType value = autoFill.value(); // 获取@AutoFill注解的value值，数据库操作类型
        // 获取被拦截方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            // 如果没有参数，直接返回
            return;
        }
        Object ob = args[0]; // 获取第一个对象

        // 需要的数据
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();

        // 根据不同的数据库操作类型，进行不同的处理
        if (value == OperationType.INSERT){
            // 反射获得实体类的属性 getDeclaredMethod()方法获得类的方法，参数为方法名,方法的参数类型
            try {
                Method setCreateTime = ob.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreatUser = ob.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateTime = ob.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser = ob.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);


                // 通过反射为实体类的属性赋值 invoke()方法为类的方法赋值，参数为类的对象和参数值
                setCreateTime.invoke(ob,now);
                setCreatUser.invoke(ob,id);
                setUpdateTime.invoke(ob,now);
                setUpdateUser.invoke(ob,id);

            }catch (Exception e){
                e.printStackTrace();
            }
        }else if (value == OperationType.UPDATE){
                try{
                    // 反射获得实体类的属性
                    Method setUpdateTime = ob.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                    Method setUpdateUser = ob.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                    // 通过反射为实体类的属性赋值
                    setUpdateTime.invoke(ob,now);
                    setUpdateUser.invoke(ob,id);

                }catch (Exception e){
                    e.printStackTrace();
                }
        }
    }
}

