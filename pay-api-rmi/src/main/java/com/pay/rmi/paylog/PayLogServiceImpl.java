package com.pay.rmi.paylog;

import com.pay.data.entity.PayLogEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.PayLogRepository;
import com.pay.data.supper.AbstractHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class PayLogServiceImpl extends AbstractHelper<PayLogRepository, PayLogEntity, Long> implements PayLogService {

    @Autowired
    PayLogRepository payLogRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Override
    public void insert(String name, String msg,  Object... arguments) {
        PayLogEntity payLogEntity=new PayLogEntity();
        channelRepository.findByChannelFlag(name);
        payLogEntity.setChannelName(channelRepository.findByChannelFlag(name).getChannelName());
        String[] sArray=msg.split("\\{");
        String newMsg=sArray[0]+"{";
        for(int i=0;i<arguments.length;i++){
            newMsg=newMsg+arguments[i]+sArray[i+1];
        }
        payLogEntity.setLogContent(newMsg);
        payLogEntity.setChannelFlag(name);
        payLogEntity.setCreateTime(new Date());
        payLogRepository.save(payLogEntity);
    }

}
