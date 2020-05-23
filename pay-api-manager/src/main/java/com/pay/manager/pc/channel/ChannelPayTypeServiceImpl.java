package com.pay.manager.pc.channel;

import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.PayTypeEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.PayTypeRepository;
import com.pay.data.mapper.UpPayTypeRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.channel.params.ChannelPayTypeParams;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChannelPayTypeServiceImpl extends AbstractHelper<UpPayTypeRepository, UpPayTypeEntity, Long> implements ChannelPayTypeService {

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    PayTypeRepository payTypeRepository;

    @Autowired
    UpPayTypeRepository upPayTypeRepository;

    @Override
    public void insert(Long channelId, List<ChannelPayTypeParams> reqParamsList) {
        Optional<ChannelEntity> byId = channelRepository.findById(channelId);
        Assert.mustBeTrue(byId.isPresent(), "通道不存在");
        saveChannelPayType(byId.get(), reqParamsList);
    }

    private void saveChannelPayType(ChannelEntity channel, List<ChannelPayTypeParams> reqParamsList) {

        Set<UpPayTypeEntity> upPayTypeEntities = new HashSet<>();
        for (ChannelPayTypeParams payTypeParams : reqParamsList) {
            Long payTypeId = payTypeParams.getPayTypeId();
            Optional<PayTypeEntity> payType = payTypeRepository.findById(payTypeId);
            Assert.mustBeTrue(payType.isPresent(), "支付方式不存在");

            UpPayTypeEntity upPayTypeEntity = BeanCopyUtils.copyBean(payTypeParams, UpPayTypeEntity.class);
            upPayTypeEntity.setPayType(payType.get());
            upPayTypeEntity.setChannel(channel);
            upPayTypeEntities.add(upPayTypeEntity);
        }
        channel.getUpPayTypes().clear();
        channel.getUpPayTypes().addAll(upPayTypeEntities);
        channelRepository.save(channel);
    }

    @Override
    public void update(Long channelId, List<ChannelPayTypeParams> reqParamsList) {
        Optional<ChannelEntity> byId = channelRepository.findById(channelId);
        Assert.mustBeTrue(byId.isPresent(), "通道不存在");

        List<UpPayTypeEntity> upPayTypeEntities = upPayTypeRepository.findAllByChannel(byId.get());
        upPayTypeEntities.forEach(e->deleteById(e.getId()));
        saveChannelPayType(byId.get(), reqParamsList);
    }

}
