package com.pay.manager.pc.log;

import com.pay.common.constant.Constant;
import com.pay.common.page.PageReqParams;
import com.pay.common.utils.IPUtils;
import com.pay.data.entity.SysLogEntity;
import com.pay.data.mapper.SysLogRepository;
import com.pay.data.supper.AbstractHelper;
import com.tuyang.beanutils.BeanCopyUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;

@Service
public class SysLogServiceImpl extends AbstractHelper<SysLogRepository, SysLogEntity, Long> implements SysLogService {

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<SysLogRespParams> getLogPage(LogQuery logQuery, PageReqParams reqParams) {
        Page<SysLogEntity> list = getPage(logQuery, toPageable(reqParams, Constant.CREATE_TIME));
        return list.map(e -> pageCopy(e, SysLogRespParams.class));
    }

    @Override
    public SysLogUpDateRespParams select(Long id) {
        SysLogEntity byId = getById(id);
        SysLogUpDateRespParams sysLogUpDateRespParams = BeanCopyUtils.copyBean(byId, SysLogUpDateRespParams.class);
        sysLogUpDateRespParams.setException(byId.getExceptionDetail());
        return sysLogUpDateRespParams;
    }

    @Override
    public void insert(String username, String browser, String ipAddr, ProceedingJoinPoint joinPoint, SysLogEntity log) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        io.swagger.annotations.ApiOperation aopLog = method.getAnnotation(io.swagger.annotations.ApiOperation.class);

        // 方法路径
        String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()";

        StringBuilder params = new StringBuilder("{");
        //参数值
        Object[] argValues = joinPoint.getArgs();
        //参数名称
        String[] argNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        if (!ObjectUtils.isEmpty(argValues)) {
            for (int i = 0; i < argValues.length; i++) {
                params.append(" ").append(argNames[i]).append(": ").append(argValues[i]);
            }
        }
        // 描述
        if (!ObjectUtils.isEmpty(log)) {
            log.setDescription(aopLog.value());
        }
        log.setRequestIp(ipAddr);

        String loginPath = "login";
        if (loginPath.equals(signature.getName())) {
            username = argValues[0].toString();
        }
        log.setAddress(IPUtils.getCityInfo(log.getRequestIp()));
        log.setMethod(methodName);
        log.setUsername(username);
        log.setParams(params.toString() + " }");
        log.setBrowser(browser);
        logRepository.save(log);
    }

    @Autowired
    SysLogRepository logRepository;
}
